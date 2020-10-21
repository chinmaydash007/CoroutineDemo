package com.chinmay.coroutinedemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.chinmay.coroutinedemo.databinding.FlowActivityBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class FlowActivity : AppCompatActivity() {
    lateinit var binding: FlowActivityBinding
    lateinit var job: Job
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_flow)
        binding.button6.setOnClickListener {


            job = CoroutineScope(Dispatchers.IO).launch {
//                zip()
//                combine()

//                tryCatch()
//                catch()
                onComplete()


//                val factorial = (1..10).asFlow().reduce { accumulator: Int, value: Int ->
//                    accumulator * value
//                }
//                Log.d("mytag", factorial.toString())

//                var time = measureDurationMillis {
//                    (1..10).asFlow().flowOn(Dispatchers.Default).collect {
//                        Log.d("mytag", "from flowOn $it")
//                        delay(2000)
//                    }
//                }
//                Log.d("mytag", time.toString())


//                Log.d("mytag", "started collecting")
//
//                var time = measureTimeMillis {
//                    getValuesfromApi()
//                        .map { num -> num * 2 }
//                        .filter { num -> num % 1 == 0 }
//                        .buffer()
////                    .transform<Int, String> { num -> "The $num is divisible by 2 and 3" }
//                        .collect {
//                            Log.d("mytag", "$it")
//                            delay(2000)
//                            withContext(Dispatchers.Main) {
//                                binding.textView5.append("$it\n")
//                            }
//                        }
//                    Log.d("mytag", "collecting ended")
//                }
//                Log.d("mytag", time.toString())
//

            }
        }
    }

    suspend fun zip() {
        val english = flowOf("One", "Two", "Three", "four", "Five")
        var numbers = flowOf(1, 2, 3, 4)
        english.zip(numbers) { eng, num ->
            "$eng $num"
        }.collect {
            Log.d("mytag", it)
        }
    }

    suspend fun combine() {
        val english = flowOf("One", "Two", "Three", "four", "Five").onEach { delay(2000) }
        var numbers = flowOf(1, 2, 3, 4).onEach { delay(3000) }

        numbers.combine(english) { a, b ->
            "$a---->$b"
        }.collect {
            println(it)
        }
    }

    fun getValuesfromApi(): Flow<Int> = flow<Int> {
        var list = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
        list.forEach {
            delay(1000)
            emit(it)
        }
    }

    fun getValuesfromApi2() = flowOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
    fun getValuesfromApi3() = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9).asFlow()
    override fun onDestroy() {
        super.onDestroy()
        job.cancel(CancellationException())
    }


    suspend fun tryCatch() {
        try {
            (1..3).asFlow()
                .onEach { check(it != 2) }
                .collect { print(it) }
        } catch (e: Exception) {
            println("Caught exception $e")
        }
    }

    suspend fun catch() {
        (1..3).asFlow()
            .onEach { check(it != 2) }
            .catch { e -> println("Caught Exception $e") }
            .collect { println(it) }
    }

    @ExperimentalCoroutinesApi
    suspend fun onComplete() {
        (1..3).asFlow()
            .onEach { check(it != 2) }
            .catch { e -> println("Caught Exception $e") }
            .onCompletion { e ->
                if (e != null) {
                    println("FLow Complete with Exception $e`")
                } else {
                    println("FLow Compeleted.")
                }
            }.collect { it ->
                println(it.toString())
            }
    }
}