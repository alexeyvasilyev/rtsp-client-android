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

public  class UnacceptableTerminationTimeFaultType extends BaseFaultType implements KvmSerializable
{

    
    public java.util.Date MinimumTime;
    
    public java.util.Date MaximumTime;

    

    @Override
    public void loadFromSoap(java.lang.Object paramObj,ExtendedSoapSerializationEnvelope __envelope)
    {
        if (paramObj == null)
            return;
        AttributeContainer inObj=(AttributeContainer)paramObj;
        super.loadFromSoap(paramObj, __envelope);


    }

    @Override
    protected boolean loadProperty(PropertyInfo info,SoapObject soapObject,ExtendedSoapSerializationEnvelope __envelope)
    {
        java.lang.Object obj = info.getValue();
        if (info.name.equals("MinimumTime"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.MinimumTime = ExtendedSoapSerializationEnvelope.getDateTimeConverter().convertDateTime(j.toString());
                    }
                }
                else if (obj instanceof java.util.Date){
                    this.MinimumTime = (java.util.Date)obj;
                }
            }
            return true;
        }
        if (info.name.equals("MaximumTime"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.MaximumTime = ExtendedSoapSerializationEnvelope.getDateTimeConverter().convertDateTime(j.toString());
                    }
                }
                else if (obj instanceof java.util.Date){
                    this.MaximumTime = (java.util.Date)obj;
                }
            }
            return true;
        }
        return super.loadProperty(info,soapObject,__envelope);
    }    
    
    @Override
    public java.lang.Object getProperty(int propertyIndex) {
        int count = super.getPropertyCount();
        //!!!!! If you have a compilation error here then you are using old version of ksoap2 library. Please upgrade to the latest version.
        //!!!!! You can find a correct version in Lib folder from generated zip file!!!!!
        if(propertyIndex==count+0)
        {
            return this.MinimumTime!=null?ExtendedSoapSerializationEnvelope.getDateTimeConverter().getStringFromDateTime(this.MinimumTime):SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==count+1)
        {
            return this.MaximumTime!=null?ExtendedSoapSerializationEnvelope.getDateTimeConverter().getStringFromDateTime(this.MaximumTime):SoapPrimitive.NullSkip;
        }
        return super.getProperty(propertyIndex);
    }


    @Override
    public int getPropertyCount() {
        return super.getPropertyCount()+2;
    }

    @Override
    public void getPropertyInfo(int propertyIndex, Hashtable arg1, PropertyInfo info)
    {
            int count = super.getPropertyCount();
            if(propertyIndex==count+0)
            {
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "MinimumTime";
                info.namespace= "http://docs.oasis-open.org/wsn/b-2";
            }
            else if(propertyIndex==count+1)
            {
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "MaximumTime";
                info.namespace= "http://docs.oasis-open.org/wsn/b-2";
            }
            super.getPropertyInfo(propertyIndex,arg1,info);
    }

    @Override
    public void setProperty(int arg0, java.lang.Object arg1)
    {
    }

    
}
