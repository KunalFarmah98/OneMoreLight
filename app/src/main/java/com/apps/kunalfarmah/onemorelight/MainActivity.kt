package com.apps.kunalfarmah.onemorelight

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        email.setOnClickListener {
            intent = Intent(this,SignInActivity::class.java)
            intent.putExtra("Method","email")
            startActivity(intent)
        }

        otp.setOnClickListener {
            intent = Intent(this,SignInActivity::class.java)
            intent.putExtra("Method","otp")
            startActivity(intent)
        }

        signup.setOnClickListener {
            startActivity(Intent(this,SignUpActivity::class.java))
        }
    }
}