package snowyuki.colorpanellibrary.utils

import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import snowyuki.colorpanellibrary.R

class BackgroundPainter(val c : Context) {
    var cubeWidth = 24
    set(value) {field = value;drawElement()}

    private val backgroundPaint : Paint
    private lateinit var elementBitmap : Bitmap

    init{
        backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        drawElement()
    }

    fun getBackground(width : Int, height : Int): Bitmap{
        val backgroundBitamp = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888)
        val canvas = Canvas(backgroundBitamp)
        backgroundPaint.shader = BitmapShader(elementBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        canvas.drawRect(Rect(0,0,width,height),backgroundPaint)
        return backgroundBitamp
    }

    private fun drawElement(){
        val girdWidth = cubeWidth*2
        elementBitmap = Bitmap.createBitmap(girdWidth,girdWidth, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(elementBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = ContextCompat.getColor(c, R.color.colorPanelWhite)
        val girdRect = Rect(0,0,cubeWidth,cubeWidth)
        canvas.drawRect(girdRect,paint)
        girdRect.set(cubeWidth,cubeWidth,girdWidth,girdWidth)
        canvas.drawRect(girdRect,paint)

        paint.color = ContextCompat.getColor(c, R.color.colorPanelBlack)
        girdRect.set(cubeWidth,0,girdWidth,cubeWidth)
        canvas.drawRect(girdRect,paint)
        girdRect.set(0,cubeWidth,cubeWidth,girdWidth)
        canvas.drawRect(girdRect,paint)
    }
}