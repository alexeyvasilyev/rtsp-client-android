package com.rvirin.onvif.onvifcamera

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.StringReader

/**
 * Created by Remy Virin on 06/03/2018.
 * @getStreamURICommand: builds the body of the xml request to retrieve to stream URI
 * corresponding to the given profile.
 * @parseStreamURIXML: parses the xml response from the camera and returns the stream URI.
 */
class OnvifMediaStreamURI {

    companion object {

        fun getStreamURICommand(profile: MediaProfile): String {

            return ("<GetStreamUri xmlns=\"http://www.onvif.org/ver20/media/wsdl\">"
                    + "<ProfileToken>" + profile.token + "</ProfileToken>"
                    + "<Protocol>RTSP</Protocol>"
                    + "</GetStreamUri>")
        }

        fun parseStreamURIXML(toParse: String): String {
            var result = ""

            try {
                val factory = XmlPullParserFactory.newInstance()
                factory.isNamespaceAware = true
                val xpp = factory.newPullParser()
                xpp.setInput(StringReader(toParse))
                var eventType = xpp.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {

                    if (eventType == XmlPullParser.START_TAG && xpp.name == "Uri") {

                        xpp.next()
                        result = xpp.text
                        break
                    }
                    eventType = xpp.next()
                }

            } catch (e: XmlPullParserException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return result
        }
    }
}