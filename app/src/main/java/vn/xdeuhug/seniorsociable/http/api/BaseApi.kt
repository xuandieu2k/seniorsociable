package vn.xdeuhug.seniorsociable.http.api

import com.hjq.http.annotation.HttpHeader
import com.hjq.http.annotation.HttpRename
import com.hjq.http.config.IRequestApi
import com.hjq.http.config.IRequestBodyStrategy
import com.hjq.http.config.IRequestType
import com.hjq.http.model.RequestBodyType
import vn.xdeuhug.seniorsociable.constants.AppConstants


/**
 * @Author: Bùi Hửu Thắng
 * @Date: 03/10/2022
 */
open class BaseApi : IRequestApi, IRequestType {

    override fun getBodyType(): IRequestBodyStrategy {
        return RequestBodyType.JSON
    }

    @HttpHeader
    @HttpRename("Method")
    var method = AppConstants.HTTP_METHOD_GET

    @HttpHeader
    @HttpRename("Authorization")
    var authorization = ""

    override fun getApi(): String {
        return ""
    }
}