package vn.xdeuhug.seniorsociable.app

import com.hjq.http.listener.OnHttpListener
import okhttp3.Call
import vn.xdeuhug.base.BaseFragment
import vn.xdeuhug.base.action.ToastAction
import vn.xdeuhug.seniorsociable.http.model.HttpData
import vn.xdeuhug.seniorsociable.ui.dialog.WaitDialog

/**
 * @Author: Bùi Hửu Thắng
 * @Date: 03/10/2022
 */
abstract class AppFragment<A : AppActivity> : BaseFragment<A>(),
    ToastAction, OnHttpListener<Any> {


    /**
     * 当前加载对话框是否在显示中
     */
    open fun isShowDialog(): Boolean {
        val activity: A = getAttachActivity() ?: return false
        return activity.isShowDialog()
    }

    override fun initView() {
        //code
    }

    /**
     * 显示加载对话框
     */
    open fun showDialog() {
        getAttachActivity()?.showDialog()
    }

    open fun showDialog(message:String) {
        getAttachActivity()?.showDialog(message)
    }

    /**
     * 隐藏加载对话框
     */
    open fun hideDialog() {
        getAttachActivity()?.hideDialog()
    }

    /**
     * [OnHttpListener]
     */
    override fun onHttpStart(call: Call) {
        //  showDialog()
    }

    override fun onHttpSuccess(result: Any) {
        if (result !is HttpData<*>) {
            return
        }
        toast(result.getMessage())
        //  hideDialog()
    }

    override fun onHttpFail(e: Exception) {
        //   hideDialog()
        toast(e.message)
    }

    override fun onHttpEnd(call: Call) {
        //   hideDialog()
    }
}