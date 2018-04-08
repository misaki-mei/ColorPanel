package snowyuki.colorpanellibrary.view

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import com.xw.repo.BubbleSeekBar
import kotlinx.android.synthetic.main.dialog_rgb_panel.*
import kotlinx.android.synthetic.main.view_dialog_btn.*
import snowyuki.colorpanellibrary.R

class RgbPanelDialog(c : Context) : AlertDialog(c,R.style.rc_dialog){
    private var enableAlphaEdit = true
    private var curColor = 0
    private var colorAlpha = 0
    private var colorR = 0
    private var colorG = 0
    private var colorB = 0

    private var onColorSelectListener : OnColorSelectListener? = null

    init {
        initParams()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_rgb_panel)
        initUI()
    }

    private fun initParams(){
        colorAlpha = 255
        colorR = 255
        colorG = 0
        colorB = 0
        curColor = Color.argb(colorAlpha,colorR,colorG,colorB)
    }

    private fun initUI(){
        if(!enableAlphaEdit){
            rgbPanelControlA.visibility = View.GONE
        }
        rgbPanelColorIndicator.changeColor(curColor)

        dialog_btn_positive.visibility = View.VISIBLE
        dialog_btn_positive.setOnClickListener {
            onColorSelectListener?.onColorSelect(curColor)
            this.dismiss()
        }

        dialog_btn_negative.visibility = View.VISIBLE
        dialog_btn_negative.setOnClickListener {
            this.dismiss()
        }

        rgbPanelSeekBarR.setProgress(colorR.toFloat())
        rgbPanelSeekBarG.setProgress(colorG.toFloat())
        rgbPanelSeekBarB.setProgress(colorB.toFloat())
        if(enableAlphaEdit){
            rgbPanelSeekBarA.setProgress(colorAlpha.toFloat())
            rgbPanelSeekBarA.onProgressChangedListener = object : BubbleSeekBar.OnProgressChangedListener{
                override fun onProgressChanged(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float, fromUser: Boolean) {
                    colorAlpha = progress
                    changeColor()
                }
                override fun getProgressOnFinally(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float, fromUser: Boolean) = Unit
                override fun getProgressOnActionUp(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) = Unit
            }
        }

        rgbPanelSeekBarR.onProgressChangedListener = object : BubbleSeekBar.OnProgressChangedListener{
            override fun onProgressChanged(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float, fromUser: Boolean) {
                colorR = progress
                changeColor()
            }

            override fun getProgressOnFinally(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float, fromUser: Boolean) = Unit
            override fun getProgressOnActionUp(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) = Unit
        }
        rgbPanelSeekBarG.onProgressChangedListener = object : BubbleSeekBar.OnProgressChangedListener{
            override fun onProgressChanged(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float, fromUser: Boolean) {
                colorG = progress
                changeColor()
            }

            override fun getProgressOnFinally(p0: BubbleSeekBar?, p1: Int, p2: Float, p3: Boolean) = Unit
            override fun getProgressOnActionUp(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) = Unit
        }
        rgbPanelSeekBarB.onProgressChangedListener = object : BubbleSeekBar.OnProgressChangedListener{
            override fun onProgressChanged(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float, fromUser: Boolean) {
                colorB = progress
                changeColor()
            }

            override fun getProgressOnFinally(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float, fromUser: Boolean) = Unit
            override fun getProgressOnActionUp(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) = Unit
        }
    }

    fun setColor(color : Int): RgbPanelDialog {
        curColor = color
        colorAlpha = (curColor shr 24) and 0xff
        colorR = (curColor shr 16) and 0xff
        colorG = (curColor shr 8) and 0xff
        colorB = curColor and 0xff
        return this
    }

    private fun changeColor(){
        curColor = Color.argb(colorAlpha,colorR,colorG,colorB)
        rgbPanelColorIndicator.changeColor(curColor)
    }

    fun fixAlpha(){
        enableAlphaEdit = false
    }

    fun setOnColorSelectListener(onColorSelectListener: OnColorSelectListener): RgbPanelDialog {
        this.onColorSelectListener = onColorSelectListener
        return this
    }
}