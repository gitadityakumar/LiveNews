# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.perpetuitylab.livenews.**$$serializer { *; }
-keepclassmembers class com.perpetuitylab.livenews.** { *** Companion; }
-keepclasseswithmembers class com.perpetuitylab.livenews.** { kotlinx.serialization.KSerializer serializer(...); }

# Compose
-keepclassmembers class * { @androidx.compose.runtime.Stable <fields>; }

# Media3 ExoPlayer
-dontwarn com.google.android.exoplayer2.**
-dontwarn androidx.media3.**

# WebView JavaScript Interface
-keepclassmembers class com.perpetuitylab.livenews.web.M3u8JavascriptBridge { @android.webkit.JavascriptInterface <methods>; }

# Keep data classes used by DataStore
-keepclassmembers class com.perpetuitylab.livenews.data.** { *; }
