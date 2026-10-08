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
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(0);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        root = column();
        root.setPadding(dp(16), dp(14), dp(16), dp(12));
        scroll.addView(root);

        // BLASTER desktop header
        LinearLayout top = row();
        TextView logo = label("B", 23, WHITE, true);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(round(BLUE, 14));
        top.addView(logo, lp(dp(48), dp(48)));
        LinearLayout names = column();
        names.setPadding(dp(10), 0, 0, 0);
        names.addView(label("BLASTER EXE", 19, WHITE, true));
        names.addView(label("ESCRITORIO DIGITAL", 10, CYAN, true));
        top.addView(names, new LinearLayout.LayoutParams(0, -2, 1));
        TextView badge = label("BETA", 10, WHITE, true);
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(dp(10), dp(7), dp(10), dp(7));
        badge.setBackground(round(Color.rgb(42, 64, 105), 24));
        top.addView(badge);
        root.addView(top, lp(-1, -2));
        root.addView(label("Tu espacio. Tus herramientas. Tu identidad.", 13, MUTED, false), lp(-1, dp(38)));
        TextView search = label("⌕   Buscar aplicaciones y archivos", 13, MUTED, false);
        search.setPadding(dp(14), 0, dp(14), 0);
        search.setBackground(round(Color.rgb(20, 31, 54), 13));
        root.addView(search, lp(-1, dp(44)));
        search.setOnClickListener(v -> showPrograms());

        // Desktop icons
        root.addView(sectionTitle("APLICACIONES"), lp(-1, dp(28)));
        LinearLayout icons = row();
        icons.addView(desktopIcon("▤", "Archivos", v -> openPicker()), weighted());
        icons.addView(desktopIcon("EXE", "Programas", v -> showPrograms()), weighted());
        icons.addView(desktopIcon("▶", "Lanzador", v -> showLauncher()), weighted());
        icons.addView(desktopIcon("⚙", "Diagnóstico", v -> showDiagnostics()), weighted());
        icons.addView(desktopIcon("i", "Acerca de", v -> showAbout()), weighted());
        root.addView(icons, lp(-1, dp(112)));

        // Main desktop window
        LinearLayout window = column();
        window.setBackground(round(PANEL, 20));
        window.setClipToOutline(true);
        LinearLayout titleBar = row();
        titleBar.setGravity(Gravity.CENTER_VERTICAL);
        titleBar.setPadding(dp(15), dp(12), dp(12), dp(12));
        titleBar.setBackground(round(PANEL2, 18));
        TextView windowTitle = label("⌘   CENTRO DE ARCHIVOS", 12, WHITE, true);
        titleBar.addView(windowTitle, new LinearLayout.LayoutParams(0, -2, 1));
        TextView live = label("● ACTIVO", 10, CYAN, true);
        titleBar.addView(live);
        TextView controls = label("   −   □   ×", 11, MUTED, true);
        titleBar.addView(controls);
        window.addView(titleBar, lp(-1, dp(48)));

        LinearLayout inside = column();
        inside.setPadding(dp(16), dp(18), dp(16), dp(18));
        TextView hero = label("Bienvenido a BLASTER", 23, WHITE, true);
        inside.addView(hero);
        TextView desc = label("Un escritorio Android con herramientas para organizar y preparar tus programas.", 13, MUTED, false);
        desc.setPadding(0, dp(7), 0, dp(16));
        inside.addView(desc);

        Button pick = button("＋   Seleccionar archivo EXE", BLUE);
        inside.addView(pick, lp(-1, dp(50)));
        pick.setOnClickListener(v -> openPicker());

        selectedFile = label("Ningún archivo seleccionado", 13, WHITE, true);
        selectedFile.setPadding(0, dp(14), 0, dp(3));
        inside.addView(selectedFile);
        fileMeta = label("Puedes elegir un archivo desde Descargas o cualquier carpeta accesible.", 11, MUTED, false);
        inside.addView(fileMeta);
        Button installRuntime = button("⚙   Importar paquete del motor BLASTER", Color.rgb(76, 91, 130));
        installRuntime.setTextSize(12);
        installRuntime.setOnClickListener(v -> openRuntimePicker());
        inside.addView(installRuntime, lp(-1, dp(44)));
        Button runExe = button("▶   Intentar ejecutar programa", Color.rgb(35, 163, 132));
        runExe.setOnClickListener(v -> attemptRunSelected());
        LinearLayout.LayoutParams runParams = lp(-1, dp(48));
        runParams.setMargins(0, dp(12), 0, dp(14));
        inside.addView(runExe, runParams);

        View divider = new View(this);
        divider.setBackgroundColor(Color.rgb(45, 62, 91));
        inside.addView(divider, lp(-1, dp(1)));
        TextView systemTitle = sectionTitle("ESTADO DEL SISTEMA");
        systemTitle.setPadding(0, dp(15), 0, dp(8));
        inside.addView(systemTitle);
        addStatus(inside, "Arquitectura Android", detectArch(), CYAN);
        addStatus(inside, "Motor Windows", windowsRuntime.isInstalled() ? "Puente detectado" : "No instalado", windowsRuntime.isInstalled() ? CYAN : Color.rgb(255, 194, 92));
        runtimeStatus = label(windowsRuntime.getStatus(), 11, MUTED, false);
        runtimeStatus.setPadding(0, dp(10), 0, 0);
        inside.addView(runtimeStatus);
        window.addView(inside);
        root.addView(window, lp(-1, -2));

        // Taskbar-style controls
        root.addView(label("BARRA DE TAREAS", 10, MUTED, true), lp(-1, dp(32)));
        LinearLayout dock = row();
        dock.setGravity(Gravity.CENTER_VERTICAL);
        dock.setPadding(dp(8), dp(7), dp(8), dp(7));
        dock.setBackground(round(Color.rgb(22, 34, 59), 18));
        Button start = button("⊞  Inicio", BLUE);
        start.setTextSize(12);
        dock.addView(start, lp(dp(108), dp(43)));
        start.setOnClickListener(v -> toggleStartMenu());
        TextView running = label("BLASTER DESKTOP  •  ACTIVO", 10, MUTED, true);
        running.setGravity(Gravity.CENTER);
        dock.addView(running, new LinearLayout.LayoutParams(0, dp(43), 1));
        TextView clock = label("●", 15, CYAN, true);
        clock.setGravity(Gravity.CENTER);
        dock.addView(clock, lp(dp(30), dp(43)));
        root.addView(dock, lp(-1, -2));

        startMenu = column();
        startMenu.setPadding(dp(16), dp(14), dp(16), dp(14));
        startMenu.setBackground(round(Color.rgb(24, 38, 65), 18));
        startMenu.setVisibility(View.GONE);
        startMenu.addView(label("MENÚ BLASTER", 14, WHITE, true), lp(-1, dp(30)));
        addMenuItem(startMenu, "▤   Abrir archivos", v -> { hideStartMenu(); openPicker(); });
        addMenuItem(startMenu, "⚙   Diagnóstico del sistema", v -> { hideStartMenu(); showDiagnostics(); });
        addMenuItem(startMenu, "ⓘ   Acerca de BLASTER", v -> { hideStartMenu(); showAbout(); });
        root.addView(startMenu, lp(-1, -2));

        TextView footer = label("BLASTER EXE  •  INTERFAZ DE ESCRITORIO EN DESARROLLO", 9, MUTED, false);
        footer.setGravity(Gravity.CENTER);
        root.addView(footer, lp(-1, dp(42)));
        setContentView(scroll);
    }

    private void toggleStartMenu() {
        startOpen = !startOpen;
        startMenu.setVisibility(startOpen ? View.VISIBLE : View.GONE);
    }
    private void hideStartMenu() { startOpen = false; startMenu.setVisibility(View.GONE); }

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
        android.app.AlertDialog dialog;
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

        final android.app.AlertDialog[] dialogHolder = new android.app.AlertDialog[1];
        dialogHolder[0] = new android.app.AlertDialog.Builder(this)
            .setView(panel)
            .setNegativeButton("Cerrar", null)
            .create();
        dialog = dialogHolder[0];
        dialog.show();
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
    private LinearLayout.LayoutParams lp(int w, int h) { return new LinearLayout.LayoutParams(w, h); }
    private LinearLayout.LayoutParams weighted() { return new LinearLayout.LayoutParams(0, -1, 1); }
    private int dp(int value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }
}
