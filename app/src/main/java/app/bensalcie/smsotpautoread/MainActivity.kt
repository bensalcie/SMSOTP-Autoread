package app.bensalcie.smsotpautoread

import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import app.bensalcie.smsotpautoread.OTPReceiver.Companion.setOnSendOTPListener
import app.bensalcie.smsotpautoread.ui.theme.SMSOTPAutoreadTheme


class MainActivity : ComponentActivity(), SendOTP {

    private var text = mutableStateOf("Waiting for OTP")
    private lateinit var otpReceiver: OTPReceiver


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestsmspermission()

        // Register Otp Listening.
        registerOtpReceiver()
        enableEdgeToEdge()

        setContent {


            SMSOTPAutoreadTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = text.value,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }


    private fun registerOtpReceiver() {
        otpReceiver = OTPReceiver().apply {
            setOnSendOTPListener(this@MainActivity)
        }
        val filter = IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
        registerReceiver(otpReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(otpReceiver)
    }


    private fun requestsmspermission() {
        val smspermission: String = android.Manifest.permission.RECEIVE_SMS
        val grant = ContextCompat.checkSelfPermission(this, smspermission)
        //check if read SMS permission is granted or not
        if (grant != PackageManager.PERMISSION_GRANTED) {
            val permission_list = arrayOfNulls<String>(1)
            permission_list[0] = smspermission
            ActivityCompat.requestPermissions(this, permission_list, 1)
        }
    }

    override fun onReceiveOTP(otp: String, originator: String?) {
        Log.d("OTPReceiver", "Received OTP: $otp")
        Toast.makeText(
            this@MainActivity,
            "Received OTP: $otp Originator :$originator",
            Toast.LENGTH_SHORT
        ).show()


    }


}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SMSOTPAutoreadTheme {
        Greeting("Android")
    }
}