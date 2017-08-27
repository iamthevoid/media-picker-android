Simple RxJava 1 library for pick image or video. For image cropping used
[ArthurHub/Android-Image-Cropper](https://github.com/ArthurHub/Android-Image-Cropper).

Using:
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
For cropping you must add crop activity to your 'AndroidManifest.xml'
```xml
        <activity
            android:name="iam.thevoid.mediapicker.cropper.CropImageActivity"/>
```

Adding to project
```groovy
allprojects {
    repositories {
        maven { url "https://dl.bintray.com/iamthevoid/maven/" }
    }
}
```
```groovy
compile 'iam.void.mediapicker:mediapicker:the.last.version'
```
