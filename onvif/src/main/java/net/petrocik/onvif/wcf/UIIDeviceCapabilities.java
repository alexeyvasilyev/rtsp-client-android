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

public  class UIIDeviceCapabilities extends AttributeContainer implements KvmSerializable
{

    
    private transient java.lang.Object __source;    
    
    public String XAddr;
    
    public UIINetworkCapabilities Network;
    
    public UIISystemCapabilities System;
    
    public UIIIOCapabilities IO;
    
    public UIISecurityCapabilities Security;
    
    public UIIDeviceCapabilitiesExtension Extension;

    

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
        if (info.name.equals("XAddr"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.XAddr = j.toString();
                    }
                }
                else{
                    this.XAddr = obj.toString();
                }
            }
            return true;
        }
        if (info.name.equals("Network"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.Network = (UIINetworkCapabilities)__envelope.get(j,UIINetworkCapabilities.class,false);
            }
            return true;
        }
        if (info.name.equals("System"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.System = (UIISystemCapabilities)__envelope.get(j,UIISystemCapabilities.class,false);
            }
            return true;
        }
        if (info.name.equals("IO"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.IO = (UIIIOCapabilities)__envelope.get(j,UIIIOCapabilities.class,false);
            }
            return true;
        }
        if (info.name.equals("Security"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.Security = (UIISecurityCapabilities)__envelope.get(j,UIISecurityCapabilities.class,false);
            }
            return true;
        }
        if (info.name.equals("Extension"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.Extension = (UIIDeviceCapabilitiesExtension)__envelope.get(j,UIIDeviceCapabilitiesExtension.class,false);
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
            return this.XAddr;
        }
        else if(propertyIndex==1)
        {
            return this.Network!=null?this.Network:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==2)
        {
            return this.System!=null?this.System:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==3)
        {
            return this.IO!=null?this.IO:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==4)
        {
            return this.Security!=null?this.Security:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==5)
        {
            return this.Extension!=null?this.Extension:SoapPrimitive.NullSkip;
        }
        return null;
    }


    @Override
    public int getPropertyCount() {
        return 6;
    }

    @Override
    public void getPropertyInfo(int propertyIndex, Hashtable arg1, PropertyInfo info)
    {
            if(propertyIndex==0)
            {
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "XAddr";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==1)
            {
                info.type = UIINetworkCapabilities.class;
                info.name = "Network";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==2)
            {
                info.type = UIISystemCapabilities.class;
                info.name = "System";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==3)
            {
                info.type = UIIIOCapabilities.class;
                info.name = "IO";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==4)
            {
                info.type = UIISecurityCapabilities.class;
                info.name = "Security";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==5)
            {
                info.type = UIIDeviceCapabilitiesExtension.class;
                info.name = "Extension";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
    }

    @Override
    public void setProperty(int arg0, java.lang.Object arg1)
    {
    }

    
}
