package com.chinmay.coroutinedemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.chinmay.coroutinedemo.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class MainActivity : AppCompatActivity() {
    lateinit var activityMainBinding: ActivityMainBinding
    lateinit var job1: CompletableJob
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        job1 = Job()
        main()
        activityMainBinding.button2.setOnClickListener {
            job1.cancel(CancellationException("cancelling because of timeouts"))
        }

    }

    val handler = CoroutineExceptionHandler { _, exception ->
        println("Exception thrown in one of the children: $exception")
    }

    fun main() {
        val parentJob = CoroutineScope(IO + job1).launch(handler) {
            supervisorScope {
                // --------- JOB A ---------
                val jobA = launch {
                    val resultA = getResult(1)
                    println("resultA: ${resultA}")
                }

                jobA.invokeOnCompletion { throwable ->
                    if (throwable != null) {
                        println("Error getting resultA: ${throwable}")
                    }
                }
                // --------- JOB B ---------
                val jobB = launch {
                    val resultB = getResult(2)
                    println("resultB: ${resultB}")
                }
                jobB.invokeOnCompletion { throwable ->
                    if (throwable != null) {
                        println("Error getting resultB: ${throwable}")
                    }
                }

                // --------- JOB C ---------
                val jobC = launch {
                    val resultC = getResult(3)
                    println("resultC: ${resultC}")
                }
                jobC.invokeOnCompletion { throwable ->
                    if (throwable != null) {
                        println("Error getting resultC: ${throwable}")
                    }
                }

            }

        }
        parentJob.invokeOnCompletion { throwable ->
            if (throwable != null) {
                println("Parent job failed: ${throwable}")
            } else {
                println("Parent job SUCCESS")
            }
        }
    }


    suspend fun getResult(number: Int): Int {
        return withContext(Main) {
            delay(number * 5000L)
            if (number == 2) {
//                cancel(CancellationException("Error getting result for number: ${number}"))
//                throw CancellationException("Error getting result for number: ${number}") // treated like "cancel()"
                throw Exception("Error getting result for number: ${number}")
            }
            number * 2
        }
    }


    private fun println(message: String) {
        Log.d("mytag", message)
    }


}

