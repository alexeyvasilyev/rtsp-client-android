package net.petrocik.onvif;
//----------------------------------------------------
//
// Generated by www.easywsdl.com
// Version: 6.0.1.1
//
// Created by Quasar Development 
//
//----------------------------------------------------


import java.util.Hashtable;
import org.ksoap2.serialization.*;

public  class RealTimeStreamingCapabilities extends AttributeContainer implements KvmSerializable
{

    
    private transient java.lang.Object __source;    
    
    public Boolean RTPMulticast;
    
    public Boolean RTP_TCP;
    
    public Boolean RTP_RTSP_TCP;
    
    public RealTimeStreamingCapabilitiesExtension Extension;

    

    public void loadFromSoap(java.lang.Object paramObj,ExtendedSoapSerializationEnvelope __envelope)
    {
        if (paramObj == null)
            return;
        AttributeContainer inObj=(AttributeContainer)paramObj;
        __source=inObj; 
        
        if(inObj instanceof SoapObject)
        {
            SoapObject soapObject=(SoapObject)inObj;
            int size = soapObject.getPropertyCount();
            for (int i0=0;i0< size;i0++)
            {
                PropertyInfo info=soapObject.getPropertyInfo(i0);
                if(!loadProperty(info,soapObject,__envelope))
                {
                }
            } 
        }


    }

    
    protected boolean loadProperty(PropertyInfo info,SoapObject soapObject,ExtendedSoapSerializationEnvelope __envelope)
    {
        java.lang.Object obj = info.getValue();
        if (info.name.equals("RTPMulticast"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.RTPMulticast = Boolean.valueOf(j.toString());
                    }
                }
                else if (obj instanceof Boolean){
                    this.RTPMulticast = (Boolean)obj;
                }
            }
            return true;
        }
        if (info.name.equals("RTP_TCP"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.RTP_TCP = Boolean.valueOf(j.toString());
                    }
                }
                else if (obj instanceof Boolean){
                    this.RTP_TCP = (Boolean)obj;
                }
            }
            return true;
        }
        if (info.name.equals("RTP_RTSP_TCP"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.RTP_RTSP_TCP = Boolean.valueOf(j.toString());
                    }
                }
                else if (obj instanceof Boolean){
                    this.RTP_RTSP_TCP = (Boolean)obj;
                }
            }
            return true;
        }
        if (info.name.equals("Extension"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.Extension = (RealTimeStreamingCapabilitiesExtension)__envelope.get(j,RealTimeStreamingCapabilitiesExtension.class,false);
            }
            return true;
        }
        return false;
    }    
    public java.lang.Object getOriginalXmlSource()
    {
        return __source;
    }    
    
    @Override
    public java.lang.Object getProperty(int propertyIndex) {
        //!!!!! If you have a compilation error here then you are using old version of ksoap2 library. Please upgrade to the latest version.
        //!!!!! You can find a correct version in Lib folder from generated zip file!!!!!
        if(propertyIndex==0)
        {
            return this.RTPMulticast!=null?this.RTPMulticast:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==1)
        {
            return this.RTP_TCP!=null?this.RTP_TCP:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==2)
        {
            return this.RTP_RTSP_TCP!=null?this.RTP_RTSP_TCP:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==3)
        {
            return this.Extension!=null?this.Extension:SoapPrimitive.NullSkip;
        }
        return null;
    }


    @Override
    public int getPropertyCount() {
        return 4;
    }

    @Override
    public void getPropertyInfo(int propertyIndex, Hashtable arg1, PropertyInfo info)
    {
            if(propertyIndex==0)
            {
                info.type = PropertyInfo.BOOLEAN_CLASS;
                info.name = "RTPMulticast";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==1)
            {
                info.type = PropertyInfo.BOOLEAN_CLASS;
                info.name = "RTP_TCP";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==2)
            {
                info.type = PropertyInfo.BOOLEAN_CLASS;
                info.name = "RTP_RTSP_TCP";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==3)
            {
                info.type = RealTimeStreamingCapabilitiesExtension.class;
                info.name = "Extension";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
    }

    @Override
    public void setProperty(int arg0, java.lang.Object arg1)
    {
    }

    
}
