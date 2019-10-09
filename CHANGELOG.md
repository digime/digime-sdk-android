Change Log
==========

Version 1.3.2 *(TBD)*
----------------------------

 * Deprecated `DigiMeClient.createSession(@Nullable SDKCallback<CASession>callback)` method -
 to `DigiMeClient.authorize(@NonNull Activity activity, @Nullable SDKCallback<CASession> callback)`
 does it for you internally. Deprecated method will become private in 2.0 release