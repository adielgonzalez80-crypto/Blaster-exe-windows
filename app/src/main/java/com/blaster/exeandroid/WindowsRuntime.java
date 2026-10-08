package com.blaster.exeandroid;

import android.content.Context;
import android.net.Uri;
import android.database.Cursor;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * Runtime bridge for BLASTER EXE.
 *
 * The bridge expects a native launcher produced by the Windows-compatibility
 * runtime build at filesDir/blaster-runtime/launch-windows. That launcher must
 * configure Wine, Box64/Box86 and their libraries; this app does not bundle those
 * components yet.
 */
public final class WindowsRuntime {
    private final Context context;

    public WindowsRuntime(Context context) {
        this.context = context.getApplicationContext();
    }

    public File getLauncher() {
        return new File(new File(context.getFilesDir(), "blaster-runtime"), "launch-windows");
    }

    public boolean isInstalled() {
        File launcher = getLauncher();
        return launcher.isFile() && launcher.canExecute();
    }

    public String getStatus() {
        if (isInstalled()) {
            return "Se encontró el puente de ejecución. La compatibilidad depende de las bibliotecas del runtime.";
        }
        return "Runtime no instalado: falta blaster-runtime/launch-windows y sus componentes Wine/Box64.";
    }

    public File stageExe(Uri source) throws IOException {
        String name = queryName(source);
        if (name == null || name.trim().isEmpty()) name = "selected-program.exe";
        name = name.replaceAll("[^A-Za-z0-9._-]", "_");
        if (!name.toLowerCase().endsWith(".exe")) {
            name = name + ".exe";
        }

        File staging = new File(context.getCacheDir(), "blaster-exe");
        if (!staging.exists() && !staging.mkdirs()) {
            throw new IOException("No se pudo crear el directorio temporal.");
        }
        File destination = new File(staging, name);
        try (InputStream in = context.getContentResolver().openInputStream(source);
             FileOutputStream out = new FileOutputStream(destination, false)) {
            if (in == null) throw new IOException("Android no pudo leer el archivo seleccionado.");
            byte[] buffer = new byte[32 * 1024];
            int read;
            while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
        }
        return destination;
    }

    /**
     * Launches the runtime bridge only when a real executable bridge is installed.
     * The bridge's stdout/stderr are redirected to a private log for diagnostics.
     */
    public Process launch(File exe) throws IOException {
        File launcher = getLauncher();
        if (!launcher.isFile() || !launcher.canExecute()) {
            throw new IOException(getStatus());
        }
        File log = new File(context.getFilesDir(), "blaster-runtime-last.log");
        ProcessBuilder builder = new ProcessBuilder(launcher.getAbsolutePath(), exe.getAbsolutePath());
        builder.directory(launcher.getParentFile());
        builder.redirectErrorStream(true);
        builder.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
        return builder.start();
    }

    private String queryName(Uri uri) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri,
                new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) return cursor.getString(index);
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }
}
