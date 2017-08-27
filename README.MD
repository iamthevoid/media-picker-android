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
