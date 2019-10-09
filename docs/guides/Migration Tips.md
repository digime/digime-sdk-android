![](https://securedownloads.digi.me/partners/digime/SDKReadmeBanner.png)

<p align="center">
    <a href="https://bit.ly/2LM4GFS">
        <img src="https://img.shields.io/badge/chat-slack-blueviolet.svg" alt="Developer Chat">
    </a>
    <a href="../../LICENSE">
        <img src="https://img.shields.io/badge/license-apache 2.0-blue.svg" alt="Apache 2.0 License">
    </a>
    <a href="#">
    	<img src="https://img.shields.io/badge/build-passing-brightgreen.svg" 
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

<br>

# Migration Tips
Here are some key SDK changes that may help you get to grips with it if you are migrating from version 1.x.x:

1. The `DigiMeClient` singleton no longer exists. It has been replaced by `DMEPullClient` and `DMEPushClient`.

2. In your activity, you still need to forward `onActivityResult` to the SDK. This is now done by forwarding the event to `DMEAppCommunicator`:
 
	```kotlin
	override fun onActivityResult(requestCode: Int, responseCode: Int, data: Intent?) {
    	super.onActivityResult(requestCode, responseCode, data)
    	DMEAppCommunicator.getSharedInstance().onActivityResult(requestCode, responseCode, data)
}
	``` 

3. Each type of client has to be instantiated with a corresponding configuration object. The contractID, applicationID and private key hex must now be supplied here:

	```kotlin
	val privateKeyHex = DMECryptoUtilities.privateKeyHexFrom("p12-filename", "p12-password")
	val pullConfig = DMEPullClientConfiguration("contract-id", "app-id", privateKeyHex)
	val pullClient = DMEPullClient(context, pullConfig)
	```
	The P12 file should be placed in your project assets.

4. We recommend you turn on debug logging while evaluating the SDK, which can be done via:

	```kotlin
	// This will add extra logging to the console.
	pullConfig.debugLogEnabled = true
	```
5. We no longer support delegate based approach for SDK callbacks. Instead, a callback block should be passed. For example:

	```kotlin
	// To start the authorisation flow.
	pullClient.authorize(context) { session, error ->
        
        // If there's no error - you can now get accounts
        // and/or start downloading session data.
        
   }
	```