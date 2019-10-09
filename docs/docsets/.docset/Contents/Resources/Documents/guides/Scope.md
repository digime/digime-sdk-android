![](https://securedownloads.digi.me/partners/digime/SDKReadmeBanner.png)

<p align="center">
    <a href="https://bit.ly/2LM4GFS">
        <img src="https://img.shields.io/badge/chat-slack-blueviolet.svg" alt="Developer Chat">
    </a>
    <a href="../../LICENSE">
        <img src="https://img.shields.io/badge/license-apache 2.0-blue.svg" alt="Apache 2.0 License">
    </a>
    <a href="#">
    	<img src="https://img.shields.io/badge/build-passing-brightgreen.svg" 
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

# Limiting Scope

Sometimes you may only want to retrieve a subset of data. This is governed at two levels. At the top are any scope limitations dictated by your contract; these can only be changed by digi.me support so please [contact support](https://developers.digi.me/contact-us) if you want to discuss this further.

At a code level, you can restrict the scope of a Private Sharing session by passing in a custom `DMEScope`.

## Defining `DMEScope`

`DMEScope` is comprised of two properties. `timeRanges` is a list of `DMETimeRange` objects (more on this below) to limit the breadth of time for which applicable data will be returned. `context` is not currently used and should be left as the default.

### `DMETimeRange`:

`DMETimeRange` has 3 properties. `fromDate`, `toDate` and `last`. These are all optional but **at least one is required.**
<br>
Valid configurations are:

##### To restrict data to a certain point, forwards:

`fromDate`: A valid `Date` object representation of the earliest date you want data for.
<br>
`toDate`: null
<br>
`last`: null

##### To restrict data from a certain point, backwards:

`fromDate`: null
<br>
`toDate`: A valid `Date` object representation of the latest date you want data for.
<br>
`last`: null

##### To restrict data from a certain point, to a certain point:

`fromDate`: A valid `Date` object representation of the earliest date you want data for.
<br>
`toDate`: A valid `Date` object representation of the latest date you want data for.
<br>
`last`: null

##### To restrict data to a rolling period, defined by the current date:

`fromDate`: null
<br>
`toDate`: null
<br>
`last`: A string literal describing the period of time back from now you wish to allow.

*NB: Valid `last` strings take the format of a integer followed by `d`, `m`, `y`, representing days, months and years respectively. For example: `6m`.*

## Providing `DMEScope`

When calling `authorize` on your `DMEPullClient`, simply pass in your `DMEScope` object:

```kotlin
val scope = DMEScope(listOf(DMETimeRange(last = "6m")))
pullClient.authorize(this, scope)
```
*NB: `this` represents the activity which is setup to forward `onActivityResult`, as described in [Getting Started](./Getting%20Started.md).*

The data received from any subsequent calls to `getSessionData` will be limited by the scope of the session defined above.