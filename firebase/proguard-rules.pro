-keepparameternames
-keeppackagenames
-renamesourcefileattribute SourceFile
-keepattributes Exceptions, InnerClasses, Signature, Deprecated, SourceFile, LineNumberTable,
*Annotation*, EnclosingMethod
-keep class kotlin.Metadata { *; }

# Public API lives in the root package only. A single `*` deliberately leaves the `internal`
# subpackage (and its nested packages) out, so R8 is free to obfuscate it.
-keep public class com.tealium.prism.firebase.* { public protected *; }
-keep public interface com.tealium.prism.firebase.* { public protected *; }

-keep public class com.tealium.prism.**$DefaultImpls { *; }

# ComponentDiscoveryService reads DEFAULT_FACTORY reflectively with Class.getDeclaredField, using
# the manifest entry `FirebaseKt#DEFAULT_FACTORY`. The @JvmField makes it a plain static field, so
# both the class and the field must keep their names. Keep this in sync with consumer-rules.pro.
-keep class com.tealium.prism.firebase.FirebaseKt {
    public static com.tealium.prism.core.api.modules.ModuleFactory DEFAULT_FACTORY;
}

# Kotlin targeting JVM 11 emits invokedynamic string concatenation. The consuming app's D8/R8
# desugars it, but the platform classpath used here for the library build does not contain it.
-dontwarn java.lang.invoke.StringConcatFactory
