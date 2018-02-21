package snowyuki.colorpanel

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import snowyuki.colorpanellibrary.view.OnColorSelectListener
import snowyuki.colorpanellibrary.view.RgbPanelDialog

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUI()
    }

    private fun initUI(){
        color_element.setOnClickListener {
            RgbPanelDialog(this)
                    .setColor(color_element.getColor())
                    .setOnColorSelectListener(object : OnColorSelectListener{
                        override fun onColorSelect(color: Int) {
                            color_element.changeColor(color)
                        }
                    })
                    .show()
        }
    }
}
