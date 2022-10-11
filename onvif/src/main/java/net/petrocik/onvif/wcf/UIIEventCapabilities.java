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

public  class UIIEventCapabilities extends AttributeContainer implements KvmSerializable
{

    
    private transient java.lang.Object __source;    
    
    public String XAddr;
    
    public Boolean WSSubscriptionPolicySupport=false;
    
    public Boolean WSPullPointSupport=false;
    
    public Boolean WSPausableSubscriptionManagerInterfaceSupport=false;

    

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
        if (info.name.equals("WSSubscriptionPolicySupport"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.WSSubscriptionPolicySupport = Boolean.valueOf(j.toString());
                    }
                }
                else if (obj instanceof Boolean){
                    this.WSSubscriptionPolicySupport = (Boolean)obj;
                }
            }
            return true;
        }
        if (info.name.equals("WSPullPointSupport"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.WSPullPointSupport = Boolean.valueOf(j.toString());
                    }
                }
                else if (obj instanceof Boolean){
                    this.WSPullPointSupport = (Boolean)obj;
                }
            }
            return true;
        }
        if (info.name.equals("WSPausableSubscriptionManagerInterfaceSupport"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.WSPausableSubscriptionManagerInterfaceSupport = Boolean.valueOf(j.toString());
                    }
                }
                else if (obj instanceof Boolean){
                    this.WSPausableSubscriptionManagerInterfaceSupport = (Boolean)obj;
                }
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
            return this.WSSubscriptionPolicySupport;
        }
        else if(propertyIndex==2)
        {
            return this.WSPullPointSupport;
        }
        else if(propertyIndex==3)
        {
            return this.WSPausableSubscriptionManagerInterfaceSupport;
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
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "XAddr";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==1)
            {
                info.type = PropertyInfo.BOOLEAN_CLASS;
                info.name = "WSSubscriptionPolicySupport";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==2)
            {
                info.type = PropertyInfo.BOOLEAN_CLASS;
                info.name = "WSPullPointSupport";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==3)
            {
                info.type = PropertyInfo.BOOLEAN_CLASS;
                info.name = "WSPausableSubscriptionManagerInterfaceSupport";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
    }

    @Override
    public void setProperty(int arg0, java.lang.Object arg1)
    {
    }

    
}
