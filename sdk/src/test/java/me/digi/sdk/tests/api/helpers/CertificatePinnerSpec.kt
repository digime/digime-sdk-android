package me.digi.sdk.tests.api.helpers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import me.digi.sdk.api.helpers.CertificatePinnerBuilder
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CertificatePinnerSpec {

    @Test
    fun `cert pinner built without error`() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val builder = CertificatePinnerBuilder(ctx, "api.digi.me")
        builder.buildCertificatePinner()
    }
}