package vn.xdeuhug.seniorsociable.app.helper

import android.content.Context
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.RequestObserverDelegate

class GlobalUploadObserver : RequestObserverDelegate {

    override fun onCompleted(context: Context, uploadInfo: UploadInfo) {}

    override fun onCompletedWhileNotObserving() {}

    override fun onError(context: Context, uploadInfo: UploadInfo, exception: Throwable) {

    }

    override fun onProgress(context: Context, uploadInfo: UploadInfo) {

    }

    override fun onSuccess(
        context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse
    ) {

    }
}