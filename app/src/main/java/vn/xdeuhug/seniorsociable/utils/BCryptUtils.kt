package vn.xdeuhug.seniorsociable.utils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 09 / 10 / 2023
 */
import at.favre.lib.crypto.bcrypt.BCrypt

object BCryptUtils {
    // Mã hóa mật khẩu và trả về mật khẩu đã được mã hóa
    fun hashPassword(plainPassword: String): String {
        val cost = 12 // Độ phức tạp của bcrypt, Có thể điều chỉnh tùy ý
        // Mã hóa mật khẩu và salt để tạo mật khẩu đã được mã hóa
        return BCrypt.withDefaults().hashToString(cost, plainPassword.toCharArray())
    }

    /*
     Kiểm tra mật khẩu đã nhập với mật khẩu đã được mã hóa
     */
    fun verifyPassword(plainPassword: String, hashedPassword: String): Boolean {
        return BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword).verified
    }
}