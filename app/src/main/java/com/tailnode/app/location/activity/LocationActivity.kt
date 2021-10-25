package com.tailnode.app.location.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.tailnode.R
import com.tailnode.app.splash.activity.SplashActivity
import com.tailnode.app.store.PrefsStore
import com.tailnode.databinding.ActivityLocationBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.KClass


class LocationActivity : AppCompatActivity(){

    private var mFusedLocationClient: FusedLocationProviderClient? = null

    companion object {
        const val TAG = "LocationActivity"

        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    private val permissionHook =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (anyLocationPermissionsGranted()) {
                startLocationRequest()
            } else {
                Toast.makeText(this, R.string.location_permission, Toast.LENGTH_LONG)
                    .show()
            }
        }

    private val resolutionForResult =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                startLocationRequest()
            }
        }

    private var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.lastOrNull()?.let { onLocationAvailable(it) }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun onLocationAvailable(location: Location) {
        Log.i(TAG, "Location: $location " + location.latitude + " " + location.longitude)
        val address = Geocoder(this).getFromLocation(location.latitude, location.longitude, 1)

        var add = ""
        if (address.isNullOrEmpty()) setTextLocation(add)
        else {
            add = address[0].getAddressLine(0)
            setTextLocation(location = add)
        }

        val dateFormat = SimpleDateFormat("EEE yyyy-MM-DD HH:mm:ss a")
        val formatted = dateFormat.format(Calendar.getInstance().time)
        val loc = "$formatted\n$add\n\n"
        writeToFile(loc)
    }

    private fun writeToFile(loc: String) {
        try {
            val downloadFolder: File? = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            val file = File("$downloadFolder/location.txt")
            file.createNewFile()
            val writer = FileWriter(file, true)
            writer.write(loc)
            writer.flush()
            writer.close()
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: $e")
        }
    }

    private fun setTextLocation(location: String? = "") {
        Log.i(TAG, "Location: $location")
        binding.tvLocation.text = location
    }

    private lateinit var binding: ActivityLocationBinding

    private fun anyLocationPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PrefsStore.getUserName().observe(this, {
            val userName = "Hi $it"
            binding.tvUserName.text = userName
        })

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        startLocationRequest()

        binding.ivLogout.setOnClickListener {
            lifecycleScope.launch {
                PrefsStore.reset()
                launchActivity(SplashActivity::class)
            }
        }

        binding.tvViewLocation.setOnClickListener {
            val path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            val file = File("$path/location.txt")
            val uri: Uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.data = uri
            startActivity(intent)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationRequest() {
        if (!anyLocationPermissionsGranted()) {
            permissionHook.launch(REQUIRED_PERMISSIONS)
            return
        }

        val locationRequest = LocationRequest.create().apply {
            interval = 5 * 60 * 1000
            fastestInterval = 5 * 60 * 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val settingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
            .build()

        val client = LocationServices.getSettingsClient(this)

        val task = client.checkLocationSettings(settingsRequest)

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                    resolutionForResult.launch(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }

        task.addOnSuccessListener {
            mFusedLocationClient?.requestLocationUpdates(
                locationRequest,
                mLocationCallback,
                Looper.getMainLooper()
            )
        }
    }

    override fun onStop() {
        super.onStop()
        mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
    }

    private fun launchActivity(cls: KClass<*>) {
        val intent = Intent(this, cls.java)
        startActivity(intent)
        supportFinishAfterTransition()
    }
}