package edu.uw.ischool.dtsing.arewethereyet

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var messageEditText : EditText
    private lateinit var phoneNumberEditText: EditText
    private lateinit var intervalEditText: EditText
    private lateinit var startButton: Button

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
                    startMsg(
                        this,
                        phoneNumberEditText.text.toString(),
                        intervalEditText.text.toString().toInt()
                    )
                } else { // Otherwise, pop a Toast on why the application cannot start
                    Toast.makeText(this, "Please fill out all fields correctly", Toast.LENGTH_SHORT).show()
                }
            } else { // Otherwise, stop the service
                stopService(Intent(this, startButton::class.java))
                Log.i("Stop", "Stop button has been clicked")
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
        val minuteStr = intervalEditText.text.toString()
        val validMinute: Boolean = try {
            val minute = minuteStr.toInt()
            minute > 0
        } catch (e: NumberFormatException) {
            false // Unable to parse to integer, not a valid minute input
        }


        return validMsg && validPhoneNum && validMinute // Should be true to be legitimate
    }

    private fun startMsg(context: Context, num: String, interval: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val currentTime = System.currentTimeMillis()
        val time = currentTime + interval
        val intervalMillis = TimeUnit.MINUTES.toMillis(interval.toLong())

        // Format phone number string to be in phone number format
        val phoneNumber = PhoneNumberUtils.formatNumber(num, Locale.getDefault().country)

        val intent = Intent(context, MainActivity::class.java)

        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time, intervalMillis, pendingIntent)

        // Toast messages using the format: "(425) 555-1212: Are we there yet?".
        Toast.makeText(this, "${phoneNumber}: Are we there yet?", Toast.LENGTH_SHORT).show()
    }
}

