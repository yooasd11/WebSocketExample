package com.example.yooas.websocketchatter

/**
 * Created by yooas on 2018-01-14.
 */
class Message (var mType: Int, var mMessage: String) {
    private constructor(builder: Builder): this(builder.mType, builder.mMessage)

    class Builder {
        var mType = 0
            private set
        lateinit var mMessage: String
            private set

        fun type(type: Int) = apply { this.mType = type }

        fun message(message: String) = apply { this.mMessage = message }

        fun build() = Message(this)
    }
}