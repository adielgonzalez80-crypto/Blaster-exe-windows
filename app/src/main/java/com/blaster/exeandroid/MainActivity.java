package com.blaster.exeandroid;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.widget.*;
import android.graphics.Color;

public class MainActivity extends Activity {
    private TextView selected;
    private static final int PICK_EXE = 1001;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(32,32,32,32); root.setBackgroundColor(Color.rgb(18,18,18));
        TextView title = new TextView(this); title.setText("BLASTER EXE ANDROID"); title.setTextColor(Color.WHITE); title.setTextSize(26); title.setGravity(Gravity.CENTER); root.addView(title,new LinearLayout.LayoutParams(-1,80));
        TextView info = new TextView(this); info.setText("Windows EXE environment • ARM64/ARM32/x86_64 detection\nRuntime: Android shell prepared for Wine/Box64/WOW64 integration"); info.setTextColor(Color.LTGRAY); info.setTextSize(15); root.addView(info,new LinearLayout.LayoutParams(-1,120));
        Button pick = new Button(this); pick.setText("SELECCIONAR ARCHIVO .EXE"); root.addView(pick,new LinearLayout.LayoutParams(-1,70));
        selected = new TextView(this); selected.setText("Ningún EXE seleccionado"); selected.setTextColor(Color.WHITE); selected.setTextSize(16); selected.setPadding(0,25,0,25); root.addView(selected,new LinearLayout.LayoutParams(-1,100));
        TextView arch = new TextView(this); arch.setText("Arquitectura Android: " + detectArch()); arch.setTextColor(Color.GREEN); arch.setTextSize(16); root.addView(arch,new LinearLayout.LayoutParams(-1,70));
        Button status = new Button(this); status.setText("DIAGNÓSTICO DEL RUNTIME"); root.addView(status,new LinearLayout.LayoutParams(-1,70));
        status.setOnClickListener(v -> Toast.makeText(this,"Base Android lista. Wine/Box64/WOW64 se integrarán en la siguiente fase.",Toast.LENGTH_LONG).show());
        pick.setOnClickListener(v -> { Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT); i.setType("application/octet-stream"); i.addCategory(Intent.CATEGORY_OPENABLE); startActivityForResult(i,PICK_EXE); });
        setContentView(root);
    }
    private String detectArch(){ String a=android.os.Build.SUPPORTED_ABIS.length>0?android.os.Build.SUPPORTED_ABIS[0]:"unknown"; return a; }
    @Override protected void onActivityResult(int r,int c,Intent d){ super.onActivityResult(r,c,d); if(r==PICK_EXE&&c==RESULT_OK&&d!=null){ Uri u=d.getData(); selected.setText("EXE seleccionado:\n"+u); } }
}
