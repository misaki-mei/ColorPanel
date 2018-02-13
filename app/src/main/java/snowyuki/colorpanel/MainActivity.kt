package snowyuki.colorpanel

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import snowyuki.colorpanellibrary.view.ColorPanelDialog

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUI()
    }

    private fun initUI(){
        color_element.setOnClickListener {
            ColorPanelDialog(this)
                    .setColor(ContextCompat.getColor(this,R.color.colorAccent))
                    .setOnColorSelectedListener(object : ColorPanelDialog.OnColorSelectedListener{
                        override fun onColorSelected(color: Int) {
                            color_element.changeColor(color,1)
                        }

                    })
                    .show()
        }
    }
}
