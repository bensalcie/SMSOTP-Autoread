# Sample Implementation for SMSOTP Autoread on Android 
### The sample over here demonstrates how to use the google SMS Retriever API by google.
### You get to filter out any particular sender ID that you expect to read from
<img width="330" alt="Screenshot 2024-06-27 at 13 33 01" src="https://github.com/bensalcie/SMSOTP-Autoread/assets/17502827/0078a9a7-d4f7-45d7-8c07-d33c60c1316b">

<img width="330" alt="Screenshot 2024-06-27 at 13 34 17" src="https://github.com/bensalcie/SMSOTP-Autoread/assets/17502827/9bd21810-5698-478b-bb4d-eb5d66f9e67c">
<img width="330" alt="Screenshot 2024-06-27 at 13 39 41" src="https://github.com/bensalcie/SMSOTP-Autoread/assets/17502827/b5a60d5a-256c-4f2b-893e-f2e80f93258b">




#
#
 - Please Note that with Implementations requiring  you to use the following permissions, you need very concrete reasons to defend them on google play console.
 - SMS Retriever API does not require any permissions, just registering the broadcast reciever at run time.


```
    <uses-permission android:name="android.permission.WRITE_SMS"/>
    <uses-permission android:name="android.permission.READ_SMS"/>
    <uses-permission android:name="android.permission.RECEIVE_SMS"/>

```
# Setup Project.
1.  Add to your build.gradle (app module)
```
    implementation(libs.play.services.auth.api.phone)
```
2. Add the depedancy router in the libs.versions.toml.

```
// Versions.
playServicesAuthApiPhone = "18.1.0"

// Libraries
play-services-auth-api-phone = { group = "com.google.android.gms", name = "play-services-auth-api-phone", version.ref = "playServicesAuthApiPhone" }

```

3. Example of what it should look like.

```
[versions]
agp = "8.4.1"
kotlin = "1.9.0"
coreKtx = "1.13.1"
junit = "4.13.2"
junitVersion = "1.1.5"
espressoCore = "3.5.1"
lifecycleRuntimeKtx = "2.6.1"
activityCompose = "1.8.0"
composeBom = "2023.08.00"
playServicesAuthApiPhone = "18.1.0"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
androidx-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }
play-services-auth-api-phone = { group = "com.google.android.gms", name = "play-services-auth-api-phone", version.ref = "playServicesAuthApiPhone" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
jetbrains-kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }



```

4. Initialize your BroadCast Receiver.

Our Broadcast Receiver will be responsible for collecting information about the recieved SMS after our filter is applied [Note filter can be Null]

```
    private var otpSmsVerificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (context == null || intent == null) {
                Log.d("OTPRES", "\": taskSms: Failed\"")
                return
            }

            if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
                val extras: Bundle = intent.extras!!
                val smsRetrieverStatus: Status = extras.get(SmsRetriever.EXTRA_STATUS) as Status

                when (smsRetrieverStatus.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        val intentConsent: Intent =
                            extras.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT)!!
                        try {
                            // Start activity to show consent dialog to user, activity must be started in
                            // 5 minutes, otherwise you'll receive another TIMEOUT intent
                            getActivityResult.launch(intentConsent)
                        } catch (e: Exception) {
                            Log.d("OTPRES", "\": taskSms: failed\"")
                        }
                    }

                    CommonStatusCodes.TIMEOUT -> {
                        Log.d("OTPRES", "\": taskSms: timout\"")
                    }

                    CommonStatusCodes.ERROR -> {
                        Log.d("OTPRES", "\": taskSms: error\"")

                    }
                }
            }
        }
    }
```

5. Initialize the Concent Dialog, and let user Accept or Reject .

```
      SmsRetriever.getClient(this).apply {
            startSmsUserConsent("My Filter e.g MARAMOJA, +254704808070").apply {
                addOnSuccessListener { Log.d("OTPRES", "\": taskSms: OnSuccess\"") }
                addOnFailureListener {
                    Log.d("OTPRES", "\": taskSms: fAILED\"")
                }
                addOnCanceledListener {
                    // Let User know they can contact customer care.
                    Log.d("OTPRES", "\": taskSms: Cancelled\"")
                }
            }
        }

```

6. In your on Resume function, Register the Reciever: (Remember to Unregister the reciever when you nolonger need it.)
```
    override fun onResume() {
        super.onResume()
                registerReceiver(
                    otpSmsVerificationReceiver,
                    intentFilter,
                    Context.RECEIVER_EXPORTED
                )

        }
    }

```

7. In your on Pause/ Destroy functions - Unregister the listener.

```
 override fun onPause() {
        super.onPause()
        unregisterReceiver(otpSmsVerificationReceiver)

    }

```
8. Get the results of Autoread message - At this point is when you verify with what your server expects. 
```
    private val getActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            try {
                if (it.resultCode == Activity.RESULT_OK) {
                    val gotSmsText = it.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                    val subScriptionID = it.data?.getStringExtra(SmsRetriever.EXTRA_SIM_SUBSCRIPTION_ID)
                    Toast.makeText(this, "Sender Subscription ID: $subScriptionID", Toast.LENGTH_SHORT).show()
                    if (!gotSmsText.isNullOrBlank()) {
                        val otp = gotSmsText.parseCodeOTP()
                        Toast.makeText(this, "OTP Recieved $otp", Toast.LENGTH_SHORT).show()


                    }
                }
            } catch (exception: Exception) {
                Log.d("OTP", exception.localizedMessage)
            }
        }
```
9. Sample Util to parse retrieved OTP.

```

fun String.parseCodeOTP(): String {
    val regex = Regex("\\d{4,6}")
    val match = regex.find(this)
    return match?.value ?: ""
}
```
10. Happy Coding 🚀 😊



