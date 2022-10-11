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
import java.util.ArrayList;
import org.ksoap2.serialization.PropertyInfo;

public  class UIIPullMessagesResponse extends AttributeContainer implements KvmSerializable
{

    
    private transient java.lang.Object __source;    
    
    public java.util.Date CurrentTime;
    
    public java.util.Date TerminationTime;
    
    public java.util.ArrayList< UIINotificationMessageHolderType> NotificationMessage =new java.util.ArrayList<UIINotificationMessageHolderType >();

    

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
        if (info.name.equals("CurrentTime"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.CurrentTime = UIIExtendedSoapSerializationEnvelope.getDateTimeConverter().convertDateTime(j.toString());
                    }
                }
                else if (obj instanceof java.util.Date){
                    this.CurrentTime = (java.util.Date)obj;
                }
            }
            return true;
        }
        if (info.name.equals("TerminationTime"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.TerminationTime = UIIExtendedSoapSerializationEnvelope.getDateTimeConverter().convertDateTime(j.toString());
                    }
                }
                else if (obj instanceof java.util.Date){
                    this.TerminationTime = (java.util.Date)obj;
                }
            }
            return true;
        }
        if (info.name.equals("NotificationMessage"))
        {
            if(obj!=null)
            {
                if(this.NotificationMessage==null)
                {
                    this.NotificationMessage = new java.util.ArrayList< UIINotificationMessageHolderType>();
                }
                java.lang.Object j =obj;
                UIINotificationMessageHolderType j1= (UIINotificationMessageHolderType)__envelope.get(j,UIINotificationMessageHolderType.class,false);
                this.NotificationMessage.add(j1);
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
            return this.CurrentTime!=null?UIIExtendedSoapSerializationEnvelope.getDateTimeConverter().getStringFromDateTime(this.CurrentTime):SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==1)
        {
            return this.TerminationTime!=null?UIIExtendedSoapSerializationEnvelope.getDateTimeConverter().getStringFromDateTime(this.TerminationTime):SoapPrimitive.NullSkip;
        }
        else if(propertyIndex>=2 && propertyIndex < 2+this.NotificationMessage.size())
        {
            UIINotificationMessageHolderType NotificationMessage = this.NotificationMessage.get(propertyIndex-(2));
            return NotificationMessage!=null?NotificationMessage:SoapPrimitive.NullSkip;
        }
        return null;
    }


    @Override
    public int getPropertyCount() {
        return 2+NotificationMessage.size();
    }

    @Override
    public void getPropertyInfo(int propertyIndex, Hashtable arg1, PropertyInfo info)
    {
            if(propertyIndex==0)
            {
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "CurrentTime";
                info.namespace= "http://www.onvif.org/ver10/events/wsdl";
            }
            else if(propertyIndex==1)
            {
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "TerminationTime";
                info.namespace= "http://www.onvif.org/ver10/events/wsdl";
            }
            else if(propertyIndex>=2 && propertyIndex < 2+this.NotificationMessage.size())
            {
                info.type = UIINotificationMessageHolderType.class;
                info.name = "NotificationMessage";
                info.namespace= "http://docs.oasis-open.org/wsn/b-2";
            }
    }

    @Override
    public void setProperty(int arg0, java.lang.Object arg1)
    {
    }

    
}
