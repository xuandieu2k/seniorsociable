package vn.xdeuhug.seniorsociable.watch.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import cn.jzvd.JZUtils.getWindow
import cn.jzvd.Jzvd
import cn.jzvd.JzvdStdTikTok
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallbackProxy
import com.paginate.Paginate
import vn.xdeuhug.seniorsociable.app.AppApplication
import vn.xdeuhug.seniorsociable.app.AppFragment
import vn.xdeuhug.seniorsociable.model.entity.modelMovieShort.MovieShort
import vn.xdeuhug.seniorsociable.model.entity.modelNewsData.Topic
import vn.xdeuhug.seniorsociable.model.entity.modelPexels.ResponsePexel
import vn.xdeuhug.seniorsociable.model.entity.modelPexels.Video
import vn.xdeuhug.seniorsociable.other.CustomLoadingListItemCreator
import vn.xdeuhug.seniorsociable.router.ApiVideoRouters
import vn.xdeuhug.seniorsociable.ui.activity.HomeActivity
import vn.xdeuhug.seniorsociable.ui.adapter.TopicAdapter
import vn.xdeuhug.seniorsociable.ui.fragment.CommentFragment
import vn.xdeuhug.seniorsociable.ui.fragment.CommentVideoFragment
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.watch.R
import vn.xdeuhug.seniorsociable.watch.constants.VideoConstants
import vn.xdeuhug.seniorsociable.watch.databinding.FragmentVideoBinding
import vn.xdeuhug.seniorsociable.watch.http.GetVideoWithKeyword
import vn.xdeuhug.seniorsociable.watch.model.interfaces.OnViewPagerListener
import vn.xdeuhug.seniorsociable.watch.ui.adapter.VideoAdapter
import vn.xdeuhug.seniorsociable.watch.ui.wiget.ViewPagerLayoutManager

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 06 / 11 / 2023
 */
class VideoFragment : AppFragment<HomeActivity>(), TopicAdapter.OnListenerSelected,
    VideoAdapter.OnActionVideo {
    private lateinit var binding: FragmentVideoBinding

    /* Data and Adapter for Story */
    private lateinit var watchAdapter: VideoAdapter
    private var listVideo = ArrayList<Video>()

    // set page
    private var currentPage = 1
    private var limit = 20
    private var total = 0 // TOTAL RECORD
    private var loading = false
    private var lastPage = 0

    // Set data for Filter topic
    private lateinit var topicAdapter: TopicAdapter
    private var listTopic = ArrayList<Topic>()

    //
    private var mCurrentPosition = -1

    //
    private var textSearch = ""
    override fun getLayoutView(): View {
        binding = FragmentVideoBinding.inflate(layoutInflater)
        return binding.root
    }

    @Suppress("DEPRECATION")
    override fun initData() {
        setDataFilter()
        setDataVideo()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setDataFilter() {
        val array = arrayListOf(
            Topic(
                "",
                getString(vn.xdeuhug.seniorsociable.R.string.vietnam),
                true,
                VideoConstants.CATEGORY_VIETNAM
            ),
            Topic(
                "",
                getString(vn.xdeuhug.seniorsociable.R.string.popular),
                false,
                VideoConstants.CATEGORY_POPULAR
            ), Topic(
                "",
                getString(vn.xdeuhug.seniorsociable.R.string.life),
                false,
                VideoConstants.CATEGORY_LIFE
            ), Topic(
                "",
                getString(vn.xdeuhug.seniorsociable.R.string.nature),
                false,
                VideoConstants.CATEGORY_NATURE
            ), Topic(
                "",
                getString(vn.xdeuhug.seniorsociable.R.string.health),
                false,
                VideoConstants.CATEGORY_HEALTH
            ), Topic(
                "",
                getString(vn.xdeuhug.seniorsociable.R.string.sport),
                false,
                VideoConstants.CATEGORY_SPORT
            ), Topic(
                "",
                getString(vn.xdeuhug.seniorsociable.R.string.travel),
                false,
                VideoConstants.CATEGORY_TRAVEL
            ), Topic(
                "",
                getString(vn.xdeuhug.seniorsociable.R.string.country),
                false,
                VideoConstants.CATEGORY_COUNTRY
            ), Topic(
                "",
                getString(vn.xdeuhug.seniorsociable.R.string.relax),
                false,
                VideoConstants.CATEGORY_RELAX
            ), Topic(
                "",
                getString(vn.xdeuhug.seniorsociable.R.string.music),
                false,
                VideoConstants.CATEGORY_MUSIC
            )
        )
        listTopic.addAll(array)
        topicAdapter = TopicAdapter(requireContext(), true)
        topicAdapter.onListenerSelected = this
        // Create recycleView
        AppUtils.initRecyclerViewHorizontal(binding.rvTopic, topicAdapter)
        topicAdapter.setData(listTopic)
        topicAdapter.notifyDataSetChanged()
    }


    /*
    *Init Data For Video
    * */
    private fun setDataVideo() {
        setRecycleview()
        textSearch = listTopic[0].englishName
        getDataVideo()
        paginate()
    }

    @Suppress("DEPRECATION")
    fun setRecycleview() {
        getWindow(requireContext()).setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        watchAdapter =
            VideoAdapter(requireContext(), requireContext().applicationContext as AppApplication)
        watchAdapter.onActionVideo = this
        val mViewPagerLayoutManager =
            ViewPagerLayoutManager(requireContext(), OrientationHelper.VERTICAL)
        binding.rvVideo.layoutManager = mViewPagerLayoutManager
        binding.rvVideo.adapter = watchAdapter

        mViewPagerLayoutManager.setOnViewPagerListener(object : OnViewPagerListener {
            override fun onInitComplete() {
                //automatically Play The First Item
                autoPlayVideo(0)
            }

            override fun onPageRelease(isNext: Boolean, position: Int) {
                if (mCurrentPosition == position) {
                    Jzvd.releaseAllVideos()
                }
            }

            override fun onPageSelected(position: Int, isBottom: Boolean) {
                if (mCurrentPosition == position) {
                    return
                }
                autoPlayVideo(position)
                mCurrentPosition = position
            }
        })

        binding.rvVideo.addOnChildAttachStateChangeListener(object :
            RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                //
            }

            override fun onChildViewDetachedFromWindow(view: View) {
                val jzvd = view.findViewById<Jzvd>(R.id.plvVideo)
                if (jzvd != null && Jzvd.CURRENT_JZVD != null && jzvd.jzDataSource != null && jzvd.jzDataSource.containsTheUrl(
                        Jzvd.CURRENT_JZVD.jzDataSource.currentUrl
                    )
                ) {
                    if (Jzvd.CURRENT_JZVD != null && Jzvd.CURRENT_JZVD.screen != Jzvd.SCREEN_FULLSCREEN) {
                        Jzvd.releaseAllVideos()
                    }
                }
            }
        })
    }


    private fun autoPlayVideo(postion: Int) {
        if (binding.rvVideo.getChildAt(0) == null) {
            return
        }
        val player: JzvdStdTikTok =
            binding.rvVideo.getChildAt(0).rootView.findViewById(R.id.plvVideo)
        player.startVideoAfterPreloading()
    }

    override fun onPause() {
        super.onPause()
        Jzvd.releaseAllVideos()
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getDataVideo() {
        EasyHttp.get(this).server(ApiVideoRouters.Host)
            .api(GetVideoWithKeyword.params(currentPage, limit, textSearch))
            .request(object : HttpCallbackProxy<ResponsePexel>(this) {
                @SuppressLint("NotifyDataSetChanged")
                override fun onHttpSuccess(result: ResponsePexel) {
                    if (currentPage == 1) {
                        total = result.totalResults
                        lastPage = AppUtils.calculateTotalPage(total, limit)
                        listVideo.clear()
//                        handleSuccess(timer)
                    }
                    val listData = result.videos
                    listVideo.addAll(listData)
                    watchAdapter.setData(listVideo)
                    watchAdapter.notifyDataSetChanged()
                }
            })
    }


    private fun paginate() {
        val callback: Paginate.Callbacks = object : Paginate.Callbacks {
            override fun onLoadMore() {
                loading = true
                postDelayed({
                    if (currentPage < lastPage) {
                        currentPage += 1
                        getDataVideo()
                        loading = false
                    }
                }, 1000)
            }

            override fun isLoading(): Boolean {
                return loading
            }

            override fun hasLoadedAllItems(): Boolean {
                if (total < limit) return true
                return currentPage == lastPage
            }

        }

        Paginate.with(binding.rvVideo, callback).setLoadingTriggerThreshold(0)
            .addLoadingListItem(true).setLoadingListItemCreator(CustomLoadingListItemCreator())
            .build()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onSelected(position: Int) {
        listTopic.forEachIndexed { _, item ->
            item.isSelected = false
        }
        listTopic[position].isSelected = true
        textSearch = listTopic[position].englishName
        topicAdapter.notifyDataSetChanged()
        currentPage = 1
        getDataVideo()
    }

    override fun onClickComment(position: Int, video: Video) {
        val movieShort = MovieShort()
        movieShort.id = video.id
        val bottomSheetDialog = CommentVideoFragment(movieShort, position)
        bottomSheetDialog.show(parentFragmentManager, "")
    }

    override fun onClickReact(position: Int, video: Video) {
        //
    }

    override fun onClickShare(position: Int, video: Video) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.putExtra(Intent.EXTRA_TEXT, video.url)
        intent.type = "text/plain"
        startActivity(Intent.createChooser(intent, getString(vn.xdeuhug.seniorsociable.R.string.share_to)))
    }
}