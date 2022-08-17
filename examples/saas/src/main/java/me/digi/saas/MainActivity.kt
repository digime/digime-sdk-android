package me.digi.saas

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import me.digi.sdk.interapp.AppCommunicator

class MainActivity : AppCompatActivity() {
    companion object {
        const val STORAGE_REQUEST_CODE = 43
    }

    sealed class Error(message: String) : kotlin.Error(message) {
        object StoragePermissionDenied : Error("The user declined to grant storage access.")
    }

    private var storagePermissionDenied: Boolean = false
    private lateinit var storagePermissionObtainable: CompletableEmitter

    fun obtainStoragePermissionIfRequired(): Completable =
        Completable.create { emitter ->
            storagePermissionObtainable = emitter
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // We need to request storage access.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    STORAGE_REQUEST_CODE
                )
            } else {
                // We already have permission, fulfill the obtainable.

                storagePermissionObtainable.onComplete()
            }
        }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Digimesdkandroid)
        setContentView(R.layout.activity_main)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        AppCommunicator.getSharedInstance().onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                STORAGE_REQUEST_CODE -> storagePermissionObtainable.onComplete()
            }
        } else {
            storagePermissionDenied = grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED
            when (requestCode) {
                STORAGE_REQUEST_CODE -> storagePermissionObtainable.onError(Error.StoragePermissionDenied)
            }
        }
    }
}