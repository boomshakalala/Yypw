package cn.sinata.xldutils.utils

import java.io.*

/**
 * Created on 2018/1/12.
 */
object FileUtils {

    fun writeToDisk(inputStream: InputStream?, path: String): Boolean {
        try {
            val futureStudioIconFile = File(path)
            var outputStream: OutputStream? = null
            try {
                val fileReader = ByteArray(4096)
                var fileSizeDownloaded: Long = 0
                outputStream = FileOutputStream(futureStudioIconFile)
                while (true) {
                    val read = inputStream!!.read(fileReader)
                    if (read == -1) {
                        break
                    }
                    outputStream.write(fileReader, 0, read)
                    fileSizeDownloaded += read.toLong()
                }
                outputStream.flush()
                return true
            } catch (e: IOException) {
                return false
            } finally {
                if (inputStream != null) {
                    inputStream.close()
                }
                if (outputStream != null) {
                    outputStream.close()
                }
            }
        } catch (e: IOException) {
            return false
        }

    }
}