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

public  class Service extends AttributeContainer implements KvmSerializable
{

    
    private transient java.lang.Object __source;    
    
    public String Namespace;
    
    public String XAddr;
    
    public Service_Capabilities Capabilities;
    
    public OnvifVersion Version;
    
    public java.util.ArrayList< PropertyInfo> any =new java.util.ArrayList< PropertyInfo>();

    

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
                    info= __envelope.getAny(info);
                    this.any.add(info);
                }
            } 
        }


    }

    
    protected boolean loadProperty(PropertyInfo info,SoapObject soapObject,ExtendedSoapSerializationEnvelope __envelope)
    {
        java.lang.Object obj = info.getValue();
        if (info.name.equals("Namespace"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.Namespace = j.toString();
                    }
                }
                else{
                    this.Namespace = obj.toString();
                }
            }
            return true;
        }
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
        if (info.name.equals("Capabilities"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.Capabilities = (Service_Capabilities)__envelope.get(j,Service_Capabilities.class,false);
            }
            return true;
        }
        if (info.name.equals("Version"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.Version = (OnvifVersion)__envelope.get(j,OnvifVersion.class,false);
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
            return this.Namespace;
        }
        else if(propertyIndex==1)
        {
            return this.XAddr;
        }
        else if(propertyIndex==2)
        {
            return this.Capabilities!=null?this.Capabilities:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==3)
        {
            return this.Version;
        }
        else if(propertyIndex>=4 && propertyIndex < 4+this.any.size())
        {
            return this.any.get(propertyIndex-(4)).getValue();
        }
        return null;
    }


    @Override
    public int getPropertyCount() {
        return 4+ any.size();
    }

    @Override
    public void getPropertyInfo(int propertyIndex, Hashtable arg1, PropertyInfo info)
    {
            if(propertyIndex==0)
            {
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "Namespace";
                info.namespace= "http://www.onvif.org/ver10/device/wsdl";
            }
            else if(propertyIndex==1)
            {
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "XAddr";
                info.namespace= "http://www.onvif.org/ver10/device/wsdl";
            }
            else if(propertyIndex==2)
            {
                info.type = Service_Capabilities.class;
                info.name = "Capabilities";
                info.namespace= "http://www.onvif.org/ver10/device/wsdl";
            }
            else if(propertyIndex==3)
            {
                info.type = OnvifVersion.class;
                info.name = "Version";
                info.namespace= "http://www.onvif.org/ver10/device/wsdl";
            }
            else if(propertyIndex>=4 && propertyIndex < 4+this.any.size())
            {
                PropertyInfo j=this.any.get(propertyIndex-(4));
                info.type = j.type;
                info.name = j.name;
                info.namespace= j.namespace;
            }
    }

    @Override
    public void setProperty(int arg0, java.lang.Object arg1)
    {
    }

    
}
