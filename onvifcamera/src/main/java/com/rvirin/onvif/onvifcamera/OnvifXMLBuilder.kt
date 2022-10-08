package com.rvirin.onvif.onvifcamera

import android.annotation.SuppressLint
import android.util.Base64.DEFAULT
import android.util.Base64.encodeToString
import org.apache.commons.codec.digest.MessageDigestAlgorithms
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by remy on 09/03/2018.
 */
object OnvifXMLBuilder {

    private var utcTime = getUTCTime()
    private var nonce = java.util.UUID.randomUUID().toString()

    /**
     * The header for SOAP 1.2 with digest authentication
     */
    val soapHeader: String
        get() {
            return "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                    "<soap:Envelope " +
                    "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                    "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
                    "xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" >" +
                    "<soap:Body>"
        }

    val envelopeEnd: String
        get() = "</soap:Body></soap:Envelope>"

    /**
     * Method to encrypt the password for Digest authentication
     */
    private fun encryptPassword(password: String): String? {
        val beforeEncryption = nonce + utcTime + password
        val encryptedRaw: ByteArray
        try {
            val SHA1 = MessageDigest.getInstance(MessageDigestAlgorithms.SHA_1)
            SHA1.reset()
            SHA1.update(beforeEncryption.toByteArray())
            encryptedRaw = SHA1.digest()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            return null
        }

        var encodedString = toBase64(encryptedRaw)
        encodedString = encodedString.replace("\n", "")
        return encodedString
    }


    /**
     * @param message the message to be encoded
     *
     * @return the encoded string from the message
     */
    private fun toBase64(message: String): String? {
        val data: ByteArray
        try {
            data = message.toByteArray(charset("UTF-8"))
            return toBase64(data)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        return null
    }

    private fun toBase64(data: ByteArray): String {
        return encodeToString(data, DEFAULT)
    }

    private fun getUTCTime(): String {
        @SuppressLint("SimpleDateFormat") val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:000'Z'")
        sdf.timeZone = SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC")
        val cal = GregorianCalendar(TimeZone.getTimeZone("UTC"))
        return sdf.format(cal.time)
    }
}
