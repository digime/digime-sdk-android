![](https://securedownloads.digi.me/partners/digime/SDKReadmeBanner.png)

<p align="center">
    <a href="https://developers.digi.me/slack/join">
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

`DMEScope` is comprised of three properties. `timeRanges` is a list of `DMETimeRange` objects (more on this below) to limit the breadth of time for which applicable data will be returned. `serviceGroups` is a list of `DMEServiceGroup` objects, which nests it's own list of `DMEServiceType` objects, nesting `DMEObjectType` objects; this is also detailed below. `context` is not currently used and should be left as the default.

### _Scoping by time range_:

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

### _Scoping by service group, service type or object type:_

To restrict scope at an object level, your scope must be 'fully described'; that is to say that a service group most comprise at least one service type, which must comprise at least one object type. Furthermore, a service group may only contain service types belonging to it and said service types may only contain object types belonging to them.

Service Groups, Service Types and Objects are all listed [here](https://developers.digi.me/reference-objects) in the developer documentation. Their relationships are also shown (what belongs to what).

Below is an example of a valid scope to retrive only `Playlists` and `Followed Artists` from `Spotify`:

```kotlin
val playlistObjectType = DMEServiceObjectType(403) // 403 is the ID for a Playlist object.
val followedArtistObjectType = DMEServiceObjectType(407) // 407 is the ID for a Followed Artist object.

val spotifyServiceType = DMEServiceType(19, listOf(playlistObjectType, followedArtistObjectType)) // 19 is the ID for Spotify.

val entertainmentServiceGroup = DMEServiceGroup(5, listOf(spotifyServiceType)) // 5 is the ID for Entertainment.

val scope = DMEScope() // This scope is valid, as no restrictions have been imposed.
scope.serviceGroups = listOf(entertainmentServiceGroup) // The scope is still valid, as it conforms to the rules listed above.
```

## Providing `DMEScope`

When calling `authorize` on your `DMEPullClient`, simply pass in your `DMEScope` object:

```kotlin
val scope = DMEScope(listOf(DMETimeRange(last = "6m")))
pullClient.authorize(this, scope)
```
*NB: `this` represents the activity which is setup to forward `onActivityResult`, as described in [Getting Started](./getting-started.html).*

The data received from any subsequent calls to `getSessionData` will be limited by the scope of the session defined above.