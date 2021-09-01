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

# Reading data - Step by step guide

Requesting user data using digi.me is easy!

On this page, we will learn how to receive data from your users using digi.me Private Share platform.

In order to access the digi.me platform, you need to obtain an application ID, a contract ID for reading data, and its private key.

Please check [Getting Started](#) for more details.



### 1. Available services

To start reading user data, we first need to ask the user to onboard a service.

To see a list of available services for user to onboard: 

```kotlin
client.availableServices { servicesResponse, error ->	...	}
```

*hover with your mouse over the method name to see it's full description*

See [Available Services](#) for more explanation.



### 2. Onboarding and Authorization

To start reading user data, we need to ask the user to onboard a service and authorize to access it.

*if you already have a user access token for this user from another contract, you will still need to go through this process. Make sure to include any user access token you already have so we can link to the same library*



```kotlin
client.authorizeAccess(
                    fromActivity = activity,
                    credentials = credentials, // if one exist to link to the same library
                    serviceId = serviceId
                ) { response, error -> ... }
```

*hover with your mouse over the method name to see it's full description and see example application for more details*



### (Optional) Onboarding more services

If you need to ask the user to onboard more services, you can call:

```kotlin
 client.addService(activity, serviceId, userAccessToken) { error ->	...	}
```

After this, user has onboarded and finished with the authorization, the callback will be provided. 



### 3. Start a Read All Files

When user has onboarded all the services you require, we can kick off reading files:

```kotlin
client.readAllFiles(userAccessToken, { file, error ->	...	}) { fileList, error ->
        ...
    }
```

*hover with your mouse over the method name to see it's full description*

And that's it, you've successfully received data from the user using digi.me!

Next time you want to get data from the same user, you can reuse the User Access Token in `authorizeAccess` method call.

Make sure to see [example application](#) for detailed implementation. 