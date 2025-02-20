package vn.xdeuhug.seniorsociable.service

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.toast
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.CallConstants
import vn.xdeuhug.seniorsociable.constants.ModuleClassConstants
import vn.xdeuhug.seniorsociable.database.CallRoomManagerFSDB
import vn.xdeuhug.seniorsociable.model.entity.modelCall.CallRoom
import vn.xdeuhug.seniorsociable.utils.AppUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 13 / 12 / 2023
 */
class CallRoomJobIntentService : JobIntentService() {

    companion object {
        private const val JOB_ID = 1001

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, CallRoomJobIntentService::class.java, JOB_ID, work)
        }
    }

    override fun onHandleWork(intent: Intent) {
        // Lắng nghe call
        if (UserCache.getUser().id != "" && FirebaseAuth.getInstance().currentUser != null)
        {
            CallRoomManagerFSDB.listenToCallRoomChanges(UserCache.getUser().id,
                object : CallRoomManagerFSDB.Companion.ListenerDataCallChange {
                    override fun onDataCallChange(callRoom: CallRoom) {
                        if (callRoom.busy == CallConstants.IS_BUSY) {
                            if (callRoom.typeCall == CallConstants.TYPE_VOICE_CALL) {
                                val newIntent = Intent(
                                    applicationContext, Class.forName(
                                        ModuleClassConstants.CALL_ACTIVITY
                                    )
                                )
                                newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                if (callRoom.statusRoom == CallConstants.IS_CALLING) {
                                    newIntent.putExtra(
                                        CallConstants.ID_CALlER, callRoom.id
                                    )
                                    newIntent.putExtra(
                                        CallConstants.ID_RECEIVER, callRoom.userCall!!.id
                                    )
                                    try {
                                        startActivity(newIntent)
                                    } catch (ex: Exception) {
                                        AppUtils.logJsonFromObject(ex)
                                    }
                                }
                                if (callRoom.statusRoom == CallConstants.IS_RINGING) {

                                    newIntent.putExtra(
                                        CallConstants.ID_CALlER, callRoom.userCall!!.id
                                    )
                                    newIntent.putExtra(
                                        CallConstants.ID_RECEIVER, callRoom.id
                                    )
                                    try {
                                        startActivity(newIntent)
                                    } catch (ex: Exception) {
                                        AppUtils.logJsonFromObject(ex)
                                    }
                                }
                            } else {
                                val newIntent = Intent(
                                    applicationContext, Class.forName(
                                        ModuleClassConstants.CALL_VIDEO_ACTIVITY
                                    )
                                )
                                newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                newIntent.putExtra(
                                    CallConstants.ID_CALlER, callRoom.id
                                )
                                newIntent.putExtra(
                                    CallConstants.ID_RECEIVER, callRoom.userCall?.id
                                )
                                try {
                                    startActivity(newIntent)
                                } catch (ex: Exception) {
                                    ex.printStackTrace()
                                }
                            }
                        }
                    }

                })
        }
    }
}