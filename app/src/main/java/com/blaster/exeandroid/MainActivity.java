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

/**
 * BLASTER EXE — first native desktop-style UI pass.
 * This screen provides a real Android file picker and honest runtime status.
 * It does not claim to execute Windows programs until a compatibility runtime is integrated.
 */
public class MainActivity extends Activity {
    private static final int PICK_EXE = 1001;
    private static final int BG = Color.rgb(10, 14, 25);
    private static final int PANEL = Color.rgb(20, 28, 45);
    private static final int PANEL_LIGHT = Color.rgb(27, 39, 61);
    private static final int BLUE = Color.rgb(50, 119, 255);
    private static final int CYAN = Color.rgb(47, 213, 226);
    private static final int WHITE = Color.rgb(242, 246, 255);
    private static final int MUTED = Color.rgb(157, 171, 196);
    private TextView selectedFile;
    private TextView fileMeta;
    private TextView runtimeStatus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(0);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(20), dp(18), dp(20), dp(24));
        scroll.addView(page);

        // Brand header
        LinearLayout header = row();
        TextView mark = label("B", 26, WHITE, true);
        mark.setGravity(Gravity.CENTER);
        mark.setBackground(round(BLUE, 16));
        header.addView(mark, new LinearLayout.LayoutParams(dp(54), dp(54)));

        LinearLayout brand = column();
        brand.setPadding(dp(12), 0, 0, 0);
        brand.addView(label("BLASTER", 22, WHITE, true));
        brand.addView(label("EXE  /  ANDROID DESKTOP", 11, CYAN, true));
        header.addView(brand, new LinearLayout.LayoutParams(0, -2, 1));
        TextView version = label("BETA", 11, WHITE, true);
        version.setGravity(Gravity.CENTER);
        version.setPadding(dp(12), dp(7), dp(12), dp(7));
        version.setBackground(round(Color.rgb(41, 64, 105), 30));
        header.addView(version);
        page.addView(header, params(-1, -2, 0, 0, 0, 22));

        // Welcome panel
        LinearLayout hero = column();
        hero.setPadding(dp(20), dp(22), dp(20), dp(22));
        hero.setBackground(round(PANEL_LIGHT, 22));
        TextView eyebrow = label("TU ESPACIO DIGITAL", 11, CYAN, true);
        hero.addView(eyebrow);
        TextView headline = label("Un nuevo entorno.\nUna identidad propia.", 27, WHITE, true);
        headline.setPadding(0, dp(10), 0, dp(8));
        hero.addView(headline);
        TextView sub = label("Administra tus archivos y prepara el entorno para programas Windows compatibles.", 14, MUTED, false);
        hero.addView(sub);
        page.addView(hero, params(-1, -2, 0, 0, 0, 20));

        page.addView(sectionTitle("CENTRO DE ARCHIVOS"));
        LinearLayout fileCard = column();
        fileCard.setPadding(dp(17), dp(17), dp(17), dp(17));
        fileCard.setBackground(round(PANEL, 18));

        TextView fileIcon = label("EXE", 16, CYAN, true);
        fileIcon.setGravity(Gravity.CENTER);
        fileIcon.setBackground(round(Color.rgb(22, 55, 79), 12));
        fileCard.addView(fileIcon, params(dp(58), dp(48), 0, 0, 0, 12));

        fileCard.addView(label("Selecciona un programa", 18, WHITE, true));
        TextView hint = label("Elige un archivo .exe desde Descargas o desde otra carpeta accesible.", 13, MUTED, false);
        hint.setPadding(0, dp(6), 0, dp(14));
        fileCard.addView(hint);

        Button pick = actionButton("＋   Seleccionar archivo EXE", BLUE);
        fileCard.addView(pick, params(-1, dp(52), 0, 0, 0, 12));
        selectedFile = label("Ningún archivo seleccionado", 13, WHITE, true);
        fileCard.addView(selectedFile);
        fileMeta = label("La arquitectura del archivo se comprobará cuando sea posible.", 12, MUTED, false);
        fileMeta.setPadding(0, dp(5), 0, 0);
        fileCard.addView(fileMeta);
        page.addView(fileCard, params(-1, -2, 0, 0, 0, 20));

        // System overview
        page.addView(sectionTitle("ESTADO DEL SISTEMA"));
        LinearLayout statusCard = column();
        statusCard.setPadding(dp(17), dp(16), dp(17), dp(16));
        statusCard.setBackground(round(PANEL, 18));
        addStatusRow(statusCard, "Arquitectura Android", detectArch(), CYAN);
        addDivider(statusCard);
        addStatusRow(statusCard, "Entorno Windows", "Pendiente de integrar", Color.rgb(255, 193, 89));
        addDivider(statusCard);
        runtimeStatus = label("El selector de archivos está disponible. El motor de compatibilidad todavía no está integrado.", 12, MUTED, false);
        runtimeStatus.setPadding(0, dp(12), 0, 0);
        statusCard.addView(runtimeStatus);
        page.addView(statusCard, params(-1, -2, 0, 0, 0, 20));

        // Useful actions
        page.addView(sectionTitle("HERRAMIENTAS BLASTER"));
        LinearLayout tools = row();
        tools.addView(toolTile("▤", "Mis archivos", "Buscar archivos", v -> openPicker()), new LinearLayout.LayoutParams(0, dp(112), 1));
        View gap = new View(this);
        tools.addView(gap, new LinearLayout.LayoutParams(dp(10), 1));
        tools.addView(toolTile("⚙", "Diagnóstico", "Ver estado", v -> showDiagnostics()), new LinearLayout.LayoutParams(0, dp(112), 1));
        page.addView(tools);

        TextView footer = label("BLASTER EXE  •  Construido para Android", 11, MUTED, false);
        footer.setGravity(Gravity.CENTER);
        page.addView(footer, params(-1, -2, 0, dp(24), 0, 0));

        setContentView(scroll);
    }

    private void openPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(intent, PICK_EXE);
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir el selector de archivos.", Toast.LENGTH_LONG).show();
        }
    }

    private void showDiagnostics() {
        String message = "Android: " + detectArch()
                + "\nSelector de archivos: disponible"
                + "\nMotor Windows: no integrado todavía"
                + "\n\nElegir un EXE no significa que Android pueda ejecutarlo. Se necesita un runtime compatible.";
        new android.app.AlertDialog.Builder(this)
                .setTitle("Diagnóstico BLASTER")
                .setMessage(message)
                .setPositiveButton("Entendido", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_EXE || resultCode != RESULT_OK || data == null || data.getData() == null) return;

        Uri uri = data.getData();
        String name = getDisplayName(uri);
        if (name == null || name.trim().isEmpty()) name = uri.getLastPathSegment();
        selectedFile.setText("Archivo: " + (name == null ? "seleccionado" : name));
        fileMeta.setText("Ubicación: " + uri + "\nArchivo seleccionado; ejecución aún no disponible.");
        runtimeStatus.setText("Archivo cargado en la interfaz. Para ejecutarlo falta integrar el motor de compatibilidad Windows.");
        Toast.makeText(this, "Archivo seleccionado correctamente", Toast.LENGTH_SHORT).show();
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
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    private String detectArch() {
        String[] abis = android.os.Build.SUPPORTED_ABIS;
        return abis != null && abis.length > 0 ? abis[0] : "Desconocida";
    }

    private void addStatusRow(LinearLayout parent, String title, String value, int valueColor) {
        LinearLayout r = row();
        r.setGravity(Gravity.CENTER_VERTICAL);
        TextView left = label(title, 13, MUTED, false);
        r.addView(left, new LinearLayout.LayoutParams(0, -2, 1));
        TextView right = label(value, 12, valueColor, true);
        right.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        r.addView(right);
        parent.addView(r, params(-1, dp(34), 0, 0, 0, 0));
    }

    private void addDivider(LinearLayout parent) {
        View line = new View(this);
        line.setBackgroundColor(Color.rgb(42, 54, 75));
        parent.addView(line, params(-1, dp(1), 0, dp(3), 0, dp(3)));
    }

    private TextView sectionTitle(String text) {
        TextView t = label(text, 12, MUTED, true);
        t.setLetterSpacing(0.08f);
        t.setPadding(dp(2), 0, 0, dp(10));
        return t;
    }

    private View toolTile(String icon, String title, String subtitle, View.OnClickListener click) {
        LinearLayout tile = column();
        tile.setGravity(Gravity.CENTER_VERTICAL);
        tile.setPadding(dp(14), dp(12), dp(12), dp(12));
        tile.setBackground(round(PANEL, 16));
        TextView glyph = label(icon, 22, CYAN, true);
        tile.addView(glyph);
        TextView titleView = label(title, 14, WHITE, true);
        titleView.setPadding(0, dp(6), 0, dp(3));
        tile.addView(titleView);
        tile.addView(label(subtitle, 11, MUTED, false));
        tile.setOnClickListener(click);
        return tile;
    }

    private Button actionButton(String text, int color) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(14);
        button.setAllCaps(false);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setBackground(round(color, 14));
        button.setPadding(dp(12), 0, dp(12), 0);
        return button;
    }

    private TextView label(String text, float size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private LinearLayout row() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        return l;
    }

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private LinearLayout.LayoutParams params(int width, int height, int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(width, height);
        p.setMargins(left, top, right, bottom);
        return p;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
