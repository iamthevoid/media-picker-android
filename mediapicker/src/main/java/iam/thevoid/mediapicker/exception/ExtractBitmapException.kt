package iam.thevoid.mediapicker.exception

import java.lang.Exception

class ExtractBitmapException : RuntimeException {

    constructor(message: String) : super(message)

    constructor(cause: Exception) : super(cause)

}