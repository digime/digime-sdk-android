package me.digi.sdk.core.internal.network;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.digi.sdk.core.BuildConfig;
import me.digi.sdk.core.DigiMeClient;
import okhttp3.CertificatePinner;
import okio.ByteString;

public class SslCertificatePinning {

    private static final String TAG = SslCertificatePinning.class.getSimpleName();

    private static volatile CertificatePinner pinner;

    private SslCertificatePinning() {}

    public static CertificatePinner getInstance() {
        if (pinner == null) {
            synchronized (SslCertificatePinning.class) {
                pinner = getPinnerInstance(DigiMeClient.getApplicationContext());
            }
        }
        return pinner;
    }

    private static final String certificatesFolderPath = "certificates";

    private static CertificatePinner getPinnerInstance(Context context) {
        CertificatePinner.Builder builder = new CertificatePinner.Builder();

        try {
            AssetManager assetManager = context.getAssets();
            List<String> domainFolders = Arrays.asList(assetManager.list(certificatesFolderPath));

            String domain =  BuildConfig.BASE_HOST;
            if (!domainFolders.contains(domain)) {
                Log.e(TAG, String.format("There are no ssl certificates provided for %s", domain));
            } else {
                try {
                    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

                    String[] files = assetManager.list(certificatesFolderPath + "/" + domain);
                    List<Certificate> certificates = new ArrayList<>();

                    if (files == null || files.length == 0) {
                        Log.e(TAG, String.format("There are no ssl certificates provided for %s", domain));
                    }

                    for (String file : files) {
                        InputStream in = assetManager.open(certificatesFolderPath + "/" + domain + "/" + file);
                        certificates.addAll(certificateFactory.generateCertificates(in));
                    }

                    for (Certificate certificate : certificates) {
                        Log.d(TAG, String.format("fingerprint %s", ByteString.of(certificate.getEncoded()).sha256()));
                        builder.add(domain, CertificatePinner.pin(certificate));
                    }
                } catch (CertificateException e) {
                    Log.e(TAG, "Reading certificate failed", e);
                    //throw new IllegalStateException("generating certificates failed - check your cert data file", e);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Accessing assets folder with ssl certificates failed", e);
        }

        return builder.build();

    }
}
