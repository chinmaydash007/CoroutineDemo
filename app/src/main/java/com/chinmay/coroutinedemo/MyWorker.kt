package com.chinmay.coroutinedemo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters

class MyWorker(context: Context, param: WorkerParameters) : Worker(context, param) {
    override fun doWork(): Result {
        val dataFromActivity = inputData.getString("mytag1") ?: return Result.failure()
        Thread.sleep(2000)
        if (!isStopped) {
            val data: Data = Data.Builder().putString("mytag2", dataFromActivity).build()
            return Result.success(data)
        }
        return Result.failure()
    }

    fun displayNotification(input: String) {
        var manager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var channel =
            NotificationChannel("helloworld", "some decs", NotificationManager.IMPORTANCE_HIGH)
        manager.createNotificationChannel(channel)

        var builder = NotificationCompat.Builder(applicationContext, "helloworld")
            .setContentText(input)
            .setSmallIcon(R.mipmap.ic_launcher)
        manager.notify(1, builder.build())
    }

    override fun onStopped() {

    }
}