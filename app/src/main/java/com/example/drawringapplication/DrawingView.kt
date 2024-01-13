package com.example.drawringapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


class DrawingView(context:Context,attrs:AttributeSet): View(context,attrs) {
    //declaring all the required variables
    private var mDrawPath:CustomPath?=null //one custom path
    private var mCanvasBitmap: Bitmap?=null
    private var mDrawPaint: Paint?=null
    private var mCanvasPaint: Paint?=null
    private var mBrushSize:Float=0.toFloat()
    private var color=Color.BLACK
    private var canvas:Canvas?=null
    //to make the line persist
    private val mPaths=ArrayList<CustomPath>()

    //initialise variables
    init{
        setupDrawing()
    }

    //function to ste up the drawing canvas the brush for the first time
    private fun setupDrawing(){
        mDrawPaint=Paint()
        mDrawPaint!!.color=color
        mDrawPath=CustomPath(color,mBrushSize)
        mDrawPaint!!.style=Paint.Style.STROKE
        mDrawPaint!!.strokeJoin=Paint.Join.ROUND
        mDrawPaint!!.strokeCap=Paint.Cap.ROUND
        mCanvasPaint=Paint(Paint.DITHER_FLAG)
        mBrushSize=20.toFloat()
    }

    //once our view is displayed this is called
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        canvas=Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(mCanvasBitmap!!,0f,0f,mCanvasPaint)

        //draw all the paths saved
        for(path in mPaths){
            mDrawPaint!!.strokeWidth= path.brushThickness
            mDrawPaint!!.color= path.color
            canvas?.drawPath(path,mDrawPaint!!)
        }

        if(!(mDrawPath!!.isEmpty)){
            mDrawPaint!!.strokeWidth=mDrawPath!!.brushThickness
            mDrawPaint!!.color=mDrawPath!!.color
            canvas?.drawPath(mDrawPath!!,mDrawPaint!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        var touchX=event?.x
        var touchY=event?.y

        when(event?.action){
            //what should happen when we press the screen
            MotionEvent.ACTION_DOWN->{
                mDrawPath!!.color=color
                mDrawPath!!.brushThickness=mBrushSize
                mDrawPath!!.reset()
                if (touchX != null && touchY!=null) {
                    mDrawPath!!.moveTo(touchX,touchY) //move the pointer to the point of finger on the screen
                }
            }
            //what should happen when we drag over the screen
            MotionEvent.ACTION_MOVE->{
                if (touchX != null && touchY!=null) {
                    mDrawPath!!.lineTo(touchX,touchY) //moving the finger draws the line too
                }
            }
            //when we release the touch
            MotionEvent.ACTION_UP->{
                mPaths.add(mDrawPath!!) //before releasing save the current path
                mDrawPath=CustomPath(color,mBrushSize) //finger up resets it
            }

            //default value
            else ->return false
        }
        invalidate()
        return true
    }

    internal inner class CustomPath(var color:Int,
                                    var brushThickness:Float): Path() {

    }
}