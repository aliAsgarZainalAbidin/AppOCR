package com.example.appocr.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.NativeCanvas

fun NativeCanvas.drawClickableText(
    @NonNull text : String,
    start : Int,
    end : Int,
    x : Float,
    y : Float,
    @NonNull paint : Paint,
    rect : Rect?,
    modifier : Modifier
){
    modifier.clickable {
        Log.d(ContentValues.TAG, "PreviewScreen: ON TAP")
    }
    drawText(text, start,end,x,y,paint)
}