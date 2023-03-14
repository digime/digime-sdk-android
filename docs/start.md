![](https://securedownloads.digi.me/partners/digime/SDKReadmeBanner.png)

<p align="center">
    <a href="https://developers.digi.me/slack/join">
        <img src="https://img.shields.io/badge/chat-slack-blueviolet.svg" alt="Developer Chat">
    </a>
    <a href="../../LICENSE">
        <img src="https://img.shields.io/badge/license-apache 2.0-blue.svg" alt="Apache 2.0 License">
    </a>
    <a href="#">
    	<img src="https://img.shields.io/badge/build-passing-brightgreen.svg">
    </a>
    <a href="https://kotlinlang.org">
        <img src="https://img.shields.io/badge/language-kotlin/java-ff69b4.svg" alt="Kotlin/Java">
    </a>
    <a href="https://developers.digi.me">
        <img src="https://img.shields.io/badge/web-digi.me-red.svg" alt="Web">
    </a>
    <a href="https://digime.freshdesk.com/support/home">
        <img src="https://img.shields.io/badge/support-freshdesk-721744.svg" alt="Support">
    </a>
</p>

# Getting started

## Requirements

### Development

- Android Studio 4.2.2 or newer.
- Gradle 7.3.2 or newer.
- Kotlin 1.5.1 or newer. **\***

**\*** The SDK is written entirely in Kotlin, but is fully compatible with Java projects.

### Deployment

- Android 5.1 or newer (API Level 21).

## Installation

### Gradle/Maven

1. Ensure you have the JCenter repository in your root `build.gradle` file.<br>(This is
   automatically added to new Android Studio projects.)

2. Include the digi.me SDK as a dependency in your app `build.gradle` file:

   `implementation "me.digi:sdk:5.0.0"`

### Manual

1. Download the source code for the SDK.
2. In Android Studio, import the SDK as a module.
3. In your app `build.gradle`, include the module as a dependency:

   `implementation project(":sdk")`

### Obtaining your Contract ID, Application ID & Private Key

To access the digi.me platform, you need to obtain an AppID for your application. You can get yours
by filling out the registration form [here](https://go.digi.me/developers/register).

In a production environment, you will also be required to obtain your own Contract ID and Private
Key from digi.me support. However, for demo purposes, we provide example values. You can find
example keys in
our [example applications](https://github.com/digime/digime-sdk-android/tree/master/examples).

### Initializing the SDK

1. Edit Your Resources and Manifest

Create strings for your App ID, Contract ID, Private key and Protocol schema.
For example, if your App ID is 1234, Contract ID is 56789 and Private key is 101112 your code looks like the following:

```xml
<string name="digime_app_id">1234</string>
<string name="digime_contract_id">fb1234</string>
<string name="digime_private_key">56789</string>
<string name="protocol_schema">callback-1234</string>
```

Also, add ConsentBrowserActivity to your Android manifest.

```xml

<activity android:name="me.digi.sdk.ui.ConsentBrowserActivity" android:exported="true"
    android:launchMode="singleTop">

    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="digime-ca" />
        <data android:host="@string/protocolSchema" tools:ignore="AppLinkUrlError" />
    </intent-filter>

</activity>

```
2. Add onActivityResult
You are required to forward invocations of `onActivityResult` through to the SDK so that it may
process responses. In any activity that will be responsible for invoking methods on the SDK,
override `onActivityResult` as below:

```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, responseCode, data)
    AppCommunicator.getSharedInstance().onActivityResult(requestCode, responseCode, data)
}
```

3. Configure DigiMe client

After that we will configure DigiMe client. It's the object you can primarily interface with to use
the SDK. It is instantiated with a context, and a `DigiMeConfiguration` object.

**The provided context should always be main application context.**

The `DigiMeConfiguration` object is instantiated with you `AppID`, `Contract ID` and `Private Key`.

```kotlin
val client: DigiMe by lazy {

    val configuration = DigiMeConfiguration(
        "your_app_id",
        "your_contract_id",
        "your_private_key"
    )

    Init(requireContext().applicationContext, configuration)
}
```

### Using the SDK

* Use digi.me to [request data](read-data-overview.html).
* Use digi.me to [write data](write-data-overview.html).