package vn.xdeuhug.seniorsociable.personalPage.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.LocationBias
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.hjq.bar.OnTitleBarListener
import org.jetbrains.anko.allCaps
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.dimen
import org.jetbrains.anko.topPadding
import timber.log.Timber
import vn.xdeuhug.base.BaseDialog
import vn.xdeuhug.base.action.AnimAction
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.cache.WorkAtCache
import vn.xdeuhug.seniorsociable.model.entity.modelUser.Address
import vn.xdeuhug.seniorsociable.other.doOnQueryTextListener
import vn.xdeuhug.seniorsociable.personalPage.databinding.DialogWorkAtBinding
import vn.xdeuhug.seniorsociable.personalPage.ui.adapter.LocationAdapter
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 01 / 12 / 2023
 */
class WorkAtDialog {
    @SuppressLint("NotifyDataSetChanged")
    class Builder constructor(
        context: Context
    ) : BaseDialog.Builder<Builder>(context), LocationAdapter.OnListenerCLick {
        private var binding: DialogWorkAtBinding =
            DialogWorkAtBinding.inflate(LayoutInflater.from(context))

        private val KEY_GG = getString(R.string.google_maps_key)

        //
        private lateinit var onActionDone: OnActionDone
        private lateinit var locationAdapter: LocationAdapter
        private var listLocation = ArrayList<AutocompletePrediction>()

        //
        private var placesClient: PlacesClient? = null
        private var sessionToken: AutocompleteSessionToken? = null

        //
        private var listLocationCache = ArrayList<AutocompletePrediction>()
        fun onActionDone(onActionDone: OnActionDone): Builder = apply {
            this.onActionDone = onActionDone
        }

        init {
            setCancelable(false)
            setAnimStyle(AnimAction.ANIM_SCALE)
            setGravity(Gravity.CENTER)
            setContentView(binding.root)
            getDialog()?.window?.setBackgroundDrawableResource(R.drawable.bg_border_dialog)
            setWidth(Resources.getSystem().displayMetrics.widthPixels)
            setHeight(Resources.getSystem().displayMetrics.heightPixels)
            //
            setLayoutRightTitleBar()
            listLocationCache.addAll(WorkAtCache.getList())
            AppUtils.setFontTypeFaceTitleBar(getContext(), binding.tbTitle)
            locationAdapter = LocationAdapter(context)
            locationAdapter.onListenerCLick = this
            // Create recycleView
            AppUtils.initRecyclerViewVertical(binding.rvHobbies, locationAdapter)
            locationAdapter.setData(listLocation)
            initSearch()
            setSearch()
            setListCache()
            showLayoutNotFound()
            binding.tbTitle.setOnTitleBarListener(object : OnTitleBarListener {
                override fun onLeftClick(view: View?) {
                    dismiss()
                }

                override fun onTitleClick(view: View?) {
                    //
                }

                override fun onRightClick(view: View?) {
                    if(binding.tbTitle.rightView.isSelected)
                    {
                        onActionDone.onActionDone(true,addressSelected!!)
                    }
                }

            })
        }

        private fun setListCache() {
            //
        }

        private fun initSearch() {
            if (!Places.isInitialized()) {
                Places.initializeWithNewPlacesApiEnabled(getContext(), KEY_GG)
            }

            placesClient = Places.createClient(getContext())
            sessionToken = AutocompleteSessionToken.newInstance()
        }

        private fun setSearch() {
            binding.svSearch.doOnQueryTextListener(1000) {
                searchPlaces(it.trim())
            }

        }

        private fun detailPlace(placeId: String, callBackPlace: CallBackPlace) {
            val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
            val request = FetchPlaceRequest.newInstance(placeId, placeFields)
            placesClient!!.fetchPlace(request).addOnSuccessListener { fetchPlaceResponse ->
                val place = fetchPlaceResponse.place
                AppUtils.logJsonFromObject(place)
                callBackPlace.onCallback(place)
            }.addOnFailureListener { e ->
                if (e is ApiException) {
                    Timber.tag("Fragment Map").e("Place not found: " + e.message + e.statusCode)
                } else {
                    Timber.tag("Fragment Map").e("Place not found: " + e.message)
                }
                callBackPlace.onCallback(null)
            }
        }

        private fun searchPlaces(keySearch: String) {
            val bias: LocationBias = RectangularBounds.newInstance(
                LatLng(22.458744, 88.208162), LatLng(22.730671, 88.524896)
            )
            val newRequest =
                FindAutocompletePredictionsRequest.builder().setSessionToken(sessionToken)
                    .setTypeFilter(TypeFilter.ESTABLISHMENT).setQuery(keySearch)
                    .setLocationBias(bias).build()
            placesClient!!.findAutocompletePredictions(newRequest)
                .addOnSuccessListener { findAutocompletePredictionsResponse ->
                    val predictions = findAutocompletePredictionsResponse!!.autocompletePredictions
                    listLocation.clear()
                    listLocation.addAll(predictions)
                    locationAdapter.notifyDataSetChanged()
                    showLayoutNotFound()
                }.addOnFailureListener { e ->
                    if (e is ApiException) {
                        Log.e("Log Error", "Place not found: " + e.statusCode + e.message)
                    }
                }
        }

        private fun startShimmer() {
            binding.sflLoadData.startShimmer()
            binding.rvHobbies.hide()
            binding.sflLoadData.show()
        }

        private fun showLayoutNotFound() {
            binding.sflLoadData.stopShimmer()
            binding.sflLoadData.hide()
            if (listLocation.isNotEmpty()) {
                binding.rvHobbies.show()
                binding.rlBackgroundNotFound.hide()
            } else {
                binding.rvHobbies.hide()
                binding.rlBackgroundNotFound.show()
            }
        }

        interface OnActionDone {
            fun onActionDone(isConfirm: Boolean, address: Address)
        }

        interface CallBackPlace {
            fun onCallback(place: Place?)
        }

        @SuppressLint("NotifyDataSetChanged")
        private fun handleSuccess(timer: Long) {
            postDelayed({
                binding.rvHobbies.show()
                // hide and stop simmer
                binding.sflLoadData.stopShimmer()
                binding.sflLoadData.hide()
            }, timer)

        }

        @SuppressLint("UseCompatLoadingForDrawables")
        private fun setLayoutRightTitleBar() {
            binding.tbTitle.rightView.isSelected = false
            val layout = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, // Width
                ViewGroup.LayoutParams.WRAP_CONTENT, // Height
                Gravity.CENTER_VERTICAL or Gravity.END
            )
            binding.tbTitle.rightView.layoutParams = layout
            binding.tbTitle.rightView.topPadding = getContext().dimen(R.dimen.dp_4)
            binding.tbTitle.rightView.bottomPadding = getContext().dimen(R.dimen.dp_4)
            binding.tbTitle.rightView.allCaps = true
            binding.tbTitle.rightView.background =
                getDrawable(R.drawable.button_right_titlebar_selected_dp_8)
        }

        private var addressSelected: Address? = null

        override fun onClick(position: Int) {
            val location = listLocation[position]
            detailPlace(listLocation[position].placeId, object : CallBackPlace {
                override fun onCallback(place: Place?) {
                    binding.tbTitle.rightView.isSelected = true
                    addressSelected = Address()
                    addressSelected = place!!.latLng?.let {
                        Address(
                            location.getPrimaryText(null).toString(),
                            AppUtils.removeVietnameseFromStringNice(
                                location.getPrimaryText(null).toString()
                            ),
                            location.getSecondaryText(null).toString(),
                            AppUtils.removeVietnameseFromStringNice(
                                location.getSecondaryText(null).toString()
                            ),
                            it.longitude,
                            it.latitude
                        )
                    }
                    setViewSelected(true)
                }

            })
        }

        private fun setViewSelected(showView: Boolean) {
            if (showView) {
                binding.llSelected.show()
                binding.locationSelected.tvShortAddress.text = addressSelected?.address
                binding.locationSelected.tvLongAddress.text = addressSelected?.fullAddress
            } else {
                binding.llSelected.hide()
            }
        }
    }
}