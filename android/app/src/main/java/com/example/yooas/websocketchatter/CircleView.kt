package com.example.yooas.websocketchatter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * Created by jaeyoonyoo on 2018. 1. 22..
 */
class CircleView: View {
    private var paint: Paint
    private var size: Float = 30F

    constructor(context: Context): this(context, null)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    init {
        paint = Paint()
        paint.color = Color.BLACK
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(150F, 150F, size, paint)
    }

    fun resize(size: Float) {
        this.size = size
        invalidate()
    }
}