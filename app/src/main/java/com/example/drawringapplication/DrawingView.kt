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
    private var mDrawPath:CustomPath?=null
    private var mCanvasBitmap: Bitmap?=null
    private var mDrawPaint: Paint?=null
    private var mCanvasPaint: Paint?=null
    private var mBrushSize:Float=0.toFloat()
    private var color=Color.BLACK
    private var canvas:Canvas?=null

    //initialise variables
    init{
        setupDrawing()
    }
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

    //once our viewe is displayed this is called
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        canvas=Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(mCanvasBitmap!!,0f,0f,mCanvasPaint)

        if(!mDrawPath!!.isEmpty){
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
                    mDrawPath!!.moveTo(touchX,touchY)
                }
            }
            //what should happen when we drag over the screen
            MotionEvent.ACTION_MOVE->{
                if (touchX != null && touchY!=null) {
                    mDrawPath!!.lineTo(touchX,touchY)
                }
            }
            //when we release the touch
            MotionEvent.ACTION_UP->{
                mDrawPath=CustomPath(color,mBrushSize)
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