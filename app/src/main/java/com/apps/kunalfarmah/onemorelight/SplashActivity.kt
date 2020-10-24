package com.apps.kunalfarmah.onemorelight

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.firebase.FirebaseApp

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        FirebaseApp.initializeApp(applicationContext)
        var handler =Handler()
        handler.postDelayed(Runnable {
            startActivity(Intent(this,MainActivity::class.java))
        },2000)
    }
}