package com.chinmay.coroutinedemo.Imgur

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.work.*
import com.chinmay.coroutinedemo.R
import com.chinmay.coroutinedemo.databinding.ImgurActivityBinding
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.*
import java.util.*

class ImgurActivity : AppCompatActivity() {
    lateinit var binding: ImgurActivityBinding
    var job = Job()
    private val PERMISSION_REQUEST_STORAGE = 0
    private val REQUEST_CODE_IMAGE = 100
    var TAG = "mytag"
    var imageUri: Uri? = null
    private val MEDIA_TYPE_PNG = "image/png".toMediaTypeOrNull()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_imgur)
        val handler = CoroutineExceptionHandler { _, exception ->
            println("Exception thrown in one of the children: $exception")
        }
        binding.selectimageButton.setOnClickListener {
            if (checkPermisson()) {
                getImageUriFromStorage()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    PERMISSION_REQUEST_STORAGE
                )
            }
        }


        binding.uploadImageButton.setOnClickListener {
            if (imageUri == null) {
                Toast.makeText(this, "Select Image First", Toast.LENGTH_SHORT).show()
            }
            imageUri?.let {
                uploadImageWithWorkManager()


//                uploadWithCoroutine(handler)

            }
        }
        binding.cancelImageUploadbtn.setOnClickListener {
            WorkManager.getInstance(this).cancelUniqueWork("uploadingImageWork")
        }

        val observer = Observer<List<WorkInfo>> { workInfoList ->
            workInfoList.forEach { workInfo ->
                when (workInfo.state) {
                    WorkInfo.State.RUNNING -> binding.textView4.append("\nRUNNING\n")
                    WorkInfo.State.ENQUEUED -> binding.textView4.append("\nENQUEUED\n")
                    WorkInfo.State.SUCCEEDED -> binding.textView4.append(
                        "\nSUCCEEDED\n ${
                            workInfo.outputData.getString(
                                "imageUrl"
                            )
                        }"
                    )
                    WorkInfo.State.FAILED -> binding.textView4.append("\nFAILED\n")
                    WorkInfo.State.BLOCKED -> binding.textView4.append("\nBLOCKED\n")
                    WorkInfo.State.CANCELLED -> binding.textView4.append("\nCANCELLED\n")
                }
            }
        }
        WorkManager.getInstance(this).getWorkInfosForUniqueWorkLiveData("uploadingImageWork")
            .observe(this, observer)


    }

    private fun uploadImageWithWorkManager() {
        val oneTimeWorkRequestForImageUpload =
            OneTimeWorkRequestBuilder<ImgurWorker>().setInputData(
                workDataOf("imageUri" to imageUri.toString())
            ).build()


        WorkManager.getInstance(this)
            .beginUniqueWork(
                "uploadingImageWork",
                ExistingWorkPolicy.KEEP,
                oneTimeWorkRequestForImageUpload
            ).enqueue()


    }


    private fun uploadWithCoroutine(handler: CoroutineExceptionHandler) {
        CoroutineScope(Dispatchers.IO + job).launch(handler) {
            var newImageUri: Uri? = null
            try {
                val inputStream = inputStreamFor(applicationContext, imageUri.toString())
                val bitmap = BitmapFactory.decodeStream(inputStream)
                newImageUri = writeBitmapToFile(applicationContext, bitmap)
                Log.d("mytag", "newImageUri: " + newImageUri.toString())
            } catch (fileNotFoundException: FileNotFoundException) {
                Log.e(TAG, "Failed to decode input stream", fileNotFoundException)
                throw RuntimeException(
                    "Failed to decode input stream",
                    fileNotFoundException
                )
            } catch (throwable: Throwable) {
                Log.e(TAG, "Error applying filter", throwable)
            }

            Log.d("mytag", imageUri.toString())
            val imgurApiService = RetrofitBuilder.imgurApiService


            val imageFile = File(newImageUri?.path)
            val requestFile = imageFile.asRequestBody(MEDIA_TYPE_PNG)
            val body = MultipartBody.Part.createFormData("image", "image.png", requestFile)
            imgurApiService.postImage(body)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@ImgurActivity, "Fetching Complete", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    private fun writeBitmapToFile(
        applicationContext: Context,
        bitmap: Bitmap
    ): Uri {
        val OUTPUT_PATH = "demo_filter_outputs"

        // Bitmaps are being written to a temporary directory. This is so they can serve as inputs
        // for workers downstream, via Worker chaining.
        val name = "filter-output-${UUID.randomUUID()}.png"
        val outputDir = File(applicationContext.filesDir, OUTPUT_PATH)
        if (!outputDir.exists()) {
            outputDir.mkdirs() // should succeed
        }
        val outputFile = File(outputDir, name)
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(outputFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /* ignored for PNG */, out)
        } finally {
            if (out != null) {
                try {
                    out.close()
                } catch (ignore: IOException) {
                }
            }
        }
        return Uri.fromFile(outputFile)
    }

    fun inputStreamFor(
        context: Context,
        resourceUri: String
    ): InputStream? {
        val ASSET_PREFIX = "file:///android_asset/"

        // If the resourceUri is an Android asset URI, then use AssetManager to get a handle to
        // the input stream. (Stock Images are Asset URIs).
        return if (resourceUri.startsWith(ASSET_PREFIX)) {
            val assetManager = context.resources.assets
            assetManager.open(resourceUri.substring(ASSET_PREFIX.length))
        } else {
            // Not an Android asset Uri. Use a ContentResolver to get a handle to the input stream.
            val resolver = context.contentResolver
            resolver.openInputStream(Uri.parse(resourceUri))
        }
    }

    fun checkPermisson(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    fun getImageUriFromStorage() {
        val chooseIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(chooseIntent, REQUEST_CODE_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_IMAGE -> handleImageRequestResult(data)
                else -> Log.d(TAG, "Unknown request code.")
            }
        } else {
            Log.e(TAG, String.format("Unexpected Result code %s", resultCode))
        }
    }

    private fun handleImageRequestResult(data: Intent?) {
        // Use clip data if SDK_INT >= 16
        if (data!!.clipData != null) {
            imageUri = data.clipData!!.getItemAt(0).uri
        } else if (data!!.data != null) {
            // fallback to getData() on the intent.
            imageUri = data.data
        }

        if (imageUri == null) {
            Log.e(TAG, "Invalid input image Uri.")
            return
        }
        binding.imageView.setImageURI(imageUri)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel(CancellationException("Cancelled due to onDestroy"))
    }
}