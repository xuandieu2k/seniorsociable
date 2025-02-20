package vn.xdeuhug.seniorsociable.news.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import androidx.core.widget.NestedScrollView
import com.google.gson.Gson
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallbackProxy
import timber.log.Timber
import vn.xdeuhug.seniorsociable.app.AppFragment
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.model.entity.modelNewsData.Topic
import vn.xdeuhug.seniorsociable.model.entity.modelNewsData.News
import vn.xdeuhug.seniorsociable.model.entity.modelNewsData.ResponseNewsData
import vn.xdeuhug.seniorsociable.constants.NewsConstants
import vn.xdeuhug.seniorsociable.database.NewsManagerFSDB
import vn.xdeuhug.seniorsociable.news.databinding.FragmentNewsBinding
import vn.xdeuhug.seniorsociable.news.http.api.GetListNewsAPI
import vn.xdeuhug.seniorsociable.news.http.api.GetListNewsPagingAPI
import vn.xdeuhug.seniorsociable.news.ui.adapter.NewsAdapter
import vn.xdeuhug.seniorsociable.news.ui.adapter.TopNewsAdapter
import vn.xdeuhug.seniorsociable.router.ApiNewsRouters
import vn.xdeuhug.seniorsociable.service.UpdateNewsService
import vn.xdeuhug.seniorsociable.ui.activity.BrowserActivity
import vn.xdeuhug.seniorsociable.ui.activity.HomeActivity
import vn.xdeuhug.seniorsociable.ui.adapter.TopicAdapter
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 10 / 2023
 */
class NewsFragment : AppFragment<HomeActivity>(), TopicAdapter.OnListenerSelected,
    NewsAdapter.OnClickListener, TopNewsAdapter.OnClickListener {
    private lateinit var binding: FragmentNewsBinding

    /* Data and Adapter for News */
    private lateinit var newsTopAdapter: TopNewsAdapter
    private var listTopNews = ArrayList<News>()

    /* Data and Adapter for News */
    private lateinit var newsAdapter: NewsAdapter
    private var listNews = ArrayList<News>()

    // Set data for Filter topic
    private lateinit var topicAdapter: TopicAdapter
    private var listTopic = ArrayList<Topic>()

    /* PAGING */
    private var loading = false
    private var totalCurrent = 0
    private var currentPage = 1
    private var nextPage = 0L
    private var total = 0 // TOTAL RECORD
    private var lastPage = false

    private var topicNameEn = NewsConstants.NEWS_HEALTH
    override fun getLayoutView(): View {
        binding = FragmentNewsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initData() {
        setDataTopic()
        setDataTopNews()
        setDataNews()
        getTopDataNews()
        postDelayed({
            getDataNews(topicNameEn)
        }, 1000)
        customPaginate()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setDataNews() {
        startShimmer()
        newsAdapter = NewsAdapter(requireContext())
        newsAdapter.setData(listNews)
        newsAdapter.onClickListener = this
        // Create recycleView
        AppUtils.initRecyclerViewVertical(binding.rvNews, newsAdapter)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setDataTopic() {
        val array = arrayListOf(
            Topic("", getString(R.string.health), true, NewsConstants.NEWS_HEALTH),
            Topic("", getString(R.string.sport), false, NewsConstants.NEWS_SPORTS),
            Topic("", getString(R.string.politics), false, NewsConstants.NEWS_POLITICS),
            Topic("", getString(R.string.life), false, NewsConstants.NEWS_LIFE),
            Topic("", getString(R.string.weather), false, NewsConstants.NEWS_WEATHER)
        )
        listTopic.addAll(array)
        topicAdapter = TopicAdapter(requireContext())
        topicAdapter.onListenerSelected = this
        // Create recycleView
        AppUtils.initRecyclerViewHorizontal(binding.rvTopic, topicAdapter)
        topicAdapter.setData(listTopic)
        topicAdapter.notifyDataSetChanged()
    }

    /*
    * Init Data For Post
    * */
    @SuppressLint("NotifyDataSetChanged")
    private fun setDataTopNews() {
        startShimmerTop()
        newsTopAdapter = TopNewsAdapter(requireContext())
        newsTopAdapter.setData(listTopNews)
        newsTopAdapter.onClickListener = this
        // Create recycleView
        AppUtils.initRecyclerViewHorizontal(binding.rvTopNews, newsTopAdapter)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onSelected(position: Int) {
        topicNameEn = listTopic[position].englishName
        getDataNews(listTopic[position].englishName)
        listTopic.forEachIndexed { _, item ->
            item.isSelected = false
        }
        listTopic[position].isSelected = true
        topicAdapter.notifyDataSetChanged()
    }

    private fun getTopDataNews() {
        EasyHttp.get(this).server(ApiNewsRouters.Host)
            .api(GetListNewsAPI.params(NewsConstants.COUNTRY_DEFAULT, NewsConstants.NEWS_TOP))
            .request(object : HttpCallbackProxy<ResponseNewsData>(this) {
                @SuppressLint("NotifyDataSetChanged")
                override fun onHttpSuccess(result: ResponseNewsData) {
                    handleSuccessTop(0)
                    val listData = result.newses
                    listTopNews.clear()
                    listTopNews.addAll(listData)
                    newsTopAdapter.setData(listTopNews)
                    newsTopAdapter.notifyDataSetChanged()
                    startServiceAddListNews(listData)
                }

                override fun onHttpFail(e: Exception?) {
                    super.onHttpFail(e)
                    handleSuccessTop(0)
                    NewsManagerFSDB.getNewsTop(object :
                        NewsManagerFSDB.Companion.FireStoreCallback<ArrayList<News>> {
                        @SuppressLint("NotifyDataSetChanged")
                        override fun onSuccess(result: ArrayList<News>) {
                            listTopNews.clear()
                            listTopNews.addAll(result)
                            newsTopAdapter.setData(listTopNews)
                            newsTopAdapter.notifyDataSetChanged()
                        }

                        override fun onFailure(exception: Exception) {
                            exception.printStackTrace()
                            toast(getString(R.string.please_try_later))
                        }

                    })
                }
            })


    }

    private fun startServiceAddListNews(listData: ArrayList<News>) {
        val intent = Intent(context, UpdateNewsService::class.java)
        intent.putExtra(
            AppConstants.LIST_NEWS, Gson().toJson(listData)
        )
        UpdateNewsService.enqueueWork(requireContext(), intent)
    }

    private fun getDataNews(topic: String) {
        EasyHttp.get(this).server(ApiNewsRouters.Host)
            .api(GetListNewsAPI.params(NewsConstants.COUNTRY_DEFAULT, topic))
            .request(object : HttpCallbackProxy<ResponseNewsData>(this) {
                @SuppressLint("NotifyDataSetChanged")
                override fun onHttpSuccess(result: ResponseNewsData) {
                    handleSuccess(0)
                    val listData = result.newses
                    totalCurrent = 0
                    this@NewsFragment.nextPage = result.nextPage
                    totalCurrent += listData.size
                    listNews.clear()
                    listNews.addAll(listData)
                    newsAdapter.setData(listNews)
                    newsAdapter.notifyDataSetChanged()
                    startServiceAddListNews(listData)
                }

                override fun onHttpFail(e: Exception?) {
                    super.onHttpFail(e)
                    NewsManagerFSDB.getAllNews(object :
                        NewsManagerFSDB.Companion.FireStoreCallback<ArrayList<News>> {
                        @SuppressLint("NotifyDataSetChanged")
                        override fun onSuccess(result: ArrayList<News>) {
                            listNews.clear()
                            listNews.addAll(result)
                            newsAdapter.notifyDataSetChanged()
                        }

                        override fun onFailure(exception: Exception) {
                            exception.printStackTrace()
                            toast(getString(R.string.please_try_later))
                        }

                    })
                    Timber.tag("Tag News").i(e?.message)
                }
            })


    }

    private fun getDataNewsPaging(topic: String) {
        EasyHttp.get(this).server(ApiNewsRouters.Host)
            .api(
                GetListNewsPagingAPI.params(
                    NewsConstants.COUNTRY_DEFAULT,
                    topic,
                    this@NewsFragment.nextPage
                )
            )
            .request(object : HttpCallbackProxy<ResponseNewsData>(this) {
                @SuppressLint("NotifyDataSetChanged")
                override fun onHttpSuccess(result: ResponseNewsData) {
                    val listData = result.newses
                    this@NewsFragment.nextPage = result.nextPage
                    totalCurrent += listData.size
                    lastPage = totalCurrent == total
                    listNews.addAll(listData)
                    newsAdapter.notifyDataSetChanged()
                    loading = false
                    binding.loadMore.hide()
                    startServiceAddListNews(listData)
                }

                override fun onHttpFail(e: Exception?) {
                    super.onHttpFail(e)
                    loading = false
                    binding.loadMore.hide()
                    if (e.toString().contains("429") || e.toString().contains("422")) {
                        lastPage = true
                    }
                    Timber.tag("Tag News").i(e?.message)
                }
            })


    }

    private fun customPaginate() {
        binding.nsvData.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            var recyclerViewBottom = binding.rvNews.bottom
            if (binding.loadMore.visibility == View.VISIBLE) {
                recyclerViewBottom = binding.rvNews.bottom + binding.loadMore.height
            }
            val nestedScrollViewHeight = binding.nsvData.height

            if (binding.rvNews.visibility == View.VISIBLE && binding.sflLoadData.visibility == View.GONE) {
                // Kiểm tra xem cuộn đến cuối cùng của NestedScrollView hay không
                if (scrollY + nestedScrollViewHeight >= recyclerViewBottom) {
                    // Đã cuộn đến cuối cùng, thực hiện các xử lý tải dữ liệu mới ở đây
                    if (!loading && !lastPage) {
                        binding.loadMore.show()
                        binding.nsvData.post {
                            binding.nsvData.smoothScrollTo(
                                0,
                                binding.rvNews.bottom + binding.loadMore.height
                            )
                        }
                        loading = true
                        currentPage += 1
                        getDataNewsPaging(topicNameEn)
                    } else {
                        if (lastPage) {
//                            toast(getString(R.string.end_page))
                        }
                    }
                }
            }
        })
    }


    override fun onClickNews(news: News, position: Int) {
        BrowserActivity.start(
            getAttachActivity()!!, news.link, news.title
        )
    }

    private fun startShimmer() {
        binding.sflLoadData.startShimmer()
        binding.rvNews.hide()
        binding.sflLoadData.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleSuccess(timer: Long) {
        postDelayed({
            binding.rvNews.show()
            // hide and stop simmer
            binding.sflLoadData.stopShimmer()
            binding.sflLoadData.hide()
        }, timer)

    }

    private fun startShimmerTop() {
        binding.sflLoadDataTop.startShimmer()
        binding.rvTopNews.hide()
        binding.sflLoadDataTop.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleSuccessTop(timer: Long) {
        postDelayed({
            binding.rvTopNews.show()
            // hide and stop simmer
            binding.sflLoadDataTop.stopShimmer()
            binding.sflLoadDataTop.hide()
        }, timer)

    }

}