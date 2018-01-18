####################################################################################################
####################################################################################################
####################################################################################################
######################################### PROGUARD #################################################
####################################################################################################
####################################################################################################
####################################################################################################
-libraryjars libs
# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html
-keepattributes Signature
-keepattributes InnerClasses
#-keep #descriptor classes
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Optimization is turned off by default. Dex does not like code run
# through the ProGuard optimize and preverify steps (and performs some
# of these optimizations on its own).
#-dontoptimize
#-dontpreverify

# If you want to enable optimization, you should include the
# following:
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# Android stuff
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.billing.IInAppBillingService
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * extends android.content.Context {
    public void *(android.view.View);
    public void *(android.view.MenuItem);
}


-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }
-keep class com.actionbarsherlock.** { *; }
-keep interface com.actionbarsherlock.** { *; }
-keep class com.google.zxing.** { *; }
-keep interface com.google.zxing.** { *; }
-keep class com.google.gson.** { *; }
-keep interface com.google.gson.** { *; }
-keep class org.bouncycastle.** { *; }
-keep interface org.bouncycastle.** { *; }
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-keep class okio.** { *; }
-keep interface okio.** { *; }
# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version. We know about them, and they are safe.
-dontwarn android.support.**
-dontwarn com.google.ads.**
-dontwarn okio.**
-dontwarn org.bouncycastle.**
-dontwarn javax.**