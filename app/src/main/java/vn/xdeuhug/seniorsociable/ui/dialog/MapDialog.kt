package vn.xdeuhug.seniorsociable.ui.dialog

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.hjq.bar.OnTitleBarListener
import vn.xdeuhug.base.BaseDialog
import vn.xdeuhug.base.action.AnimAction
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.databinding.DialogMapBinding
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 13 / 10 / 2023
 */
class MapDialog {
    @SuppressLint("NotifyDataSetChanged")
    class Builder constructor(
        context: Context
    ) : BaseDialog.Builder<Builder>(context) {
        private var binding: DialogMapBinding =
            DialogMapBinding.inflate(LayoutInflater.from(context))
        //
        private lateinit var onActionDone: OnActionDone
        private lateinit var map: GoogleMap

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
            MapsInitializer.initialize(context)
            binding.mvMap.onCreate(null) // Call onCreate here
            binding.mvMap.onResume()

            // Gets to GoogleMap from the MapView and does initialization stuff

            // Gets to GoogleMap from the MapView and does initialization stuff

            binding.mvMap.getMapAsync { map ->
                // Thực hiện các thao tác trên bản đồ tại đây
                this.map = map
                val location = LatLng(10.858886392089886, 106.66508575936356)
                this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                this.map.addMarker(MarkerOptions().position(location).title("My Location"))

                this.map.uiSettings.isMyLocationButtonEnabled = false
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return@getMapAsync
                }
                this.map.isMyLocationEnabled = true


                // Needs to call MapsInitializer before doing any CameraUpdateFactory calls

                // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
                try {
                    MapsInitializer.initialize(context)
                } catch (e: GooglePlayServicesNotAvailableException) {
                    e.printStackTrace()
                }

                // Updates the location and zoom of the MapView

                // Updates the location and zoom of the MapView
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(43.1, -87.9), 10f)
                this.map.animateCamera(cameraUpdate)
            }

            binding.tbUserTag.setOnTitleBarListener(object : OnTitleBarListener {
                override fun onLeftClick(view: View?) {
                    dismiss()
                }

                override fun onTitleClick(view: View?) {
                    //
                }

                override fun onRightClick(view: View?) {
                    //
                }

            })
        }

        override fun dismiss() {
            super.dismiss()
            binding.mvMap.onPause()
            binding.mvMap.onDestroy()
        }

        interface OnActionDone {
            fun onActionDone(isConfirm: Boolean, listData: ArrayList<User>)
        }
    }
}