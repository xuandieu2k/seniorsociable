package vn.xdeuhug.seniorsociable.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
class CallRoomService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Lắng nghe call
        CallRoomManagerFSDB.listenToCallRoomChanges(
            UserCache.getUser().id,
            object : CallRoomManagerFSDB.Companion.ListenerDataCallChange {
                override fun onDataCallChange(callRoom: CallRoom) {
                    AppUtils.logJsonFromObject(callRoom)
                    if (callRoom.busy == CallConstants.IS_BUSY) {
                        if(callRoom.typeCall == CallConstants.TYPE_VOICE_CALL){
//                                toast("Voice")
                            val newIntent = Intent(applicationContext, Class.forName(ModuleClassConstants.CALL_ACTIVITY))
                            newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//                                newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            if(callRoom.typeCall == CallConstants.IS_CALLING)
                            {
                                newIntent.putExtra(
                                    CallConstants.ID_CALlER, callRoom.id
                                )
                                newIntent.putExtra(
                                    CallConstants.ID_RECEIVER, callRoom.userCall!!.id
                                )
                                toast("Caller")
                            }
                            if(callRoom.typeCall == CallConstants.IS_RINGING)
                            {

                                newIntent.putExtra(
                                    CallConstants.ID_CALlER, callRoom.userCall!!.id
                                )
                                newIntent.putExtra(
                                    CallConstants.ID_RECEIVER, callRoom.id
                                )
                                toast("Receiver")
                            }
                            try {
                                startActivity(newIntent)
                            } catch (ex: Exception) {
                                toast("Loi")
                                AppUtils.logJsonFromObject(ex)
                            }
                        }else{
                            toast("Video")
                            val newIntent = Intent(applicationContext, Class.forName(ModuleClassConstants.CALL_VIDEO_ACTIVITY))
                            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK )
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
        return START_STICKY
    }
}