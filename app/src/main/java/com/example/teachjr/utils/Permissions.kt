package com.example.teachjr.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.storage.StorageManager
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

object Permissions {

    /**
     * Code for requesting permissions
     */
    fun hasAccessFineLocation(context: Context) =
        ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun hasAccessCoarseLocation(context: Context) =
        ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun requestPermission(activity: Activity) {
        val permissionToRequest = mutableListOf<String>()
        if(Build.VERSION.SDK_INT >= 23) {
            if(!hasAccessCoarseLocation(activity)) {
                permissionToRequest.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }
        if(!hasAccessFineLocation(activity)) {
            permissionToRequest.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if(permissionToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, permissionToRequest.toTypedArray(), 0)
        }
    }

    fun getPendingPermissions(activity: Activity): Array<String> {
        val pendingPermissions = mutableListOf<String>()
        if(Build.VERSION.SDK_INT >= 23) {
            if(!hasAccessCoarseLocation(activity)) {
                pendingPermissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }
        if(!hasAccessFineLocation(activity)) {
            pendingPermissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
        return pendingPermissions.toTypedArray()
    }

    fun hasReadStoragePermissions(context: Context) =
        ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    fun hasWriteStoragePermissions(context: Context) =
        ActivityCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    fun requestStoragePermissions(activity: Activity) {
        val permissionToRequest = mutableListOf<String>()
        if(!hasReadStoragePermissions(activity)) {
            permissionToRequest.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if(!hasWriteStoragePermissions(activity)) {
            permissionToRequest.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if(permissionToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, permissionToRequest.toTypedArray(), 0)
        }
    }

    fun getPendingStoragePermissions(activity: Activity): Array<String> {
        val pendingPermissions = mutableListOf<String>()
        if(!hasReadStoragePermissions(activity)) {
            pendingPermissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if(!hasWriteStoragePermissions(activity)) {
            pendingPermissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        return pendingPermissions.toTypedArray()
    }
}