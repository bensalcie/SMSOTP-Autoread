package app.bensalcie.smsotpautoread

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log

class OTPReceiver : BroadcastReceiver() {
    // OnReceive will keep trace when SMS is received in mobile
    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages: Array<SmsMessage> = Telephony.Sms.Intents.getMessagesFromIntent(intent)

            for (sms in messages) {
                val msg: String = sms.messageBody
                val originator: String? = sms.originatingAddress
                val otp = msg.parseCodeOTP()

                Log.d("Received OTP: ", "onReceiveOTP: Message ${sms.messageBody}")
                Log.d("Received OTP: ", "onReceiveOTP: Originator address ${sms.originatingAddress}")
                Log.d("Received OTP: ", "onReceiveOTP: PseudoSubject ${sms.pseudoSubject}")
                Log.d("Received OTP: ", "onReceiveOTP: EmailFrom ${sms.emailFrom}")
                Log.d("Received OTP: ", "onReceiveOTP: ServiceCenterAddress ${sms.serviceCenterAddress}")

                onsend?.onReceiveOTP(otp, originator)
            }
        }

    }

    companion object {
        private var onsend: SendOTP? = null

        fun setOnSendOTPListener(listener: SendOTP) {
            onsend = listener
        }
    }
}

fun String.parseCodeOTP(): String {
    val regex = Regex("\\d{4,6}")
    val match = regex.find(this)
    return match?.value ?: ""
}

interface SendOTP {
    fun onReceiveOTP(otp: String, originator: String?)
}