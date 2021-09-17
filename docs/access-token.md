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



# User Access Token

In order to write or read data from the users digi.me, we will need an user access token to their digi.me. Once we have this, we can read up to date data of the services the user has onboarded to their digi.me.

User access tokens are obtained once a user successfully goes through a [authorization](authorize.md).

Separate user access tokens are required for each contract, so it is possible to have multiples for one user if you require to read and write data.

### Access Token Expiry

The access token will eventually expire. When you first create it, a timestamp in which it will expire will be returned. The SDK will attempt to refresh it automatically when you use it next. If a refresh is needed, it will return the new access token to you.

If the refresh is unsuccessful, you'll need to go though the authorization process again with the user.