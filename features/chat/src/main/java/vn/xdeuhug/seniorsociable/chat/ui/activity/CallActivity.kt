package vn.xdeuhug.seniorsociable.chat.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.CountDownTimer
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.cache.ListUserCache
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.chat.databinding.ActivityCallBinding
import vn.xdeuhug.seniorsociable.constants.CallConstants
import vn.xdeuhug.seniorsociable.database.CallRoomManagerFSDB
import vn.xdeuhug.seniorsociable.model.entity.modelCall.CallRoom
import vn.xdeuhug.seniorsociable.model.entity.modelCall.GenerateTokenAgora
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import java.util.concurrent.TimeUnit


/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 11 / 12 / 2023
 */
class CallActivity : AppActivity() {
    private lateinit var binding: ActivityCallBinding

    // Fill the App ID of your project generated on Agora Console.
    private var appId = ""

    // Fill the channel name.
    private var channelName = ""

    // Fill the temp token generated on Agora Console.
    private var token = ""

    // An integer that identifies the local user.
    private var appCertificateCall = ""
    private val uid = 0

    // Track the status of your connection
    private var isJoined = false

    // Agora engine instance
    private var agoraEngine: RtcEngine? = null
    private var idCaller = ""
    private var idReceiver = ""
    private var caller = User()
    private var receiver = User()
    private var callTimer: CountDownTimer? = null
    private var callStartTime: Long = 0
    private var sumTimeCall = ""
    private var generateTokenAgora: GenerateTokenAgora? = null

    private fun setupVoiceSDKEngine() {
        try {
            val config = RtcEngineConfig()
            config.mContext = baseContext
            config.mAppId = appId
            config.mEventHandler = mRtcEventHandler
            agoraEngine = RtcEngine.create(config)
        } catch (e: Exception) {
            Timber.tag("Error setupVoiceSDKEngine").e(e)
            throw RuntimeException("Check the error.")
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("moveTaskToBack(true)"))
    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    private fun startCountTimeCall() {
        callStartTime = System.currentTimeMillis()
        callTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val callDuration = System.currentTimeMillis() - callStartTime
                @SuppressLint("DefaultLocale") val callDurationFormatted = String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(callDuration),
                    TimeUnit.MILLISECONDS.toSeconds(callDuration) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(callDuration)
                    )
                )
                binding.tvStatus.text = callDurationFormatted
            }

            override fun onFinish() {
                // Do nothing
                sumTimeCall = binding.tvStatus.text.toString()
            }
        }
        (callTimer as CountDownTimer).start()
    }

    override fun getLayoutView(): View {
        binding = ActivityCallBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        setFirstView()
        initDataForView()
        listenerChange()
    }

    private fun listenerChange() {
        CallRoomManagerFSDB.listenBusy(UserCache.getUser().id,object :
            CallRoomManagerFSDB.Companion.ListenerCalling{

            override fun onBusy(isBusy: Boolean) {
                if(!isBusy)
                {
                    finish()
                }
            }

            override fun onAcceptStatusRoom() {
                //
            }

            override fun onRefuseStatusRoom() {
                //
            }

        })
        CallRoomManagerFSDB.listenStatusRoom(idReceiver,object :
            CallRoomManagerFSDB.Companion.ListenerCalling{

            override fun onBusy(isBusy: Boolean) {
                //
            }

            override fun onAcceptStatusRoom() {
                setupVoiceSDKEngine()
                joinChannel()
                setViewCalling()
            }

            override fun onRefuseStatusRoom() {
                finish()
            }
        })
    }

    private fun setViewCalling() {
        binding.llCaller.show()
        binding.llReceiver.hide()
    }

    private fun setFirstView() {
        generateTokenAgora = GenerateTokenAgora()
        appId = getString(R.string.appIdCall)
        appCertificateCall = getString(R.string.appCertificateCall)
        setView()
        val listUser = ListUserCache.getList()
        binding.tvStatus.text = getString(R.string.is_calling)
        // Nhận thông tin từ intent
        idCaller = intent.getStringExtra(CallConstants.ID_CALlER)!!
        idReceiver = intent.getStringExtra(CallConstants.ID_RECEIVER)!!
//        idCaller = intent.getStringExtra("idCaller")
        channelName = AppUtils.combineAndHashIds(idCaller,idReceiver)
        token =
            generateTokenAgora!!.GenerateTokenVoiceCall(appId, appCertificateCall, channelName, uid)
        caller = listUser.firstOrNull { it.id == idCaller }!!
        receiver = listUser.firstOrNull { it.id == idReceiver }!!
        setViewCall()
    }

    @SuppressLint("SetTextI18n")
    private fun setViewCall() {
        if(UserCache.getUser().id == idCaller)
        {
            binding.llCaller.show()
            binding.llReceiver.hide()
            PhotoShowUtils.loadAvatarImage(receiver.avatar,binding.imvAvatar)
            binding.tvUsername.text = receiver.name
            binding.tvStatus.text = getString(R.string.is_ringing)
        }else{
            binding.llCaller.hide()
            binding.llReceiver.show()
            PhotoShowUtils.loadAvatarImage(caller.avatar,binding.imvAvatar)
            binding.tvUsername.text = caller.name
            binding.tvStatus.text =   AppUtils.fromHtml( "${getString(R.string.call_from)} <b>${caller.name}</b>")
        }
    }

    private fun initDataForView(){
        //
    }

    private fun setView() {
        // If all the permissions are granted, initialize the RtcEngine object and join a channel.

        // If all the permissions are granted, initialize the RtcEngine object and join a channel.
        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(
                this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID
            )
        }
        setupVoiceSDKEngine()
        //
        // Layout Caller
        setClickEventButton()
    }

    private fun setClickEventButton() {

        binding.btnAccept.clickWithDebounce {
            isJoined = true
            CallRoomManagerFSDB.updateStatusRoom(idReceiver,CallConstants.IS_ACCEPT,object:
                CallRoomManagerFSDB.Companion.ListenUpdate{
                override fun updateStatusRoom(value: Boolean) {
                    if(value)
                    {
                        //
                    }
                }

            })
        }
        // layout receiver
        // layout receiver
        binding.btnRefuse.clickWithDebounce {
            CallRoomManagerFSDB.updateStatusRoom(idReceiver,CallConstants.IS_REFUSE,object:
                CallRoomManagerFSDB.Companion.ListenUpdate{
                override fun updateStatusRoom(value: Boolean) {
                    if(value)
                    {
                        //
                    }
                }

            })
        }
        // layout joined
        // layout joined
        binding.btnEnd.clickWithDebounce {
            agoraEngine!!.leaveChannel()
            finish()
        }
        binding.btnMic.setOnClickListener {
            if (!ismutex) {
                ismutex = true
                agoraEngine!!.muteLocalAudioStream(true)
                binding.btnMic.setImageResource(R.drawable.ic_call_mic)
            } else {
                ismutex = false
                agoraEngine!!.muteLocalAudioStream(false)
                binding.btnMic.setImageResource(R.drawable.ic_call_mic_active)
            }
        }
        binding.btnSpeaker.setOnClickListener{
            if (!isSpeaker) {
                isSpeaker = true
                agoraEngine!!.setEnableSpeakerphone(true)
                binding.btnSpeaker.setImageResource(R.drawable.ic_call_speaker)
            } else {
                isSpeaker = false
                agoraEngine!!.setEnableSpeakerphone(false)
                binding.btnSpeaker.setImageResource(R.drawable.ic_call_speaker_active)
            }
        }
    }


    override fun initData() {
        //
    }

    // Set Join call
    var countToast: Int = 0
    var valueEventListener: ValueEventListener? = null

    private var ismutex = false
    private var isSpeaker = true
    private val PERMISSION_REQ_ID = 22
    private val REQUESTED_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO
    )

    private fun checkSelfPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, REQUESTED_PERMISSIONS[0]
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun showMessage(message: String?) {
        runOnUiThread {
            toast(message)
        }
    }

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        // Listen for the remote user joining the channel.
        override fun onUserJoined(uid: Int, elapsed: Int) {
            toast("remote join ")
            runOnUiThread {
//                toast("remote join ")
            }
        }

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            // Successfully joined a channel
            isJoined = true
                        showMessage("Đã tham gia vào kênh " + channel);
            runOnUiThread {
                toast("Đã tham gia vào kênh " + channel)
            }
            //            tv_infor.setText("Waiting for a remote user to join")
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            // Listen for remote users leaving the channel
//            updateChildren();
            showMessage("Cuộc gọi đã kết thúc " + uid + " " + reason);
//            finish();
//            runOnUiThread(() -> finish());
//            if (isJoined) runOnUiThread(()->tv_infor.setText("Waiting for a remote user to join"));
        }

        override fun onLeaveChannel(stats: RtcStats) {
            // Listen for the local user leaving the channel
            runOnUiThread {
                //
            }
            isJoined = false
            finish()
        }
    }


    private fun joinChannel() {
        val options = ChannelMediaOptions()
        options.autoSubscribeAudio = true
        // Set both clients as the BROADCASTER.
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
        // Set the channel profile as BROADCASTING.
        options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
        // Join the channel with a temp token.
        // You need to specify the user ID yourself, and ensure that it is unique in the channel.
        agoraEngine!!.joinChannel(token, channelName, uid, options)
        Timber.tag("Log Chanel").i("$token $channelName $uid $options")
        startCountTimeCall()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Destroy the engine in a sub-thread to avoid congestion
        runBlocking {
            // Room Receiver
            val callRoomCaller = CallRoom(
                idCaller,
                CallConstants.IS_NOT_BUSY,
                CallConstants.TYPE_VOICE_CALL,
                CallConstants.IS_DEFAULT,
                null
            )
            // Room Caller
            val callRoomReceiver = CallRoom(
                idReceiver,
                CallConstants.IS_NOT_BUSY,
                CallConstants.TYPE_VOICE_CALL,
                CallConstants.IS_DEFAULT,
                null
            )
            CallRoomManagerFSDB.updateCall(callRoomCaller)
            CallRoomManagerFSDB.updateCall(callRoomReceiver)
        }
        agoraEngine!!.leaveChannel()
        Thread {
            RtcEngine.destroy()
            agoraEngine = null
        }.start()
        finish()
    }
}