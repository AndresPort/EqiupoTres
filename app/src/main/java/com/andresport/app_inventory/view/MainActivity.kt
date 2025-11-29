package com.andresport.app_inventory.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.andresport.app_inventory.R
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)
    }
}
