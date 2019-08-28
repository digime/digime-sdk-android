package api.helpers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import me.digi.sdk.api.helpers.DMECertificatePinnerBuilder
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DMECertificatePinnerSpec {

    @Test
    fun `cert pinner built without error`() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val builder = DMECertificatePinnerBuilder(ctx, "api.digi.me")
        builder.buildCertificatePinner()
    }
}