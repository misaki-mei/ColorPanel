package snowyuki.colorpanellibrary.view

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import kotlinx.android.synthetic.main.dialog_color_panel.*
import kotlinx.android.synthetic.main.view_dialog_btn.*
import snowyuki.colorpanellibrary.R

class ColorPanelDialog(private val c : Context) : AlertDialog(c){

    private var enableAlphaEdit = true
    private var isColorPanelChanged = false
    private var curColor = 0

    private var onColorSelectedListener : OnColorSelectedListener? = null

    init {
        initParams()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_color_panel)

        initUI()
    }

    private fun initParams(){
        curColor = ContextCompat.getColor(c,R.color.colorPanelRed)
    }

    private fun initUI(){
        colorPanelView.enableAlphaEdit = enableAlphaEdit
        colorPanelView.setColor(curColor)
        colorIndicator.changeColor(curColor,0)
        colorIndicator.changeColor(curColor,1)
        colorEditText.setText(colorPanelView.getColorString(true))

        colorPanelView.onColorChangeListener = object : ColorPanelView.OnColorChangeListener{
            override fun onColorChange(newColor: Int) {
                curColor = newColor
                colorIndicator.changeColor(curColor,1)
                isColorPanelChanged = true
                colorEditText.setText(colorPanelView.getColorString(true))
            }
        }

        colorEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(newEditable : Editable?) {
                if(isColorPanelChanged){
                    isColorPanelChanged = false
                    return
                }
                if(newEditable == null || newEditable.length == 0)return
                if(newEditable.matches(Regex("^#[0-9a-f]{6}([0-9a-f]{2})?$"))){
                    curColor = Color.parseColor(newEditable.toString())
                    colorPanelView.setColor(curColor)
                    colorIndicator.changeColor(curColor,1)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int)  = Unit

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

        })

        dialog_btn_positive.visibility = View.VISIBLE
        dialog_btn_positive.setOnClickListener {
            onColorSelectedListener?.onColorSelected(curColor)
            this.dismiss()
        }

        dialog_btn_negative.visibility = View.VISIBLE
        dialog_btn_negative.setOnClickListener {
            this.dismiss()
        }
    }

    fun setColor(color : Int): ColorPanelDialog {
        this.curColor = color
        return this
    }

    fun fixColorAlpha(fixedAlpha : Int): ColorPanelDialog {
        enableAlphaEdit = false
        curColor = curColor and (fixedAlpha shl 24)
        return this
    }

    fun setOnColorSelectedListener(onColorSelectedListener: OnColorSelectedListener): ColorPanelDialog {
        this.onColorSelectedListener = onColorSelectedListener
        return this
    }

    public interface OnColorSelectedListener{
        fun onColorSelected(color : Int)
    }
}