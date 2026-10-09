package com.blaster.exeandroid;

import android.content.Context;
import android.net.Uri;
import android.database.Cursor;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * BLASTER Windows runtime bridge.
 *
 * Runtime layout:
 * filesDir/blaster-runtime/
 *   runtime.json
 *   launch-windows
 *   bin/box64
 *   wine/bin/wine64
 */
public final class WindowsRuntime {
    private final Context context;

    public WindowsRuntime(Context context) {
        this.context = context.getApplicationContext();
    }

    public File getRuntimeRoot() { return new File(context.getFilesDir(), "blaster-runtime"); }
    public File getLauncher() { return new File(getRuntimeRoot(), "launch-windows"); }
    public File getBox64() { return new File(new File(getRuntimeRoot(), "bin"), "box64"); }
    public File getWine64() { return new File(new File(getRuntimeRoot(), "wine/usr/local/bin"), "wine64"); }
    public File getRuntimeManifest() { return new File(getRuntimeRoot(), "runtime.json"); }

    public boolean isInstalled() {
        return getLauncher().isFile() && getLauncher().canExecute()
                && getBox64().isFile() && getBox64().canExecute()
                && getWine64().isFile() && getWine64().canExecute()
                && getRuntimeManifest().isFile();
    }

    public String getStatus() {
        if (isInstalled()) return "Motor BLASTER detectado: launcher + Box64 + Wine64.";
        StringBuilder missing = new StringBuilder("Motor Windows incompleto. Falta: ");
        boolean first = true;
        if (!getLauncher().isFile()) { missing.append("launcher"); first = false; }
        if (!getBox64().isFile()) { if (!first) missing.append(", "); missing.append("Box64"); first = false; }
        if (!getWine64().isFile()) { if (!first) missing.append(", "); missing.append("Wine64"); first = false; }
        if (!getRuntimeManifest().isFile()) { if (!first) missing.append(", "); missing.append("runtime.json"); }
        return missing.toString();
    }

    public File stageExe(Uri source) throws IOException {
        String name = queryName(source);
        if (name == null || name.trim().isEmpty()) name = "selected-program.exe";
        name = name.replaceAll("[^A-Za-z0-9._-]", "_");
        if (!name.toLowerCase().endsWith(".exe")) name += ".exe";

        File staging = new File(context.getCacheDir(), "blaster-exe");
        if (!staging.exists() && !staging.mkdirs()) throw new IOException("No se pudo crear el directorio temporal.");

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

    /** Downloads the package published by the repository runtime workflow and installs it. */
    public void downloadAndInstallRuntime() throws IOException {
        String address = "https://github.com/adielgonzalez80-crypto/Blaster-exe-windows/releases/download/runtime-latest/blaster-windows-runtime-real.zip";
        File packageFile = new File(context.getCacheDir(), "blaster-windows-runtime-real.zip");
        HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
        connection.setConnectTimeout(20000);
        connection.setReadTimeout(60000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", "BLASTER-EXE-Android");
        try {
            int code = connection.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                if (code == HttpURLConnection.HTTP_NOT_FOUND)
                    throw new IOException("El paquete oficial todavía no está publicado. Espera a que termine la compilación del motor en GitHub Actions.");
                throw new IOException("No se pudo descargar el motor. HTTP " + code);
            }
            try (InputStream in = connection.getInputStream();
                 FileOutputStream out = new FileOutputStream(packageFile, false)) {
                byte[] buffer = new byte[64 * 1024];
                int read;
                while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
            }
        } finally {
            connection.disconnect();
        }
        try {
            installRuntimePackage(Uri.fromFile(packageFile));
        } finally {
            packageFile.delete();
        }
    }

    /**
     * Installs a BLASTER runtime ZIP supplied by the user or produced by the
     * runtime preparation workflow. ZIP paths are validated against traversal.
     */
    public void installRuntimePackage(Uri source) throws IOException {
        File root = getRuntimeRoot();
        File parent = root.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) throw new IOException("No se pudo preparar el almacenamiento.");
        File temp = new File(parent, "blaster-runtime-installing");
        deleteTree(temp);
        if (!temp.mkdirs()) throw new IOException("No se pudo crear el área temporal.");

        try (InputStream raw = context.getContentResolver().openInputStream(source);
             ZipInputStream zip = new ZipInputStream(new BufferedInputStream(raw))) {
            if (raw == null) throw new IOException("No se pudo leer el paquete runtime.");
            ZipEntry entry;
            byte[] buffer = new byte[32 * 1024];
            while ((entry = zip.getNextEntry()) != null) {
                String name = entry.getName().replace('\\', '/');
                if (name.startsWith("/") || name.contains("../") || name.equals(".."))
                    throw new IOException("Paquete runtime rechazado: ruta insegura.");

                while (name.startsWith("./")) name = name.substring(2);
                if (name.isEmpty()) continue;

                File target = new File(temp, name);
                String rootPath = temp.getCanonicalPath() + File.separator;
                String targetPath = target.getCanonicalPath();
                if (!targetPath.startsWith(rootPath)) throw new IOException("Paquete runtime rechazado.");

                if (entry.isDirectory()) {
                    if (!target.exists() && !target.mkdirs()) throw new IOException("No se pudo crear " + name);
                    continue;
                }

                File p = target.getParentFile();
                if (!p.exists() && !p.mkdirs()) throw new IOException("No se pudo crear " + p);
                try (FileOutputStream out = new FileOutputStream(target)) {
                    int read;
                    while ((read = zip.read(buffer)) != -1) out.write(buffer, 0, read);
                }
            }
        }

        File launcher = new File(temp, "launch-windows");
        File box64 = new File(temp, "bin/box64");
        File wine64 = new File(temp, "wine/usr/local/bin/wine64");
        File manifest = new File(temp, "runtime.json");
        if (!launcher.isFile() || !box64.isFile() || !wine64.isFile() || !manifest.isFile()) {
            deleteTree(temp);
            throw new IOException("Runtime inválido: faltan launch-windows, Box64, Wine64 o runtime.json.");
        }

        launcher.setExecutable(true, false);
        box64.setExecutable(true, false);
        wine64.setExecutable(true, false);

        File old = new File(parent, "blaster-runtime-old");
        deleteTree(old);
        if (root.exists() && !root.renameTo(old)) {
            deleteTree(temp);
            throw new IOException("No se pudo reemplazar el runtime anterior.");
        }
        if (!temp.renameTo(root)) {
            if (old.exists()) old.renameTo(root);
            deleteTree(temp);
            throw new IOException("No se pudo activar el nuevo runtime.");
        }
        deleteTree(old);
    }

    public Process launch(File exe) throws IOException {
        if (!isInstalled()) throw new IOException(getStatus());
        File log = new File(context.getFilesDir(), "blaster-runtime-last.log");
        ProcessBuilder builder = new ProcessBuilder(getLauncher().getAbsolutePath(), exe.getAbsolutePath());
        builder.directory(getRuntimeRoot());
        builder.environment().put("BLASTER_RUNTIME", getRuntimeRoot().getAbsolutePath());
        builder.environment().put("BLASTER_EXE", exe.getAbsolutePath());
        builder.environment().put("HOME", new File(getRuntimeRoot(), "home").getAbsolutePath());
        builder.environment().put("TMPDIR", new File(getRuntimeRoot(), "tmp").getAbsolutePath());
        builder.redirectErrorStream(true);
        builder.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
        return builder.start();
    }

    private String queryName(Uri uri) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
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

    private void deleteTree(File file) {
        if (file == null || !file.exists()) return;
        File[] children = file.listFiles();
        if (children != null) for (File child : children) deleteTree(child);
        file.delete();
    }
}
