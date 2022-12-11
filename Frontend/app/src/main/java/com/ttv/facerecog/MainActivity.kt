package com.ttv.facerecog

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.ttv.face.FaceEngine
import com.ttv.face.FaceResult
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(){


    private var context: Context? = null
    private var mydb: DBHelper? = null
    private var imageUri: Uri? = null
    private var bitmapUri: Uri? = null
    private var email: String? = null
    private val permissionsDelegate = PermissionsDelegate(this)
    private var hasPermission = false



    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = intent
        email = intent.getStringExtra("email")

        context = this

        mydb = DBHelper(this)



        val btnCapture = findViewById<Button>(R.id.btnCapture)
        btnCapture.setOnClickListener {
            print(hasPermission);
            println("uwuwuuw0");
            hasPermission = permissionsDelegate.hasPermissions()
            if (!hasPermission) {
                permissionsDelegate.requestPermissions()
            } else {
                val fileName = File.separator + "IMG" + SimpleDateFormat(
                    "yyyyMMddHHmmss"
                ).format(
                    Date()
                ) + ".png"
                val values = ContentValues()
                values.put(MediaStore.Images.Media.TITLE, fileName)
                values.put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera")
                imageUri = contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(intent, 1)
            }
        }

        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnRegister.setOnClickListener {
            if (imageUri == null) {
                Toast.makeText(this, "Please take your image!", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    var bitmap: Bitmap = ImageRotator.getCorrectlyOrientedImage(this, imageUri)
                    bitmapUri = getImageUri(this, bitmap)
                    val faceResults: MutableList<FaceResult> =
                        FaceEngine.getInstance(this).detectFace(bitmap)
                    if (faceResults.count() == 1) {
                        uploadToServer()
                    } else if (faceResults.count() > 1) {
                        Toast.makeText(this, "Multiple face detected!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "No face detected!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: java.lang.Exception) {
                    //handle exception
                    e.printStackTrace()
                }
            }
        }
    }

    fun uploadToServer() {
        val list: MutableList<MultipartBody.Part> = ArrayList()

//        list.add(prepareFiles("file", imageUri!!))
        list.add(prepareFiles("file", bitmapUri!!))
        list.add(MultipartBody.Part.createFormData("email", email))
        val service = RetrofitBuilder.getClient().create(UploadApis::class.java)
        val call = service.callMultipleUploadApi(list)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                Toast.makeText(this@MainActivity, "Uploaded", Toast.LENGTH_LONG).show()
                val myIntent = Intent(this@MainActivity, HomepageActivity::class.java)
                myIntent.putExtra("Q", "trashed=false")
                myIntent.putExtra("folderName", "")
                startActivity(myIntent)
                finish()
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(this@MainActivity, "Failure", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun prepareFiles(partName: String, fileUri: Uri): MultipartBody.Part {
        println("AIYA PORLO")
        val file = File(FileUtils.getRealPath(this, fileUri))
        println(fileUri.path)
        println(FileUtils.getRealPath(this, fileUri))
        val requestBody = RequestBody.create(MediaType.parse("image/*"), file)
        return MultipartBody.Part.createFormData(partName, file.name, requestBody)
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        System.out.println(resultCode)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val imageView = findViewById<ImageView>(R.id.capturedImage)
            imageView.setImageURI(imageUri)
        }
    }
}