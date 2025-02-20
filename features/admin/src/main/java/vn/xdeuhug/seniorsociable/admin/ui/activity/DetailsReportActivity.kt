package vn.xdeuhug.seniorsociable.admin.ui.activity

import android.view.View
import cn.jzvd.JZDataSource
import com.google.gson.Gson
import vn.xdeuhug.seniorsociable.admin.databinding.ActivityDetailsReportBinding
import vn.xdeuhug.seniorsociable.admin.ui.adapter.ReportAdapter
import vn.xdeuhug.seniorsociable.admin.ui.adapter.ReportDetailsAdapter
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.cache.ListUserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.constants.UploadFireStorageConstants
import vn.xdeuhug.seniorsociable.model.entity.modelEvent.Event
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Post
import vn.xdeuhug.seniorsociable.model.entity.modelReport.Report
import vn.xdeuhug.seniorsociable.ui.adapter.MediaAdapter
import vn.xdeuhug.seniorsociable.ui.dialog.PreviewMediaDialog
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 03 / 01 / 2024
 */
class DetailsReportActivity : AppActivity(), MediaAdapter.OnListenerCLick {
    private lateinit var binding: ActivityDetailsReportBinding
    private var post: Post? = null
    private var listReport = ArrayList<Report>()
    private lateinit var reportAdapter: ReportDetailsAdapter
    override fun getLayoutView(): View {
        binding = ActivityDetailsReportBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        setDataForView()
    }

    private fun setDataForView() {
        post = Gson().fromJson(
            intent.getStringExtra(AppConstants.OBJECT_POST), Post::class.java
        )
        setViewPost()
        setAdapter()
        setViewNoData()
    }

    private fun setViewNoData() {
        if(listReport.isNotEmpty())
        {
            binding.llNotFound.hide()
            binding.rvReport.show()
        }else{
            binding.llNotFound.show()
            binding.rvReport.hide()
        }
    }

    private fun setAdapter() {
        reportAdapter = ReportDetailsAdapter(getContext())
        listReport.clear()
        listReport.addAll(post!!.reports)
        reportAdapter.setData(listReport)
        AppUtils.initRecyclerViewVertical(binding.rvReport,reportAdapter)
    }

    private fun setViewPost() {
        if(post != null)
        {
            val userPost = ListUserCache.getList().firstOrNull { it.id == post!!.idUserPost }
            val mediaAdapter = MediaAdapter(getContext(), userPost!!, post!!.typePost, post!!.id)
            mediaAdapter.onListenerCLick = this
            userPost.let {
                PhotoShowUtils.loadAvatarImage(
                    UploadFireStorageUtils.getRootURLAvatarById(it.id), it.avatar, binding.imvAuthor
                )
                binding.tvAuthor.text = AppUtils.fromHtml("<b>${it.name}</b>")
            }
            binding.tvContent.text = post!!.content
            AppUtils.setMedia(binding.rvMedia, post!!.multiMedia, mediaAdapter)
            if (post!!.multiMedia.isNotEmpty()) {
                binding.rvMedia.show()
            } else {
                binding.rvMedia.hide()
            }
        }
    }

    override fun initData() {
        //
    }

    override fun onClick(
        jzDataSource: JZDataSource?,
        multiMedia: MultiMedia,
        lisMultiMedia: ArrayList<MultiMedia>,
        idPost: String
    ) {
        if (jzDataSource == null) {
            PreviewMediaDialog.Builder(
                getContext(),
                multiMedia,
                lisMultiMedia,
                UploadFireStorageConstants.TYPE_POST,
                UploadFireStorageUtils.getRootURLPostById(idPost),
                null
            ).onActionDone(object : PreviewMediaDialog.Builder.OnActionDone {
                override fun onActionDone(isConfirm: Boolean) {
                    //
                }
            }).create().show()
        } else {
            val dialog = PreviewMediaDialog.Builder(
                getContext(),
                multiMedia,
                lisMultiMedia,
                UploadFireStorageConstants.TYPE_POST,
                UploadFireStorageUtils.getRootURLPostById(idPost),
                jzDataSource
            ).onActionDone(object : PreviewMediaDialog.Builder.OnActionDone {
                override fun onActionDone(isConfirm: Boolean) {
                    //
                }
            })
            dialog.create().show()
        }
    }
}