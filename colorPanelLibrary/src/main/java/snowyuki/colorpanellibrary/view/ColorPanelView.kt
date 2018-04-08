package snowyuki.colorpanellibrary.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import snowyuki.colorpanellibrary.R

class ColorPanelView : View {

    private var w = 0
    private var h = 0
    private var viewPadding = 16
    private var viewSpace = 16
    private val huePanelWidth = 64
    private var huePanelHeight = 0
    private lateinit var huePanelRect : Rect
    private val dfAlphaPanelHeight = 64
    private var alphaPanelWidth = 0
    private var alphaPanelHeight = dfAlphaPanelHeight
    private lateinit var alphaPanelRect : Rect
    private var satPanelWidth = 0
    private var satPanelHeight = 0
    private lateinit var satPanelRect : Rect

    var enableAlphaEdit = true
        set(value) {
            field = value
            alphaPanelHeight = if(enableAlphaEdit)
                dfAlphaPanelHeight else 0
        }

    private var curColor = 0
    private var colorAlpha = 0
    private var colorHue = 0f
    private var colorSat = 0f
    private var colorValue = 0f

    private lateinit var drawCanvas : Canvas
    private lateinit var drawPaint : Paint
    private lateinit var drawRect : Rect
    private lateinit var bitmapPaint : Paint
    private lateinit var trackerPaint: Paint
    private var hueBitmap: Bitmap? = null
    private var satBitmap : Bitmap? = null
    private var alphaBitmap : Bitmap? = null
    private var backgroundBitmap : Bitmap? = null

    private var satPointRadius = 24f
    private var satPointX = 0f
    private var satPointY = 0f
    private var trackerWidth = 32
    private var trackerStroke = 6f
    private lateinit var hueTrackerRect : Rect
    private lateinit var alphaTrackerRect : Rect

    var onColorChangeListener : OnColorChangeListener? = null

    private var whiteColor = 0
    private var blackColor = 0
    private var accentColor = 0
    private lateinit var valueGradient : LinearGradient

    constructor(context: Context) : this(context,null)

    constructor(context: Context, attrs: AttributeSet?) : this(context,attrs,0)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs)
    }

    private fun initParams(){
        colorAlpha = 256
        colorHue = 0f
        colorSat = 1f
        colorValue = 1f
        curColor = ContextCompat.getColor(context, R.color.colorPanelRed)

        whiteColor = ContextCompat.getColor(context,R.color.colorPanelWhite)
        blackColor = ContextCompat.getColor(context,R.color.colorPanelBlack)
        accentColor = ContextCompat.getColor(context,R.color.colorPanelAccent)

        if(!enableAlphaEdit) alphaPanelHeight = 0

        drawPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        drawRect = Rect()
        bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        trackerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        trackerPaint.style = Paint.Style.STROKE
        trackerPaint.strokeWidth = trackerStroke
        trackerPaint.color = whiteColor

        satPanelRect = Rect()
        huePanelRect = Rect()
        alphaPanelRect = Rect()
    }

    private fun init(attrs: AttributeSet?){
        initParams()
        if (attrs == null) return
        val ta = context.obtainStyledAttributes(attrs,R.styleable.ColorPanelView)
        curColor = ta.getColor(R.styleable.ColorPanelView_default_color,curColor)
        setColor(curColor)
        ta.recycle()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if(canvas==null)return
        with(canvas) {
            drawBitmap(satBitmap,satPanelRect.left.toFloat(),satPanelRect.top.toFloat(),bitmapPaint)
            drawBitmap(hueBitmap,huePanelRect.left.toFloat(),huePanelRect.top.toFloat(),bitmapPaint)
            if(enableAlphaEdit){
                drawBitmap(backgroundBitmap,alphaPanelRect.left.toFloat(),alphaPanelRect.top.toFloat(),bitmapPaint)
                drawBitmap(alphaBitmap,alphaPanelRect.left.toFloat(),alphaPanelRect.top.toFloat(),bitmapPaint)
            }
        }
        drawTracker(canvas)
    }

    private fun drawHuePanel(){
        val hueColorArray = IntArray(7)
        for(i in 0..6){
            hueColorArray[i] = Color.HSVToColor(floatArrayOf(i*60f%360,1f,1f))
        }
        hueBitmap = Bitmap.createBitmap(huePanelWidth,huePanelHeight, Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(hueBitmap)
        drawRect.set(0,0,huePanelWidth,huePanelHeight)
        drawPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val linearShader = LinearGradient(0f,0f,0f,huePanelHeight.toFloat(),hueColorArray,null,Shader.TileMode.REPEAT)
        drawPaint.shader = linearShader
        drawCanvas.drawRect(drawRect,drawPaint)
    }

    private fun drawSatPanel(){
        if(satBitmap == null)
            satBitmap = Bitmap.createBitmap(satPanelWidth,satPanelHeight,Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(satBitmap)
        val hueColor = Color.HSVToColor(floatArrayOf(colorHue,1f,1f))
        val colorGradient = LinearGradient(0f,0f,satPanelWidth.toFloat(),0f, whiteColor,hueColor,Shader.TileMode.CLAMP)
        val composeShader = ComposeShader(valueGradient,colorGradient,PorterDuff.Mode.MULTIPLY)
        drawPaint.shader = composeShader
        drawRect.set(0,0,satPanelWidth,satPanelHeight)
        drawCanvas.drawRect(drawRect,drawPaint)
    }

    private fun drawAlphaPanel(){
        if(!enableAlphaEdit)return
        if(alphaBitmap!=null){
            alphaBitmap!!.recycle()
        }
        alphaBitmap = Bitmap.createBitmap(alphaPanelWidth, alphaPanelHeight, Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(alphaBitmap)
        val satColor = Color.HSVToColor(floatArrayOf(colorHue,colorSat,colorValue))
        val transparentColor = satColor and 0x00ffffff
        val alphaShader = LinearGradient(0f, 0f, alphaPanelWidth.toFloat(), 0f, transparentColor, satColor, Shader.TileMode.CLAMP)
        drawRect.set(0, 0, alphaPanelWidth, alphaPanelHeight)
        drawPaint.shader = alphaShader

        drawCanvas.drawRect(drawRect,drawPaint)
    }

    private fun drawAlphaBackground(){
        if(!enableAlphaEdit)return
        val elementWidth = 24
        val girdWidth = elementWidth*2
        val elementBitmap = Bitmap.createBitmap(girdWidth,girdWidth, Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(elementBitmap)
        val drawPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        drawPaint.color = ContextCompat.getColor(context,R.color.colorPanelWhite)
        val girdRect = Rect(0,0,elementWidth,elementWidth)
        drawCanvas.drawRect(girdRect,drawPaint)
        girdRect.set(elementWidth,elementWidth,girdWidth,girdWidth)
        drawCanvas.drawRect(girdRect,drawPaint)

        drawPaint.color = ContextCompat.getColor(context,R.color.colorPanelBlack)
        girdRect.set(elementWidth,0,girdWidth,elementWidth)
        drawCanvas.drawRect(girdRect,drawPaint)
        girdRect.set(0,elementWidth,elementWidth,girdWidth)
        drawCanvas.drawRect(girdRect,drawPaint)

        backgroundBitmap = Bitmap.createBitmap(alphaPanelWidth,alphaPanelHeight,Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(backgroundBitmap)
        girdRect.set(0,0,alphaPanelWidth,alphaPanelHeight)
        val bitmapShader = BitmapShader(elementBitmap,Shader.TileMode.REPEAT,Shader.TileMode.REPEAT)
        drawPaint.shader = bitmapShader
        drawCanvas.drawRect(girdRect,drawPaint)
    }

    private fun drawTracker(canvas: Canvas){
        satPointX = colorSat*satPanelWidth + satPanelRect.left
        satPointY = satPanelRect.bottom - satPanelHeight*colorValue
        trackerPaint.color = if(colorSat < 0.5 && colorValue > 0.5) blackColor else whiteColor
        canvas.drawCircle(satPointX,satPointY,satPointRadius, trackerPaint)

        hueTrackerRect.top = ((colorHue/360)*huePanelHeight + huePanelRect.top - trackerWidth/2).toInt()
        hueTrackerRect.bottom = hueTrackerRect.top + trackerWidth
        trackerPaint.color = whiteColor
        canvas.drawRect(hueTrackerRect, trackerPaint)

        if(enableAlphaEdit){
            alphaTrackerRect.left = ((colorAlpha/256f)*alphaPanelWidth + alphaPanelRect.left - trackerWidth/2).toInt()
            alphaTrackerRect.right = alphaTrackerRect.left + trackerWidth
            if(colorAlpha < 96)trackerPaint.color = accentColor
            canvas.drawRect(alphaTrackerRect, trackerPaint)
        }
    }

    private fun changeColor(){
        curColor = Color.HSVToColor(colorAlpha, floatArrayOf(colorHue,colorSat,colorValue))
        onColorChangeListener?.onColorChange(curColor)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(event==null)return super.onTouchEvent(event)
        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                moveTracker(event)
            }
            MotionEvent.ACTION_MOVE -> {
                moveTracker(event)
            }
        }
        return true
    }

    private fun moveTracker(event: MotionEvent){
        val x = event.x.toInt()
        val y = event.y.toInt()
        when {
            satPanelRect.contains(x,y) -> {
                colorSat = (x.toFloat() - satPanelRect.left)/satPanelWidth
                colorValue = (satPanelRect.bottom - y.toFloat())/satPanelHeight
                drawAlphaPanel()
                invalidate(satPanelRect)
                invalidate(alphaPanelRect)
            }
            huePanelRect.contains(x,y) -> {
                colorHue = 360f*(y - huePanelRect.top)/huePanelHeight
                drawSatPanel()
                drawAlphaPanel()
                invalidate()
            }
            enableAlphaEdit && alphaPanelRect.contains(x,y) -> {
                colorAlpha = (256 * (x - alphaPanelRect.left) / alphaPanelWidth)
                invalidate(alphaPanelRect)
            }
        }
        changeColor()
    }

    fun setColor(color : Int){
        setColor(color,(color shr 24) and 0xff)
    }

    fun setColor(color : Int, alpha : Int){
        curColor = color
        val resultSet = FloatArray(3)
        Color.colorToHSV(color,resultSet)
        colorAlpha = alpha
        colorHue = resultSet[0]
        colorSat = resultSet[1]
        colorValue = resultSet[2]
        setColor()
    }

    fun setColor(red : Int, green : Int, blue : Int, alpha : Int){
        val resultSet = FloatArray(3)
        Color.RGBToHSV(red,green,blue,resultSet)
        colorHue = resultSet[0]
        colorSat = resultSet[1]
        colorValue = resultSet[2]
        colorAlpha = alpha
        curColor = Color.HSVToColor(colorAlpha, floatArrayOf(colorHue, colorSat, colorValue))
        setColor()
    }

    private fun setColor(){
        if(w != 0){
            drawSatPanel()
            drawAlphaPanel()
            invalidate()
        }
    }

    fun getColor() = curColor

    fun getColorString(withSign : Boolean = false) = if(withSign)
        String.format("#%x",curColor)
    else
        String.format("%x",curColor)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val finalWidth : Int
        val finalHeight : Int
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val allocatedWidth = MeasureSpec.getSize(widthMeasureSpec)
        val allocatedHeight = MeasureSpec.getSize(heightMeasureSpec)
        if(widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.AT_MOST){
            finalWidth = allocatedWidth
            finalHeight = if(finalWidth - huePanelWidth + alphaPanelHeight < allocatedHeight){
                 finalWidth
            }else{
                allocatedHeight
            }
        }else if(widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.EXACTLY){
            finalHeight = allocatedHeight
            finalWidth = if(finalHeight - alphaPanelHeight + huePanelWidth < allocatedWidth)
                finalHeight
            else
                allocatedWidth
        }else if(widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST){
            if(allocatedWidth - huePanelWidth < allocatedHeight - alphaPanelHeight){
                finalWidth = allocatedWidth
                finalHeight = finalWidth - huePanelWidth + alphaPanelHeight
            }else{
                finalHeight = allocatedHeight
                finalWidth = finalHeight - alphaPanelHeight + huePanelWidth
            }
        }else{
            finalWidth = allocatedWidth
            finalHeight = allocatedHeight
        }
        setMeasuredDimension(finalWidth,finalHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        this.w = w
        this.h = h

        satPanelWidth = w - 2*viewPadding - viewSpace - huePanelWidth
        satPanelHeight = h - 2*viewPadding - viewSpace - alphaPanelHeight
        huePanelHeight = satPanelHeight
        alphaPanelWidth = satPanelWidth

        satPanelRect.left = viewPadding
        satPanelRect.right = satPanelRect.left + satPanelWidth
        satPanelRect.top = viewPadding
        satPanelRect.bottom = satPanelRect.top + satPanelHeight

        huePanelRect.left = viewPadding + viewSpace + satPanelWidth
        huePanelRect.right = huePanelRect.left + huePanelWidth
        huePanelRect.top = viewPadding
        huePanelRect.bottom = huePanelRect.top + huePanelHeight

        alphaPanelRect.left = viewPadding
        alphaPanelRect.right = alphaPanelRect.left + alphaPanelWidth
        alphaPanelRect.top = viewPadding + viewSpace + satPanelHeight
        alphaPanelRect.bottom = alphaPanelRect.top + alphaPanelHeight

        valueGradient = LinearGradient(0f, 0f, 0f, satPanelHeight.toFloat(), whiteColor, blackColor, Shader.TileMode.CLAMP)

        hueTrackerRect = Rect(huePanelRect)
        alphaTrackerRect = Rect(alphaPanelRect)

        drawHuePanel()
        drawSatPanel()
        drawAlphaPanel()
        drawAlphaBackground()
    }

    interface OnColorChangeListener{
        fun onColorChange(newColor : Int)
    }
}
