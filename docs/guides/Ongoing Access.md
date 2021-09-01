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

# Ongoing Access


## Introduction

Ongoing Access allows continuous access to user's data without the use of digi.me app **after** initial consent has been given.*

From a developer's perspective, the authorization process is almost identical to regular authorization. Under the hood we use OAuth 2.0 with JWT, and JWS with RSA signing and verification to issue a medium lived, refreshable OAuth token, which is used to re-query user's data without the need to leave your app.

Here is a simplified sequence diagram of how the OAuth flow is implemented:
![](https://securedownloads.digi.me/partners/digime/OngoingAccess.png)

*The SDK handles all of this for you.*

Ongoing Access is for you if:

* You need regular access to user's data
* You are using an ongoing contract

\* *`refreshTokens` used to refresh `accessTokens` do eventually expire (for example - 30 days). When this happens, user will need to be directed back to the digi.me app for re-authorization.*


## How to use

### Requesting Consent

Simply use the new `authorizeOngoingAccess` method on an instance of `DMEPullClient`:

```kotlin
pullClient.authorizeOngoingAcess(activity) { session, credentials, error ->

}
```

*See [Fetching Data](./getting-started.md#5-fetching-data) for more details on post-consent steps.*

You may notice that upon completion of this method, the SDK supplies a `DMEOAuthToken`. This is **key** to access restoration and we recommend you store this - you will need it later.

Our recommendation would be to save it into the device keychain or other secure storage.


### Access Restoration

If you have previously obtained user's consent, and are in posession of a `DMEOAuthToken`, you can get data from your users without them having to leave your app.

To do this, simply call the following method on a **new** `DMEPullClient` instance, where `cachedCredentials` is the `DMEOAuthToken` you cached:

```kotlin
pullClient.authorizeOngoingAcess(activity, cachedCredentials) { session, credentials, error ->

}
```

One important thing to note here - the `DMEAuthToken` returned in `completion` may not be the same token you have passed in. This is because the SDK will try to automatically refresh an `accessToken` using a `refreshToken` (both of these contained in `DMEOAuthToken`), generating a new `DMEOAuthToken`. This means you should replace your old token with the one you receive in `completion`.

Under the hood the SDK will trigger data query using the `DMEOAuthToken` which, if valid, will start preparing user's protected resources for access. This time however, the user will remain in your app.

#### Configuration Options

There is a new property available on `DMEPullConfiguration` object - `autoRecoverExpiredCredentials`. This defaults to `true`, which means that if the `refreshToken` contained in `DMEOAuthToken` has expired, the user will be directed to the digi.me app, so that this can be regenerated.

If you wish to direct the user back to digi.me app manually, set this property to `false`.

### Anything else?

In the examples above we have used a method without a `scope` parameter. Scoping is useful when you want to only access a subset of data, such as data generated after certain date. If you would like to learn more - see [Scoping](./scoping.md).

If you need help setting up the rest of the flow, or simply more detail, then head on over to [Getting Started](./getting-started.md).