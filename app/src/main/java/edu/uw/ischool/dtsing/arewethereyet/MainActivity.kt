package edu.uw.ischool.dtsing.arewethereyet

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Telephony
import android.telephony.PhoneNumberUtils
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.util.Locale
import java.util.concurrent.TimeUnit
import android.Manifest


class MainActivity : AppCompatActivity() {

    private lateinit var messageEditText : EditText
    private lateinit var phoneNumberEditText: EditText
    private lateinit var intervalEditText: EditText
    private lateinit var startButton: Button
    private var receiver : BroadcastReceiver? = null
    private var SMS_PERMISSION_REQUEST_CODE: Int = 101

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
                        intervalEditText.text.toString().toInt(),
                        messageEditText.text.toString()
                    )
                    sendSMS()
                } else { // Otherwise, pop a Toast on why the application cannot start
                    Toast.makeText(
                        this,
                        "Please fill out all fields correctly",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else { // Otherwise, stop the service
                stopService(Intent(this, startButton::class.java))
                Log.i("Stop", "Stop button has been clicked")
                startButton.text = getString(R.string.start)
            }
        }
        checkForSmsPermission()
    }

    private fun checkForSmsPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS),
                SMS_PERMISSION_REQUEST_CODE
            )
        } else {
            startButton.isEnabled = true
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            SMS_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        this@MainActivity,
                        "SMS Permission Granted",
                        Toast.LENGTH_SHORT
                    ).show()
                    startButton.isEnabled = true
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "SMS Permission Denied",
                        Toast.LENGTH_SHORT
                    ).show()
                    startButton.isEnabled = false
                }
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

    private fun startMsg(context: Context, num: String, interval: Int, message: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val currentTime = System.currentTimeMillis()
        val time = currentTime + interval
        val intervalMillis = TimeUnit.MINUTES.toMillis(interval.toLong())

        // Format phone number string to be in phone number format
        val phoneNumber = PhoneNumberUtils.formatNumber(num, Locale.getDefault().country)

        val intent = Intent("edu.uw.ischool.dtsing.ALARM")

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        if (receiver == null) {
            receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val toastMessage = "${phoneNumber}: $message"

                    // Toast messages using the format: "(425) 555-1212: Are we there yet?".
                    // Toast.makeText(this, "${phoneNumber}: Are we there yet?", Toast.LENGTH_SHORT).show()
                    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
                }
            }
            val filter = IntentFilter("edu.uw.ischool.dtsing.ALARM")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED)
            }
        }

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time, intervalMillis, pendingIntent)
    }

    private fun sendSMS() {
        checkForSmsPermission()

        val defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(this)

        val sendIntent = Intent(Intent.ACTION_SEND)
        sendIntent.setType("text/plain")
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Hello there!")

        if (defaultSmsPackageName != null) {
            Log.i("MainActivity","defaultSmsPackageName is $defaultSmsPackageName")
            sendIntent.setPackage(defaultSmsPackageName)
        }
        startActivity(sendIntent)
    }
}