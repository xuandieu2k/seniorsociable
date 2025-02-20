package vn.xdeuhug.seniorsociable.utility.ui.activity

import android.annotation.SuppressLint
import android.view.View
import com.google.gson.Gson
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallbackProxy
import org.jetbrains.anko.startActivity
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.other.doAfterTextChanged
import vn.xdeuhug.seniorsociable.router.ApiDiseasesRouters
import vn.xdeuhug.seniorsociable.utility.cache.DiseaseCache
import vn.xdeuhug.seniorsociable.utility.databinding.ActivityLookUpDiseasesBinding
import vn.xdeuhug.seniorsociable.utility.https.GetDiseasesListAPI
import vn.xdeuhug.seniorsociable.utility.model.Diseases
import vn.xdeuhug.seniorsociable.utility.ui.adapter.DiseasesAdapter
import vn.xdeuhug.seniorsociable.utility.utils.SearchUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.toArrayList

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 12 / 12 / 2023
 */
class LookUpDiseasesActivity : AppActivity(), DiseasesAdapter.OnClickListener {
    private lateinit var binding: ActivityLookUpDiseasesBinding
    private var listDisease = ArrayList<Diseases>()
    private var listDiseaseRoot = ArrayList<Diseases>()
    private lateinit var diseasesAdapter: DiseasesAdapter
    private var searchUtils = SearchUtils()
    override fun getLayoutView(): View {
        binding = ActivityLookUpDiseasesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        initRecycleView()
        setBack()
        getData()
        listenerDataSearch()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun listenerDataSearch() {
//        binding.svSearch.doAfterTextChanged {
//            if (binding.svSearch.query.isNotEmpty()) {
//                try {
//                    listDisease.clear()
//                    listDisease.addAll(searchUtils.searchDiseases(listDiseaseRoot, binding.svSearch.query.toString().trim()).toArrayList())
//                } catch (e: java.lang.Exception) {
//                    e.printStackTrace()
//                }
//            } else {
//                listDisease.clear()
//                listDisease.addAll(listDiseaseRoot)
//                diseasesAdapter.notifyDataSetChanged()
//            }
//            setViewNoData(listDisease.isEmpty())
//        }
    }

    private fun setBack() {
        binding.ivmBack.clickWithDebounce {
            finish()
        }
    }

    private fun initRecycleView() {
        diseasesAdapter = DiseasesAdapter(getContext())
        diseasesAdapter.onClickListener = this
        diseasesAdapter.setData(listDisease)
        AppUtils.initRecyclerViewVertical(binding.rvDiseases, diseasesAdapter)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getData() {
        listDisease.clear()
        listDiseaseRoot.clear()
        listDisease.addAll(DiseaseCache.getList())
        listDiseaseRoot.addAll(DiseaseCache.getList())
        AppUtils.logJsonFromObject("SIZE: " + listDisease.size)
        AppUtils.logJsonFromObject(listDisease)
        if (listDisease.isEmpty()) {
            showDialog(getString(R.string.is_getting_data))
            EasyHttp.get(this).server(ApiDiseasesRouters.Host).api(GetDiseasesListAPI.params())
                .request(object : HttpCallbackProxy<ArrayList<Diseases>>(this) {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onHttpSuccess(result: ArrayList<Diseases>) {
                        if (result.isNotEmpty()) {
                            DiseaseCache.saveList(result)
                            listDisease.addAll(result)
                        }
                        diseasesAdapter.notifyDataSetChanged()
                        hideDialog()
                        setViewNoData(listDisease.isEmpty())
                    }

                    override fun onHttpFail(e: Exception?) {
                        super.onHttpFail(e)
                        toast(getString(R.string.please_try_later))
                        hideDialog()
                        setViewNoData(listDisease.isEmpty())
                    }
                })
        } else {
            diseasesAdapter.notifyDataSetChanged()
        }
    }

    private fun setViewNoData(empty: Boolean) {
        if (empty) {
            binding.rvDiseases.hide()
            binding.llNotData.show()
        } else {
            binding.rvDiseases.show()
            binding.llNotData.hide()
        }
    }

    override fun initData() {
        //
    }

    override fun onClick(position: Int) {
        try {
            startActivity<DetailDiseaseActivity>(
                AppConstants.OBJECT_DISEASES to Gson().toJson(
                    listDisease[position]
                )
            )
        } catch (ex: Exception) {
            //
        }
    }
}