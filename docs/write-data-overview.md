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

<br>

# Writing data - Step by step guide

Use this guide to write data to your users library in digi.me.

In order to access the digi.me platform, you need to obtain an application ID, a contract ID for reading data, and its private key.

Please check [Getting Started](#) for more details. 



### 1. Onboarding and Authorization

Before we can write data to user, we need to go through the authorization flow and obtain a user access token.

*if you already have a user access token for this user from another contract, you will still need to go through this process. Make sure to include any user access token you already have so we can link to the same library*

```kotlin
client.authorizeAccess(fromActivity = activity, credentials = credentials) { response, error -> ... }
```

*hover with your mouse over the method name to see it's full description*



### 2. Write data

Once you have the `postboxId`, `publicKey `and the `accessToken` from the step above, we can write data.

Please take a look at write data to find out more about how to format the data to write.

Also see [example application](#) for detail usage.

```kotlin
 client.write(
     writeDataPayload,
     accessToken
 ) { response:	DataWriteResponse?, error ->	...	}
```

*hover with your mouse over the method name to see it's full description*

If we need to write other files to the users in the future, we can keep writing as long as the user access token is valid.



### 3. Reading files back out

If you've written data to the user, you can read it back out using the [same process for reading user data](#). You will need a new contract which reads out raw data, so please contact digi.me [here](#) to get yours.

Make sure you pass in the user access token which you obtained in step 1 above when authorizing the new contract, so it can link to the same library.

Make sure to see [example application](#) for detailed implementation. 