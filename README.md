# digi.me SDK for Android

## DEPRECATION

Legacy v1.6.1 of the digi.me Private Sharing SDK for Android. Please use v2.0.0 onwards for the best we have to offer and for priority support.
We recommend using the master branch as this always reflects the latest release.

## Preamble

The Digi.me SDK for Android is a multi-module library that allows seamless authentication with `digi.me Private Sharing`. For details on the API and general `Private Sharing` architecture, visit the [Dev Support Docs](https://developers.digi.me/consent-access).


## Table of Contents

  * [Installation](#installation)
     * [Using Gradle](#using-gradle)
     * [Directly From Source Code (downloaded or git submodule)](#directly-from-source-code-downloaded-or-git-submodule)
  * [Proguard Setup](#proguard-setup)
  * [Configuring SDK](#configuring-sdk)
     * [Obtaining your Contract ID and App ID](#obtaining-your-contract-id-and-app-id)
     * [Configuring DigiMeClient](#configuring-digimeclient)
  * [Callbacks & Responses](#callbacks-&-responses)
     * [SDKCallback](#sdkcallback)
     * [SDKListener](#sdklistener)
  * [Authorization](#authorization)
     * [Authorization Flow](#authorization-flow)
     * [authorize() specifics](#authorize-specifics)
     * [Guest Authorization Flow](#guest-authorization-flow)
  * [Fetching Data](#fetching-data)
     * [Handling Failures & Automatic Exponential Backoff](#handling-failures-&-automatic-exponential-backoff)
     * [Fetching Account Metadata](#fetching-account-metadata)
     * [Fetching Data in a Given Time Range](#fetching-data-in-a-given-time-range)
     * [Decryption](#decryption)

## Installation

### Using Gradle:

1. Set minSdkVersion to 21 in build.gradle.

2. In your project build.gradle (for example, app.build.gradle), add the sdk dependency:

```gradle

   dependencies {
        implementation 'me.digi:sdk:1.6.2'
   }
```

You should now be able to import `me.digi.sdk.core.DigiMeClient`.


### Directly From Source Code (downloaded or git submodule):

1. Download the source code.

2. Set minSdkVersion to 21 in build.gradle.

3. In Android Studio, go to File > New > New Module, select "Import Existing Project as Module".

4. Specify the location of the downloaded code.

5. Go to File > Project Structure, then add the SDK module as a dependency for your project.

6. You should now be able to import `me.digi.sdk.core.DigiMeClient`.


## _Proguard_ setup

If _Proguard_ is enabled in the project, you might need to add following parameters to _Proguard_ configuration:

```proguard
-dontwarn retrofit2.**
-dontwarn javax.naming.**
-keep class retrofit2.** { *; }
-keepattributes Signature

-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn`` okhttp3.**
-dontwarn okio.**

-dontwarn com.google.gson.**
-dontwarn org.spongycastle.**
```


## Configuring SDK

### Obtaining your Contract ID and App ID

Before accessing the APIs, you'll need a consent contract. A contract details exactly what data will be supplied, and the terms on which this happens. It also specifies how the data is encrypted in transit. We provide an example contract as part of the example project, this can be used for testing.

In addition to a contract, you'll require a unique app ID. You can obtain one [here](https://go.digi.me/developers/register).

### Configuring DigiMeClient

**DigiMeClient** is the main hub for all the interaction with the SDK. You access it through it's singleton accessor:
 
```java
DigiMeClient.getInstance()
```

DigiMeClient is automatically bootstrapped so there is no need to initialize it onCreate.
However, before you start interacting with it in your app, you will need to configure it with your **contractId** and **appId**, as well as your private key. Contracts are created in tandem with a cryptographic key pair - the data is encrypted in transit with this key pair and as such you must provide the SDK with the private key to facilitate decryption. For convenience, this key is stored in a PKCS#12 file.

We recommend creating entries in your `strings.xml` file for these; for example:

```xml
<string name="digime_contract_id">YOUR-CONTRACT-ID</string>
<string name="digime_application_id">YOUR-APP-ID</string>
<string name="digime_p12_filename">YOUR-P12-NAME-AND-EXTENSION</string>
<string name="digime_p12_password">YOUR-P12-PASSWORD</string>
```

Once added to the project resources, these can be referenced in your manifest file:

```xml
<application>
...
    <meta-data android:name="me.digi.sdk.Contracts" android:value="@string/digime_contract_id"/>
    <meta-data android:name="me.digi.sdk.AppId" android:value="@string/digime_application_id"/>
    <meta-data android:name="me.digi.sdk.AppName" android:value="@string/app_name"/>
    <meta-data android:name="me.digi.sdk.Keys" android:value="@string/digime_p12_filename"/>
    <meta-data android:name="me.digi.sdk.KeysPassphrase" android:value="@string/digime_p12_password"/>
...
</application>
```
(Specifying your app name is optional.)


The SDK needs to communicate with our servers, as such, it requires the `INTERNET` permission. Add this to your manifest file if it's not already there:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```


Since DigiMeClient calls out to _digi.me_ app to let the user authorize your request for data, you need to add the following `intent-filter` to your manifest file:
 
 ```xml
<intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.LAUNCHER" />
</intent-filter>
 ```

## Callbacks & Responses
 
Digi.me SDK is built to be asynchronous and thread-safe and as such it provides a couple of mechanisms of redirecting results back to the application.
For that purpose the SDK provides the **SDKCallback** interface and the **SDKListener** interface. 

Both of them are interchangeable and can be used depending on preference.
 
### SDKCallback

Each call has an optional `SDKCallback` parameter which can either be passed or nulled, depending on whether you're using the callback or listener pattern. `SDKCallback` wraps the response type of the given call. For example:

```java
import me.digi.sdk.core.DigiMeClient;
import me.digi.sdk.core.SDKCallback;
import me.digi.sdk.core.errorhandling.SDKException;
import me.digi.sdk.core.SDKResponse;

DigiMeClient.getInstance().getFileList(new SDKCallback<CAFiles>() {
    @Override
    public void succeeded(SDKResponse<CAFiles> result) {
                CAFiles files = result.body;
    }
            
    @Override
    public void failed(SDKException exception)  {
        //Handle exception or error response
    }
});
```

### SDKListener

`SDKListener` provides a central listening pipe for all the relevant SDK events.
 
To start listening you must implement the `SDKListener` interface (most frequently in your Launch Activity) and register it with the DigiMeClient (for example, in the `onCreate` method of your Launch Activity).

The listener methods represent the success/failure callbacks of the various SDK methods.
 
```java
public class MainActivity extends ... implements SDKListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ...
        DigiMeClient.getInstance().addListener(this);
    }
}
```

## Authorization

To start getting data into your application, you'll need to authorize a session.

### Authorization Flow

The authorization flow is separated into two phases:

1. Initialize a session with the digi.me API (returns a `CASession` object)

2. Authorize session with the digi.me app and prepare data if the user accepts.

SDK starts and handles these steps automatically by calling the `authorize(Activity, SDKCallback)` method.
This method expects a reference to the calling activity and optionally a callback.

```java
DigiMeClient.getInstance().authorize(this, new SDKCallback<CASession>() {
    @Override
    public void succeeded(SDKResponse<CASession> result) {
                
    }

    @Override
    public void failed(SDKException exception) {

    }
});
```

On success, it returns a `CASession` object in your callback, which encapsulates the session data required for further calls.

Since `authorize()` automatically calls into digi.me app, you'll need some way of handling the switch back to your app.
You will accomplish this by overriding `onActivityResult` for your Activity.

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    DigiMeClient.getInstance().getCAAuthManager().onActivityResult(requestCode, resultCode, data);
}
```

### `authorize()` Specifics

If you're using the SDKListener interface instead of callbacks, it will trigger these events:

```java
void sessionCreated(CASession session);
void sessionCreateFailed(SDKException reason);

/*
 * User approved in the digi.me app and data ready
 */
void authorizeSucceeded(CASession session);
/*
 * User declined 
 */
void authorizeDenied(AuthorizationException reason);
/*
 * Activity passed a wrong request code, most likely from another application
 */
void authorizeFailedWithWrongRequestCode();
```

### Guest Authorization Flow

The guest authorization flow is separated into two phases:

1. Initialize a session with digi.me API (returns a `CASession` object)

2. Authorize session with digi.me web app for guests and prepare data if the user accepts.

Our SDK orchestrates this when you call the `authorizeGuest(Activity, SDKCallback)` method.
This method expects a reference to the calling activity and optionally a callback.

```java
DigiMeClient.getInstance().authorizeGuest(this, new SDKCallback<CASession>() {
    @Override
    public void succeeded(SDKResponse<CASession> result) {

    }

    @Override
    public void failed(SDKException exception) {

    }
});
```

On success, it returns a `CASession` in your callback, which encapsulates the session data required for further calls.

The switch back to your activity and the format of the (optional) callback is the same as for the regular authorize(Activity, SDKCallback) flow.

## Fetching Data

Upon successful authorization, you can request user's files. 
To fetch the list of available files for your contract, do the following:

```java
 /* @param callback reference to the SDKCallback<CAFiles> or null if using SDKListener
  */
DigiMeClient.getInstance().getFileList(callback)
```

Upon success, `DigiMeClient` returns a `CAFiles` object which contains a single field `fileIds`, a list of file IDs.

You can then use the returned file IDs to fetch the file data in JSON format:

```java
 /* @param fileId ID of the file to retrieve
  * @param callback Reference to the SDKCallback<JsonElement> or null if using SDKListener
  */
DigiMeClient.getInstance().getFile(fileId, callback)
```

Generally, you'll want to parse the returned JSON for a `fileContent` key. The value of this will, in most cases, be of JSON format and you can run it straight into a JSON parser.

The exception to this rule is when handling raw data, in which case you'll also want to parse the `fileMetadata` key (also JSON), and then read `mimeType` from it. You should use this mime type to determine the best course of action for the bytes contained in `fileContent`.

### Handling Failures & Automatic Exponential Backoff
 
Due to the asynchronous nature of Private Sharing, it is possible for our servers to return a 404 when requesting a file. 

404 errors in this context indicate that **the file is not yet ready**. In other words, our servers are still in the process of copying and encrypting the data requested by your session.
 
The digi.me SDK handles those errors internally and retries such requests with an exponential backoff policy. 
The defaults are set to 3 retries with base lower interval of 500ms.

**In the event that content is not ready even after retrying, SDK will return an exception to the appropriate callback/listener.**

All of these parameters can be adjusted globally, including toggling the backoff policy on and off.

Connection timeout in seconds:
```java
    int globalConnectTimeout;
```

Connection read/write IO timeout in seconds:
```java
    int globalReadWriteTimeout;
```

Controls retries globally, toggling automatic retries on/off:
```java
    boolean retryOnFail;
```

Minimal base delay for retries:
```java
    long minRetryPeriod;
```

Toggle exponential backoff policy on/off:
```java
    boolean retryWithExponentialBackoff;
```

Maximum number of times to retry before failing. 0 uses per call defaults, >0 sets a global hard limit. Defaults to 0:
```java
    int maxRetryCount;
```


These configuration options are set statically on `DigiMeClient`:

```java
    // Set base delay to 1000 ms
    DigiMeClient.minRetryPeriod = 1000;
```

### Fetching Account Metadata

DigiMeSDK also provides relevant metadata about any **service accounts** linked to returned file content.
You can fetch account details after obtaining a valid authorized session key with:

```java
 /* 
  * @param callback         reference to the SDKCallback<CAAccounts> or null if using SDKListener
  */
DigiMeClient.getInstance().getAccounts(callback)
```

Upon success, DigiMeClient returns a `CAAccounts` object which contains `List<>` of `CAAccount` objects.

Among others, most notable properties of`CAAccount` object are `service.name`, the name of the underlying service, and `accountId`, the identifier which can be used to link the returned entities to a specific account.

### Fetching Data in a Given Time Range

Every Contract will define a time range that restricts your data access. The default `authorize(Activity, SDKCallback)` method will provide access to the whole of this time range.
Depending on your context or whether you have previously cached data, it may be desirable to only request data within a narrower time range (less data == faster response).
Use this alternative `authorize` method to achieve this:

```java
authorize(@NonNull Activity activity, TimeRange timeRange, @Nullable SDKCallback<CASession> callback)
```

Create your `TimeRange` with one of these:

```java
public static TimeRange from(@NonNull Long from)
public static TimeRange priorTo(@NonNull Long priorTo)
public static TimeRange fromTo(@NonNull Long from, @NonNull Long to)
public static TimeRange last(int x, @NonNull Unit unit)
```

The timestamps are in the UNIX EPOCH timestamp format, relative to UTC.
Please note that due to the fact that files are sharded by month, this is the lowest amound of granularity we can offer at this time. With this in mind, a timestamp is fuzzy to the month in which it falls.

## Resources

The digi.me developer docs contain a plethora of useful resources. You can check them out [here](https://developers.digi.me).