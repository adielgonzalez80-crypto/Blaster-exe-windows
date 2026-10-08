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
 * BLASTER Windows runtime bridge.
 *
 * Runtime layout:
 *   filesDir/blaster-runtime/
 *       runtime.json
 *       launch-windows
 *       bin/box64
 *       wine/bin/wine64
 *
 * The native runtime itself is deliberately kept outside Java source. It must
 * contain compatible ARM64 Android/Linux binaries and their Wine userspace.
 */
public final class WindowsRuntime {
    private final Context context;

    public WindowsRuntime(Context context) {
        this.context = context.getApplicationContext();
    }

    public File getRuntimeRoot() {
        return new File(context.getFilesDir(), "blaster-runtime");
    }

    public File getLauncher() {
        return new File(getRuntimeRoot(), "launch-windows");
    }

    public File getBox64() {
        return new File(new File(getRuntimeRoot(), "bin"), "box64");
    }

    public File getWine64() {
        return new File(new File(getRuntimeRoot(), "wine/bin"), "wine64");
    }

    public File getRuntimeManifest() {
        return new File(getRuntimeRoot(), "runtime.json");
    }

    public boolean isInstalled() {
        return getLauncher().isFile() && getLauncher().canExecute()
                && getBox64().isFile() && getBox64().canExecute()
                && getWine64().isFile() && getWine64().canExecute();
    }

    public String getStatus() {
        if (isInstalled()) {
            return "Motor BLASTER detectado: launcher + Box64 + Wine64.";
        }
        StringBuilder missing = new StringBuilder("Motor Windows incompleto. Falta: ");
        boolean first = true;
        if (!getLauncher().isFile()) { missing.append("launcher"); first = false; }
        if (!getBox64().isFile()) { if (!first) missing.append(", "); missing.append("Box64"); first = false; }
        if (!getWine64().isFile()) { if (!first) missing.append(", "); missing.append("Wine64"); }
        return missing.toString();
    }

    public File stageExe(Uri source) throws IOException {
        String name = queryName(source);
        if (name == null || name.trim().isEmpty()) name = "selected-program.exe";
        name = name.replaceAll("[^A-Za-z0-9._-]", "_");
        if (!name.toLowerCase().endsWith(".exe")) name += ".exe";

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
     * Starts the BLASTER launcher. The launcher is responsible for configuring
     * PATH, WINEPREFIX, Box64 and the Wine libraries.
     */
    public Process launch(File exe) throws IOException {
        if (!isInstalled()) throw new IOException(getStatus());

        File log = new File(context.getFilesDir(), "blaster-runtime-last.log");
        ProcessBuilder builder = new ProcessBuilder(
                getLauncher().getAbsolutePath(),
                exe.getAbsolutePath()
        );
        builder.directory(getRuntimeRoot());
        builder.environment().put("BLASTER_RUNTIME", getRuntimeRoot().getAbsolutePath());
        builder.environment().put("BLASTER_EXE", exe.getAbsolutePath());
        builder.redirectErrorStream(true);
        builder.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
        return builder.start();
    }

    private String queryName(Uri uri) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    uri,
                    new String[]{OpenableColumns.DISPLAY_NAME},
                    null, null, null);
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
