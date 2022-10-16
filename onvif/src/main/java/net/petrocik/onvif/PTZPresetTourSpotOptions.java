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

public  class PTZPresetTourSpotOptions extends AttributeContainer implements KvmSerializable
{

    
    private transient java.lang.Object __source;    
    
    public PTZPresetTourPresetDetailOptions PresetDetail;
    
    public DurationRange StayTime;
    
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
        if (info.name.equals("PresetDetail"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.PresetDetail = (PTZPresetTourPresetDetailOptions)__envelope.get(j,PTZPresetTourPresetDetailOptions.class,false);
            }
            return true;
        }
        if (info.name.equals("StayTime"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.StayTime = (DurationRange)__envelope.get(j,DurationRange.class,false);
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
            return this.PresetDetail;
        }
        else if(propertyIndex==1)
        {
            return this.StayTime;
        }
        else if(propertyIndex>=2 && propertyIndex < 2+this.any.size())
        {
            return this.any.get(propertyIndex-(2)).getValue();
        }
        return null;
    }


    @Override
    public int getPropertyCount() {
        return 2+ any.size();
    }

    @Override
    public void getPropertyInfo(int propertyIndex, Hashtable arg1, PropertyInfo info)
    {
            if(propertyIndex==0)
            {
                info.type = PTZPresetTourPresetDetailOptions.class;
                info.name = "PresetDetail";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==1)
            {
                info.type = DurationRange.class;
                info.name = "StayTime";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex>=2 && propertyIndex < 2+this.any.size())
            {
                PropertyInfo j=this.any.get(propertyIndex-(2));
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
