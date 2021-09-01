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



# Writing data

Before we can write data to the user, we should already have a user access token for this user.

If not, you'll need to [authorize](authorize.md) them first. Make sure the user access token is for a **WRITE** contract.

Once you have authorized a write contract, you should have the `postboxId`, `publicKey`, and the `accessToken` for this user.

```kotlin
client.write(payload, accessToken) { response, error ->	...	}
```

A status of `delivered` will be returned if it is written to the users digi.me. Newly updated session will also be returned.

### FileMeta

This is how you should format the `data` properly:

```kotlin
val data = ... // Obtain the data you wish to post, as a ByteArray.
val metadata = ... // All submissions must be pushed with appropriate metadata. See the example apps for more details.
val mimeType = MimeType.APPLICATION_JSON // This tells how to treat your push. Please use the most appropriate mime type.
val payload = WriteDataPayload(postbox, data, metadata, mimeType)
```

