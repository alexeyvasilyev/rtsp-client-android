package com.rvirin.onvif.onvifcamera

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.StringReader

/**
 * Created from https://www.onvif.org/ver10/device/wsdl/devicemgmt.wsdl
 *
 * GetDeviceInformation
 * Description:
 * This operation gets basic device information from the device.
 *
 * Input:
 * [GetDeviceInformation]
 *
 * Output:
 * [GetDeviceInformationResponse]
 * Manufacturer [string] - The manufactor of the device.
 * Model [string] - The device model.
 * FirmwareVersion [string] - The firmware version in the device.
 * SerialNumber [string] - The serial number of the device.
 * HardwareId [string] - The hardware ID of the device.
 */

class OnvifDeviceInformation {
    private var manufacturerName = "unknown"
    private var modelName = "unknown"
    private var fwVersion = "unknown"
    private var serialNumber = "unknown"
    private var hwID = "unknown"

    companion object {

        val deviceInformationCommand: String
            get() =
                "<GetDeviceInformation xmlns=\"http://www.onvif.org/ver10/device/wsdl\">" + "</GetDeviceInformation>"

        fun parseDeviceInformationResponse(response: String, parsed: OnvifDeviceInformation): Boolean {

            try {
                val factory = XmlPullParserFactory.newInstance()
                factory.isNamespaceAware = true
                val xpp = factory.newPullParser()
                xpp.setInput(StringReader(response))
                var eventType = xpp.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {

                    if (eventType == XmlPullParser.START_TAG && xpp.name == "Manufacturer") {
                        xpp.next()
                        parsed.manufacturerName = xpp.text
                    } else if (eventType == XmlPullParser.START_TAG && xpp.name == "Model") {
                        xpp.next()
                        parsed.modelName = xpp.text
                    } else if (eventType == XmlPullParser.START_TAG && xpp.name == "FirmwareVersion") {
                        xpp.next()
                        parsed.fwVersion = xpp.text
                    } else if (eventType == XmlPullParser.START_TAG && xpp.name == "SerialNumber") {
                        xpp.next()
                        parsed.serialNumber = xpp.text
                    } else if (eventType == XmlPullParser.START_TAG && xpp.name == "HardwareId") {
                        xpp.next()
                        parsed.hwID = xpp.text
                    }
                    eventType = xpp.next()
                }

            } catch (e: XmlPullParserException) {
                e.printStackTrace()
                return false
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }

            return true
        }

        fun deviceInformationToString(parsed: OnvifDeviceInformation): String {
            var parsedResult = "Device information:\n"
            parsedResult += "Manufacturer: " + parsed.manufacturerName + "\n"
            parsedResult += "Model: " + parsed.modelName + "\n"
            parsedResult += "FirmwareVersion: " + parsed.fwVersion + "\n"
            parsedResult += "SerialNumber: " + parsed.serialNumber + "\n"
            //parsedResult += "HardwareId: " + parsed.hwID + "\n";
            return parsedResult
        }
    }
}
