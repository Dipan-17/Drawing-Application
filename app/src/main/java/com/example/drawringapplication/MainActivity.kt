package com.example.drawringapplication

import android.app.AlertDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.get
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.Image
import android.provider.MediaStore
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private var drawingView:DrawingView?=null
    private var mImageButtonCurrentPaint:ImageButton?=null
    var customProgressDialog:Dialog?=null

    private val openGalleryLauncher:ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
            if(result.resultCode== RESULT_OK && result.data!=null){
                val imageBackGround:ImageView=findViewById(R.id.iv_background)
                imageBackGround.setImageURI(result.data?.data)
            }
        }

    private val requestPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                permissions ->

                permissions.entries.forEach {
                val perMissionName = it.key
                val isGranted = it.value
                //if permission is granted show a toast and perform operation
                if (isGranted ) {
                    Toast.makeText(
                        this@MainActivity,
                        "Permission granted now you can read the storage files.",
                        Toast.LENGTH_SHORT
                    ).show()
                    //perform operation

                    val pickIntent=Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    openGalleryLauncher.launch(pickIntent)


                } else {
                    //Displaying another toast if permission is not granted and this time focus on
                    //    Read external storage
                    if (perMissionName == Manifest.permission.READ_MEDIA_IMAGES)
                        Toast.makeText(
                            this@MainActivity,
                            "Oops you just denied the permission.",
                            Toast.LENGTH_LONG
                        ).show()
                }
            }

        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView=findViewById(R.id.drawing_view)
        drawingView?.setSizeForBrush(10.toFloat())

        val linearLayoutPaintColors=findViewById<LinearLayout>(R.id.ll_paint_colors)

        //set default color as the color 0 -> skin color
        mImageButtonCurrentPaint=linearLayoutPaintColors[0] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
        )

        //brush button to select the brush size
        val ibBrush: ImageButton =findViewById(R.id.ib_brush)
        ibBrush.setOnClickListener {
            showBrushSizeChooserDialog()
        }

        //image  button to select image from gallery as background
        val ibGallery: ImageButton = findViewById(R.id.ib_gallery)
        //Adding an click event to image button for selecting the image from gallery.)
        ibGallery.setOnClickListener {
            requestStoragePermission()
        }

        //undo button
        val ibUndo:ImageButton=findViewById(R.id.ib_undo)
        ibUndo.setOnClickListener {
            drawingView?.onClickUndo()
        }

        //redo button
        val ibRedo:ImageButton=findViewById(R.id.ib_redo)
        ibRedo.setOnClickListener {
            drawingView?.onClickRedo()
        }

        //save button
        val ibSave:ImageButton=findViewById(R.id.ib_save)
        ibSave.setOnClickListener {
            if(isReadStorageAllowed()){//latest version allows write if read is allowed
                showProgressDialog()
                lifecycleScope.launch{
                    val flDrawingView:FrameLayout=findViewById(R.id.fl_drawing_view_container)
                    val myBitmap:Bitmap=getBitmapFromView(flDrawingView)
                    saveBitmapFile(myBitmap)
                }
            }else{
                Toast.makeText(this@MainActivity,"Storage Permission Denied",Toast.LENGTH_SHORT).show()
            }
        }
    }



    /*
    private fun requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission already granted
            // Perform your operation here
            Toast.makeText(
                this@MainActivity,
                "Permission already granted.",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            // Check if the user denied the permission previously and show rationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                // Show rationale dialog to explain why the permission is needed
                showRationaleDialog(
                    "Drawing App",
                    "Drawing App needs to access your external storage."
                )
            } else {
                // Request permission if not previously denied or rationale not shown
                requestPermission.launch(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                )
            }
        }
    }
    */


    //save image

    //1.Permission check
    private fun isReadStorageAllowed():Boolean{
        val result=ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_MEDIA_IMAGES)
        return result==PackageManager.PERMISSION_GRANTED
    }
    //2.convert the view into a bitmap to store
    private fun getBitmapFromView(view:View):Bitmap{
        val returnedBitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
        //bind the canvas
        val canvas=Canvas(returnedBitmap)
        val bgDrawable=view.background
        if(bgDrawable!=null){
            bgDrawable.draw(canvas) //if background -> draw on canvas
        }else{
            canvas.drawColor(Color.WHITE)
        }

        //draw the canvas onto the view
        //finalise the thing
        view.draw(canvas)
        return returnedBitmap
    }
    //3.create a coroutine function to work in background
    private suspend fun saveBitmapFile(mBitmap: Bitmap?):String{
        var result="" //where is the image

        //calling the save function on the background thread
        withContext(Dispatchers.IO){
            if(mBitmap!=null){
                try{
                    val bytes=ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 75,bytes)

                    val f=File(externalCacheDir?.absoluteFile.toString()
                            +File.separator+"DrawingApp_"+System.currentTimeMillis()/1000+".png"
                    )
                    val fo=FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                    fo.close()

                    result=f.absolutePath

                    runOnUiThread{
                        cancelProgressDialog() //run on ui thread
                        if(result.isNotEmpty()){
                            Toast.makeText(this@MainActivity,
                                "File saved at: $result", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(this@MainActivity,
                                "Something went wrong", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                catch(e:Exception){
                    result=""
                    e.printStackTrace()
                }

            }
        }
        return result
    }



    //Create a method to requestStorage permission
    private fun requestStoragePermission(){
        // Check if the permission was denied and show rationale
        if (
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_MEDIA_IMAGES)
        ){
            //call the rationale dialog to tell the user why they need to allow permission request
            showRationaleDialog("Drawing App","Drawing App " +
                    "needs to Access Your External Storage")
        }
        else {
            // You can directly ask for the permission.
            //  if it has not been denied then request for permission
            //  The registered ActivityResultCallback gets the result of this request.
            requestPermission.launch(
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }

    }

    //clicking on the brush icon opens up the size selector
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


    //method to set the color when clicked on the pallet
    fun paintClicked(view: View){
        //change only if a different color is selected
        if(view!== mImageButtonCurrentPaint){
            val imageButton=view as ImageButton
            val colorTag=imageButton.tag.toString()
            drawingView?.setColor(colorTag)

            //update the background of the buttons
            mImageButtonCurrentPaint?.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_normal)
            )
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
            )
            //update the button
            mImageButtonCurrentPaint=view
        }
    }


    /**
     * Shows rationale dialog for displaying why the app needs permission
     * Only shown if the user has denied the permission request previously
     */
    private fun showRationaleDialog(title: String, message: String, ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    //method to show a custom dialog whenever image is being uploaded in the background
    private fun showProgressDialog(){
        customProgressDialog=Dialog(this@MainActivity)
        //set the content from a layout resource
        customProgressDialog?.setContentView(R.layout.dialog_custom_progress)
        //start the dialog
        customProgressDialog?.show()
    }

    private fun cancelProgressDialog(){
        if(customProgressDialog!=null){
            customProgressDialog?.dismiss()
            customProgressDialog=null
        }
    }
}