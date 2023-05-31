package com.example.mapapplicationdemo


import android.R.attr.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.location.LocationManagerCompat
import com.example.mapapplicationdemo.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import java.util.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var binding: ActivityMainBinding

    private lateinit var mMap: GoogleMap

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    var currentLocation: Location? = null

    private var REQUEST_CODE = 101
    private var flags = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLocationMap()
        binding.fab.setOnClickListener {
            startActivity(Intent(this@MainActivity,QrCodeActivity::class.java))
        //            flags=true
        //            getLocationMap()
        }
    }

    private fun getLocationMap() {
        if (ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE
            )
            return;
        }
        if(isLocationEnabled(this)) {
            val task: Task<Location> = fusedLocationProviderClient.lastLocation
            task.addOnSuccessListener(OnSuccessListener<Any?> { location ->
                if (location != null) {
                    currentLocation = location as Location?

                    val supportMapFragment =
                        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)!!
                    supportMapFragment!!.getMapAsync(this@MainActivity)
                }
            })
        }else{
            startActivity( Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    private fun fromVectorToBitmap(id: Int, color: Int): BitmapDescriptor
    {
        val vectorDrawable: Drawable = ResourcesCompat.getDrawable(resources, id, null)
            ?: return BitmapDescriptorFactory.defaultMarker()

        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0,0,canvas.width, canvas.height)
        DrawableCompat.setTint(vectorDrawable, color)
        vectorDrawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Add a marker in Sydney and move the camera
        val latLng = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>?
        val address: Address?
        var fulladdress = ""
        addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))

        if (addresses!!.isNotEmpty()) {
            address = addresses[0]
            fulladdress = address.getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex
            var city = address.getLocality();
            var state = address.getAdminArea();
            var country = address.getCountryName();
            var postalCode = address.getPostalCode();
            var knownName = address.getFeatureName(); // Only if available else return NULL
        } else{
            fulladdress = "Location not found"
        }

        googleMap.setOnCameraIdleListener {
            val position = googleMap.cameraPosition
            movedLocation(position)
        }

////        val circleDrawable = getMarkerIconFromDrawable(resources.getDrawable(R.drawable.locations)/*BitmapDescriptorFactory.fromResource(R.drawable.locations)*/)
//
//        mMap.addMarker(
//            MarkerOptions()
//                .position(latLng)
//                .title(fulladdress)
//                .icon(getMarkerIconFromDrawable())
//        )
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
//
//        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
//
//        if(!flags){
//            val midLatLng: LatLng = mMap.cameraPosition.target
//            mMap.addCircle(CircleOptions()
//                .center(midLatLng)
//                .radius(50.0)
//                .strokeWidth(1f)
//                .fillColor(0x550000FF))
//        }
    }

    private fun movedLocation(position: CameraPosition) {
        val latLng = LatLng(position.target.latitude, position.target.longitude)
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>?
        val address: Address?
        var fulladdress = ""
        addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

        if (addresses!!.isNotEmpty()) {
            address = addresses[0]
            fulladdress = address.getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex
            var city = address.getLocality();
            var state = address.getAdminArea();
            var country = address.getCountryName();
            var postalCode = address.getPostalCode();
            var knownName = address.getFeatureName(); // Only if available else return NULL
        } else{
            fulladdress = "Location not found"
        }


        Toast.makeText(applicationContext,
            "$fulladdress", Toast.LENGTH_SHORT).show()
    }

    private fun getMarkerIconFromDrawable(): BitmapDescriptor? {

        val result = Bitmap.createBitmap(
            76,
            76,
            Bitmap.Config.ARGB_8888
        )
        result.eraseColor(Color.TRANSPARENT)
        val canvas = Canvas(result)
        val drawable = resources.getDrawable(R.drawable.locations)
        drawable.setBounds(0, 0, 76,76)
        drawable.draw(canvas)

        val roundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val bitmapRect = RectF()
        canvas.save()

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.avatar)
        //Bitmap bitmap = BitmapFactory.decodeFile(path.toString()); /*generate bitmap here if your image comes from any url*/
        //Bitmap bitmap = BitmapFactory.decodeFile(path.toString()); /*generate bitmap here if your image comes from any url*/
        if (bitmap != null) {
            val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            val matrix = Matrix()
            val scale = dp(0F) / bitmap.width.toFloat()
            matrix.postTranslate(dp(5F).toFloat(), dp(5F).toFloat())
            matrix.postScale(scale, scale)
            roundPaint.shader = shader
            shader.setLocalMatrix(matrix)
            bitmapRect[dp(5F).toFloat(), dp(5F).toFloat(), dp(52F + 5).toFloat()] =
                dp(52F + 5).toFloat()
            canvas.drawRoundRect(bitmapRect, dp(26F).toFloat(), dp(26F).toFloat(), roundPaint)
        }
        canvas.restore()
        try {
            canvas.setBitmap(null)
        } catch (e: Exception) {
        }

        return BitmapDescriptorFactory.fromBitmap(result)
    }

    private fun dp(value: Float): Int {
        return if (value == 0f) {
            0
        } else Math.ceil((resources.displayMetrics.density * value).toDouble()).toInt()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] ==
            PackageManager.PERMISSION_GRANTED) {
            getLocationMap()
        }
        }
    }
}