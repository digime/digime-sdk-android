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



# Onboarding Additional Services

Once a user has authorized, you can onboard additional services to their library before reading them all at once.

To trigger new service onboard, you can do the following:

```kotlin
 client.addService(activity, serviceId, userAccessToken) { error ->	...	}
```

The `url` returned might look something like this:

```https://api.digi.me/apps/saas/onboard?code=<code>&callback=<callback>&service=<service-id>```

You will be asked to onboard the service, and consent to share the requested data.

At the end of the process, the `callback` provided above will be called with the following of extra query parameters:

| Parameter | Description | Returned Always |
|-|-|-|
| `success` | Whether the call was successful. `true` or `false` | Yes |
| `errorCode` | If there was an error, an error code will be returned. Please see the error code section for a list of possible errors. | Yes |

