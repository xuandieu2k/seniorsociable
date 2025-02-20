package vn.xdeuhug.seniorsociable.post.ui.activity


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
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
import org.jetbrains.anko.toast
import timber.log.Timber
import vn.xdeuhug.seniorsociable.other.doOnQueryTextListener
import vn.xdeuhug.seniorsociable.post.ui.adapter.PlacesAdapter
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.post.databinding.FragmentMapBinding
import vn.xdeuhug.seniorsociable.utils.AppUtils
/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 27 / 11 / 2023
 */
class MapActivity : AppCompatActivity(), PlacesAdapter.OnListenerCLick {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var binding: FragmentMapBinding
    private lateinit var map: GoogleMap
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private var placesClient: PlacesClient? = null
    private var sessionToken: AutocompleteSessionToken? = null
    private lateinit var adapter: PlacesAdapter
    private var listPlaces = ArrayList<AutocompletePrediction>()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mvMap.onCreate(null)
        binding.mvMap.onResume()

        binding.mvMap.getMapAsync { map ->
            this.map = map
            checkLocationPermission()
        }

        val apiKey: String = getString(R.string.google_maps_key)
        if (!Places.isInitialized()) {
            Places.initialize(this, apiKey)
        }

        placesClient = Places.createClient(this)
        sessionToken = AutocompleteSessionToken.newInstance()
        adapter = PlacesAdapter(this)
        adapter.onListenerCLick = this
        adapter.setData(listPlaces)
        AppUtils.initRecyclerViewVertical(binding.rvData, adapter)
        binding.svSearch.doOnQueryTextListener(3000) {
            if (it.isNotEmpty()) {
                searchPlaces()
            } else {
                listPlaces.clear()
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getLastKnownLocation()
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLastKnownLocation()
                    toast("Vô")
                } else {
                    // Handle when the user denies location permission
                    // For example, show a notification or exit the app
                    finish()
                    toast("Thoát")
                }
            }
        }
    }

    private fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
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
            toast("Tu choi")
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                location?.let {
                    updateMapWithCurrentLocation(it.latitude, it.longitude)
                }
            }
    }

    private fun updateMapWithCurrentLocation(latitude: Double, longitude: Double) {
        val currentLocation = LatLng(latitude, longitude)
        map.clear()
        map.addMarker(MarkerOptions().position(currentLocation).title("Current Location"))
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
    }

    private fun searchPlaces() {
        val bias: LocationBias = RectangularBounds.newInstance(
            LatLng(22.458744, 88.208162),
            LatLng(22.730671, 88.524896)
        )
        val newRequest = FindAutocompletePredictionsRequest
            .builder()
            .setSessionToken(sessionToken)
            .setTypeFilter(TypeFilter.ESTABLISHMENT)
            .setQuery(binding.svSearch.query.toString())
            .setLocationBias(bias)
            .build()
        placesClient!!.findAutocompletePredictions(newRequest)
            .addOnSuccessListener { findAutocompletePredictionsResponse ->
                val predictions = findAutocompletePredictionsResponse!!.autocompletePredictions
                listPlaces.clear()
                listPlaces.addAll(predictions)
                adapter.notifyDataSetChanged()
            }.addOnFailureListener { e ->
                if (e is ApiException) {
                    Timber.tag("MainActivity").e("Place not found: " + e.statusCode + e.message)
                }
            }
    }

    override fun onClick(position: Int) {
        detailPlace(listPlaces[position].placeId)
    }

    private fun detailPlace(placeId: String) {
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)
        placesClient!!.fetchPlace(request)
            .addOnSuccessListener { fetchPlaceResponse ->
                val place = fetchPlaceResponse.place
                val latLng = place.latLng
                if (latLng != null) {
                    map.addMarker(MarkerOptions().position(latLng).title(place.name))
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10f)
                    this.map.animateCamera(cameraUpdate)
                }
            }.addOnFailureListener { e ->
                if (e is ApiException) {
                    Timber.tag("MapActivity").e("Place not found: " + e.message + e.statusCode)
                }
            }
    }
}