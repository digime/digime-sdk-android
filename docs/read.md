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



# Reading data

Before data can be read, we should already have a user access token for this user.

If not, you'll need to [authorize](authorize.md) them first, and ask them to [onboard any extra services](onboard.md) to provide the data you're requesting. Make sure the user access token is for a **READ** contract.

### Getting a Updated Session

To start reading user data, we first need to obtain session:

```kotlin
client.readSession { isSessionUpdated, error ->	...	}
```

The session received has been updated, and can now be used to query data.

### Reading all files

Using the session received above, we can trigger **readAllFiles()** to read all available files from this user.

```kotlin
client.readAllFiles(userAccessToken, { file, error ->
			// Handle each downloaded file here.
}) { fileList, error ->
      // Any errors interupting the flow of data will be directed here, or null once all files are retrieved.
    	// The file list here will represent the complete list of files that are downloaded.
}
```

*In Android Studio, hover with your mouse over the method name to see its full description*

### Selecting files

If you'd like more control over the downloading of the files, we can call **readFileList()** to see all available files from the user:

```kotlin
client.readFileList(userAccessToken) { fileList, error ->	...	}
```

*In Android Studio, hover with your mouse over the method name to see its full description*

Response contains a `fileList` and `error` of each user service onboarded.

You can then download the files manually using **readFile()**:

```kotlin
client.readFile(fileId) { file, error ->	...	}
```

*In Android Studio, hover with your mouse over the method name to see it's full description*