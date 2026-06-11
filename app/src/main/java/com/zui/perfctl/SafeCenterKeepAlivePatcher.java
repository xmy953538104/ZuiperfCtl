package com.zui.perfctl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class SafeCenterKeepAlivePatcher {
    private static final String DEFAULT_PACKAGE = "com.zui.zuiperfctl";
    private static final String DEFAULT_DB =
            "/data/user/0/com.zui.safecenter/databases/BlackWhite.db";
    private static final String AES_KEY = "qWmHYUg8Gfr50Njg";

    private SafeCenterKeepAlivePatcher() {
    }

    public static void main(String[] args) {
        String pkg = args.length > 0 && !args[0].isEmpty() ? args[0] : DEFAULT_PACKAGE;
        String dbPath = args.length > 1 && !args[1].isEmpty() ? args[1] : DEFAULT_DB;
        SQLiteDatabase db = null;
        int exitCode = 0;
        try {
            String encryptedPkg = encrypt(pkg);
            db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
            db.beginTransaction();
            boolean exists = hasAccelerateEntry(db, encryptedPkg);
            if (!exists) {
                ContentValues values = new ContentValues();
                values.put("PkgName", encryptedPkg);
                values.put("Type", 1);
                long rowId = db.insert("AccelerateList", null, values);
                if (rowId < 0) {
                    throw new IllegalStateException("insert AccelerateList failed");
                }
                System.out.println("inserted=true rowId=" + rowId
                        + " package=" + pkg + " encrypted=" + encryptedPkg);
            } else {
                System.out.println("inserted=false package=" + pkg
                        + " encrypted=" + encryptedPkg);
            }
            db.setTransactionSuccessful();
        } catch (Throwable t) {
            System.err.println("error=" + t.getClass().getSimpleName()
                    + " message=" + String.valueOf(t.getMessage()));
            t.printStackTrace(System.err);
            exitCode = 1;
        } finally {
            if (db != null) {
                if (db.inTransaction()) {
                    db.endTransaction();
                }
                db.close();
            }
        }
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    private static boolean hasAccelerateEntry(SQLiteDatabase db, String encryptedPkg) {
        try (Cursor cursor = db.query(
                "AccelerateList",
                new String[]{"Id"},
                "PkgName=?",
                new String[]{encryptedPkg},
                null,
                null,
                null,
                "1")) {
            return cursor.moveToFirst();
        }
    }

    private static String encrypt(String value) throws Exception {
        byte[] key = AES_KEY.getBytes(StandardCharsets.UTF_8);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(
                Cipher.ENCRYPT_MODE,
                new SecretKeySpec(key, "AES"),
                new IvParameterSpec(key));
        byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(encrypted, Base64.NO_WRAP);
    }
}
