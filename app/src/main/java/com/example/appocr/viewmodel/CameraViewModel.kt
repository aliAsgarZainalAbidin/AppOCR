package com.example.appocr.viewmodel

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appocr.Navigation
import com.example.appocr.data.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(val repository: Repository) : ViewModel() {
    private val _navigate = MutableLiveData<String>()
    val navigate : LiveData<String> = _navigate

    private val _photoPath = MutableLiveData<String>()
    val photoPath : LiveData<String> = _photoPath

    private val _uri = MutableLiveData<Uri>()
    val uri : LiveData<Uri> = _uri

    private var _selectedlabel = mutableStateOf("")
    val selectedLabel : MutableState<String> = _selectedlabel

    fun setSelectedLabel(data : List<String>){
        var result = ""
        data.forEach {
            result += " $it"
            _selectedlabel.value = result
        }
    }

    fun setImageUri(value : Uri, path : String){
        _uri.postValue(value)
        _photoPath.postValue(path)
    }

    fun navigate(to :String){
        _navigate.value = to
    }
}