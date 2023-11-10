package com.devandroid.appcoordenadas

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.location.LocationRequest.Builder
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import java.text.DecimalFormat


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val APP_PERMISSION_ID = 2

    private var txtLatitude: TextView? = null
    private var txtLongitude: TextView? = null

    private var latitude: Double = 0.00
    private var longitude: Double = 0.00

    private val locationRequest = com.google.android.gms.location.LocationRequest().apply {
        interval = 1000

        LocationRequest.QUALITY_HIGH_ACCURACY
    }
    private var locationManager: LocationManager? = null
    private lateinit var locationCallback: LocationCallback
    private var gpsAtivo: Boolean? = null

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        txtLatitude = findViewById(R.id.txtValorLatitude)
        txtLongitude = findViewById(R.id.txtValorLongitude)

        locationManager =
            application.getSystemService(LOCATION_SERVICE) as LocationManager

        gpsAtivo = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (gpsAtivo as Boolean) {
            getLocationFirstLocation()
        } else {
            latitude = 0.00
            longitude = 0.00

            txtLatitude!!.text = formatGeoPoint(latitude)
            txtLongitude!!.text = formatGeoPoint(longitude)

            Toast.makeText(
                this,
                "Coordenadas não Disponíveis", Toast.LENGTH_LONG
            ).show()
        }
            updateLocation()
    }



    private fun getLocationFirstLocation() {
        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                APP_PERMISSION_ID
            )
        }

//        fusedLocationClient.lastLocation
//            .addOnSuccessListener { location : Location? ->
//                latitude = location!!.latitude
//                longitude = location.longitude
//            }

        fusedLocationClient.getCurrentLocation(LocationRequest.QUALITY_HIGH_ACCURACY,object : CancellationToken(){
            override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token

            override fun isCancellationRequested() = false
        }).addOnSuccessListener {location:Location?->

            if(location == null){
                Toast.makeText(this, "Cant get Location in the moment", Toast.LENGTH_SHORT).show()
            }else{
                latitude = location.latitude
                longitude = location.longitude

                txtLatitude!!.text = formatGeoPoint(latitude)
                txtLongitude!!.text = formatGeoPoint(longitude)

                val city = LatLng(latitude, longitude)
                mMap.addMarker(MarkerOptions().position(city).title("Marker here"))
                mMap.moveCamera(CameraUpdateFactory.newLatLng(city))
                mMap.uiSettings.isZoomControlsEnabled = true
            }

        }
    }

    private fun updateLocation(){
        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                APP_PERMISSION_ID
            )
        }

        locationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                if (ActivityCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                fusedLocationClient.requestLocationUpdates(locationRequest,locationCallback,Looper.getMainLooper())

            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == APP_PERMISSION_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun formatGeoPoint(valor: Double): String? {
        val decimal: DecimalFormat = DecimalFormat("#.####")
        return decimal.format(valor)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val city = LatLng(latitude, longitude)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(city))
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setMinZoomPreference(9.3f)
    }
}

