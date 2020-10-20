package com.chinmay.coroutinedemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.chinmay.coroutinedemo.databinding.MainActivity3Binding

class MainActivity3 : AppCompatActivity() {
    lateinit var binding: MainActivity3Binding
    lateinit var numberViewmodel: NumberViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main3)
        numberViewmodel = ViewModelProvider(this).get(NumberViewModel::class.java)

        binding.button5.setOnClickListener {
            numberViewmodel.increment()
        }
        numberViewmodel.getCurrentCount().observe(this, Observer { number ->
            binding.textView3.text = number.toString()
        })
    }


}