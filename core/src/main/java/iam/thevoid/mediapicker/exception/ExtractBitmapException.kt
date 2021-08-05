package iam.thevoid.mediapicker.exception

class ExtractBitmapException : RuntimeException {

    constructor(message: String) : super(message)

    constructor(cause: Exception) : super(cause)

}