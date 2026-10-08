package com.blaster.exeandroid;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

final class RuntimeVerifier {
    private RuntimeVerifier() {}

    static String sha256(File file) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (FileInputStream in = new FileInputStream(file)) {
                byte[] buffer = new byte[64 * 1024];
                int n;
                while ((n = in.read(buffer)) > 0) md.update(buffer, 0, n);
            }
            StringBuilder out = new StringBuilder();
            for (byte b : md.digest()) out.append(String.format("%02x", b));
            return out.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
