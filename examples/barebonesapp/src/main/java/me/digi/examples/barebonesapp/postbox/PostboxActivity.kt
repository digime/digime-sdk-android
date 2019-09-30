package me.digi.barebonesapp.postbox

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.postbox_activity_layout.*
import me.digi.barebonesapp.util.ConsentAccesInProgress
import me.digi.barebonesapp.util.CryptoUtil
import me.digi.examples.barebonesapp.R
import java.io.File

class PostboxActivity : AppCompatActivity() {

    private val cryptoUtil : CryptoUtil = CryptoUtil()
    private lateinit var preparedData: CryptoUtil.ResultsData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.postbox_activity_layout)

//        DigiMeClient.init(this)

        item_postbox_button.setOnClickListener {
            createPostbox()
        }
    }

    private fun encryptData(publicKey: String, postboxId: String){
//        preparedData = cryptoUtil.encryptData(publicKey, postboxId, filesDir, assets, "media.json")

        sendData()
    }

    private fun createPostbox() {

        displaySendingData()

//        DigiMeClient.getInstance().createPostbox(this, object : SDKCallback<SessionResult>() {
//            @RequiresApi(Build.VERSION_CODES.O)
//            override fun succeeded(result: SDKResponse<SessionResult>) {
//                val postboxSession = result.body as CreatePostboxSession
//                encryptData(postboxSession.postboxPublicKey, postboxSession.postboxId)
//            }
//
//            override fun failed(exception: SDKException) {
//                exception.message?.let { removeSending(it) }
//            }
//        })
    }

    private fun sendData(){
        val file = File(filesDir.path, "file.json")

        // Create a request body with file and image media type
//        val fileReqBody = RequestBody.create(MediaType.parse("application/json"), file)
//
////         Create MultipartBody.Part using file request-body,file name and part name
//        val part = MultipartBody.Part.createFormData("file", "file", fileReqBody)
//
////        Create request body with text description and text media type
//        val description = RequestBody.create(MediaType.parse("application/json"), "file")
//
//        DigiMeClient.getInstance().pushData(preparedData.base64EncryptedKey, preparedData.iv, preparedData.base64encodedMetadata, preparedData.postboxId, part, description, object: SDKCallback<Void>() {
//            override fun succeeded(result: SDKResponse<Void>?) {
//                displayResults()
//            }
//
//            override fun failed(exception: SDKException?) {
//                exception?.message?.let { removeSending(it) }
//            }
//        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        DigiMeClient.getInstance().postboxAuthManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun displayResults(){
        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, PostboxFragment())
                .commit()
    }

    private fun removeSending(errorMessage: String){
        supportFragmentManager.popBackStack()
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    private fun displaySendingData(){
        val bundle = Bundle()
        bundle.putString("progressText", "Sending data")

        val sendingDataFragment = ConsentAccesInProgress()
        sendingDataFragment.arguments = bundle

        supportFragmentManager.beginTransaction()
                .addToBackStack("in_progress")
                .replace(android.R.id.content, sendingDataFragment)
                .commit()
    }
}