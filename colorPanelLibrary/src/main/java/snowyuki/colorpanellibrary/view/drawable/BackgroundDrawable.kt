package snowyuki.colorpanellibrary.view.drawable

import android.graphics.*
import android.graphics.drawable.Drawable

class BackgroundDrawable : Drawable(){
    var width = 100f
    var height = 100f
    private val contentRect = RectF()
    private var elementWidth = 32

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        backgroundPaint.shader = BitmapShader(getElementBitmap(),Shader.TileMode.REPEAT,Shader.TileMode.REPEAT)
    }

    override fun draw(canvas : Canvas?) {
        if(canvas == null)return
        canvas.drawRect(contentRect, backgroundPaint)
    }

    override fun setAlpha(alpha : Int) {
        backgroundPaint.alpha = alpha
    }

    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?){
        backgroundPaint.colorFilter = colorFilter
    }

    private fun getElementBitmap(): Bitmap? {
        val girdWidth = elementWidth*2
        val elementBitmap = Bitmap.createBitmap(girdWidth,girdWidth, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(elementBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = Color.WHITE
        val girdRect = Rect(0,0,elementWidth,elementWidth)
        canvas.drawRect(girdRect,paint)
        girdRect.set(elementWidth,elementWidth,girdWidth,girdWidth)
        canvas.drawRect(girdRect,paint)

        paint.color = Color.BLACK
        girdRect.set(elementWidth,0,girdWidth,elementWidth)
        canvas.drawRect(girdRect,paint)
        girdRect.set(0,elementWidth,elementWidth,girdWidth)
        canvas.drawRect(girdRect,paint)
        return elementBitmap
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        contentRect.set(left.toFloat(),top.toFloat(),right.toFloat(),bottom.toFloat())
        width = (right-left).toFloat()
        height = (top - bottom).toFloat()
    }
}