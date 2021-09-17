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



# Authorizing

In order to write or read data from digi.me, we first need to create an user access token for each user.
User access tokens are linked to a contract, and it is possible to create multiple tokens that has access to the same digi.me library.
Authorization is the process to obtain an user access token for the user.

### When do we need to authorize?

Authorization is needed:

* For new users. You have the option to also ask the user to onboard a service during this process.
* For an existing user working with a different contract. eg, They have shared data but now we would like to write data in their digi.me.
* For an existing user when their user access token has expired and we need to renew it.

*If you already have a user access token for this user from another contract, you will still need to go through this process. Make sure to include any user access token you already have so we can link to the same library*

```kotlin
client.authorizeAccess(activity) { response, error -> ... }
```

*In Android Studio, hover with your mouse over the method name to see its full description*

