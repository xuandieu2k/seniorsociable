package vn.xdeuhug.seniorsociable.http.model

import com.hjq.http.config.IRequestBodyStrategy
import vn.xdeuhug.seniorsociable.other.AppConfig
import com.hjq.http.config.IRequestServer
import com.hjq.http.model.RequestBodyType

/**
 * @Author: Bùi Hửu Thắng
 * @Date: 03/10/2022
 */
class RequestServer : IRequestServer {

    override fun getHost(): String {
        return AppConfig.getHostUrl()
    }

    fun getPath(): String {
        return "api/"
    }

    fun getType(): IRequestBodyStrategy {
        // 以表单的形式提交参数
        return RequestBodyType.JSON
    }
}