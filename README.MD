Simple RxJava 1 library for pick image or video. For image cropping used
[ArthurHub/Android-Image-Cropper](https://github.com/ArthurHub/Android-Image-Cropper).

## Using:
```java
    RxMediaPicker.builder(this)
        .crop(CropArea.circle())
        .pick(Purpose.Pick.IMAGE)
        .take(Purpose.Take.PHOTO)
        .build()
        .subscribe(uri -> { });
```

OR

```java
    RxMediaPicker.builder(this)
        .pick(Purpose.Pick.IMAGE, Purpose.Pick.VIDEO)
        .take(Purpose.Take.PHOTO, Purpose.Take.VIDEO)
        .build()
        .compose(UriTransformer.filepath(context))
        .subscribe(filepath -> { });
```
OR

```java
    RxMediaPicker.builder(this)
        .pick(Purpose.Pick.IMAGE)
        .take(Purpose.Take.PHOTO)
        .build()
        .compose(UriTransformer.file(context))
        .subscribe(file -> { });
```
## For cropping you must add crop activity to your 'AndroidManifest.xml'
```xml
        <activity
            android:name="iam.thevoid.mediapicker.cropper.CropImageActivity"/>
```

## WARNING!!!
Since N needs fileprovider for correct saving and using photo. Than you need to do next:

1) Add provider to manifest
```xml
        <provider
            android:name="android.support.v4.content.FileProvider"
            android:authorities="**{name of package of your app}**.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/**{name of file provider descriptor file}**" />
        </provider>
```
2) Add file with **{name of file provider descriptor file}** (for example my_paths.xml) in
res/xml folder. Note than in manifest this filename uses without ".xml", just like @xml/my_paths.

File contains next:
```xml
        <paths>
            <external-path
                name="my_images" // Any name
                path="Android/data/**{name of package of your app}**/files/Pictures" />
        </paths>
```

## Adding to project
```groovy
        allprojects {
            repositories {
                maven { url "https://dl.bintray.com/iamthevoid/maven/" }
            }
        }
```
```groovy
        compile 'iam.void.mediapicker:mediapicker:0.2.3'
```
