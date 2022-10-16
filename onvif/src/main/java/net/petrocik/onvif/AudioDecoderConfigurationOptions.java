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

public  class AudioDecoderConfigurationOptions extends AttributeContainer implements KvmSerializable
{

    
    private transient java.lang.Object __source;    
    
    public AACDecOptions AACDecOptions;
    
    public G711DecOptions G711DecOptions;
    
    public G726DecOptions G726DecOptions;
    
    public AudioDecoderConfigurationOptionsExtension Extension;

    

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
        if (info.name.equals("AACDecOptions"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.AACDecOptions = (AACDecOptions)__envelope.get(j,AACDecOptions.class,false);
            }
            return true;
        }
        if (info.name.equals("G711DecOptions"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.G711DecOptions = (G711DecOptions)__envelope.get(j,G711DecOptions.class,false);
            }
            return true;
        }
        if (info.name.equals("G726DecOptions"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.G726DecOptions = (G726DecOptions)__envelope.get(j,G726DecOptions.class,false);
            }
            return true;
        }
        if (info.name.equals("Extension"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.Extension = (AudioDecoderConfigurationOptionsExtension)__envelope.get(j,AudioDecoderConfigurationOptionsExtension.class,false);
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
            return this.AACDecOptions!=null?this.AACDecOptions:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==1)
        {
            return this.G711DecOptions!=null?this.G711DecOptions:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==2)
        {
            return this.G726DecOptions!=null?this.G726DecOptions:SoapPrimitive.NullSkip;
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
                info.type = AACDecOptions.class;
                info.name = "AACDecOptions";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==1)
            {
                info.type = G711DecOptions.class;
                info.name = "G711DecOptions";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==2)
            {
                info.type = G726DecOptions.class;
                info.name = "G726DecOptions";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==3)
            {
                info.type = AudioDecoderConfigurationOptionsExtension.class;
                info.name = "Extension";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
    }

    @Override
    public void setProperty(int arg0, java.lang.Object arg1)
    {
    }

    
}
