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

public  class UIIPTControlDirection extends AttributeContainer implements KvmSerializable
{

    
    private transient java.lang.Object __source;    
    
    public UIIEFlip EFlip;
    
    public UIIReverse Reverse;
    
    public UIIPTControlDirectionExtension Extension;

    

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


    }

    
    protected boolean loadProperty(PropertyInfo info,SoapObject soapObject,UIIExtendedSoapSerializationEnvelope __envelope)
    {
        java.lang.Object obj = info.getValue();
        if (info.name.equals("EFlip"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.EFlip = (UIIEFlip)__envelope.get(j,UIIEFlip.class,false);
            }
            return true;
        }
        if (info.name.equals("Reverse"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.Reverse = (UIIReverse)__envelope.get(j,UIIReverse.class,false);
            }
            return true;
        }
        if (info.name.equals("Extension"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.Extension = (UIIPTControlDirectionExtension)__envelope.get(j,UIIPTControlDirectionExtension.class,false);
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
            return this.EFlip!=null?this.EFlip:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==1)
        {
            return this.Reverse!=null?this.Reverse:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==2)
        {
            return this.Extension!=null?this.Extension:SoapPrimitive.NullSkip;
        }
        return null;
    }


    @Override
    public int getPropertyCount() {
        return 3;
    }

    @Override
    public void getPropertyInfo(int propertyIndex, Hashtable arg1, PropertyInfo info)
    {
            if(propertyIndex==0)
            {
                info.type = UIIEFlip.class;
                info.name = "EFlip";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==1)
            {
                info.type = UIIReverse.class;
                info.name = "Reverse";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==2)
            {
                info.type = UIIPTControlDirectionExtension.class;
                info.name = "Extension";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
    }

    @Override
    public void setProperty(int arg0, java.lang.Object arg1)
    {
    }

    
}
