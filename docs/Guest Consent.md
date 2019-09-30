# Guest Consent

Whilst the true power of the digi.me Private Sharing platform lies in the user's digi.me, and as such highly encourage developers to properly utilise this, we do facilitate your app accessing the data of users, without the digi.me app. This feature is known as *Guest Consent* or *One Time Private Sharing*. It is effecively the digi.me onboarding process, within a web browser.

## How to Use

Guest Consent is disabled by default. This is because we believe utilising digi.me properly provides a superior user experience, and encourage it.

If the digi.me app is not installed, the default behaviour is to open the relevant app store listing for the user to subsequently download and set up digi.me.

Guest Consent can be enabled when configuring your `DMEPullClient` by setting the `guestEnabled` property of `DMEPullConfiguration` to `true`. This changes the behaviour to, in the event that the digi.me app isn't installed, use the web Guest Consent flow.

Note that even if Guest Consent is enabled, if the digi.me app is present on the device, we will always process the consent request through the native app.


## Considerations

Guest Consent removes the need for your user to have the digi.me app on their device. Whilst this might sound like a positive, there are a number of drawbacks to Guest Consent.

#### Drawbacks:

- Guest Consent is a notably slower experience. This is due to the fact that we must virtualise a cloud storage medium in order to facilitate a data conduit to you, the data consumer.
- Any data the user imports is lost after your SDK session expires. Data is temporarily cached in memory (RAM is volatile, which is important as we don't see, touch or hold user data), as such, this is lost once the corresponding session container is invalidated.
- Ongoing shares are not available through Guest Consent, due to aforementioned loss of data once a session has lapsed.
- Whilst data is transferred over HTTPS with full TLS 1.2, we are unable to verify the server's certificate client side and as such, we can't guarentee the integrity of the data served. The digi.me client is fully certificate pinned and has a strict trust policy enforced.