

<a href="https://bintray.com/beta/#/iamthevoid/maven/MediaPicker"><img src="https://github.com/iamthevoid/media-picker-android/blob/master/lib.svg"></a>  
  
  # Picker  
Simple reactive library for pick image or video. There are Rx Java 1 and RxJava 2 implementation.  
Later will be added Rx Java 3 and Coroutines implementation.  

Next usages returns `Observable<Uri>` (RxJava1), `Maybe<Uri>` (RxJava2)  
  
**1) Pick image from storage**  
```kotlin  
    MediaPicker.builder().pickImage(<ImageOptions>)  
```  
**2) Take image from camera**  
```kotlin  
    MediaPicker.builder().takePhoto(<ImageOptions>)  
```  
**3) Pick video from storage**  
```kotlin  
    MediaPicker.builder().pickVideo() 
```  
**4) Take video from camera**  
```kotlin  
    MediaPicker.builder().takeVideo(<VideoOptions>)  
```  
**5) Pick image from storage  or take image from camera  (if you want single intent for both actions)**
```kotlin  
    MediaPicker.builder().pickImageOrPhoto(<ImageOptions>)  
```  
### Options
`VideoOptions` and `PhotoOptions` are data classes to customize camera `Intent`
```kotlin  
data class VideoOptions @JvmOverloads constructor(  
     val maxDuration: Duration = (-1).milliseconds,  
     val maxSize: MemorySize = MemorySize(size = -1, unit = SizeUnit.BYTE),  
     val quality: VideoQuality = VideoQuality.HIGH,  
     val chooserTitle : Int = R.string.take_video  
)
```
```kotlin  
data class PhotoOptions @JvmOverloads constructor(  
     val maxResolution: Resolution = Resolution(width = -1 /* px */, height = -1 /* px */),  
     val maxSize: MemorySize = MemorySize(size = -1, unit = SizeUnit.BYTE),  
     val chooserTitle : Int = R.string.take_photo  
)
```

You can leave default values. Then they just won't be used. `chooserTitle` will be described later.

### Builder 

If there are few options to pick image then will be shown BottomSheet chooser for app. Default title is `"Choose an application"`, but you can override it (for l10n, for example) with 
```kotlin  
    MediaPicker.builder().setChooserTitle(R.string.whatever_title_you_want)
    
    --- OR ---

    MediaPicker.builder().setChooserTitle("whatever_title_you_want")
```  
Also you can customize chooser title for take photo or video directly in `VideoOptions` and `PhotoOptions`

Sometimes needs to react to event when user just close Picker Activity. So you can set callback to `builder`.

```kotlin  
    MediaPicker.builder().onDismiss(<OnDismissListener>)
```  
### Transformers
For streams also provided transformers


```kotlin  
file()  // Converts Uri to File

filepath()  // Converts Uri to String (file location)

bitmap() // Converts Uri to Bitmap   
```
All of these realized as global functions. In Java can be used as static functions in class `FileUtil`

  ## Example 
  ##### All examples in RxJava
    
```kotlin    
 MediaPicker.builder()  
  .pickImage()  
  .request(this /* context */ )  
  .compose(file())  
  .subscribe { file : File -> } 
```  
  
--- OR ---  
  
```kotlin  
 MediaPicker.builder()
  .setChooserTitle("Select video source")  
  .takeVideo(VideoOptions(duration = 10.toSeconds(), quality = VideoQuality.HIGH))  
  .request(this /* context */ )  
```  
Also examples available in demo application [Code](app/src/main/java/iam/thevoid/mediapickertest)
  
## WARNING!!!  
Since **N (Api 24)** needs `fileprovider` for correct saving and using photo. For reaching your purposes you must to do next:  
  
1) Add provider to your `AndroidManifest.xml`  
```xml  
 <provider android:name="android.support.v4.content.FileProvider"  
  android:authorities="**{name of package of your app}**.fileprovider"  
  android:exported="false"  
  android:grantUriPermissions="true">  
    <meta-data  
      android:name="android.support.FILE_PROVIDER_PATHS"  
      android:resource="@xml/**{name of file provider descriptor file}**" />  
 </provider>  
```  
  
2) Add file with **{name of file provider descriptor file}** (for example `my_paths.xml`) in  
`res/xml` folder. Note than in manifest this filename uses without ".xml", just like `@xml/my_paths`  
  
File contains next:  
```xml  
 <paths>  
   <external-path name="my_images" // Any name path="Android/data/**{name of package of your app}**/files/Pictures" />  
 </paths>  
```  
  
## Adding to project  
  
Add repository:  
```groovy  
 maven { url "https://dl.bintray.com/iamthevoid/maven/" }  
```    
RxJava 1  
```groovy    
 implementation "iam.thevoid.mediapicker:rx1:1.0-beta03"  
```  
RxJava 2  
```groovy    
 implementation "iam.thevoid.mediapicker:rx2:1.0-beta03"  
```

