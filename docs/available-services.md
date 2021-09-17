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



# Getting available services

The SDK provides a method which you can use to determine what services can be onboarded for your contract. The Contract you pass in must be a **READ** contract, which means it's a contract that asks for users data.

### Examples

The most basic initialization:

```kotlin
client.availableServices { servicesResponse, error ->	...	}
```

For contract that only asks for Twitter data, the response might be:

```json
[{
    "name": "Twitter",
    "publishedDate": 1518428711000,
    "publishedStatus": "approved",
    "reference": "twitter",
    "id": 2,
    "serviceGroups": [
        {
            "id": 1
        }
    ],
    "countries": [],
    "homepageURL": "https://twitter.com",
    "title": "Add your Twitter...",
    "subTitle": "Your tweets, likes and mentions",
    "resources": [
        {
            "mimetype": "image/png",
            "resize": "fit",
            "type": 0,
            "url": "https://securedownloads.digi.me/static/development/discovery/services/twitter/icon25x25.png",
            "aspectratio": {
                "accuracy": 100,
                "actual": "1:1",
                "closest": "1:1"
            },
            "height": 25,
            "width": 25
        }
    ]
}]
```

