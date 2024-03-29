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

# Error Handling

Whilst using the SDK, you may encounter a number of errors. Some of these, we will attempt to recover from on your behalf, however, there are a large number that will require you to take some kind of action.

## Error Types

All invocations of the SDK that can return some form of error will return an instance of `Error` (or one of it's subclasses. These are detailed in [Error.kt](#); all errors come with a message explaining the reason behind them. There are 3 subclasses: `SDKError`, `AuthError` and `APIError`. Let's break them down:

### SDKError

These errors generally result from the misconfiguration of the SDK in some way, or other problems derived from the SDK's implementation within your app. The error messages are designed to be self-explanatory, so not all will be covered here. For some examples of the most common issues, and troubleshooting steps, see [Troubleshooting Common Issues](#troubleshooting-common-issues). This type of error will require address by the integrator of the SDK, due to being caused by an error on their part.

### APIError

Just as `SDKError` represents issues on the integrator's side, `APIError` reflects problems with the digi.me service. The one exception to this is `APIError.NetworkUnreachable()`; as you'd expect, this one is caused by a lack of internet connection on the device, and is out of our control.

When the digi.me service returns an error, this will be passed back as `PIError.Server(<error message>)`. We handle this internally within the SDK, retrying requests where appropriate, as per the retry rules you set in your `ClientConfiguration`. If, after exhausting this, we are unable to resolve the error, it will be passed onto you.

Very rarely, `APIError.Generic()` may be used. In such instances, we are unable to deduce an error from our server's response, and as such are unable to pass one on. A HTTP 500 status would be one such use case.

Most server side errors are short lived, so the recommended course of action is to try again a bit later, but in the case of a persisting error, please contact digi.me support.

### AuthError

This should be the most common error encounter. In the event that a user declines to grant their consent, you will receive a `AuthError.Cancelled()`; you may handle this in a way that's appropriate to your app.

## Retrying Requests

As touched on above, the SDK will retry any requests it deems potentially recoverable. The logic governing this can be influenced by properties on `ClientConfiguration`. Namely, the following:

```kotlin
// How long the SDK will wait for a response from the server.
var globalTimeout: Int = 25

// Whether the SDK should attempt to recover errors where possible.
var retryOnFail: Boolean = true

// How long to wait (in milliseconds) before retrying a request.
var retryDelay: Int = 750

// Whether to use an exponential back-off approach to retrying requests.
var retryWithExponentialBackOff: Boolean = true

// How many times a request should be retried before ultimately failing.
var maxRetryCount: Int = 5
```
You can also see the defaults assigned to each property above, should you need to explicitly override this.

## Troubleshooting Common Issues

Below are the 5 most common errors you could run into, and the steps you should take to rectify them:

#### `AuthError.InvalidSession()`:

**Encountered**: If you try to fetch data after a session has expired.<br>
**Resolution**: Invoke `DigiMe.authorize` again to obtain a new session. This may require the user to re-consent if you do not have an ongoing share agreement in place with them.

#### `APIError.Server("NoSuchApplication")`:

**Encountered**: If you provide an invalid Application ID when configuring your client.<br>
**Resolution**: Ensure the passed ID is the one you received from digi.me support when registering.

#### `APIError.Server("NoSuchContract")`:

**Encountered**: If you provide an invalid Contract ID when configuring your client.<br>
**Resolution**: Ensure the passed ID is either the sandbox contract provided in the [README](), or, in production, the ID you received from digi.me support.

## Further Issues

If, after reading this section, your issue persists, please contact digi.me developer support. The easiest way to do this is via our [Slack Workspace](https://digime-api.slack.com/). Here you can speak directly with other developers working with us, as well as digi.me's own development team.

