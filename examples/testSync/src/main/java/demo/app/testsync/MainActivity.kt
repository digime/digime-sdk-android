package demo.app.testsync

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import me.digi.app.remote_access.MainRemoteDataSourceImpl
import me.digi.sdk.interapp.AppCommunicator

class MainActivity : AppCompatActivity() {
    private val SHARED_PREFS_KEY = "SHARED_PREFS_KEY"
    private val CACHED_SESSION_DATA = "CACHED_SESSION_DATA"
    private val CACHED_CREDENTIAL_KEY = "CACHED_CREDENTIAL_KEY"
    lateinit var mainRemoteDataSourceImpl: MainRemoteDataSourceImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainRemoteDataSourceImpl = MainRemoteDataSourceImpl(this.applicationContext)

        val authorize = findViewById<Button>(R.id.authorize)
        val syncNewData = findViewById<Button>(R.id.syncNewData)

        authorize.setOnClickListener {
            mainRemoteDataSourceImpl.authorizeAccess(this, null, null, "420")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess { authorizationResponse ->
                    val sharedPrefs: SharedPreferences = this.getSharedPreferences(
                        SHARED_PREFS_KEY, MODE_PRIVATE
                    )

                    val gson = Gson()
                    val credentials: String = gson.toJson(authorizationResponse.credentials)
                    val session: String = gson.toJson(authorizationResponse.session)
                    sharedPrefs.edit().putString(CACHED_SESSION_DATA, session).apply()
                    sharedPrefs.edit().putString(CACHED_CREDENTIAL_KEY, credentials).apply()
                }
                .subscribe()
        }

        syncNewData.setOnClickListener {
            val sharedPrefs: SharedPreferences = this.getSharedPreferences(
                SHARED_PREFS_KEY, MODE_PRIVATE
            )
            val authToken = sharedPrefs.getString(CACHED_CREDENTIAL_KEY, "")

            authToken?.let {
                mainRemoteDataSourceImpl.getSessionData(it, null)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext {
                        Log.d("aaaaa", it.fileName.toString())
                    }
                    .doOnComplete {
                        Log.d("aaaaa", "complete")
                    }
                    .subscribe()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        AppCommunicator.getSharedInstance(this).onActivityResult(requestCode, resultCode, data)
    }
}