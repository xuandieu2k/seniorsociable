@file:Suppress("DEPRECATION")

package vn.xdeuhug.seniorsociable.service

import android.content.Context
import android.content.Intent
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.app.JobIntentService
import androidx.core.os.HandlerCompat.postDelayed
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import org.greenrobot.eventbus.EventBus
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.ModuleClassConstants
import vn.xdeuhug.seniorsociable.database.UserManagerFSDB
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.model.eventbus.BlockUserEventBus
import vn.xdeuhug.seniorsociable.ui.dialog.BlockUserDialog

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 30 / 12 / 2023
 */
class BlockUserService : JobIntentService() {
    companion object {
        private const val JOB_ID = 1001

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, BlockUserService::class.java, JOB_ID, work)
        }
    }

    override fun onHandleWork(intent: Intent) {
        runBlocking {
            UserManagerFSDB.listenerTypeActiveChange(object :
                UserManagerFSDB.Companion.ListenerBlockChange{
                override fun onBlock(isBlock: Boolean, user: User) {
                    if(isBlock)
                    {
                        BlockUserDialog.Builder(baseContext,user).onActionDone(object :
                            BlockUserDialog.Builder.OnActionDone{
                            override fun onActionDone(isConfirm: Boolean) {
                                if(isConfirm)
                                {
                                    Firebase.auth.signOut()
                                    UserCache.saveUser(User())
                                    val intent = Intent(
                                        this@BlockUserService, Class.forName(ModuleClassConstants.LOGIN_ACTIVITY)
                                    )
                                    startActivity(intent)
                                    EventBus.getDefault().post(BlockUserEventBus(true))
                                }
                            }

                        })
                    }
                }

                override fun onFailure(exception: Exception) {
                    exception.printStackTrace()
                }

            })
        }
    }
}