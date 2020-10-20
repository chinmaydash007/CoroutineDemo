package com.chinmay.coroutinedemo

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.work.*
import com.chinmay.coroutinedemo.databinding.MainActivity2Binding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.net.URL
import java.util.*

class MainActivity2 : AppCompatActivity() {
    var image_url = ""
    var coroutineScope = CoroutineScope(Dispatchers.Main)
    lateinit var binding: MainActivity2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main2)
//        val builder = WorkManager.getInstance(this).beginUniqueWork("sadasd",ExistingWorkPolicy.KEEP,
//            OneTimeWorkRequest.from(MyWorker::class.java))


//        coroutineScope.launch {
//            var originalDeffered = coroutineScope.async {
//                getOriginalBitmap()
//            }
//            var originalBitmap = originalDeffered.await()
//
//        }
        val observer = Observer<WorkInfo> { workInfo ->
            when (workInfo.state) {
                WorkInfo.State.RUNNING -> binding.textView2.append("RUNNING\n")
                WorkInfo.State.ENQUEUED -> binding.textView2.append("ENQUEUED\n")
                WorkInfo.State.SUCCEEDED -> binding.textView2.append(
                    "SUCCEEDED\n ${
                        workInfo.outputData.getString(
                            "mytag2"
                        )
                    }"
                )
                WorkInfo.State.FAILED -> binding.textView2.append("FAILED\n")
                WorkInfo.State.BLOCKED -> binding.textView2.append("BLOCKED\n")
                WorkInfo.State.CANCELLED -> binding.textView2.append("CANCELLED\n")
            }

        }

        var data: Data = Data.Builder().putString("mytag1", "testing workmanager").build()


        var constraints: Constraints = Constraints.Builder().setRequiresCharging(true).build()

        var oneTimeWorkRequest: OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<MyWorker>().setInputData(
                workDataOf(
                    Pair(
                        "mytag1",
                        "testing workmanager"
                    ),
                )
            ).build()
        var uuid: UUID = oneTimeWorkRequest.id
//            OneTimeWorkRequest.Builder(MyWorker::class.java).setInputData(data)
//                .setConstraints(constraints).build()
        binding.runWorkButton.setOnClickListener {
            WorkManager.getInstance(this)
                .enqueueUniqueWork("sendLogs", ExistingWorkPolicy.KEEP, oneTimeWorkRequest)

        }

        binding.cancelWorkBtn.setOnClickListener {
            WorkManager.getInstance(this).cancelUniqueWork("sendLogs")
        }
//        WorkManager.getInstance(this).getWorkInfoByIdLiveData(oneTimeWorkRequest.id).observe(this,
//            { workInfo ->
//                run {
//                    workInfo?.let {
//                        var output: String? = ""
//                        if (workInfo.state.isFinished) {
//                            var data = workInfo.outputData
//                            output = data.getString("mytag2")
//                        }
//                        var status = workInfo.state.name + "\n"
//                        binding.textView2.append(status + output)
//                    }
//                }
//            })

//        WorkManager.getInstance(this).getWorkInfosForUniqueWorkLiveData("sendLogs").observe(this,
//            { workInfoList ->
//                workInfoList?.forEach { workInfo ->
//                    Log.d("mytag", workInfo.outputData.getString("mytag2").toString())
//                }
//            })
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(oneTimeWorkRequest.id).observe(
            this,
            observer
        )


    }


}