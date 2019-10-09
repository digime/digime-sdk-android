/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.examples.ca_no_sdk.service;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

import me.digi.examples.ca_no_sdk.BuildConfig;
import me.digi.examples.ca_no_sdk.service.models.DataGetEncryptedResponse;
import me.digi.examples.ca_no_sdk.service.models.DataGetResponse;

import me.digi.sdk.crypto.CACryptoProvider;
import me.digi.sdk.crypto.CAKeyStore;
import me.digi.sdk.crypto.DGMCryptoFailureException;
import retrofit2.Response;

public class GetUserDataTask extends AsyncTask<GetUserDataTask.GetUserDataTaskParams, Void, Response<DataGetResponse>> {
    private GetUserDataTask.Listener listener;
    private static final String DEC_KEY = "308204a30201000282010100a9aedea6779e528992a039f40c19a39062d33b0fa6de4f2af4b74805655cd0119069dbeb0bab90b481cdeceb2dea9f014f5ddeb0a93316e7146946e9fa8b897fb480989037587c73811231e4d22cf28b6d2ce811d7e6f1275f0783fd345cd03be945e026326188a1aa12d9174f6bd0c6d90304339eb721942fcafeee13256135359c98442b72dd10471e6dfaffdf0d916599cadfadaae025b726faf88ec44bb0945e9e8fab6e7a98152de7171e6503930fffe2f32dd3a4447a6327ce8601f795d6d57ad9640fcd8fa1b55dc248ebb7269f2da430e85de688eff1321d00097158b8f9c1f7816ab95c519375b256b0c6040bd7b9858c2d27fed2370567ba8427990203010001028201010093f3de3fd85d3c2aa8a6fce1470bb40ad9a0c506c8c15ed65dbad219a260632c6d7760427a5286425e4c682048512383c8e8589c416c42b40aa0212d3341280b2a2056e6a8db86e84fcac5a6777ca99fd8fa270027f93e9ccdc787d6e829658857c68dc3c07a3ae07ba32397a7b0a2c23fc6d98b0901354e38be0fbb1706a8d287834ec8c411f80fa44ecabd714675b0d59633acc6995512c2dc16875c586a1847688c186fa2f69c4eb9eae4331d464cd8509bf99d441eae98f7e9537ef31d6da58850f08d4b2aab82d6273f2000447bc02d5b458712fa77e356f739c32290348836db924c8eec90afb87b7dd8c20a806e0f97c98d35f94c3120bad1936241e102818100e4865f90b71a4f5b1a12fa92ea0444684ac9b3e0f1ddc1e6cb6a489537410c91b36037dec12171ccc33563cdab2a15e1cfa722a94c6407bd2be6e5dba881d8838854199f746b312d9e00bb4c171585bc5051e6ba03a152487f3718eb0967d2a8025f0f01993689eca47d605bd23ee128e3bf3c2977530f20b89c374e50af404302818100be1570ce9b9d0f4b9c14f3d5ea2f55d539e3cbb81eb9e44fa878cf9e11ce689564852d4ed79d09158e2721eb8637e961d673f84274d1384f4a95e883b0c77e5d606f24fa01b9a610bdfda1415ef608b83cd38477905e62f5b9aa3ef76d6222304c9cc2c2b12f60b78ceee5892cd17fa2872d5b980dd45e99e3123431ef7eb8f3028180521f13b28e8a2ee03f2378d658b045e0f0974143e1c6de0a5129158241c3e77f6865784e5d3ae6893dd12ed756de1dd4f2e94de466e63f7db48c1a27f08b10c25bb85528df0e32330167a3e6f918abe17b3fa3594f3aa6b614b93904257220da6d57b9adca6035fa4b361eed804546668a494b965f2202fab03cbb0732a977bf0281803860c79aa0110f6e4f96ef536d2828ff1b327343e2e923cc749d9086c3a542e3bc72bba37cd3f8d3c9dbd575b3d375872d422c4a19b7cc49c8477a35450386794f96e792b75c46e30456ebb325e53764ddb5a6be87b55708a6ced5ea31294016af427789a35ff801b8ed4a6b4b3dbfeb86c86f384431cef539a23694f101d6fd0281807ed1368c8b524e2af0bdf6d7b24f028b1f587583e617f5e599abdedb049ae9c8ae66a15c1c27a12e422521dba6316d830fdd2d3707a861793c3b93039d988ba5923ee483db546d8dfca792c7e9aa86b36796732ea6815e9e2283e21d77664b7089574e3cdd9a899b39344327fc7a057a007850fcf9252bf98c5c2c96b9a38368";
    private static CACryptoProvider provider;
    private static final Gson gson = new Gson();

    private CACryptoProvider getProvider(Context context) throws DGMCryptoFailureException{
        if (provider == null) {
            synchronized (GetUserDataTask.class) {
                CAKeyStore mainKeystore = new CAKeyStore(DEC_KEY);
                //Check if we have a private key defined (for v2 contracts)
                if (!TextUtils.isEmpty(BuildConfig.P12_STORE) && context != null) {
                    mainKeystore.addPKCS12KeyFromAssets(context, BuildConfig.P12_STORE, null, BuildConfig.P12_PASS, null);
                }
                provider = new CACryptoProvider(mainKeystore);
            }
        }
        return provider;
    }

    @Override
    protected Response<DataGetResponse> doInBackground(GetUserDataTask.GetUserDataTaskParams... getUserDataTaskParams) {
        GetUserDataTask.GetUserDataTaskParams params = getUserDataTaskParams[0];
        this.listener = params.getListener();
        try {
            if (params.fileName == null) {
                return params.getPermissionService().listDataFiles(params.sessionKey).execute();
            } else if (!BuildConfig.CA_ENCRYPTED){
                return params.getPermissionService().getDataFileUnencrypted(params.sessionKey, params.fileName).execute();
            } else {
                Response<DataGetEncryptedResponse> response = params.getPermissionService().getDataFile(params.sessionKey, params.fileName).execute();
                return decrypt(params.appContext.get(), response);
            }
        } catch (final Exception e) {
            if (listener != null) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        listener.userDataTaskFailed(e);
                    }
                });
            }
            cancel(true);
            return null;
        }
    }

    private Response<DataGetResponse> decrypt(Context context, Response<DataGetEncryptedResponse> encResponse) throws DGMCryptoFailureException, IOException{
        String decrypted = getProvider(context).decryptStream(new ByteArrayInputStream(encResponse.body().fileContent.getBytes(StandardCharsets.UTF_8)), true);
        if (!TextUtils.isEmpty(decrypted)) {
            DataGetResponse dataObject = new DataGetResponse();
            dataObject.fileList = encResponse.body().fileList;
            Type type = new TypeToken<List<JsonElement>>(){}.getType();
            dataObject.fileContent = gson.fromJson(decrypted, type);

            return Response.success(dataObject, encResponse.raw());
        } else {
            throw new InvalidObjectException("Could not decrypt response");
        }
    }

    @Override
    protected void onPostExecute(Response<DataGetResponse> response) {
        if (listener != null) {
            listener.userDataTaskComplete(response);
        }
    }

    public interface Listener {
        void userDataTaskComplete(Response<DataGetResponse> response);
        void userDataTaskFailed(Exception e);
    }

    public static class GetUserDataTaskParams {
        private PermissionService permissionService;
        private String sessionKey;
        private String fileName;
        private GetUserDataTask.Listener listener;
        private WeakReference<Context> appContext;

        public GetUserDataTaskParams(Context context, PermissionService permissionService, String sessionKey, @Nullable String fileName, GetUserDataTask.Listener listener) {
            this.permissionService = permissionService;
            this.sessionKey = sessionKey;
            this.fileName = fileName;
            this.listener = listener;
            this.appContext = new WeakReference<>(context);
        }

        public PermissionService getPermissionService() {
            return permissionService;
        }

        public GetUserDataTask.Listener getListener() {
            return listener;
        }
    }
}
