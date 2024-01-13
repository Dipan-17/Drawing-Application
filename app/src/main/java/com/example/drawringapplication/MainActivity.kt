package com.example.drawringapplication

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton

class MainActivity : AppCompatActivity() {

    private var drawingView:DrawingView?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView=findViewById(R.id.drawing_view)
        drawingView?.setSizeForBrush(10.toFloat())

        val ib_brush: ImageButton =findViewById(R.id.ib_brush)
        ib_brush.setOnClickListener {
            showBrushSizeChooserDialog()
        }
    }

    private fun showBrushSizeChooserDialog(){
        //set up the dialog
        var brushDialog= Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush size: ")
        //set up the buttons
        val smallBtn=brushDialog.findViewById<ImageButton>(R.id.ib_small_brush)
        smallBtn.setOnClickListener{
            drawingView?.setSizeForBrush(7.toFloat())
            brushDialog.dismiss()
        }

        val mediumBtn=brushDialog.findViewById<ImageButton>(R.id.ib_medium_brush)
        mediumBtn.setOnClickListener{
            drawingView?.setSizeForBrush(9.toFloat())
            brushDialog.dismiss()
        }

        val largeBtn=brushDialog.findViewById<ImageButton>(R.id.ib_large_brush)
        largeBtn.setOnClickListener{
            drawingView?.setSizeForBrush(12.toFloat())
            brushDialog.dismiss()
        }

        //show the dialog when this method is called
        brushDialog.show()
    }
}