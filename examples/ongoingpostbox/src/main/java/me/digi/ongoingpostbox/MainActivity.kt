package me.digi.ongoingpostbox

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import me.digi.ongoingpostbox.data.localaccess.MainLocalDataAccess
import me.digi.ongoingpostbox.features.create.view.CreatePostboxFragment
import me.digi.ongoingpostbox.features.upload.view.UploadContentFragment
import me.digi.ongoingpostbox.usecases.UpdateSessionUseCase
import me.digi.ongoingpostbox.utils.replaceFragment
import me.digi.sdk.entities.Session
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.response.ConsentAuthResponse
import me.digi.sdk.interapp.AppCommunicator
import org.koin.android.ext.android.inject
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private val localAccess: MainLocalDataAccess by inject()
    private val updateSession: UpdateSessionUseCase by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Digimesdkandroid)
        setContentView(R.layout.activity_main)

        handleFlow()
    }

    private fun handleFlow() {
        val credentials: CredentialsPayload? = localAccess.getCachedCredential()
        val postbox: ConsentAuthResponse? = localAccess.getCachedPostbox()
        val session: Session? = localAccess.getCachedSession()

        // User has all needed information to push their data
        if (credentials != null && postbox != null && session != null)
            updateSessionProceedToUpload()
        else CreatePostboxFragment.newInstance().replaceFragment(supportFragmentManager)
    }

    private fun updateSessionProceedToUpload() {
        updateSession
            .invoke()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    if (it) UploadContentFragment.newInstance()
                        .replaceFragment(supportFragmentManager)
                },
                onError = {
                    Toast.makeText(
                        this,
                        it.localizedMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
    }

    override fun onResume() {
        super.onResume()

        // Check that an Application ID has been configured.
        if (getString(R.string.digime_application_id).isEmpty()) {

            val msg = AlertDialog.Builder(this)
            msg.setTitle("Missing Application ID")
            msg.setMessage(
                """
                You must provide an application ID in strings.xml.
                Please follow the instructions in the README to obtain yours.
                
                The application will now exit.
                """.trimIndent()
            )
            msg.setNeutralButton("Okay") { _, _ -> exitProcess(1) }
            msg.create().show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        AppCommunicator.getSharedInstance().onActivityResult(requestCode, resultCode, data)
    }
}