package jp.gr.java_conf.foobar.testmaker.service

import java.io.Closeable
import java.io.IOException

/**
 * Created by keita on 2016/10/26.
 */
object IOUtil {

    /**
     * Closeableインスタンスを強制的に閉じます<br></br>
     *
     * @param closeable
     */
    fun forceClose(closeable: Closeable?) {
        if (closeable == null)
            return
        try {
            closeable.close()
        } catch (e: IOException) {
            // ignore
        }

    }

}

