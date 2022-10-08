package com.rvirin.onvif.onvifcamera


import android.os.AsyncTask
import android.util.Log
import com.rvirin.onvif.onvifcamera.OnvifDeviceInformation.Companion.deviceInformationCommand
import com.rvirin.onvif.onvifcamera.OnvifDeviceInformation.Companion.deviceInformationToString
import com.rvirin.onvif.onvifcamera.OnvifDeviceInformation.Companion.parseDeviceInformationResponse
import com.rvirin.onvif.onvifcamera.OnvifMediaProfiles.Companion.getProfilesCommand
import com.rvirin.onvif.onvifcamera.OnvifMediaStreamURI.Companion.getStreamURICommand
import com.rvirin.onvif.onvifcamera.OnvifMediaStreamURI.Companion.parseStreamURIXML
import com.rvirin.onvif.onvifcamera.OnvifServices.Companion.servicesCommand
import com.rvirin.onvif.onvifcamera.OnvifXMLBuilder.envelopeEnd
import com.rvirin.onvif.onvifcamera.OnvifXMLBuilder.soapHeader
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.Buffer
import java.io.IOException
import java.util.concurrent.TimeUnit


@JvmField
var currentDevice = OnvifDevice("", "", "")

interface OnvifListener {
    /**
     * Called by OnvifDevice each time a request have been retrieved and parsed.
     * @param response The result of the request also containing the request
     */
    fun requestPerformed(response: OnvifResponse)
}

/**
 * Contains a request for an OnvifDevice
 * @param xmlCommand the xml string to send in the body of the request
 * @param type The type of the request
 */
class OnvifRequest(val xmlCommand: String, val type: Type) {

    enum class Type {
        GetServices,
        GetDeviceInformation,
        GetProfiles,
        GetStreamURI;

        fun namespace(): String {
            when (this) {
                GetServices, GetDeviceInformation -> return "http://www.onvif.org/ver10/device/wsdl"
                GetProfiles, GetStreamURI -> return "http://www.onvif.org/ver20/media/wsdl"
            }
        }
    }
}

/**
 * Paths used to call each differents web services. These paths will be updated
 * by calling getServices.
 */
class OnvifCameraPaths {
    var services = "/onvif/device_service"
    var deviceInformation = "/onvif/device_service"
    var profiles = "/onvif/device_service"
    var streamURI = "/onvif/device_service"
}

/**
 * Contains the response from the Onvif device
 * @param success if the request have been successful
 * @param parsingUIMessage message to be displayed to the user
 * @param result The xml string if the request have been successful
 * @param error The xml string if the request have not been successful
 */
class OnvifResponse(val request: OnvifRequest) {

    var success = false
        private set

    private var message = ""
    var parsingUIMessage = ""

    fun updateResponse(success: Boolean, message: String) {
        this.success = success
        this.message = message
    }

    var result: String? = null
        get() {
            if (success) return message
            else return null
        }

    var error: String? = null
        get() {
            if (!success) return message
            else return null
        }

}

/**
 * @author Remy Virin on 04/03/2018.
 * This class represents an ONVIF device and contains the methods to interact with it
 * (getDeviceInformation, getProfiles and getStreamURI).
 * @param IPAddress The IP address of the camera
 * @param username the username to login on the camera
 * @param password the password to login on the camera
 */
class OnvifDevice(val ipAddress: String, @JvmField val username: String, @JvmField val password: String) {

    var listener: OnvifListener? = null
    /// We use this variable to know if the connection has been successful (retrieve device information)
    var isConnected = false

    private val url = "http://$ipAddress"
    private val deviceInformation = OnvifDeviceInformation()
    private val paths = OnvifCameraPaths()

    var mediaProfiles: List<MediaProfile> = emptyList()

    var rtspURI: String? = null

    fun getServices() {
        val request = OnvifRequest(servicesCommand, OnvifRequest.Type.GetServices)
        ONVIFcommunication().execute(request)
    }

    fun getDeviceInformation() {
        val request = OnvifRequest(deviceInformationCommand, OnvifRequest.Type.GetDeviceInformation)
        ONVIFcommunication().execute(request)
    }

    fun getProfiles() {
        val request = OnvifRequest(getProfilesCommand(), OnvifRequest.Type.GetProfiles)
        ONVIFcommunication().execute(request)
    }

    fun getStreamURI() {

        mediaProfiles.firstOrNull()?.let {
            val request = OnvifRequest(getStreamURICommand(it), OnvifRequest.Type.GetStreamURI)
            ONVIFcommunication().execute(request)
        }
    }

    fun getStreamURI(profile: MediaProfile) {
        val request = OnvifRequest(getStreamURICommand(profile), OnvifRequest.Type.GetStreamURI)
        ONVIFcommunication().execute(request)
    }

    /**
     * Communication in Async Task between Android and ONVIF camera
     */
    private inner class ONVIFcommunication : AsyncTask<OnvifRequest, Void, OnvifResponse>() {

        /**
         * Background process of communication
         *
         * @param params The Onvif Request to execute
         * @return `OnvifResponse`
         */
        override fun doInBackground(vararg params: OnvifRequest): OnvifResponse {
            val onvifRequest = params[0]

            val client = OkHttpClient.Builder()
                    .connectTimeout(10000, TimeUnit.SECONDS)
                    .writeTimeout(100, TimeUnit.SECONDS)
                    .readTimeout(10000, TimeUnit.SECONDS)
                    .build()

            val reqBodyType = MediaType.parse("application/soap+xml; charset=utf-8;")

            val reqBody = RequestBody.create(reqBodyType,
                    soapHeader + onvifRequest.xmlCommand + envelopeEnd)

            /* Request to ONVIF device */
            var request: Request? = null
            try {
                request = Request.Builder()
                        .url(urlForRequest(onvifRequest))
                        .addHeader("Content-Type", "text/xml; charset=utf-8")
                        .post(reqBody)
                        .build()
            } catch (e: IllegalArgumentException) {
                Log.e("ERROR", e.message!!)
                e.printStackTrace()
            }

            var result = OnvifResponse(onvifRequest)

            if (request != null) {
                try {
                    /* Response from ONVIF device */
                    var response = client.newCall(request).execute()
                    Log.d("BODY", bodyToString(request))
                    Log.d("RESPONSE", response.toString())

                    if (response.code() == 401) {
                        /* Retrieve Digest header */
                        val digestHeader = response.header("WWW-Authenticate")
                        val digestInformation = OnvifDigestInformation(username, password, pathForRequest(onvifRequest), digestHeader!!)
                        val authorizationHeader = digestInformation.authorizationHeader
                        Log.d("AUTHORIZATION HEADER", authorizationHeader)

                        try {
                            request = Request.Builder().url(urlForRequest(onvifRequest))
                                    .addHeader("Content-Type", "text/xml; charset=utf-8")
                                    .addHeader("Authorization", authorizationHeader)
                                    .post(reqBody)
                                    .build()
                        } catch (e: IllegalArgumentException) {
                            Log.e("ERROR", e.message!!)
                            e.printStackTrace()
                        }

                        result = OnvifResponse(onvifRequest)
                        response = client.newCall(request).execute()
                    }

                    if (response.code() != 200) {
                        val responseBody = response.body()!!.string()
                        val message = response.code().toString() + " - " + response.message() + "\n" + responseBody
                        result.updateResponse(false, message)
                    } else {
                        result.updateResponse(true, response.body()!!.string())
                        val uiMessage = parseOnvifResponses(result)
                        result.parsingUIMessage = uiMessage
                    }
                } catch (e: IOException) {
                    result.updateResponse(false, e.message!!)
                }
            }

            return result
        }

        /**
         * @return the appropriate URL for calling a web service.
         * Working if the camera is behind a firewall also.
         * @param request the kind of request we're processing.
         */
        fun urlForRequest(request: OnvifRequest): String {
            return currentDevice.url + pathForRequest(request)
        }

        fun pathForRequest(request: OnvifRequest): String {
            when (request.type) {
                OnvifRequest.Type.GetServices -> return currentDevice.paths.services
                OnvifRequest.Type.GetDeviceInformation -> return currentDevice.paths.deviceInformation
                OnvifRequest.Type.GetProfiles -> return currentDevice.paths.profiles
                OnvifRequest.Type.GetStreamURI -> return currentDevice.paths.streamURI
            }
        }


        /**
         * Util function to log the body of a `Request`
         */
        private fun bodyToString(request: Request): String {

            try {
                val copy = request.newBuilder().build()
                val buffer = Buffer()
                copy.body()!!.writeTo(buffer)
                return buffer.readUtf8()
            } catch (e: IOException) {
                return "did not work"
            }
        }

        /**
         * Called when AsyncTask background process is finished
         *
         * @param result the `OnvifResponse`
         */
        override fun onPostExecute(result: OnvifResponse) {
            Log.d("RESULT", result.success.toString())

            listener?.requestPerformed(result)
        }
    }

    /**
     * Util method to append the credentials to the rtsp URI
     * Working if the camera is behind a firewall.
     * @param streamURI the URI to modify
     * @return the rtsp URI with the credentials
     */
    private fun appendCredentials(streamURI: String): String {
        val protocol = "rtsp://"
        val uri = streamURI.substring(protocol.length)
        var port: String = ""

        // Retrieve the rtsp port
        val portIndex = uri.indexOf(":")
        if (portIndex > 0) {
            val portEndIndex = uri.indexOf("/")
            port = uri.substring(portIndex, portEndIndex)
        }

        // path and query
        val path = uri.substringAfter('/')

        // We take the URI passed as an input by the user (in case the
        // camera is behind a firewall).
        val ipAddressWithoutPort = ipAddress.substringBefore(":")

        return protocol + currentDevice.username + ":" + currentDevice.password + "@" +
                ipAddressWithoutPort + port + "/" + path
    }

    /**
     * Parsing method for the SOAP XML response
     * @param result `OnvifResponse` to parse
     */
    private fun parseOnvifResponses(result: OnvifResponse): String {
        var parsedResult = "Parsing failed"
        if (!result.success) {
            parsedResult = "Communication error trying to get " + result.request + ":\n\n" + result.error

        } else {
            if (result.request.type == OnvifRequest.Type.GetServices) {
                result.result?.let {
                    parsedResult = OnvifServices.parseServicesResponse(it, currentDevice.paths)
                }
            } else if (result.request.type == OnvifRequest.Type.GetDeviceInformation) {
                isConnected = true
                if (parseDeviceInformationResponse(result.result!!, currentDevice.deviceInformation)) {
                    parsedResult = deviceInformationToString(currentDevice.deviceInformation)
                }

            } else if (result.request.type == OnvifRequest.Type.GetProfiles) {
                result.result?.let {
                    val profiles = OnvifMediaProfiles.parseXML(it)
                    currentDevice.mediaProfiles = profiles
                    parsedResult = profiles.count().toString() + " profiles retrieved."
                }

            } else if (result.request.type == OnvifRequest.Type.GetStreamURI) {
                result.result?.let {
                    val streamURI = parseStreamURIXML(it)
                    currentDevice.rtspURI = appendCredentials(streamURI)
                    parsedResult = "RTSP URI retrieved."
                }
            }
        }

        return parsedResult
    }
}