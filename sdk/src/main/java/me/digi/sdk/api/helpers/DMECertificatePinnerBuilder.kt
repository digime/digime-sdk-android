package me.digi.sdk.api.helpers

import android.content.Context
import okhttp3.CertificatePinner
import java.security.cert.CertificateFactory

internal class DMECertificatePinnerBuilder(private val context: Context, private val domain: String) {

    fun buildCertificatePinner(): CertificatePinner {

        var pinnerBuilder = CertificatePinner.Builder()
        val assetManager = context.assets
        val certFactory = CertificateFactory.getInstance("X.509")

        listCertificateFiles().flatMap { certPath ->
            val fileStream = assetManager.open(certPath)
            certFactory.generateCertificates(fileStream)
        }.forEach { cert ->
            pinnerBuilder.add(domain, CertificatePinner.pin(cert))
        }

        return pinnerBuilder.build()
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