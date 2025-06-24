package com.example.hitproduct.base

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    protected lateinit var binding: VB

    //bỏ qua cảnh báo liên quan đến việc khóa hướng màn hình
    @SuppressLint("SourceLockedOrientationActivity")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //khóa hướng màn hình của activity = chế độ dọc
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        binding = inflateViewBinding(layoutInflater)
        setContentView(binding.root)

        initView()
        initData()
        initListener()
        handleEvent()
        bindData()

    }

    abstract fun initListener()

    abstract fun initData()

    abstract fun initView()

    abstract fun handleEvent()

    abstract fun bindData()

    abstract fun inflateViewBinding(layoutInflater: LayoutInflater): VB

}