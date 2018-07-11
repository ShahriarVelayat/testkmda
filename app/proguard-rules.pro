# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/nevercom/.local/opt/adt-bundle-linux/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontwarn com.squareup.okhttp.**
-dontwarn com.squareup.okhttp3.**

-keepattributes SourceFile,LineNumberTable

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

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

-libraryjars libs

-dontwarn com.parse.**
-dontwarn org.ksoap2.**
-dontwarn com.devspark.**
-dontwarn com.tjerkw.**
-dontwarn com.espian.**
-dontwarn org.xmlpull.**
-dontwarn bolts.**
-dontwarn com.square.**
-dontwarn okio.**
-dontwarn com.sayanpco.**
-dontwarn com.rey.material.**
-dontwarn com.astuetz.**
-dontwarn com.github.PhilJay.**
-dontwarn com.github.ozodrukh.**
-dontwarn org.apache.commons.**
-dontwarn com.github.johnkil.**
-dontwarn com.github.clans.**
-dontwarn okhttp3.**

-keep class okhttp3.** { *; }
-keep class com.parse.** { *; }
-keep class org.ksoap2.** { *; }
-keep class com.devspark.** { *; }
-keep class com.tjerkw.** { *; }
-keep class com.espian.** { *; }
-keep class org.xmlpull.** { *; }
-keep class bolts.** { *; }
-keep class com.square.** { *; }
-keep class okio.** { *; }
-keep class com.sayanpco.** { *; }
-keep class com.rey.material.** { *; }
-keep class com.astuetz.** { *; }
-keep class com.github.PhilJay.** { *; }
-keep class com.github.ozodrukh.** { *; }
-keep class org.apache.commons.** { *; }
-keep class com.github.johnkil.** { *; }
-keep class com.github.clans.** { *; }
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep public class com.google.android.gms.* { public *; }