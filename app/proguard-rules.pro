-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

-keep class com.neotermux.** { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class kotlinx.serialization.** { *; }

-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe
-dontwarn org.bouncycastle.**
-dontwarn com.fasterxml.jackson.**
-dontwarn org.eclipse.jgit.**
-dontwarn org.apache.sshd.**

-keep class * extends java.io.InputStream { *; }
-keep class * extends java.io.OutputStream { *; }

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep Hilt components
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }