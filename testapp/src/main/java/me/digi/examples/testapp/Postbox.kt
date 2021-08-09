package me.digi.examples.testapp

import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.postbox.*
import me.digi.sdk.DMEPushClient
import me.digi.sdk.entities.MimeType
import me.digi.sdk.entities.configuration.WriteConfiguration
import me.digi.sdk.entities.payload.DMEPushPayload
import me.digi.sdk.interapp.DMEAppCommunicator
import me.digi.sdk.utilities.crypto.DMECryptoUtilities
import java.io.IOException

class Postbox : AppCompatActivity() {
    private lateinit var cfg: WriteConfiguration
    private var jsonRadio : Boolean =  false
    private var pngRadio : Boolean = false
    private var pdfRadio : Boolean = false
    private lateinit var client: DMEPushClient

//    private val client: DMEPushClient by lazy {
//
//        val privateKey = DMECryptoUtilities(this).privateKeyHexFrom(
//            this.getString(R.string.digime_p12_filename),
//            this.getString(R.string.digime_p12_password)
//        )
//
//        val configuration = DMEPushConfiguration(
//            this.getString(R.string.digime_application_id),
//            this.getString(R.string.digime_postbox_contract_id),
//            privateKey
//        )
//
//        configuration.baseUrl = "https://api.stagingdigi.me/"
//
//
//        DMEPushClient(, configuration)
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.postbox)

        val privateKey = DMECryptoUtilities(this).privateKeyHexFrom(
            this.getString(R.string.digime_p12_filename)
        )

        val configuration = WriteConfiguration(
            this.getString(R.string.digime_application_id),
            this.getString(R.string.digime_postbox_contract_id),
            privateKey
        )

//        val configuration = DMEPushConfiguration(
//            this.getString(R.string.digime_application_id),
//            this.getString(R.string.digime_postbox_contract_id)
//        )

//        configuration.baseUrl = "https://api.stagingdigi.me/"
//        configuration.baseUrl = "https://api.development.devdigi.me/"

        client = DMEPushClient(applicationContext, configuration)

        console_log.movementMethod = ScrollingMovementMethod()

        push_data_to_postbox.setOnClickListener {
            if(json.isChecked || pdf.isChecked || png.isChecked) {
                updateConsoleLog("Creating postbox...")
                createPostbox()
            }
            else{
                Toast.makeText(this@Postbox, "Please select data to send", Toast.LENGTH_SHORT).show()
            }
        }


        json.setOnClickListener {
            json.isChecked = !jsonRadio
            jsonRadio = !jsonRadio
        }

        pdf.setOnClickListener {
            pdf.isChecked = !pdfRadio
            pdfRadio = !pdfRadio
        }

        png.setOnClickListener {
            png.isChecked = !pngRadio
            pngRadio = !pngRadio
        }

//        push_json_to_postbox.setOnClickListener {
//            updateConsoleLog("Creating postbox...")
//            createPostbox("file.json", "metadatajson.json", DMEMimeType.APPLICATION_JSON)
//        }
//
//        push_pdf_to_postbox.setOnClickListener {
//            updateConsoleLog("Creating postbox...")
//            //createPostbox("file.pdf", "metadatapdf.json", DMEMimeType.APPLICATION_PDF)
//        }
    }

    fun updateConsoleLog(text: String) {
        console_log.append(text + "\n")
    }

    private fun createPostbox() {
        client.createPostbox(this) { dmePostbox, error ->
            if (dmePostbox != null) {
                updateConsoleLog("Postbox created")
                updateConsoleLog("id" + dmePostbox.postboxId)
                updateConsoleLog("publicKey" + dmePostbox.publicKey)

                if(json.isChecked){
                    updateConsoleLog("Push json data to postbox started")

                    val fileContent = getFileContent("file.json")
                    val metadata = getFileContent("metadatajson.json")

                    client.pushDataToPostbox(
                        DMEPushPayload(
                            dmePostbox,
                            metadata,
                            fileContent,
                            MimeType.APPLICATION_JSON
                        )
                    ) {
                        if (error == null) {
                            updateConsoleLog("Json data pushed to postbox")
                        } else
                            updateConsoleLog("Json data pushed to postbox failed")

                    }
                }

                if(png.isChecked){
                    updateConsoleLog("Push png data to postbox started")

                    val fileContent = getFileContent("file.png")
                    val metadata = getFileContent("metadatapng.json")

                    client.pushDataToPostbox(
                        DMEPushPayload(
                            dmePostbox,
                            metadata,
                            fileContent,
                            MimeType.IMAGE_PNG
                        )
                    ) {
                        if (error == null) {
                            updateConsoleLog("Png data pushed to postbox")
                        } else
                            updateConsoleLog("Png data pushed to postbox failed")

                    }
                }

                if(pdf.isChecked){
                    updateConsoleLog("Push pdf data to postbox started")
                    val metadata = getFileContent("metadatapdf.json")

                    val fileContent = getFileContent("file.pdf")

                    client.pushDataToPostbox(
                        DMEPushPayload(
                            dmePostbox,
                            metadata,
                            fileContent,
                            MimeType.APPLICATION_PDF
                        )
                    ) {
                        if (error == null) {
                            updateConsoleLog("Pdf data pushed to postbox")
                        } else
                            updateConsoleLog("Pdf data pushed to postbox failed")

                    }
                }

            } else {
                updateConsoleLog("Failed to create postbox")
                Log.i("DME", "Postbox Create Error: $error")
            }
        }
    }

    private fun getFileContent(fileName: String): ByteArray {
        return try {
            val stream = assets.open(fileName)
            val size = stream.available()
            val buffer = ByteArray(size)
            stream.read(buffer)
            stream.close()
            buffer
        } catch (ex: IOException) {
            ex.printStackTrace()
            return ByteArray(2)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        DMEAppCommunicator.getSharedInstance().onActivityResult(requestCode, resultCode, data)
    }
}