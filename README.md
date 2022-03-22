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

# Introduction

The digi.me private sharing platform empowers developers to make use of user data from thousands of sources in a way that fully respects a user's privacy, and whilst conforming to GDPR. Our consent driven solution allows you to define exactly what terms you want data by, and the user to see these completely transparently, allowing them to make an informed choice as to whether to grant consent or not.

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

## Example applications

You can check out an [example applications](https://github.com/digime/digime-sdk-android/tree/master/examples) which uses the digi.me SDK.

## Getting started

Please check out the [getting started guide](https://digime.github.io/digime-sdk-android/#/start) to start using digi.me.

## Contributions

digi.me prides itself in offering our SDKs completely open source, under the <a href="./LICENCE.md">Apache 2.0 Licence</a>; we welcome contributions from all developers.

We ask that when contributing, you ensure your changes meet our <a href="">Contribution Guidelines</a> before submitting a pull request.

## Further Reading

We highly encourage you to explore the <a href="https://digime.github.io/digime-sdk-android/">Documentation</a> for more in-depth examples and guides, as well as troubleshooting advice and showcases of the plethora of capabilities on offer.

Additionally, there are a number of example apps built on digi.me in the examples folder. Feel free to have a look at those to get an insight into the power of Private Sharing.
