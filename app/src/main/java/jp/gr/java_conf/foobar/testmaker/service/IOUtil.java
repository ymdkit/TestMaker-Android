package jp.gr.java_conf.foobar.testmaker.service;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by keita on 2016/10/26.
 */
public class IOUtil {

    /**
     * Closeableインスタンスを強制的に閉じます<br>
     *
     * @param closeable
     */
    public static void forceClose(Closeable closeable) {
        if (closeable == null)
            return;
        try {
            closeable.close();
        } catch (IOException e) {
            // ignore
        }
    }

}

