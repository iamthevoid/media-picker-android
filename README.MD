
[<img src="lib.svg">](https://search.maven.org/search?q=g:io.github.iamthevoid.mediapicker)  

#### Features

Android Sdk ≥ ***14*** 

Minimum Dependencies 

# Picker
Simple reactive library for pick image or video. There are Rx Java 1, 2, 3 and Coroutines implementation. 
 
Usage such a simple. Next example returns `Observable<Uri>` (RxJava1), `Maybe<Uri>` (RxJava2), `Maybe<Uri>` (RxJava3), `Flow<Uri>` (Coroutines)    
 
```kotlin    
MediaPicker.builder()
  .pick(Purpose.Pick.Image)
  .take(Purpose.Take.Video) 
  .build()
``` 

Under the hood purposes combines into available Intents list and user see chooser (or doesn't see if there is only one app available). After finishing stream emits result or completes. 

**Pick** means that you will get media from storage. Available `Pick.Image` and `Pick.Video` options

**Take** means that you will capture media with camera. Available `Take.Photo` and `Take.Video` options
  
## Builder   
#### Options  
If there needs a kind of customization you can pass options to builder
 ```kotlin 
<builder>
   .setTakeVideoOptions(<VideoOptions>)
   .setImageOptions(<ImageOptions>)
```  
`VideoOptions` and `PhotoOptions` are data classes to customize camera `Intent`  
```kotlin 
data class VideoOptions @JvmOverloads constructor(    
     val maxDuration: Duration = (-1).milliseconds,    
     val maxSize: MemorySize = MemorySize(size = -1, unit = SizeUnit.BYTE),    
     val quality: VideoQuality = VideoQuality.HIGH
)  
```  
```kotlin 
data class PhotoOptions @JvmOverloads constructor(    
     val maxResolution: Resolution = Resolution(width = -1 /* px */, height = -1 /* px */),    
     val maxSize: MemorySize = MemorySize(size = -1, unit = SizeUnit.BYTE)   
)  
```  
  
You can leave default values for any parameter or pass nothing to builder. Then this parameters just won't be applied in processing. 

Note that `ImageOptions` applies for both `Pick` and `Take` purposes, but VideoOptions applies only to `Take.Video` purpose. `Pick.Video` not customizable. Maybe later it will be changed. 
#### Titles  
If there are few options to get media then will be shown BottomSheet chooser for app. Default title is `"Choose an application"`, but you can override it (for l10n, for example) with 
```kotlin    
<builder>.setChooserTitle(R.string.whatever_title_you_want)  
 ``` 
 When you pass to builder both `Take.Photo` and `Take.Video` purposes, you can see title "Camera" twice. Its weird, but it is correct, because it is different intents. To avoid this added possibility to customize these titles. 
```kotlin    
<builder>
  .setTakePhotoAppChooserTitle(R.string.whatever_title_you_want)
  .setTakeVideoAppChooserTitle(R.string.whatever_title_you_want_2)  
 ``` 

Every title can be set as a `String` or as a resource. If you want to see camera app title, you must pass `null` as title.
#### Some callbacks

There are provides two callbacks

 1. When user dismissed picker activity 
```kotlin    
    <builder>.onDismissPick(<OnDismissListener>)  
``` 
 2. When user dismissed select app chooser
```kotlin    
    <builder>.onDismissAppSelect(<OnDismissListener>)  
```
Both are `OnDismissListener` which are functional interface with only one method `void onDismiss()`. It can be helpful (for example) if you show `ProgressBar` to user, but he just closes picker activity without completes his choice.  
 3. On permissions result (If you need add reaction wnen permissions granted or denied)
```kotlin    
    <builder>.setPermissionResultHandler(<PermissionsHandler>)  
```

```kotlin    
interface PermissionsHandler {
    fun onRequestPermissionsResult(result: PermissionResult) = Unit
    fun onRequestPermissionsFailed(throwable: Throwable) = Unit
}
```

```kotlin    
data class PermissionResult(
    val granted: List<String> = emptyList(),
    val notGranted: List<String> = emptyList(),
    val foreverDenied: List<String> = emptyList()
)
```

## Transformers  
For streams also provided transformers  
  
  ```kotlin 
file()  // Converts Uri to File  
```   
```kotlin 
filepath()  // Converts Uri to String (file location)  
```  
```kotlin 
bitmap() // Converts Uri to Bitmap 
```  
All of these realized as global functions. In Java can be used as static functions of class `UriTransformers`  
  
 ## Example  
 ##### All examples in RxJava  
  ```kotlin      
 MediaPicker.builder()    
  .setImageOptions(
      ImageOptions(
          maxResolution = Resolution(3000, 3000), 
          maxSize = MemorySize(size = 3, unit = SizeUnit.MEGABYTE)
      ))
  .pick(Purpose.Take.Image) 
  .build()   
  .request(this /* context */ )    
  .compose(file())    
.subscribe { file : File -> } 
```    
 --- OR ---    
 ```kotlin    
 MediaPicker.builder()  
 .setChooserTitle("Select video source")  
 .setTakeVideoOptions(VideoOptions(duration = 10.toSeconds(), quality = VideoQuality.HIGH))
 .take(Purpose.Take.Video)    
 .request(this /* context */ ) 
 .subscribe { uri : Uri -> showVideo(uri) } 
``` 
Also examples available in demo application [Code](app/src/main/java/iam/thevoid/mediapickertest)  
  ## WARNING!!! 
  Since **Android N (Api 24)** needs `fileprovider` for correct saving and using photo. setup your project you must to do next:    
   1) Add file with **{name of file provider descriptor file}** (for example `my_paths.xml`) in `res/xml` folder. Note than in manifest this filename uses without extension ".xml", just like `@xml/my_paths`    
 File contains next: 
 ```xml    
 <paths>    
   <external-path   
      name="my_images"  
      path="Android/data/{name of package of your app}/files/Pictures" />    
</paths> 
```    
 2) Add provider to your `AndroidManifest.xml` 
 ```xml    
 <provider 
   android:name="android.support.v4.content.FileProvider"    
   android:authorities="{name of package of your app}.fileprovider"    
   android:exported="false"    
   android:grantUriPermissions="true">    
     <meta-data    
       android:name="android.support.FILE_PROVIDER_PATHS"    
       android:resource="@xml/**{name of file provider descriptor file}**" />    
</provider> 
```    


 ## Adding to project    

 Add core (each case)
 
 ```groovy      
implementation "io.github.iamthevoid.mediapicker:core:1.1.2"
 ```
 
 And then:

RxJava 1 
```groovy      
implementation "io.github.iamthevoid.mediapicker:rx1:1.1.2"
 ```
 RxJava 2 
 ```groovy      
implementation "io.github.iamthevoid.mediapicker:rx2:1.1.2"
 ```
 RxJava 3 
 ```groovy      
implementation "io.github.iamthevoid.mediapicker:rx3:1.1.2"
 ```
 Coroutines 
 ```groovy      
implementation "io.github.iamthevoid.mediapicker:coroutines:1.1.2"
 ```
