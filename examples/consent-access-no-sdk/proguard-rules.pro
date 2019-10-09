-dontwarn rx.**
-dontwarn retrofit2.**
-dontwarn javax.naming.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# OkHttp 3
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

-dontwarn okio.**

-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

-keep class me.digi.examples.ca_no_sdk.service.models.File { *; }

-keep class me.digi.sdk.crypto.** { *; }
-keep class org.spongycastle.jcajce.provider.**
-keep class org.spongycastle.jce.provider.PKIXCertPathValidatorSpi { *; }