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
- Gradle 6.7.1 or newer.
- Kotlin 1.5.1 or newer. **\***

**\*** The SDK is written entirely in Kotlin, but is fully compatible with Java projects.

### Deployment
- Android 5.1 or newer (API Level 21).

## Installation

### Gradle/Maven

1. Ensure you have the JCenter repository in your root `build.gradle` file.<br>(This is automatically added to new Android Studio projects.)
	
2. Include the digi.me SDK as a dependency in your app `build.gradle` file:

	`implementation "me.digi:sdk:4.0.3"`

### Manual

1. Download the source code for the SDK.
2. In Android Studio, import the SDK as a module.
3. In your app `build.gradle`, include the module as a dependency:

	`implementation project(":sdk")`

### Obtaining your Contract ID, Application ID & Private Key

To access the digi.me platform, you need to obtain an AppID for your application. You can get yours by filling out the registration form [here](https://go.digi.me/developers/register).

In a production environment, you will also be required to obtain your own Contract ID and Private Key from digi.me support. However, for demo purposes, we provide example values. You can find example keys in our [example applications](https://github.com/digime/digime-sdk-android/tree/master/examples).

###  Initializing the SDK

Once you have the above, we can initiate the SDK.

You are required to forward invocations of `onActivityResult` through to the SDK so that it may process responses. In any activity that will be responsible for invoking methods on the SDK, override `onActivityResult` as below:

```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
	super.onActivityResult(requestCode, responseCode, data)
	AppCommunicator.getSharedInstance().onActivityResult(requestCode, responseCode, data)
}
```

After that we will configure DigiMe client. It's the object you can primarily interface with to use the SDK. It is instantiated with a context, and a `DigiMeConfiguration` object. 

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

* Use digi.me to [request data from your users](read-data-overview.md).
* Use digi.me to [write data to your users](write-data-overview.md).