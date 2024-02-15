package edu.uw.ischool.dtsing.arewethereyet

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    lateinit var messageEditText : EditText
    lateinit var phoneNumberEditText: EditText
    lateinit var intervalEditText: EditText
    lateinit var startButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        messageEditText = findViewById(R.id.messageEditText)
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText)
        intervalEditText = findViewById(R.id.intervalEditText)
        startButton = findViewById(R.id.startButton)

        startButton.setOnClickListener {
            Log.i("Start", "Start button has been clicked")
            if (startButton.text.equals("Start")) {
                if (legitimateValues()) { // If all fields have been correctly filled out, then start the service
                    startButton.text = getString(R.string.stop)
                } else { // Otherwise, pop a Toast on why the application cannot start
                    Toast.makeText(this, "Please fill out all fields correctly", Toast.LENGTH_SHORT).show()
                }
            } else { // Otherwise, stop the service
                stopService(Intent(this, startButton::class.java))
                startButton.text = getString(R.string.start)
        }
    }
}

    private fun legitimateValues(): Boolean {
        // Message should be filled out to be legit
        val validMsg = !TextUtils.isEmpty(messageEditText.text.toString())

        // A legit phone number contains 10 digits
        val validPhoneNum = phoneNumberEditText.length() == 10

        // A legit minute contains non-zero values, positive values, and must be an integer
        val minute = intervalEditText.text.toString().toInt()
        val validMinute = minute > 0

        return validMsg && validPhoneNum && validMinute // Should be true to be legitimate
    }
}

