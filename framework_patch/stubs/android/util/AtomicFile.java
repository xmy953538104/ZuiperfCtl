package android.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AtomicFile {
    public AtomicFile(File baseName) {
    }

    public byte[] readFully() throws IOException {
        return new byte[0];
    }

    public FileOutputStream startWrite() throws IOException {
        return null;
    }

    public void finishWrite(FileOutputStream str) {
    }

    public void failWrite(FileOutputStream str) {
    }
}
