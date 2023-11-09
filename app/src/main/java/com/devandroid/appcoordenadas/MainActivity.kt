package com.devandroid.appcoordenadas

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.text.DecimalFormat
import java.util.Locale
import kotlin.concurrent.fixedRateTimer


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    var requestPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val APP_PERMISSION_ID = 2

    private var txtLatitude: TextView? = null
    private var txtLongitude: TextView? = null

    private var latitude: Double = 0.00
    private var longitude: Double = 0.00

    private var locationManager: LocationManager? = null
    private var gpsAtivo: Boolean? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


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
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
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

        val firstLocation = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        latitude = firstLocation!!.latitude
        longitude = firstLocation.longitude

        txtLatitude!!.text = formatGeoPoint(latitude)
        txtLongitude!!.text = formatGeoPoint(longitude)

    }

    private fun updateLocation(){
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
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

        locationManager!!.requestLocationUpdates(
            LocationManager.GPS_PROVIDER, 60000, 0F
        ) { location ->
            latitude = location.latitude
            longitude = location.longitude

            txtLatitude!!.text = formatGeoPoint(latitude)
            txtLongitude!!.text = formatGeoPoint(longitude)

            val place = LatLng(latitude,longitude)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(place))
            mMap.addMarker(MarkerOptions().position(place).title("You Are Here"))
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
        mMap.addMarker(MarkerOptions().position(city).title("Marker here"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(city))
        mMap.uiSettings.isZoomControlsEnabled = true
    }
}
