package com.srmstudios.srmgallery.util

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/*
*
* Helper class to ask runtime permissions from the user
* Author: Shahrukh Malik
*
* */
class PermissionsHelper(
    private val activity: ComponentActivity,
    private val permissions: List<String>,
    private val iPermissionsHelper: IPermissionsHelper,
) {
    private var requestPermissionLauncher: ActivityResultLauncher<Array<String>>? = null

    init {
        setPermissionsLauncherCallback()
    }

    private fun setPermissionsLauncherCallback() {
        requestPermissionLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGrantedMap ->
            val unGrantedPermissions = isGrantedMap.filter { !it.value }
            if (unGrantedPermissions.isEmpty()) {
                if (permissions.filter {
                        ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_DENIED
                    }.isEmpty()) {
                    // All permissions are granted
                    // We are good to go
                    iPermissionsHelper?.onAllPermissionsGranted()
                }
            } else {
                // Some permissions were denied permanently by the user
                // means he selected "Don't ask again"
                // So here we can show him a dialog explaining why the
                // permission is important, and there should be an action
                // button somewhere so that user can go the app settings
                // to allow the permission
                // If there are more than one permissions that were
                // denied permanently, then we should show the dialog for each
                // permission one by one by picking up the first denied permission and so on
                val first = unGrantedPermissions.entries.first()
                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, first.key)) {
                    iPermissionsHelper?.onPermissionDeniedPermanently(first.key)
                }
            }
        }
    }

    fun requestPermissions() {
        activity?.let { activity ->
            if (permissions.filter {
                    ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_DENIED
                }.isEmpty()) {
                // All permissions are granted
                // We are good to go
                iPermissionsHelper?.onAllPermissionsGranted()
            } else {
                val permissionsNotGranted = permissions.filter {
                    ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_DENIED
                }
                // we need to check here first if there are any permissions
                // that the user denied for the first time
                // this means that he was confused about it and we need
                // to show him a rationale explaining why this permission is important
                val permissionsThatNeedRationale = permissionsNotGranted.filter { ActivityCompat.shouldShowRequestPermissionRationale(activity, it) }
                if (permissionsThatNeedRationale.isEmpty()) {
                    // this means the permissions are being asked
                    // from the user for the first time
                    requestPermissionLauncher?.launch(
                        permissionsNotGranted.toTypedArray()
                    )
                } else {
                    // this means some permissions were denied by the user for the first time
                    iPermissionsHelper.onPermissionDenied(permissionsThatNeedRationale.first())
                }
            }
        }
    }

    fun requestPermissionThatWasDeniedByUser(permission: String) {
        requestPermissionLauncher?.launch(
            arrayOf(permission)
        )
    }

    fun areAllPermissionsGranted(): Boolean =
        permissions.filter { ContextCompat.checkSelfPermission(activity,it) == PackageManager.PERMISSION_DENIED }.isEmpty()

    fun goToPermissionSettings() {
        activity?.let { activity ->
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${activity.packageName}")).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }.also { intent ->
                activity.startActivity(intent)
            }
        }
    }

    fun showDialog(title: String, message: String, positiveButtonText: String, positiveButtonListener: () -> Unit, negativeButtonText: String, negativeButtonListener: () -> Unit) {
        activity?.let { activity ->
            MaterialAlertDialogBuilder(activity)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton(negativeButtonText) { dialog, which ->
                    negativeButtonListener.invoke()
                }
                .setPositiveButton(positiveButtonText) { dialog, which ->
                    positiveButtonListener.invoke()
                }.show()
        }
    }

    fun showDialog(title: String, message: String, positiveButtonText: String, positiveButtonListener: () -> Unit) {
        activity?.let { activity ->
            MaterialAlertDialogBuilder(activity)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(positiveButtonText) { dialog, which ->
                    positiveButtonListener.invoke()
                }.show()
        }
    }

    interface IPermissionsHelper {
        fun onAllPermissionsGranted()
        fun onPermissionDenied(permission: String)
        fun onPermissionDeniedPermanently(permission: String)
    }
}