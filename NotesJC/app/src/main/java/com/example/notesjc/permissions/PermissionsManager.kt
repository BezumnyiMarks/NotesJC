package com.example.mangojc.permissions

import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.notesjc.MainActivity

class PermissionsManager(private val activity: MainActivity) {
    private val launcher = activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ map ->
        if(!map.values.all { it } && Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU){
            Toast.makeText(activity, "Permissions are not Granted", Toast.LENGTH_SHORT).show()
        }
    }

    fun checkPermissions(): Boolean {
        val isAllGranted = REQUEST_PERMISSIONS.all { permission ->
            activity.let { ContextCompat.checkSelfPermission(it.applicationContext, permission) } == PackageManager.PERMISSION_GRANTED
        }
        if (isAllGranted){
            return true
        }
        else {
            launcher.launch(REQUEST_PERMISSIONS)
            return false
        }
    }

    companion object{
        private val REQUEST_PERMISSIONS: Array<String> = buildList {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(android.Manifest.permission.READ_MEDIA_IMAGES)
                add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
            else
            add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }.toTypedArray()
    }
}