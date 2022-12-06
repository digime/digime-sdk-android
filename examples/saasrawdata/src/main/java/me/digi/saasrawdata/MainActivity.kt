package me.digi.saasrawdata

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import io.reactivex.rxjava3.kotlin.subscribeBy
import me.digi.sdk.Init
import me.digi.sdk.entities.*
import me.digi.sdk.entities.configuration.DigiMeConfiguration
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.interapp.AppCommunicator
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var pushFile: Button
    private lateinit var readFile: Button
    private lateinit var readFilesList: RecyclerView
    private lateinit var progressBar: ProgressBar

    private lateinit var credentials: CredentialsPayload

    private lateinit var fileId: String

    private val readListAdapter: ReadAdapter by lazy { ReadAdapter() }

    private val writeClient: Init by lazy {

        val configuration = DigiMeConfiguration(
            this.resources.getString(R.string.appId),
            this.resources.getString(R.string.writeContractId),
            this.resources.getString(R.string.writePrivateKey),
            "https://api.digi.me/"
        )

        Init(applicationContext, configuration)
    }

    private val readClient: Init by lazy {

        val configuration = DigiMeConfiguration(
            this.resources.getString(R.string.appId),
            this.resources.getString(R.string.readRawContractId),
            this.resources.getString(R.string.readRawPrivateKey),
            "https://api.digi.me/"
        )

        Init(applicationContext, configuration)
    }

    private lateinit var storagePermissionObtainable: CompletableEmitter
    private val STORAGE_REQUEST_CODE = 43

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pushFile = findViewById(R.id.pushFile)
        readFile = findViewById(R.id.readFile)
        readFilesList = findViewById(R.id.readList)
        progressBar = findViewById(R.id.progressBar)

        readFilesList.adapter = readListAdapter

        pushFile.setOnClickListener {
            progressBar.isVisible = true
            writeFile()
        }

        readFile.setOnClickListener {
            progressBar.isVisible = true
            readFile()
        }
    }

    private fun writeFile() {
        writeClient.authorizeAccess(
            this,
            null,
            null,
            null

        ) { response, authorizeError ->
            if (authorizeError == null) {
                credentials = response?.credentials!!

                val fileContent: ByteArray = getFileContent(this, "file.pdf")

                val metadata = WriteMetadata(
                    listOf(WriteAccount("1")),
                    listOf("file.pdf"),
                    listOf("testTag"),
                    "application/pdf",
                )

                val writeDataPayload = WriteDataPayload(
                    metadata,
                    fileContent
                )
                writeClient.write(
                    response.credentials?.accessToken?.value!!,
                    writeDataPayload,
                ) { _, error ->
                    progressBar.isVisible = false
                    if (error == null)
                        readFile.isEnabled = true
                    else
                        Log.d("MainActivity", "Failed to write data")
                }
            } else {
                progressBar.isVisible = false
                Log.d("MainActivity", "Failed to authorize")
            }
        }
    }

    private fun readFile() {

        val scope = CaScope()

        val metadata = MetadataScope()
        metadata.mimeType = listOf("application/pdf")

        val metadataCriteria = MetadataCriteria()
        metadataCriteria.metadata = metadata

        val criteria = listOf(metadataCriteria)
        scope.criteria = criteria

        readClient.authorizeAccess(
            this,
            scope,
            credentials,
            null

        ) { response, authorizeError ->
            if (authorizeError == null) {

                readClient.readFileList(response?.credentials?.accessToken?.value!!) { readFileResponse, error ->
                    progressBar.isVisible = false
                    if (error == null) {
                        val data = readFileResponse?.fileList
                        readListAdapter.submitList(data)

                        readListAdapter.setOnFileItemClickListener {
                            obtainStoragePermissionIfRequired()
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeBy(
                                    onComplete = {
                                        fileId = it.fileId

                                        readClient.readFile(
                                            response.credentials?.accessToken?.value!!, fileId
                                        ) { readFileResponse, error ->
                                            val fileContentBytes = readFileResponse?.fileContent

                                            openFile(fileContentBytes!!)
                                        }
                                    })
                        }

                    } else
                        Log.d("MainActivity", "Failed to read files")
                }
            } else {
                Log.d("MainActivity", "Failed to authorize")
                progressBar.isVisible = false
            }
        }
    }

    private fun openFile(content: ByteArray) {
        val path = applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File.createTempFile("test", ".pdf", path)

        val os = FileOutputStream(file)
        os.write(content)
        os.close()

        val uri: Uri = FileProvider.getUriForFile(
            applicationContext,
            "me.digi.examples.saasrawdata.saasProvider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW)

        intent.setDataAndType(uri, "*/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        ContextCompat.startActivity(this, intent, null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        AppCommunicator.getSharedInstance().onActivityResult(requestCode, resultCode, data)
    }

    private fun obtainStoragePermissionIfRequired(): Completable =
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

}