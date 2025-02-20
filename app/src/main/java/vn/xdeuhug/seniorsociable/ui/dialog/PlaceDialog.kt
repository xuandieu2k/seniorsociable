package vn.xdeuhug.seniorsociable.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.hjq.bar.OnTitleBarListener
import com.hjq.toast.ToastUtils
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.Place
import com.tomtom.sdk.search.Search
import com.tomtom.sdk.search.SearchCallback
import com.tomtom.sdk.search.SearchOptions
import com.tomtom.sdk.search.SearchResponse
import com.tomtom.sdk.search.common.error.SearchFailure
import com.tomtom.sdk.search.online.OnlineSearch
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.dimen
import org.jetbrains.anko.topPadding
import vn.xdeuhug.base.BaseDialog
import vn.xdeuhug.base.action.AnimAction
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.constants.EventConstant
import vn.xdeuhug.seniorsociable.databinding.DialogPlaceBinding
import vn.xdeuhug.seniorsociable.model.entity.modelUser.Address
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.other.doAfterTextChanged
import vn.xdeuhug.seniorsociable.ui.adapter.PlaceAdapter
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 12 / 2023
 */
class PlaceDialog {
    @SuppressLint("NotifyDataSetChanged")
    class Builder constructor(
        context: Context, var title: String, var addressSelected: Address?
    ) : BaseDialog.Builder<Builder>(context), PlaceAdapter.OnListenerSelected {
        private var binding: DialogPlaceBinding =
            DialogPlaceBinding.inflate(LayoutInflater.from(context))
        private lateinit var placeAdapter: PlaceAdapter
        private var listPlace = ArrayList<Place>()

        //
        private lateinit var onActionDone: OnActionDone

        fun onActionDone(onActionDone: OnActionDone): Builder = apply {
            this.onActionDone = onActionDone
        }

        init {
            setCancelable(true)
            setAnimStyle(AnimAction.ANIM_DEFAULT)
            setGravity(Gravity.CENTER)
            setContentView(binding.root)
            getDialog()?.window?.setBackgroundDrawableResource(R.drawable.bg_border_dialog)
            setWidth(Resources.getSystem().displayMetrics.widthPixels)
            setHeight(Resources.getSystem().displayMetrics.heightPixels)
            //
            setLayoutRightTitleBar()
            AppUtils.setFontTypeFaceTitleBar(getContext(), binding.tbUserTag)
            setDataAddress()
            showLayoutNotFound()
            initAdapter()
            setQuery()

            binding.tbUserTag.setOnTitleBarListener(object : OnTitleBarListener {
                override fun onLeftClick(view: View?) {
                    dismiss()
                }

                override fun onTitleClick(view: View?) {
                    //
                }

                override fun onRightClick(view: View?) {
                    if (binding.tbUserTag.rightView.isSelected) {
                        onActionDone.onActionDone(true, addressSelected)
                        dismiss()
                    }
                }

            })

        }

        private fun setDataAddress() {
            if(addressSelected != null){
                if(addressSelected!!.longitude != 0.00 && addressSelected!!.latitude != 0.00 && (addressSelected!!.address != "" || addressSelected!!.fullAddress != "") )
                {
                    setViewSelected(true)
                }
            }else{
                setViewSelected(false)
            }
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        private fun setLayoutRightTitleBar() {
            if(title.isNotEmpty())
            {
                binding.tbUserTag.title = title
            }
            binding.tbUserTag.rightView.isSelected = false
            val layout = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, // Width
                ViewGroup.LayoutParams.WRAP_CONTENT, // Height
                Gravity.CENTER_VERTICAL or Gravity.END
            )
            binding.tbUserTag.rightView.layoutParams = layout
            binding.tbUserTag.rightView.topPadding = getContext().dimen(R.dimen.dp_4)
            binding.tbUserTag.rightView.bottomPadding = getContext().dimen(R.dimen.dp_4)
            binding.tbUserTag.rightView.allCaps = true
            binding.tbUserTag.rightView.background =
                getDrawable(R.drawable.button_right_titlebar_selected_dp_8)
        }

        private fun initAdapter() {
            placeAdapter = PlaceAdapter(getContext())
            placeAdapter.onListenerSelected = this
            placeAdapter.setData(listPlace)
            AppUtils.initRecyclerViewVertical(binding.rvPlace, placeAdapter)
        }

        private fun setQuery() {
            val searchApi = OnlineSearch.create(getContext(), getString(R.string.tom_tom_key)!!)
            binding.svSearch.doAfterTextChanged(1000) {
                if (it.isNotEmpty()) {
                    getPlace(it, searchApi)
                } else {
                    listPlace.clear()
                    placeAdapter.notifyDataSetChanged()
                    showLayoutNotFound()
                }
            }
        }

        private fun getPlace(keySearch: String, searchApi: Search) {

            val setCountryCodes = setOf(EventConstant.VN)
            val options = SearchOptions(
                query = keySearch, countryCodes = setCountryCodes,

                )

            searchApi.search(options, object : SearchCallback {
                override fun onSuccess(result: SearchResponse) {/* handle success */
                    AppUtils.logJsonFromObject(result.results)
                    val list = result.results
                    val listPlaces = list.map { it.place }
                    listPlace.clear()
                    listPlace.addAll(listPlaces)
                    placeAdapter.notifyDataSetChanged()
                    showLayoutNotFound()
                }

                override fun onFailure(failure: SearchFailure) {/* handle failure */
                    AppUtils.logJsonFromObject(failure)
                }
            })
        }

        private fun showLayoutNotFound() {
            if (listPlace.isNotEmpty()) {
                binding.rvPlace.show()
                binding.rlBackgroundNotFound.hide()
            } else {
                binding.rvPlace.hide()
                binding.rlBackgroundNotFound.show()
            }
        }


        interface OnActionDone {
            fun onActionDone(isConfirm: Boolean, address: Address?)
        }

        override fun onSelected(position: Int) {
            binding.tbUserTag.rightView.isSelected = true
            val place = listPlace[position]
            val shortAddress = place.address!!.freeformAddress
            val fullAddress =
                "${place.address!!.streetNumber} ${place.address!!.streetName} ${place.address!!.municipalitySubdivision} ${place.address!!.municipality} "
            addressSelected = Address(
                shortAddress, AppUtils.removeVietnameseFromStringNice(
                    shortAddress
                ), fullAddress.trimStart(), AppUtils.removeVietnameseFromStringNice(
                    fullAddress.trimStart()
                ), place.coordinate.longitude, place.coordinate.latitude
            )
            setViewSelected(true)
        }

        private fun setViewSelected(showView: Boolean) {
            if (showView) {
                binding.llSelected.show()
                binding.locationSelected.tvShortAddress.text = addressSelected?.address
                binding.locationSelected.tvLongAddress.text = addressSelected?.fullAddress
            } else {
                binding.llSelected.hide()
                binding.locationSelected.tvShortAddress.text = ""
                binding.locationSelected.tvLongAddress.text = ""
            }
        }
    }
}