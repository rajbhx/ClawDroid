# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.clawdroid.android.data.network.dto.** { *; }
-keep class com.clawdroid.android.data.local.entity.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ONNX Runtime
-keep class ai.onnxruntime.** { *; }
-dontwarn ai.onnxruntime.**

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Terminal
-keep class com.termux.terminal.** { *; }
-keep class com.termux.view.** { *; }

# JavaScript Interface
-keepclassmembers class com.clawdroid.android.JsBridge {
    @android.webkit.JavascriptInterface <methods>;
}
-keepclassmembers class com.clawdroid.android.AgentBridge {
    @android.webkit.JavascriptInterface <methods>;
}
