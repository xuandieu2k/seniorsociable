package vn.xdeuhug.seniorsociable.router

import vn.xdeuhug.seniorsociable.cache.UserCache

@Suppress("FunctionName")
object ApiOauthRouters {
    private const val VERSION = "v5"

    fun API_LOGIN(): String {
        return "api/$VERSION/suppliers/login"
    }

    fun API_LOGOUT(): String {
        return "api/$VERSION/employees/push-token/logout"
    }

    fun API_GET_SESSION(): String {
        return "api/$VERSION/sessions"
    }

    fun API_GET_CONFIG(): String {
        return "api/$VERSION/configs"
    }

    fun API_GET_SETTING(): String {
        return "api/$VERSION/employees/settings"
    }

    fun API_GET_VERSION(): String {
        return "api/$VERSION/check-version?os_name=seemt_android"
    }

    fun API_CHECK_OTP(): String {
        return "api/$VERSION/suppliers/verify-change-password"
    }

    fun API_FORGOT_PASSWORD(): String {
        return "api/$VERSION/suppliers/forgot-password"
    }

    fun API_REGISTER_DEVICE(): String {
        return "api/$VERSION/register-supplier-device"
    }

    fun API_VERIFY_CHANGE_PASSWORD(): String {
        return "api/$VERSION/employees/verify-change-password"
    }

    fun API_CHANGE_PASSWORD(): String {
        return "api/$VERSION/employees/${UserCache.getUser().id}/change-password"
    }

    fun API_CHECK_OTP_NEW(): String {
        return "api/$VERSION/suppliers/verify-code"
    }

    fun API_FEED_BACK(): String {
        return "api/$VERSION/employees/feedback"
    }

    fun API_GET_VERSION_UPDATE(): String {
        return "api/$VERSION/check-version"
    }
}