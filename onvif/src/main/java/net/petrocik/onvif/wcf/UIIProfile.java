package net.petrocik.onvif.wcf;
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

public  class UIIProfile extends AttributeContainer implements KvmSerializable
{

    
    private transient java.lang.Object __source;    
    
    public String Name;
    
    public UIIVideoSourceConfiguration VideoSourceConfiguration;
    
    public UIIAudioSourceConfiguration AudioSourceConfiguration;
    
    public UIIVideoEncoderConfiguration VideoEncoderConfiguration;
    
    public UIIAudioEncoderConfiguration AudioEncoderConfiguration;
    
    public UIIVideoAnalyticsConfiguration VideoAnalyticsConfiguration;
    
    public UIIPTZConfiguration PTZConfiguration;
    
    public UIIMetadataConfiguration MetadataConfiguration;
    
    public UIIProfileExtension Extension;
    
    public String token;
    
    public Boolean _fixed=false;

    

    public void loadFromSoap(java.lang.Object paramObj,UIIExtendedSoapSerializationEnvelope __envelope)
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

		if (inObj.hasAttribute("token"))
        {	
            java.lang.Object j = inObj.getAttribute("token");
            if (j != null)
            {
                token = j.toString();
            }
        }
		if (inObj.hasAttribute("fixed"))
        {	
            java.lang.Object j = inObj.getAttribute("fixed");
            if (j != null)
            {
                _fixed = Boolean.valueOf(j.toString());
            }
        }

    }

    
    protected boolean loadProperty(PropertyInfo info,SoapObject soapObject,UIIExtendedSoapSerializationEnvelope __envelope)
    {
        java.lang.Object obj = info.getValue();
        if (info.name.equals("Name"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.Name = j.toString();
                    }
                }
                else if (obj instanceof String){
                    this.Name = (String)obj;
                }
                else{
                    this.Name = "";
                }
            }
            return true;
        }
        if (info.name.equals("VideoSourceConfiguration"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.VideoSourceConfiguration = (UIIVideoSourceConfiguration)__envelope.get(j,UIIVideoSourceConfiguration.class,false);
            }
            return true;
        }
        if (info.name.equals("AudioSourceConfiguration"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.AudioSourceConfiguration = (UIIAudioSourceConfiguration)__envelope.get(j,UIIAudioSourceConfiguration.class,false);
            }
            return true;
        }
        if (info.name.equals("VideoEncoderConfiguration"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.VideoEncoderConfiguration = (UIIVideoEncoderConfiguration)__envelope.get(j,UIIVideoEncoderConfiguration.class,false);
            }
            return true;
        }
        if (info.name.equals("AudioEncoderConfiguration"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.AudioEncoderConfiguration = (UIIAudioEncoderConfiguration)__envelope.get(j,UIIAudioEncoderConfiguration.class,false);
            }
            return true;
        }
        if (info.name.equals("VideoAnalyticsConfiguration"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.VideoAnalyticsConfiguration = (UIIVideoAnalyticsConfiguration)__envelope.get(j,UIIVideoAnalyticsConfiguration.class,false);
            }
            return true;
        }
        if (info.name.equals("PTZConfiguration"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.PTZConfiguration = (UIIPTZConfiguration)__envelope.get(j,UIIPTZConfiguration.class,false);
            }
            return true;
        }
        if (info.name.equals("MetadataConfiguration"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.MetadataConfiguration = (UIIMetadataConfiguration)__envelope.get(j,UIIMetadataConfiguration.class,false);
            }
            return true;
        }
        if (info.name.equals("Extension"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.Extension = (UIIProfileExtension)__envelope.get(j,UIIProfileExtension.class,false);
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
            return this.Name;
        }
        else if(propertyIndex==1)
        {
            return this.VideoSourceConfiguration!=null?this.VideoSourceConfiguration:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==2)
        {
            return this.AudioSourceConfiguration!=null?this.AudioSourceConfiguration:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==3)
        {
            return this.VideoEncoderConfiguration!=null?this.VideoEncoderConfiguration:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==4)
        {
            return this.AudioEncoderConfiguration!=null?this.AudioEncoderConfiguration:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==5)
        {
            return this.VideoAnalyticsConfiguration!=null?this.VideoAnalyticsConfiguration:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==6)
        {
            return this.PTZConfiguration!=null?this.PTZConfiguration:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==7)
        {
            return this.MetadataConfiguration!=null?this.MetadataConfiguration:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==8)
        {
            return this.Extension!=null?this.Extension:SoapPrimitive.NullSkip;
        }
        return null;
    }


    @Override
    public int getPropertyCount() {
        return 9;
    }

    @Override
    public void getPropertyInfo(int propertyIndex, Hashtable arg1, PropertyInfo info)
    {
            if(propertyIndex==0)
            {
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "Name";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==1)
            {
                info.type = UIIVideoSourceConfiguration.class;
                info.name = "VideoSourceConfiguration";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==2)
            {
                info.type = UIIAudioSourceConfiguration.class;
                info.name = "AudioSourceConfiguration";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==3)
            {
                info.type = UIIVideoEncoderConfiguration.class;
                info.name = "VideoEncoderConfiguration";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==4)
            {
                info.type = UIIAudioEncoderConfiguration.class;
                info.name = "AudioEncoderConfiguration";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==5)
            {
                info.type = UIIVideoAnalyticsConfiguration.class;
                info.name = "VideoAnalyticsConfiguration";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==6)
            {
                info.type = UIIPTZConfiguration.class;
                info.name = "PTZConfiguration";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==7)
            {
                info.type = UIIMetadataConfiguration.class;
                info.name = "MetadataConfiguration";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==8)
            {
                info.type = UIIProfileExtension.class;
                info.name = "Extension";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
    }

    @Override
    public void setProperty(int arg0, java.lang.Object arg1)
    {
    }



        public int getAttributeCount() {
        return 2;
    }
    
    @Override
    public void getAttributeInfo(int index, AttributeInfo info) {
if(index==0)
        {
            info.name = "token";
            info.namespace= "";
            if(this.token!=null)
            {
                info.setValue(this.token);
            }
        
        }
        if(index==1)
        {
            info.name = "fixed";
            info.namespace= "";
            if(this._fixed!=null)
            {
                info.setValue(this._fixed);
            }
        
        }
        
    }

    @Override
    public void getAttribute(int i, AttributeInfo attributeInfo) {

    }

    @Override
    public void setAttribute(AttributeInfo attributeInfo) {

    }    
}
