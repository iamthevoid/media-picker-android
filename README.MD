
<a href="https://bintray.com/beta/#/iamthevoid/maven/MediaPicker"><img src="https://github.com/iamthevoid/media-picker-android/blob/master/lib.svg"></a>


Simple reactive library for pick image or video. There is Rx Java 1 and RxJava 2 implementation. 
Later will be added Rx Java 3 and Coroutines implementation.  

Next example returns stream: `Observable<Uri>`(RxJava1), `Maybe<Uri>`(RxJava2)
  
```kotlin  
 MediaPicker.builder(this)
  .pickImage()
  .build()
```

--- OR ---

```kotlin
 MediaPicker.builder(this)
  .takeVideo(VideoOptions(duration = 10.toSeconds()))
  .build()
```

also transformers provided

 - file()
 - filepath()
 - bitmap()

Example in rxJava
```kotlin
mediaPickerObservable
  .compose(file())
  .subscribe { file : File -> }
```

## WARNING!!!
Since N needs `fileprovider` for correct saving and using photo. Than you need to do next:

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
