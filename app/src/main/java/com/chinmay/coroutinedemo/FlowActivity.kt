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
                Log.d("mytag", "started collecting")
                getValuesfromApi()
                    .map { num -> num * 2 }
                    .filter { num -> num % 3 == 0 }
                    .collect {
                        Log.d("mytag", "Values emitted are: $it")
                        withContext(Dispatchers.Main) {
                            binding.button6.text = "$it"
                        }
                    }
                Log.d("mytag", "collecting ended")

            }
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

}