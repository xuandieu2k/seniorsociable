package vn.xdeuhug.seniorsociable.utils

import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * Created by kylin on 2017/4/6.
 */
object StringUtils {
    /**
     * string to byte[]
     */
    fun strTobytes(str: String): ByteArray? {
        var b: ByteArray? = null
        var data: ByteArray? = null
        try {
            b = str.toByteArray(StandardCharsets.UTF_8)
            data = String(b, StandardCharsets.UTF_8).toByteArray(charset("gbk"))
        } catch (e: UnsupportedEncodingException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
        return data
    }

    /**
     * byte[] merger
     */
    fun byteMerger(byte_1: ByteArray, byte_2: ByteArray): ByteArray {
        val byte_3 = ByteArray(byte_1.size + byte_2.size)
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.size)
        System.arraycopy(byte_2, 0, byte_3, byte_1.size, byte_2.size)
        return byte_3
    }

    fun strTobytes(str: String, charset: String?): ByteArray? {
        var b: ByteArray? = null
        var data: ByteArray? = null
        try {
            b = str.toByteArray(StandardCharsets.UTF_8)
            data = String(b, StandardCharsets.UTF_8).toByteArray(charset(charset!!))
        } catch (e: UnsupportedEncodingException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
        return data
    }

    fun combineAndHashIds(id1: String, id2: String): String {
        // Sắp xếp theo thứ tự từ điển
        val sortedIds = listOf(id1, id2).sorted()

        // Ghép chuỗi theo thứ tự
        val combinedString = sortedIds.joinToString("")

        // Sử dụng hàm hash (ở đây là SHA-256) để tạo một chuỗi duy nhất
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(combinedString.toByteArray())

        // Chuyển đổi mảng byte thành chuỗi hex
        val hashedString = StringBuilder()
        for (byte in hashedBytes) {
            hashedString.append(String.format("%02x", byte))
        }

        return hashedString.toString()
    }

    fun splitStringIntoWords(str: String): ArrayList<String> {
        // Sử dụng regex để tách chuỗi dựa trên các khoảng trắng, dấu chấm câu, v.v.
        val words = str.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        return ArrayList(words)
    }



}