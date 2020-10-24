package com.apps.kunalfarmah.onemorelight

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_sign_in.*
import java.util.concurrent.TimeUnit

class SignInActivity : AppCompatActivity() {
    private val TAG = "SignIn"
    private lateinit var auth: FirebaseAuth
    private lateinit var callbacks:PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var user:FirebaseUser
    var storedVerificationId: String? = null
    var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        var method = intent.getStringExtra("Method")
        auth = FirebaseAuth.getInstance()
        if (method == "otp") {
            et_email.setHint(getString(R.string.enter_phone))
            et_password.visibility = GONE
            bt_submit.setText(getString(R.string.send_otp))
        } else {
            et_email.setHint(getString(R.string.enter_your_email))
            et_password.visibility = VISIBLE
            bt_submit.setText(getString(R.string.submit))

            callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // we want to login with OTP so nothing happens on auto verification
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.w(TAG, "onVerificationFailed", e)

                    if (e is FirebaseAuthInvalidCredentialsException) {
                        Log.w(TAG, "onVerificationFailed", e)
                    } else if (e is FirebaseTooManyRequestsException) {
                        Log.w(TAG, "onVerificationFailed", e)
                    }

                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    // The SMS verification code has been sent to the provided phone number, we
                    // now need to ask the user to enter the code and then construct a credential
                    // by combining the code with a verification ID.
                    Log.d(TAG, "onCodeSent:$verificationId")

                    // Save verification ID and resending token so we can use them later
                    storedVerificationId = verificationId
                    resendToken = token

                    method = "verify"

                    object : CountDownTimer(60000, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            tv_timer!!.setText("Resend OTP in: " + (millisUntilFinished) / 1000)
                        }

                        override fun onFinish() {
                            tv_timer!!.visibility = GONE
                            bt_resend!!.alpha = 1f
                        }
                    }.start()

                }
            }

            et_email!!.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) {}

                override fun beforeTextChanged(
                    s: CharSequence, start: Int,
                    count: Int, after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence, start: Int,
                    before: Int, count: Int
                ) {
                    if (s.toString().length == 10) {
                        bt_submit!!.alpha = 1f
                    }
                }
            })
        }

        bt_submit.setOnClickListener {
            when (method) {
                "email" -> {
                    signInWithEmail(et_email.text.toString(), et_password.text.toString())
                }
                "otp" -> {
                    var phone = et_email.text.toString()
                    if(!isNumeric(phone)){
                        Toast.makeText(this,"Phone Number Invalid",Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    phone = "+91" + phone
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        phone, // Phone number to verify
                        60, // Timeout duration
                        TimeUnit.SECONDS, // Unit of timeout
                        this@SignInActivity, // Activity (for callback binding)
                        callbacks
                    )
                    et_email.text=null
                    et_email.hint = getString(R.string.enter_otp)
                    bt_submit.text = getString(R.string.submit)
                    bt_resend.visibility= VISIBLE
                    bt_resend.alpha = 0.1f
                    tv_timer.visibility= VISIBLE
                }

                "verify" ->{
                    verifyPhoneNumberWithCode(storedVerificationId,et_email.text.toString())
                }

            }
        }


        bt_resend.setOnClickListener {
            resendVerificationCode("+91" + et_email!!.text.toString(), resendToken)
        }
    }


    private fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()

                }

            }
    }

    private fun isNumeric(text: String?): Boolean {
        var t = 0
        while (t < 10) {
            if (!(text!!.get(t) in '0'..'9'))
                return false
            ++t
        }
        return true
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    user = task.result?.user!!
                    Toast.makeText(
                        this@SignInActivity, "Welcome " + user!!.phoneNumber.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                    //startActivity(Intent(this, ProductActivity::class.java))
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(this, "The OTP was Invalid", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?
    ) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            callbacks, // OnVerificationStateChangedCallbacks
            token
        )
    }
}