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
import java.util.ArrayList;
import org.ksoap2.serialization.PropertyInfo;

public  class AudioSourceConfigurationOptions extends AttributeContainer implements KvmSerializable
{

    
    private transient java.lang.Object __source;    
    
    public java.util.ArrayList< String> InputTokensAvailable =new java.util.ArrayList<String >();
    
    public AudioSourceOptionsExtension Extension;

    

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
        if (info.name.equals("InputTokensAvailable"))
        {
            if(obj!=null)
            {
                if(this.InputTokensAvailable==null)
                {
                    this.InputTokensAvailable = new java.util.ArrayList< String>();
                }
                java.lang.Object j =obj;
                String j1= j.toString();
                this.InputTokensAvailable.add(j1);
            }
            return true;
        }
        if (info.name.equals("Extension"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.Extension = (AudioSourceOptionsExtension)__envelope.get(j,AudioSourceOptionsExtension.class,false);
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
        if(propertyIndex>=0 && propertyIndex < 0+this.InputTokensAvailable.size())
        {
            String InputTokensAvailable = this.InputTokensAvailable.get(propertyIndex-(0));
            return InputTokensAvailable;
        }
        else if(propertyIndex==0+this.InputTokensAvailable.size())
        {
            return this.Extension!=null?this.Extension:SoapPrimitive.NullSkip;
        }
        return null;
    }


    @Override
    public int getPropertyCount() {
        return 1+InputTokensAvailable.size();
    }

    @Override
    public void getPropertyInfo(int propertyIndex, Hashtable arg1, PropertyInfo info)
    {
            if(propertyIndex>=0 && propertyIndex < 0+this.InputTokensAvailable.size())
            {
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "InputTokensAvailable";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==0+this.InputTokensAvailable.size())
            {
                info.type = AudioSourceOptionsExtension.class;
                info.name = "Extension";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
    }

    @Override
    public void setProperty(int arg0, java.lang.Object arg1)
    {
    }

    
}
