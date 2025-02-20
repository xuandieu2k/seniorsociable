package vn.xdeuhug.seniorsociable.event.ui.fragment

import android.annotation.SuppressLint
import android.view.View
import com.google.gson.Gson
import org.jetbrains.anko.support.v4.startActivity
import vn.xdeuhug.seniorsociable.app.AppFragment
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.database.EventManagerFSDB
import vn.xdeuhug.seniorsociable.event.constants.EventConstants
import vn.xdeuhug.seniorsociable.event.databinding.FragmentEventBinding
import vn.xdeuhug.seniorsociable.event.ui.activity.CreateEventActivity
import vn.xdeuhug.seniorsociable.event.ui.activity.DetailEventActivity
import vn.xdeuhug.seniorsociable.event.ui.adapter.EventAdapter
import vn.xdeuhug.seniorsociable.model.entity.modelEvent.Event
import vn.xdeuhug.seniorsociable.model.entity.modelNewsData.Topic
import vn.xdeuhug.seniorsociable.ui.activity.HomeActivity
import vn.xdeuhug.seniorsociable.ui.adapter.TopicAdapter
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 28 / 11 / 2023
 */
class EventFragment : AppFragment<HomeActivity>(), TopicAdapter.OnListenerSelected,
    EventAdapter.OnClickListener {

    private var startDate = ""
    private var endDate = ""
    private lateinit var binding: FragmentEventBinding

    /* Data and Adapter for News */
    private lateinit var eventAdapter: EventAdapter
    private var listEvent = ArrayList<Event>()

    // Set data for Filter topic
    private lateinit var topicAdapter: TopicAdapter
    private var listTopic = ArrayList<Topic>()
    override fun getLayoutView(): View {
        binding = FragmentEventBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initData() {
        setDataTopic()
        setDataEvent()
        setClickEvent()
    }

    private fun setClickEvent() {
        binding.imvAdd.clickWithDebounce {
            try {
                startActivity<CreateEventActivity>()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        binding.imvFilter.clickWithDebounce {
            try {
                toast("Show Dialog")
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setDataEvent() {
        startShimmer()
        eventAdapter = EventAdapter(requireContext())
        eventAdapter.onClickListener = this
        // Create recycleView
        AppUtils.initRecyclerView(binding.rvEvent, eventAdapter)
        eventAdapter.setData(listEvent)
        eventAdapter.notifyDataSetChanged()
        getDataEvent()
    }

    private fun getDataEvent() {
        EventManagerFSDB.getAllEvent(object :
            EventManagerFSDB.Companion.FireStoreCallback<ArrayList<Event>> {
            override fun onSuccess(result: ArrayList<Event>) {
                handleSuccess(0)
                listEvent.clear()
                listEvent.addAll(result)
                eventAdapter.notifyDataSetChanged()
            }

            override fun onFailure(exception: Exception) {
                toast(getString(R.string.event))
                handleSuccess(0)
            }

        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setDataTopic() {
        val array = arrayListOf(
            Topic("", getString(R.string.all), true),
            Topic("", getString(R.string.football), false),
            Topic("", getString(R.string.entertainment), false),
            Topic("", getString(R.string.gossip), false),
            Topic("", getString(R.string.caffe), false),
            Topic("", getString(R.string.health), false)
        )
        listTopic.addAll(array)
        topicAdapter = TopicAdapter(requireContext())
        topicAdapter.onListenerSelected = this
        // Create recycleView
        AppUtils.initRecyclerViewHorizontal(binding.rvTopic, topicAdapter)
        topicAdapter.setData(listTopic)
        topicAdapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onSelected(position: Int) {
        listTopic.forEachIndexed { _, item ->
            item.isSelected = false
        }
        listTopic[position].isSelected = true
        topicAdapter.notifyDataSetChanged()
    }

    override fun onClickEvent(event: Event, position: Int) {
        try {
            startActivity<DetailEventActivity>(EventConstants.OBJECT_EVENT to Gson().toJson(event))
        } catch (ex: Exception) {
            //
        }
    }

    private fun startShimmer() {
        binding.sflLoadData.startShimmer()
        binding.rvEvent.hide()
        binding.sflLoadData.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleSuccess(timer: Long) {
        postDelayed({
            binding.rvEvent.show()
            // hide and stop simmer
            binding.sflLoadData.stopShimmer()
            binding.sflLoadData.hide()
        }, timer)

    }

}