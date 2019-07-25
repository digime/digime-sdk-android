package me.digi.sdk.api.helpers

import android.content.Context
import android.content.res.AssetManager
import okhttp3.CertificatePinner
import java.security.cert.CertificateFactory

class DMECertificatePinnerBuilder(private val context: Context, private val domain: String) {

    fun buildCertificatePinner(): CertificatePinner {

        val certFactory = CertificateFactory.getInstance("X.509")

        for (certPath in listCertificateFiles()) {

        }

    }

    fun shouldPinCommunicationsWithDomain() = listCertificateFiles().isNotEmpty()

    private fun listCertificateFiles(): List<String> {

        val assetManager = context.assets

        val filePathComponents = listOf(
            "certificates",
            domain
        )

        val fileLocation = filePathComponents.joinToString("/")

        val certsForDomain = assetManager.list(fileLocation) ?: emptyArray()

        return certsForDomain.toList()
    }

}