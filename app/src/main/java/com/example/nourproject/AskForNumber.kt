package com.example.nourproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AskForNumber : AppCompatActivity() {
    lateinit var database: FirebaseDatabase
    lateinit var auth : FirebaseAuth
    lateinit var  emailEditText : EditText
    lateinit var  emergency_number : EditText
    lateinit var nameText : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ask_for_number)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        emailEditText = findViewById(R.id.email)
        nameText = findViewById(R.id.name)


        emergency_number = findViewById<EditText>(R.id.emergency_number)
        val button_enter = findViewById<Button>(R.id.enter)

        button_enter.setOnClickListener {
            var pass = true

            // validate email
            if(!validateEmail() || !ValidateNumber()) pass = false
            // check the number length
            if(pass)  {
                startActivity(Intent(this , HomePage::class.java))
                database.getReference().child("users_emergency").child(auth.currentUser!!.uid.toString())
                    .setValue(emergency_number.text.toString())
                database.getReference().child("users_email").child(auth.currentUser!!.uid.toString())
                    .setValue(emailEditText.text.toString())
                database.getReference().child("users_name").child(auth.currentUser!!.uid.toString())
                    .setValue(nameText.text.toString())
                finish()
            }
        }


    }

    private fun ValidateNumber(): Boolean{
        if (!(emergency_number.text.length > 11)) {
            Toast.makeText(this, "Please Enter valid length of number", Toast.LENGTH_LONG)
                .show()
            return false
        }else{
            return true
        }
    }

    private fun validateEmail() : Boolean {
        val email = emailEditText.text.toString().trim()

        if (email.isEmpty()) {
            // Email is empty
            Toast.makeText(this , "This field cannot be empty",Toast.LENGTH_LONG).show();
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // Invalid email format
            Toast.makeText(this , "Email address is not correct",Toast.LENGTH_LONG).show();
            return false
        }

        return true

        // The email is valid, you can proceed with further actions
        // For example, you can use 'email' variable to get the email address
        // Do something with the valid email address
    }

}