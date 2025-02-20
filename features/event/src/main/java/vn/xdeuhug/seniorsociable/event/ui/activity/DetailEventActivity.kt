package vn.xdeuhug.seniorsociable.event.ui.activity

import android.annotation.SuppressLint
import android.view.View
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.cache.ListUserCache
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.database.UserManagerFSDB
import vn.xdeuhug.seniorsociable.event.constants.EventConstants
import vn.xdeuhug.seniorsociable.event.databinding.ActivityDetailEventBinding
import vn.xdeuhug.seniorsociable.model.entity.modelEvent.Event
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.DateUtils
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils
import java.util.Date

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 07 / 12 / 2023
 */
class DetailEventActivity : AppActivity() {
    private lateinit var binding: ActivityDetailEventBinding
    private var event = Event()
    override fun getLayoutView(): View {
        binding = ActivityDetailEventBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        setDataForView()
    }

    @SuppressLint("SetTextI18n")
    private fun setDataForView() {
        event = Gson().fromJson(
            intent.getStringExtra(EventConstants.OBJECT_EVENT), Event::class.java
        )
        PhotoShowUtils.loadBackgroundImageCenterCrop("", event.poster, binding.imvBg)
        binding.tvTime.text = "${DateUtils.getDateByFormatTimeDate(event.timeStart)} - ${
            DateUtils.getDateByFormatTimeDate(event.timeEnd)
        }"
        val userCurrent = UserCache.getUser()
        PhotoShowUtils.loadAvatarImage(UploadFireStorageUtils.getRootURLAvatarById(userCurrent.id),userCurrent.avatar,binding.imvAvatar2)
        binding.tvContent.text = event.content
        setStatus(event.timeStart)
        binding.tvLocation.text  = AppUtils.fromHtml("${event.address.address}<br/> <small>${event.address.fullAddress}</small>")
        val listUser = ListUserCache.getList()
        val userPost = listUser.firstOrNull { it.id == event.idUserCreate }
        binding.tvUserCreated.text = "${getString(R.string.create_by)} ${userPost?.name}"
        setNoData()
        if(UserCache.getUser().id !in event.membersJoin.map { it.id })
        {
            binding.llCreatePost.hide()
        }else{
            binding.llCreatePost.show()
        }
    }

    private fun setNoData() {
        if(event.posts.isNotEmpty())
        {
            binding.rlNoData.hide()
        }else{
            binding.rlNoData.show()
        }
    }

    private fun setStatus(timeStart: Date) {
        val today = Date()
        if(today.time < timeStart.time)
        {
            binding.tvStatus.text = AppUtils.fromHtml("<font color='#2F9672'>${getString(R.string.coming_soon)}</font>")
            return
        }
        if(today.time > timeStart.time)
        {
            binding.tvStatus.text = AppUtils.fromHtml("<font color='#E96012'>${getString(R.string.finish)}</font>")
            return
        }

        if(today.time == timeStart.time)
        {
            binding.tvStatus.text = AppUtils.fromHtml("<font color='#1462B0'>${getString(R.string.starting)}</font>")
            return
        }

    }

    override fun initData() {
        //
    }
}