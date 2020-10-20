package com.chinmay.coroutinedemo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NumberViewModel(var age: Int) : ViewModel() {


    private var count: MutableLiveData<Int> = MutableLiveData()

    fun getCurrentCount(): LiveData<Int> {
        return count
    }

    fun increment() {
        count.value = count.value?.plus(1)
    }
}