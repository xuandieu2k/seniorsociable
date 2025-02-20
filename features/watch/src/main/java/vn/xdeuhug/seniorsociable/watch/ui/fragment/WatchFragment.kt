package vn.xdeuhug.seniorsociable.watch.ui.fragment

import android.annotation.SuppressLint
import android.view.View
import cn.jzvd.Jzvd
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallbackProxy
import vn.xdeuhug.seniorsociable.app.AppFragment
import vn.xdeuhug.seniorsociable.model.entity.modelPexels.ResponsePexel
import vn.xdeuhug.seniorsociable.model.entity.modelPexels.Video
import vn.xdeuhug.seniorsociable.router.ApiVideoRouters
import vn.xdeuhug.seniorsociable.ui.activity.HomeActivity
import vn.xdeuhug.seniorsociable.watch.databinding.FragmentWatchBinding
import vn.xdeuhug.seniorsociable.watch.ui.adapter.WatchAdapter
import vn.xdeuhug.seniorsociable.watch.http.GetVideoAPI


/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 10 / 2023
 */
class WatchFragment : AppFragment<HomeActivity>() {
    private lateinit var binding: FragmentWatchBinding

    /* Data and Adapter for Story */
    private lateinit var watchAdapter: WatchAdapter
    private var listVideo = ArrayList<Video>()
    // set page
    private var currentPage = 1
    private var limit = 20

    override fun getLayoutView(): View {
        binding = FragmentWatchBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initData() {
        //
        setUpSelectedTextView()
        setDataVideo()
    }

    /*
    *Init Data For Video
    * */
    private fun setDataVideo() {
        watchAdapter = WatchAdapter(requireContext())
        // Create recycleView
//        AppUtils.initRecyclerViewVertical(binding.rvVideo, watchAdapter)
        binding.vpVideo.adapter = watchAdapter
        getDataVideo()
    }

    private fun getDataVideo() {
        currentPage = 3
        EasyHttp.get(this).server(ApiVideoRouters.Host).api(GetVideoAPI.params(currentPage,limit))
            .request(object : HttpCallbackProxy<ResponsePexel>(this) {
                @SuppressLint("NotifyDataSetChanged")
                override fun onHttpSuccess(result: ResponsePexel) {
                    val listData = result.videos
                    listVideo.clear()
                    listVideo.addAll(listData)
                    watchAdapter.setData(listVideo)
                    watchAdapter.notifyDataSetChanged()
                }
            })
    }

    private fun setUpSelectedTextView() {
//        binding.tvForYou.isSelected = true
//        binding.tvForYou.clickWithDebounce {
//            binding.tvForYou.isSelected = true
//            binding.tvGame.isSelected = false
//            binding.tvLive.isSelected = false
//            binding.tvReels.isSelected = false
//
//        }
//        binding.tvGame.clickWithDebounce {
//            binding.tvForYou.isSelected = false
//            binding.tvGame.isSelected = true
//            binding.tvLive.isSelected = false
//            binding.tvReels.isSelected = false
//
//        }
//        binding.tvLive.clickWithDebounce {
//            binding.tvForYou.isSelected = false
//            binding.tvGame.isSelected = false
//            binding.tvLive.isSelected = true
//            binding.tvReels.isSelected = false
//
//        }
//        binding.tvReels.clickWithDebounce {
//            binding.tvForYou.isSelected = false
//            binding.tvGame.isSelected = false
//            binding.tvLive.isSelected = false
//            binding.tvReels.isSelected = true
//
//        }
    }

    override fun onPause() {
        super.onPause()
        Jzvd.releaseAllVideos()
    }
}