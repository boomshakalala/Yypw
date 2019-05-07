package cn.sinata.util

import android.annotation.SuppressLint
import cn.sinata.xldutils.sysErr
import java.io.UnsupportedEncodingException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object DES {

    private val CHAR_SET = "utf-8"

    private var iv: ByteArray? = null
    //密钥
    private var skey: ByteArray? = null

    external fun getKeyValue(): ByteArray

    external fun getIv(): ByteArray

    init {
//        System.loadLibrary("security")
//        skey = getKeyValue()
//        iv = getIv()
        skey = "YYPWAPP=".toByteArray()
        iv = byteArrayOf(12, 22, 32, 43, 51, 64, 57, 98)
    }

    /**
     * 加密
     * @param encryptString 待加密字符
     * @return 加密后字符串
     */
    @SuppressLint("TrulyRandom")
    fun encryptDES(encryptString: String): String {

        val zeroIv = IvParameterSpec(iv)

        val key = SecretKeySpec(skey, "DES")

        try {
            val cipher = Cipher.getInstance("DES/CBC/PKCS5Padding")

            cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv)

            val encryptedData = cipher.doFinal(encryptString.toByteArray(charset(CHAR_SET)))

            return Base64DES.encode(encryptedData)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        }

        return encryptString
    }

    /**
     * DES解密
     *
     * @param decryptString 待解密字符
     * @return 解密后字符串
     */
    fun decryptDES(decryptString: String): String {

        val byteMi = Base64DES.decode(decryptString)

        val zeroIv = IvParameterSpec(iv)

        val key = SecretKeySpec(skey, "DES")

        try {
            val cipher = Cipher.getInstance("DES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, key, zeroIv)

            val decryptedData = cipher.doFinal(byteMi)

            return String(decryptedData, charset(CHAR_SET))
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        }

//        val s = decryptDES(decryptString)
//        System.err.println("des--------------->" + s)
        return decryptString

//        val byteMi = Base64DES.decode(decryptString)
//
//        val zeroIv = IvParameterSpec(iv)
//
//        val key = SecretKeySpec(skey, "DES")
//
//        try {
//            val cipher = Cipher.getInstance("DES/CBC/PKCS5Padding")
//            cipher.init(Cipher.DECRYPT_MODE, key, zeroIv)
//            val decryptedData = cipher.doFinal(byteMi)
//            return String(decryptedData, charset(CHAR_SET))
//        } catch (e: NoSuchAlgorithmException) {
//            e.printStackTrace()
//        } catch (e: NoSuchPaddingException) {
//            e.printStackTrace()
//        } catch (e: InvalidKeyException) {
//            e.printStackTrace()
//        } catch (e: InvalidAlgorithmParameterException) {
//            e.printStackTrace()
//        } catch (e: UnsupportedEncodingException) {
//            e.printStackTrace()
//        } catch (e: IllegalBlockSizeException) {
//            e.printStackTrace()
//        } catch (e: BadPaddingException) {
//            e.printStackTrace()
//        }
//
//        return decryptString
    }
}
