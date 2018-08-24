-optimizationpasses 5

-useuniqueclassmembernames

-allowaccessmodification

-keepattributes InnerClasses,Signature,EnclosingMethod

-keepattributes SourceFile,LineNumberTable

-renamesourcefileattribute SourceFile

-keepattributes *Annotation*

-keep class * extends java.lang.annotation.Annotation { *; }

-keep public class com.chavinchen.itoast.IToast {
    public <methods>;
}

-keep public interface com.chavinchen.itoast.IToast$AppController{ *; }

-keep public class com.chavinchen.itoast.IToastUtil{ *; }
