package com.rvirin.onvif.onvifcamera

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.StringReader

/**
 * Created by Remy Virin on 05/03/2018.
 * @MediaProfile: is used to store an Onvif media profile (token and name)
 * @OnvifMediaProfiles: provide the xml command to retrieve the profiles and its parser.
 */

class MediaProfile(val name: String, val token: String)


class OnvifMediaProfiles {
    companion object {

        fun getProfilesCommand(): String {
            return "<GetProfiles xmlns=\"http://www.onvif.org/ver10/media/wsdl\"/>"
        }

        fun parseXML(toParse: String): List<MediaProfile> {
            val results = ArrayList<MediaProfile>()

            try {
                val factory = XmlPullParserFactory.newInstance()
                factory.isNamespaceAware = true
                val xpp = factory.newPullParser()
                xpp.setInput(StringReader(toParse))
                var eventType = xpp.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {

                    if (eventType == XmlPullParser.START_TAG && xpp.name == "Profiles") {

                        val token = xpp.getAttributeValue(null, "token")
                        xpp.nextTag()
                        if (xpp.name == "Name") {
                            xpp.next()
                            val name = xpp.text
                            val profile = MediaProfile(name, token)
                            results.add(profile)
                        }
                    }
                    eventType = xpp.next()
                }

            } catch (e: XmlPullParserException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return results
        }
    }
}