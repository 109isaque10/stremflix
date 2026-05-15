# === Keep Kotlin Metadata for Hilt/KSP ===
-keepattributes KotlinMetadata, RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations

# Keep Kotlin metadata for serialization
-keepattributes InnerClasses, EnclosingMethod

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.* { *; }
-keep class * implements dagger.hilt.internal.aggregatedroot.codegen.* { *; }

-keep class javax.inject.** { *; }
-keep class javax.annotation.** { *; }

-keep class com.stremflix.**Hilt* { *; }
-keep class com.stremflix.Hilt_* { *; }

# Keep Room entities and DAOs
-keep class com.stremflix.data.local.entity.** { *; }
-keep class com.stremflix.data.local.dao.** { *; }
-keepattributes Signature

-keep,allowobfuscation,allowshrinking @dagger.hilt.EntryPoint class *
-keep,allowobfuscation,allowshrinking @dagger.hilt.android.EarlyEntryPoint class *

# Keep Kotlinx Serialization metadata
-keepattributes RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations
-keepclassmembers class kotlinx.serialization.** { *; }
-keep class kotlinx.serialization.internal.** { *; }

# Keep Compose compiler metadata
-keep class androidx.compose.** { *; }
-keepattributes RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations

# Keep ViewModel metadata for Hilt
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

-dontwarn androidx.window.extensions.area.ExtensionWindowAreaPresentation
-dontwarn androidx.window.extensions.core.util.function.Consumer
-dontwarn androidx.window.extensions.core.util.function.Function
-dontwarn androidx.window.extensions.core.util.function.Predicate