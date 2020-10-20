package com.chinmay.coroutinedemo.Imgur

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.*
import java.util.*

class ImgurWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    var TAG = this.javaClass.simpleName
    private val MEDIA_TYPE_PNG = "image/png".toMediaTypeOrNull()
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result {
        val imageUri = inputData.getString("imageUri")
        var newImageUri: Uri? = null

        try {
            setForeground(createForegroundInfo())
            if (TextUtils.isEmpty(imageUri)) {
                Log.e(TAG, "Invalid input uri")
                return Result.failure(workDataOf("resultUrl" to "wrong image uri from file system"))
            }
            val inputStream = inputStreamFor(applicationContext, imageUri.toString())
            val bitmap = BitmapFactory.decodeStream(inputStream)
            // write bitmap to a file and set the output
            newImageUri = writeBitmapToFile(applicationContext, bitmap)
            Log.d(TAG, "newImageUri: $newImageUri")


            val imageFile = File(newImageUri.path)
            val requestFile = imageFile.asRequestBody(MEDIA_TYPE_PNG)


            val imgurApiService = RetrofitBuilder.imgurApiService
            val body = MultipartBody.Part.createFormData("image", "image.png", requestFile)
            val postImage = imgurApiService.postImage(body)
            var imageUrlAfterUpload = postImage.data.link
            return Result.success(workDataOf("imageUrl" to imageUrlAfterUpload))
        } catch (fileNotFoundException: FileNotFoundException) {
            Log.e(TAG, "Failed to decode input stream", fileNotFoundException)
            throw RuntimeException("Failed to decode input stream", fileNotFoundException)
        } catch (throwable: Throwable) {
            Log.e(TAG, "Error applying filter", throwable)
            return Result.failure()
        }


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

    private fun createForegroundInfo(): ForegroundInfo {
        // For a real world app you might want to use a different id for each Notification.
        val notificationId = 1
        return ForegroundInfo(notificationId, createNotification())
    }

    private fun createNotification(): Notification {
        val context = applicationContext
        val channelId = "WorkManagerNotificationId"
        val title = "Uploading Image"
        val cancel = "Cancel Uploading"
        val name = "WorkManger Upload Channel"
        // This PendingIntent can be used to cancel the Worker.
        val intent = WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)

        val builder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setTicker(title)
            .setSmallIcon(R.drawable.notification_icon_background)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_delete, cancel, intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(channelId, name).also {
                builder.setChannelId(it.id)
            }
        }
        return builder.build()
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(
        channelId: String,
        name: String
    ): NotificationChannel {
        return NotificationChannel(
            channelId, name, NotificationManager.IMPORTANCE_LOW
        ).also { channel ->
            notificationManager.createNotificationChannel(channel)
        }
    }

}