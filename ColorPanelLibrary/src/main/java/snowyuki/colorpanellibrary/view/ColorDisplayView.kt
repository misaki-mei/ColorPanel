package snowyuki.colorpanellibrary.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import snowyuki.colorpanellibrary.R

class ColorDisplayView : View {

    companion object {
        //transition style
        const val TRANSITION_LINEAR = 0
        const val TRANSITION_RADIAL = 1
        const val TRANSITION_SWEEP = 2

        //display_mode
        const val DISPLAY_SINGLE_COLOR = 0
        const val DISPLAY_MULTI_COLOR = 1
        const val DISPLAY_TRANSITION_COLOR = 2

        //mask shape
        const val SHAPE_RECT = 0
        const val SHAPE_ROUND_RECT = 1
        const val SHAPE_CIRCLE = 2
    }

    private var w = 0
    private var h = 0
    private var wf = 0f
    private var hf = 0f
    private var cw = 0f
    private var ch = 0f
    var borderWidth = 0f
    var borderColor = 0
    var roundRectRadius = 0f

    private var multiColorNum = 2
    private val colorSet = ArrayList<Int>()

    private var displayMode = DISPLAY_SINGLE_COLOR
    private var transitionMode = TRANSITION_LINEAR
    private var linearGradientAngle = 0

    private var girdRow = 1
    private var girdColumn = 1
    var girdSpace = 0f

    private lateinit var maskPath : Path
    private var maskShape = SHAPE_RECT
    private lateinit var borderPaint : Paint
    private lateinit var contentPaint: Paint
    private lateinit var contentShader : Shader

    constructor(context: Context?) : this(context,null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context,attrs,0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        init(attrs)
    }

    private fun initParams(){
        maskPath = Path()

        contentPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        contentPaint.style = Paint.Style.FILL

        borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        borderPaint.style = Paint.Style.STROKE
    }

    private fun init(attrs : AttributeSet?){
        initParams()
        if(attrs == null)return
        val ta = context.obtainStyledAttributes(attrs, R.styleable.ColorDisplayView)
        colorSet.add(ta.getColor(R.styleable.ColorDisplayView_color1,Color.WHITE))
        colorSet.add(ta.getColor(R.styleable.ColorDisplayView_color2,Color.WHITE))
        val colorArrayID = ta.getResourceId(R.styleable.ColorDisplayView_color_data,-1)
        if(colorArrayID != -1){
            val colorIDArray = context.resources.obtainTypedArray(colorArrayID)
            val len = colorIDArray.length()
            if(len > 0)multiColorNum = len
            colorSet.clear()
            for(i in 0 until len){
                colorSet.add(colorIDArray.getColor(i,Color.WHITE))
            }
            colorIDArray.recycle()
        }
        roundRectRadius = ta.getDimension(R.styleable.ColorDisplayView_rect_radius,roundRectRadius)
        borderWidth = ta.getDimension(R.styleable.ColorDisplayView_border_width,0f)
        borderColor = ta.getColor(R.styleable.ColorDisplayView_border_color,Color.WHITE)
        borderPaint.color = borderColor
        borderPaint.strokeWidth = borderWidth
        displayMode = ta.getInt(R.styleable.ColorDisplayView_display_mode, 0)
        maskShape = ta.getInt(R.styleable.ColorDisplayView_mask_shape,0)
        when(displayMode){
            DISPLAY_SINGLE_COLOR -> {
                setSingleColor()
            }
            DISPLAY_MULTI_COLOR -> {
                girdSpace = ta.getDimension(R.styleable.ColorDisplayView_gird_space,girdSpace)
                girdRow = ta.getInt(R.styleable.ColorDisplayView_gird_row,1)
                girdColumn = ta.getInt(R.styleable.ColorDisplayView_gird_column, 1)
                if(girdRow <= 0)girdRow = 1
                if(girdColumn <= 0)girdColumn = 1
                setMultiColor()
            }
            DISPLAY_TRANSITION_COLOR -> {
                linearGradientAngle = ta.getInt(R.styleable.ColorDisplayView_transition_angle,0)
                transitionMode = ta.getInt(R.styleable.ColorDisplayView_transition_mode,0)
            }
        }
        ta.recycle()
    }

    fun setSingleColor(color : Int){
        colorSet.clear()
        colorSet.add(color)
        setSingleColor()
    }

    private fun setSingleColor(){
        contentPaint.color = colorSet[0]
        displayMode = DISPLAY_SINGLE_COLOR
    }

    fun setMultiColor(colorArray : Array<Int>, row : Int , column : Int){
        colorSet.clear()
        colorSet.addAll(colorArray)
        this.girdRow = row
        this.girdColumn = column
        if(girdRow <= 0)girdRow = 1
        if(girdColumn <= 0)girdColumn = 1
        multiColorNum = colorArray.size
        setMultiColor()
    }

    fun setMultiColor(colorArray : ArrayList<Int>, row : Int , column : Int){
        colorSet.clear()
        colorSet.addAll(colorArray)
        this.girdRow = row
        this.girdColumn = column
        if(girdRow <= 0)girdRow = 1
        if(girdColumn <= 0)girdColumn = 1
        multiColorNum = colorArray.size
        setMultiColor()
    }

    private fun setMultiColor(){
        displayMode = DISPLAY_MULTI_COLOR
    }

    fun changeColor(color : Int, index : Int = 0){
        colorSet[index] = color
        invalidate()
    }

    fun setLinearColor(color1 : Int,color2 : Int,angle : Int = 0){
        colorSet.clear()
        colorSet.add(color1)
        colorSet.add(color2)
        if(w != 0)
        setLinearColor(angle)
    }
    
    fun setRadialColor(color1 : Int, color2 : Int){
        colorSet.clear()
        colorSet.add(color1)
        colorSet.add(color2)
        if(w != 0)
        setRadialColor()
    }
    
    fun setSweepColor(color1: Int, color2: Int){
        colorSet.clear()
        colorSet.add(color1)
        colorSet.add(color2)
        if(w != 0)
        setSweepColor()
    }

    private fun setLinearColor(angle : Int = 0){
        val shortestEdge = Math.min(wf,hf)
        val x2 = Math.cos(Math.toRadians(angle.toDouble())).toFloat()*shortestEdge
        val y2 = Math.sin(Math.toRadians(angle.toDouble())).toFloat()*shortestEdge
        contentShader = LinearGradient(wf/2-x2,hf/2-y2,wf/2+x2,hf/2+y2,colorSet[0],colorSet[1],Shader.TileMode.CLAMP)
        contentPaint.shader = contentShader
    }

    private fun setRadialColor(){
        contentShader = RadialGradient(wf/2,hf/2,Math.min(wf,hf)/2,colorSet[0], colorSet[1], Shader.TileMode.CLAMP)
        contentPaint.shader = contentShader
    }
    
    private fun setSweepColor(){
        contentShader = SweepGradient(wf/2,hf/2,colorSet[0],colorSet[1])
        contentPaint.shader = contentShader
    }

    fun setMaskShape(maskShape : Int){
        this.maskShape = maskShape
        if(w != 0){
            setMaskPath()
            invalidate()
        }
    }

    private fun setMaskPath(){
        maskPath.reset()
        when(maskShape){
            SHAPE_RECT -> {
                maskPath.addRect(RectF(borderWidth,borderWidth,wf - borderWidth,hf - borderWidth),Path.Direction.CW)
            }
            SHAPE_ROUND_RECT -> {
                maskPath.addRoundRect(RectF(borderWidth, borderWidth, wf - borderWidth, hf - borderWidth), roundRectRadius, roundRectRadius, Path.Direction.CW)
            }
            SHAPE_CIRCLE -> {
                val radius = Math.min(wf,hf)/2
                maskPath.addCircle(wf/2,hf/2,radius - borderWidth,Path.Direction.CW)
            }
        }
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if(canvas == null)return
        when(displayMode){
            DISPLAY_SINGLE_COLOR, DISPLAY_TRANSITION_COLOR -> {
                canvas.drawPath(maskPath,contentPaint)
                canvas.drawPath(maskPath,borderPaint)
            }
            DISPLAY_MULTI_COLOR -> {
                drawColorGird(canvas)
            }
        }
    }

    private fun drawColorGird(canvas : Canvas){
        var row = girdRow
        var column = girdColumn
        if(column <= 0)column = 1
        if(row <= 0)row = 1

        val tempRectF = RectF()

        cw = (w - girdSpace*(column+1))/column - borderWidth*2
        ch = (h - girdSpace*(row+1))/row - borderWidth*2
        val beginExtend = girdSpace + borderWidth/2
        var x = beginExtend
        var y = beginExtend
        val spanX = girdSpace + cw + borderWidth
        val spanY = girdSpace + ch + borderWidth
        val radius = Math.min(cw,ch)/2
        for(i in 0 until multiColorNum){
            maskPath.reset()
            when(maskShape){
                SHAPE_RECT -> {
                    tempRectF.set(x,y,x+cw,y+ch)
                    maskPath.addRect(tempRectF,Path.Direction.CW)
                }
                SHAPE_CIRCLE -> {
                    maskPath.addCircle(x + cw/2,y+ch/2,radius,Path.Direction.CW)
                }
                SHAPE_ROUND_RECT -> {
                    tempRectF.set(x,y,x+cw,y+ch)
                    maskPath.addRoundRect(tempRectF,roundRectRadius,roundRectRadius,Path.Direction.CW)
                }
            }
            contentPaint.color = colorSet[i]
            canvas.drawPath(maskPath,contentPaint)
            canvas.drawPath(maskPath,borderPaint)
            if((i+1)%column == 0){
                if(i+1 >= row*column)break
                x = beginExtend
                y += spanY
            }else{
                x += spanX
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        this.w = w
        this.h = h
        this.wf = w.toFloat()
        this.hf = h.toFloat()

        setMaskPath()
        if(displayMode == DISPLAY_TRANSITION_COLOR){
            when(transitionMode){
                TRANSITION_LINEAR -> setLinearColor(linearGradientAngle)
                TRANSITION_RADIAL -> setRadialColor()
                TRANSITION_SWEEP -> setSweepColor()
                else -> setLinearColor(0)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val allocatedWidth = MeasureSpec.getSize(widthMeasureSpec)
        val allocatedHeigth = MeasureSpec.getSize(heightMeasureSpec)
        var finalWidth = allocatedWidth
        var finalHeight = allocatedHeigth

        if(widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.AT_MOST){
            when(displayMode){
                DISPLAY_SINGLE_COLOR, DISPLAY_TRANSITION_COLOR -> {
                    if(finalWidth < allocatedHeigth){
                        finalHeight = allocatedWidth
                    }else{
                        finalHeight = allocatedHeigth
                    }
                }
                DISPLAY_MULTI_COLOR -> {
                    if(finalWidth*girdRow/girdColumn < allocatedHeigth){
                        finalHeight = finalWidth*girdRow/girdColumn
                    }else{
                        finalHeight = allocatedHeigth
                    }
                }
            }
        }else if(widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.EXACTLY){
            when(displayMode){
                DISPLAY_SINGLE_COLOR, DISPLAY_TRANSITION_COLOR -> {
                    if(finalHeight < allocatedWidth){
                        finalWidth = allocatedHeigth
                    }else{
                        finalWidth = allocatedWidth
                    }
                }
                DISPLAY_MULTI_COLOR -> {
                    if(finalHeight*girdColumn/girdRow < allocatedWidth){
                        finalWidth = finalHeight*girdColumn/girdRow
                    }else{
                        finalWidth = allocatedWidth
                    }
                }
            }
        }else if(widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST){
            when(displayMode){
                DISPLAY_SINGLE_COLOR, DISPLAY_TRANSITION_COLOR -> {
                    finalWidth = Math.min(allocatedWidth,allocatedHeigth)
                    finalHeight = finalWidth
                }
                DISPLAY_MULTI_COLOR -> {
                    if(allocatedWidth.toFloat()/allocatedHeigth > girdColumn.toFloat()/girdRow){
                        finalHeight = allocatedHeigth
                        finalWidth = finalHeight*girdColumn/girdRow
                    }else{
                        finalWidth = allocatedWidth
                        finalHeight = finalWidth*girdRow/girdColumn
                    }
                }
            }
        }

        setMeasuredDimension(finalWidth,finalHeight)
    }
}