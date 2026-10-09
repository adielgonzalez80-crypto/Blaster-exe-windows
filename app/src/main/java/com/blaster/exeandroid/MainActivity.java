package com.blaster.exeandroid;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int PICK_EXE = 1001;
    private static final int PICK_RUNTIME = 1002;
    private static final int BG = Color.rgb(8, 14, 31);
    private static final int PANEL = Color.rgb(18, 29, 54);
    private static final int PANEL2 = Color.rgb(26, 42, 73);
    private static final int BLUE = Color.rgb(48, 119, 255);
    private static final int CYAN = Color.rgb(53, 218, 229);
    private static final int WHITE = Color.rgb(242, 247, 255);
    private static final int MUTED = Color.rgb(157, 177, 207);
    private TextView selectedFile;
    private TextView fileMeta;
    private TextView runtimeStatus;
    private Uri selectedUri;
    private WindowsRuntime windowsRuntime;
    private LinearLayout root;
    private LinearLayout startMenu;
    private boolean startOpen = false;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        windowsRuntime = new WindowsRuntime(this);
        getWindow().setStatusBarColor(Color.rgb(9, 17, 35));
        getWindow().setNavigationBarColor(Color.rgb(9, 17, 35));
        getWindow().getDecorView().setSystemUiVisibility(0);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(9, 17, 35));
        root = column();
        root.setPadding(dp(16), dp(13), dp(16), dp(16));
        scroll.addView(root);

        // New BLASTER workspace layout: navigation rail, app workspace and runtime control center.
        LinearLayout header = row();
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView mark = label("B/", 20, WHITE, true);
        mark.setGravity(Gravity.CENTER);
        mark.setBackground(gradient(Color.rgb(0, 188, 212), Color.rgb(42, 92, 255), 15));
        header.addView(mark, lp(dp(50), dp(50)));
        LinearLayout identity = column();
        identity.setPadding(dp(11), 0, 0, 0);
        identity.addView(label("BLASTER", 21, WHITE, true));
        identity.addView(label("WORKSPACE  /  EXE", 9, CYAN, true));
        header.addView(identity, new LinearLayout.LayoutParams(0, -2, 1));
        TextView avatar = label("AG", 12, WHITE, true);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackground(round(Color.rgb(35, 53, 83), 30));
        header.addView(avatar, lp(dp(42), dp(42)));
        root.addView(header, lp(-1, dp(54)));

        LinearLayout statusLine = row();
        statusLine.setGravity(Gravity.CENTER_VERTICAL);
        TextView statusDot = label("●  SISTEMA LISTO", 10, CYAN, true);
        statusLine.addView(statusDot, new LinearLayout.LayoutParams(0, dp(34), 1));
        TextView edition = label("EDICIÓN ANDROID", 9, MUTED, true);
        statusLine.addView(edition);
        root.addView(statusLine, lp(-1, dp(38)));

        // The main canvas is intentionally different from the old single-card screen.
        LinearLayout canvas = column();
        canvas.setPadding(dp(17), dp(18), dp(17), dp(18));
        canvas.setBackground(gradient(Color.rgb(21, 44, 83), Color.rgb(17, 27, 49), 24));
        TextView eyebrow = label("TU CENTRO DE CONTROL", 10, CYAN, true);
        eyebrow.setLetterSpacing(0.14f);
        canvas.addView(eyebrow, lp(-1, dp(24)));
        TextView heading = label("Todo tu mundo\ndigital, en un lugar.", 27, WHITE, true);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        canvas.addView(heading, lp(-1, dp(76)));
        TextView subtitle = label("Abre tus archivos y administra el motor de compatibilidad desde tu espacio BLASTER.", 12, Color.rgb(190, 207, 231), false);
        subtitle.setPadding(0, dp(7), 0, dp(16));
        canvas.addView(subtitle, lp(-1, -2));
        LinearLayout primaryActions = row();
        Button open = button("＋  Abrir archivo", Color.rgb(42, 112, 255));
        open.setTextSize(12);
        primaryActions.addView(open, new LinearLayout.LayoutParams(0, dp(45), 1));
        open.setOnClickListener(v -> openPicker());
        View spacer = new View(this);
        primaryActions.addView(spacer, lp(dp(9), dp(1)));
        Button apps = button("▦  Mis programas", Color.rgb(38, 58, 91));
        apps.setTextSize(12);
        primaryActions.addView(apps, new LinearLayout.LayoutParams(0, dp(45), 1));
        apps.setOnClickListener(v -> showPrograms());
        canvas.addView(primaryActions, lp(-1, dp(45)));
        LinearLayout.LayoutParams canvasParams = lp(-1, -2);
        canvasParams.setMargins(0, dp(6), 0, dp(19));
        root.addView(canvas, canvasParams);

        LinearLayout section = row();
        section.setGravity(Gravity.CENTER_VERTICAL);
        section.addView(label("ESPACIO DE TRABAJO", 11, WHITE, true), new LinearLayout.LayoutParams(0, dp(30), 1));
        section.addView(label("05 ACCESOS", 9, MUTED, true));
        root.addView(section, lp(-1, dp(30)));

        // Independent tiles form BLASTER's own application launcher.
        LinearLayout rowOne = row();
        rowOne.addView(appTile("▤", "Archivos", "Explorar", BLUE, v -> openPicker()), new LinearLayout.LayoutParams(0, dp(112), 1));
        View gap1 = new View(this); rowOne.addView(gap1, lp(dp(9), 1));
        rowOne.addView(appTile("EXE", "Programas", "Ejecutar", Color.rgb(0, 151, 167), v -> showPrograms()), new LinearLayout.LayoutParams(0, dp(112), 1));
        View gap2 = new View(this); rowOne.addView(gap2, lp(dp(9), 1));
        rowOne.addView(appTile("▶", "Lanzador", "Abrir motor", Color.rgb(103, 80, 164), v -> showLauncher()), new LinearLayout.LayoutParams(0, dp(112), 1));
        root.addView(rowOne, lp(-1, dp(112)));
        LinearLayout rowTwo = row();
        rowTwo.addView(appTile("⚙", "Diagnóstico", "Estado", Color.rgb(41, 120, 105), v -> showDiagnostics()), new LinearLayout.LayoutParams(0, dp(104), 1));
        View gap3 = new View(this); rowTwo.addView(gap3, lp(dp(9), 1));
        rowTwo.addView(appTile("i", "Acerca de", "BLASTER", Color.rgb(73, 91, 122), v -> showAbout()), new LinearLayout.LayoutParams(0, dp(104), 1));
        View gap4 = new View(this); rowTwo.addView(gap4, lp(dp(9), 1));
        LinearLayout blankTile = column();
        blankTile.setBackground(round(Color.rgb(14, 24, 43), 17));
        blankTile.setGravity(Gravity.CENTER);
        blankTile.addView(label("BLASTER", 10, Color.rgb(77, 103, 143), true));
        rowTwo.addView(blankTile, new LinearLayout.LayoutParams(0, dp(104), 1));
        LinearLayout.LayoutParams rowTwoParams = lp(-1, dp(104));
        rowTwoParams.setMargins(0, dp(9), 0, dp(19));
        root.addView(rowTwo, rowTwoParams);

        // Dedicated engine card with visible install/download actions.
        LinearLayout engine = column();
        engine.setPadding(dp(15), dp(15), dp(15), dp(15));
        engine.setBackground(round(Color.rgb(17, 30, 52), 20));
        LinearLayout engineHead = row();
        engineHead.setGravity(Gravity.CENTER_VERTICAL);
        TextView engineIcon = label("⚙", 20, CYAN, true);
        engineIcon.setGravity(Gravity.CENTER);
        engineIcon.setBackground(round(Color.rgb(23, 61, 83), 13));
        engineHead.addView(engineIcon, lp(dp(44), dp(44)));
        LinearLayout engineNames = column();
        engineNames.setPadding(dp(10), 0, 0, 0);
        engineNames.addView(label("Motor Windows", 15, WHITE, true));
        engineNames.addView(label("BOX64  +  WINE", 9, CYAN, true));
        engineHead.addView(engineNames, new LinearLayout.LayoutParams(0, -2, 1));
        TextView engineBadge = label(windowsRuntime.isInstalled() ? "DETECTADO" : "PENDIENTE", 9, windowsRuntime.isInstalled() ? CYAN : Color.rgb(255, 190, 90), true);
        engineHead.addView(engineBadge);
        engine.addView(engineHead, lp(-1, dp(48)));
        runtimeStatus = label(windowsRuntime.getStatus(), 11, MUTED, false);
        runtimeStatus.setPadding(0, dp(11), 0, dp(12));
        engine.addView(runtimeStatus, lp(-1, -2));
        Button download = button("↓  Descargar e instalar motor", Color.rgb(0, 126, 150));
        download.setTextSize(12);
        download.setOnClickListener(v -> downloadRuntimePackage());
        engine.addView(download, lp(-1, dp(44)));
        Button importZip = button("＋  Instalar paquete ZIP local", Color.rgb(40, 58, 88));
        importZip.setTextSize(12);
        LinearLayout.LayoutParams importP = lp(-1, dp(42));
        importP.setMargins(0, dp(8), 0, 0);
        engine.addView(importZip, importP);
        importZip.setOnClickListener(v -> openRuntimePicker());
        LinearLayout.LayoutParams engineP = lp(-1, -2);
        engineP.setMargins(0, 0, 0, dp(14));
        root.addView(engine, engineP);

        // Selected program panel.
        LinearLayout files = column();
        files.setPadding(dp(15), dp(14), dp(15), dp(14));
        files.setBackground(round(Color.rgb(17, 30, 52), 20));
        files.addView(label("ARCHIVO SELECCIONADO", 10, CYAN, true), lp(-1, dp(24)));
        selectedFile = label("Ningún programa abierto", 14, WHITE, true);
        selectedFile.setPadding(0, dp(4), 0, dp(3));
        files.addView(selectedFile, lp(-1, -2));
        fileMeta = label("Selecciona un archivo .EXE para preparar una prueba.", 11, MUTED, false);
        files.addView(fileMeta, lp(-1, -2));
        Button run = button("▶  Intentar ejecutar programa", Color.rgb(24, 153, 116));
        LinearLayout.LayoutParams runP = lp(-1, dp(46));
        runP.setMargins(0, dp(12), 0, 0);
        files.addView(run, runP);
        run.setOnClickListener(v -> attemptRunSelected());
        root.addView(files, lp(-1, -2));

        // Bottom BLASTER navigation dock.
        LinearLayout dock = row();
        dock.setGravity(Gravity.CENTER_VERTICAL);
        dock.setPadding(dp(8), dp(7), dp(8), dp(7));
        dock.setBackground(round(Color.rgb(24, 37, 62), 18));
        Button home = button("B/  Inicio", Color.rgb(42, 112, 255));
        home.setTextSize(11);
        dock.addView(home, lp(dp(100), dp(42)));
        home.setOnClickListener(v -> scroll.smoothScrollTo(0, 0));
        TextView dockLabel = label("BLASTER SPACE", 9, MUTED, true);
        dockLabel.setGravity(Gravity.CENTER);
        dock.addView(dockLabel, new LinearLayout.LayoutParams(0, dp(42), 1));
        Button info = button("⋯", Color.rgb(40, 58, 88));
        info.setTextSize(17);
        dock.addView(info, lp(dp(48), dp(42)));
        info.setOnClickListener(v -> toggleStartMenu());
        LinearLayout.LayoutParams dockP = lp(-1, -2);
        dockP.setMargins(0, dp(15), 0, dp(10));
        root.addView(dock, dockP);

        startMenu = column();
        startMenu.setPadding(dp(14), dp(12), dp(14), dp(12));
        startMenu.setBackground(round(Color.rgb(24, 38, 65), 18));
        startMenu.setVisibility(View.GONE);
        startMenu.addView(label("MENÚ BLASTER", 13, WHITE, true), lp(-1, dp(28)));
        addMenuItem(startMenu, "▤   Abrir archivos", v -> { hideStartMenu(); openPicker(); });
        addMenuItem(startMenu, "▦   Mis programas", v -> { hideStartMenu(); showPrograms(); });
        addMenuItem(startMenu, "⚙   Diagnóstico", v -> { hideStartMenu(); showDiagnostics(); });
        addMenuItem(startMenu, "ⓘ   Acerca de BLASTER", v -> { hideStartMenu(); showAbout(); });
        root.addView(startMenu, lp(-1, -2));

        TextView footer = label("BLASTER  •  UN ESPACIO DIGITAL PROPIO", 9, Color.rgb(105, 128, 163), true);
        footer.setGravity(Gravity.CENTER);
        root.addView(footer, lp(-1, dp(38)));
        setContentView(scroll);
    }

    private View appTile(String symbol, String title, String detail, int accent, View.OnClickListener action) {
        LinearLayout tile = column();
        tile.setPadding(dp(10), dp(11), dp(8), dp(9));
        tile.setBackground(round(Color.rgb(19, 32, 54), 17));
        TextView icon = label(symbol, symbol.length() > 2 ? 11 : 21, WHITE, true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(round(accent, 12));
        tile.addView(icon, lp(dp(40), dp(40)));
        TextView name = label(title, 11, WHITE, true);
        name.setPadding(0, dp(7), 0, 0);
        tile.addView(name, lp(-1, dp(20)));
        TextView caption = label(detail, 9, MUTED, false);
        tile.addView(caption, lp(-1, dp(17)));
        tile.setOnClickListener(action);
        return tile;
    }

    private void toggleStartMenu() {
        startOpen = !startOpen;
        startMenu.setVisibility(startOpen ? View.VISIBLE : View.GONE);
    }
    private void hideStartMenu() { startOpen = false; startMenu.setVisibility(View.GONE); }

    private void downloadRuntimePackage() {
        if (runtimeStatus != null) runtimeStatus.setText("Conectando con el repositorio oficial de BLASTER…");
        new Thread(() -> {
            try {
                windowsRuntime.downloadAndInstallRuntime();
                runOnUiThread(() -> {
                    runtimeStatus.setText(windowsRuntime.getStatus());
                    Toast.makeText(this, "Paquete del motor descargado e instalado.", Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    runtimeStatus.setText(windowsRuntime.getStatus());
                    new android.app.AlertDialog.Builder(this)
                        .setTitle("Motor BLASTER")
                        .setMessage(e.getMessage() == null ? "No se pudo descargar o instalar el paquete." : e.getMessage())
                        .setPositiveButton("Entendido", null).show();
                });
            }
        }).start();
    }

    private void openRuntimePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try { startActivityForResult(intent, PICK_RUNTIME); }
        catch (Exception e) { Toast.makeText(this, "No se pudo abrir el selector del paquete runtime.", Toast.LENGTH_LONG).show(); }
    }

    private void openPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try { startActivityForResult(intent, PICK_EXE); }
        catch (Exception e) { Toast.makeText(this, "No se pudo abrir el selector de archivos.", Toast.LENGTH_LONG).show(); }
    }

    private void showLauncher() {
        final android.app.AlertDialog[] dialogHolder = new android.app.AlertDialog[1];
        LinearLayout panel = column();
        panel.setPadding(dp(18), dp(8), dp(18), dp(8));
        panel.setBackground(round(BG, 18));

        TextView brand = label("BLASTER  /  CENTRO DE LANZAMIENTO", 13, CYAN, true);
        panel.addView(brand, lp(-1, dp(38)));
        TextView headline = label("Ejecutar programas", 23, WHITE, true);
        panel.addView(headline, lp(-1, dp(42)));
        TextView explanation = label("Selecciona un archivo EXE, instala el paquete del motor y consulta el estado desde esta ventana.", 12, MUTED, false);
        explanation.setPadding(0, 0, 0, dp(12));
        panel.addView(explanation, lp(-1, -2));

        TextView statusTitle = label("ESTADO DEL MOTOR", 10, CYAN, true);
        panel.addView(statusTitle, lp(-1, dp(24)));
        TextView status = label(windowsRuntime.getStatus(), 12, WHITE, false);
        status.setPadding(dp(12), dp(10), dp(12), dp(10));
        status.setBackground(round(PANEL, 12));
        panel.addView(status, lp(-1, -2));

        Button choose = button("▣   Seleccionar archivo EXE", BLUE);
        LinearLayout.LayoutParams chooseParams = lp(-1, dp(48));
        chooseParams.setMargins(0, dp(12), 0, dp(7));
        panel.addView(choose, chooseParams);
        choose.setOnClickListener(v -> { dialogHolder[0].dismiss(); openPicker(); });

        Button install = button("↓   Instalar paquete del motor", Color.rgb(67, 87, 132));
        panel.addView(install, lp(-1, dp(46)));
        install.setOnClickListener(v -> { dialogHolder[0].dismiss(); openRuntimePicker(); });

        Button run = button("▶   Ejecutar programa seleccionado", Color.rgb(35, 163, 132));
        LinearLayout.LayoutParams runParams = lp(-1, dp(48));
        runParams.setMargins(0, dp(7), 0, dp(7));
        panel.addView(run, runParams);
        run.setOnClickListener(v -> { dialogHolder[0].dismiss(); attemptRunSelected(); });

        TextView note = label("BLASTER DESKTOP  •  MOTOR WINDOWS EN INTEGRACIÓN", 9, MUTED, true);
        note.setGravity(Gravity.CENTER);
        panel.addView(note, lp(-1, dp(32)));

        dialogHolder[0] = new android.app.AlertDialog.Builder(this)
            .setView(panel)
            .setNegativeButton("Cerrar", null)
            .create();
        dialogHolder[0].show();
    }

    private void showPrograms() {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Programas")
            .setMessage("ADMINISTRADOR DE PROGRAMAS\n\n• Seleccionar archivo .EXE: disponible\n• Motor Windows: " + windowsRuntime.getStatus() + "\n• Lanzador: " + (windowsRuntime.getLauncher().canExecute() ? "detectado" : "falta") + "\n\nLa lista de programas instalados y la apertura de ventanas de Windows se activarán cuando el paquete real del motor esté compilado e instalado.")
            .setPositiveButton("Seleccionar EXE", (d, w) -> openPicker())
            .setNegativeButton("Cerrar", null).show();
    }
    private void showDiagnostics() {
        String status = windowsRuntime.getStatus();
        new android.app.AlertDialog.Builder(this)
            .setTitle("Diagnóstico BLASTER")
            .setMessage("Arquitectura Android: " + detectArch()
                + "\nSelector de archivos: disponible"
                + "\nEscritorio gráfico: activo"
                + "\nLanzador: " + (windowsRuntime.getLauncher().canExecute() ? "detectado" : "pendiente")
                + "\nBox64: " + (windowsRuntime.getBox64().canExecute() ? "detectado" : "pendiente")
                + "\nWine64: " + (windowsRuntime.getWine64().canExecute() ? "detectado" : "pendiente")
                + "\n\n" + status)
            .setPositiveButton("Abrir lanzador", (d, w) -> showLauncher())
            .setNegativeButton("Cerrar", null).show();
    }
    private void showAbout() {
        new android.app.AlertDialog.Builder(this)
            .setTitle("BLASTER EXE")
            .setMessage("Un entorno Android con identidad propia, diseñado para evolucionar hacia un escritorio de aplicaciones y compatibilidad Windows.")
            .setPositiveButton("Cerrar", null).show();
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
        if (requestCode == PICK_RUNTIME) {
            Uri runtimeUri = data.getData();
            runtimeStatus.setText("Instalando paquete del motor Windows…");
            new Thread(() -> {
                try {
                    windowsRuntime.installRuntimePackage(runtimeUri);
                    runOnUiThread(() -> {
                        runtimeStatus.setText(windowsRuntime.getStatus());
                        Toast.makeText(this, "Motor BLASTER instalado.", Toast.LENGTH_LONG).show();
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> new android.app.AlertDialog.Builder(this)
                        .setTitle("No se pudo instalar el motor")
                        .setMessage(e.getMessage() == null ? "Paquete runtime inválido." : e.getMessage())
                        .setPositiveButton("Entendido", null).show());
                }
            }).start();
            return;
        }
        if (requestCode != PICK_EXE) return;
        Uri uri = data.getData();
        selectedUri = uri;
        String name = getDisplayName(uri);
        if (name == null || name.trim().isEmpty()) name = uri.getLastPathSegment();
        selectedFile.setText("Archivo seleccionado: " + (name == null ? "EXE" : name));
        fileMeta.setText("Ubicación: " + uri);
        runtimeStatus.setText("Archivo seleccionado. " + windowsRuntime.getStatus());
        Toast.makeText(this, "Archivo seleccionado", Toast.LENGTH_SHORT).show();
    }

    private void attemptRunSelected() {
        if (selectedUri == null) {
            new android.app.AlertDialog.Builder(this)
                .setTitle("No hay programa seleccionado")
                .setMessage("Primero pulsa «Seleccionar archivo EXE» y elige el archivo que quieres probar.")
                .setPositiveButton("Seleccionar archivo", (d, w) -> openPicker())
                .setNegativeButton("Cancelar", null).show();
            return;
        }
        if (!windowsRuntime.isInstalled()) {
            new android.app.AlertDialog.Builder(this)
                .setTitle("Motor Windows todavía no disponible")
                .setMessage(windowsRuntime.getStatus() + "\\n\\nEl archivo EXE está seleccionado, pero no se ejecutará hasta que BLASTER tenga su paquete real de Box64 + Wine WOW64. El instalador solo acepta el ZIP oficial del runtime BLASTER; no elijas un ZIP cualquiera.")
                .setPositiveButton("Ver diagnóstico", (d, w) -> showDiagnostics())
                .setNeutralButton("Importar paquete", (d, w) -> openRuntimePicker())
                .setNegativeButton("Cerrar", null)
                .show();
            return;
        }
        final Uri uri = selectedUri;
        runtimeStatus.setText("Preparando archivo para el runtime…");
        new Thread(() -> {
            try {
                java.io.File staged = windowsRuntime.stageExe(uri);
                windowsRuntime.launch(staged);
                runOnUiThread(() -> {
                    runtimeStatus.setText("Se envió el archivo al puente de ejecución. Comprueba el registro si el programa no abre.");
                    Toast.makeText(this, "Solicitud de ejecución enviada", Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> new android.app.AlertDialog.Builder(this)
                    .setTitle("No se pudo iniciar el programa")
                    .setMessage(e.getMessage() == null ? "Error desconocido del runtime." : e.getMessage())
                    .setPositiveButton("Entendido", null).show());
            }
        }).start();
    }

    private String getDisplayName(Uri uri) {
        android.database.Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, new String[]{android.provider.OpenableColumns.DISPLAY_NAME}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (index >= 0) return cursor.getString(index);
            }
        } catch (Exception ignored) {
        } finally { if (cursor != null) cursor.close(); }
        return null;
    }
    private String detectArch() {
        String[] abis = android.os.Build.SUPPORTED_ABIS;
        return abis != null && abis.length > 0 ? abis[0] : "Desconocida";
    }
    private void addStatus(LinearLayout parent, String title, String value, int color) {
        LinearLayout line = row();
        line.setGravity(Gravity.CENTER_VERTICAL);
        TextView left = label(title, 12, MUTED, false);
        line.addView(left, new LinearLayout.LayoutParams(0, dp(32), 1));
        TextView right = label(value, 11, color, true);
        right.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        line.addView(right);
        parent.addView(line, lp(-1, dp(32)));
    }
    private View desktopIcon(String symbol, String title, View.OnClickListener action) {
        LinearLayout icon = column();
        icon.setGravity(Gravity.CENTER);
        TextView tile = label(symbol, symbol.length() > 2 ? 13 : 23, WHITE, true);
        tile.setGravity(Gravity.CENTER);
        tile.setBackground(round(Color.rgb(32, 57, 99), 15));
        icon.addView(tile, lp(dp(54), dp(54)));
        TextView name = label(title, 10, WHITE, true);
        name.setGravity(Gravity.CENTER);
        name.setPadding(0, dp(6), 0, 0);
        icon.addView(name, lp(-1, dp(26)));
        icon.setOnClickListener(action);
        return icon;
    }
    private void addMenuItem(LinearLayout menu, String text, View.OnClickListener action) {
        TextView item = label(text, 13, WHITE, true);
        item.setPadding(dp(10), 0, dp(10), 0);
        menu.addView(item, lp(-1, dp(44)));
        item.setOnClickListener(action);
    }
    private TextView sectionTitle(String text) {
        TextView t = label(text, 10, MUTED, true);
        t.setLetterSpacing(0.09f);
        return t;
    }
    private Button button(String text, int color) {
        Button b = new Button(this);
        b.setText(text); b.setTextColor(WHITE); b.setTextSize(14);
        b.setAllCaps(false); b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackground(round(color, 13));
        b.setPadding(dp(8), 0, dp(8), 0);
        return b;
    }
    private TextView label(String text, float size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(text); t.setTextSize(size); t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }
    private LinearLayout row() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); return l; }
    private LinearLayout column() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }
    private GradientDrawable round(int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private GradientDrawable gradient(int start, int end, int radius) {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{start, end});
        d.setCornerRadius(dp(radius));
        d.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        return d;
    }
    private LinearLayout.LayoutParams lp(int w, int h) { return new LinearLayout.LayoutParams(w, h); }
    private LinearLayout.LayoutParams weighted() { return new LinearLayout.LayoutParams(0, -1, 1); }
    private int dp(int value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }
}
