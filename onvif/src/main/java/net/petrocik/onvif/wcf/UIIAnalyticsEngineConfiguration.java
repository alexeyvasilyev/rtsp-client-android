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

public  class UIIAnalyticsEngineConfiguration extends AttributeContainer implements KvmSerializable
{

    
    private transient java.lang.Object __source;    
    
    public java.util.ArrayList< UIIConfig> AnalyticsModule =new java.util.ArrayList<UIIConfig >();
    
    public UIIAnalyticsEngineConfigurationExtension Extension;

    

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
        if (info.name.equals("AnalyticsModule"))
        {
            if(obj!=null)
            {
                if(this.AnalyticsModule==null)
                {
                    this.AnalyticsModule = new java.util.ArrayList< UIIConfig>();
                }
                java.lang.Object j =obj;
                UIIConfig j1= (UIIConfig)__envelope.get(j,UIIConfig.class,false);
                this.AnalyticsModule.add(j1);
            }
            return true;
        }
        if (info.name.equals("Extension"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.Extension = (UIIAnalyticsEngineConfigurationExtension)__envelope.get(j,UIIAnalyticsEngineConfigurationExtension.class,false);
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
        if(propertyIndex>=0 && propertyIndex < 0+this.AnalyticsModule.size())
        {
            UIIConfig AnalyticsModule = this.AnalyticsModule.get(propertyIndex-(0));
            return AnalyticsModule!=null?AnalyticsModule:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==0+this.AnalyticsModule.size())
        {
            return this.Extension!=null?this.Extension:SoapPrimitive.NullSkip;
        }
        return null;
    }


    @Override
    public int getPropertyCount() {
        return 1+AnalyticsModule.size();
    }

    @Override
    public void getPropertyInfo(int propertyIndex, Hashtable arg1, PropertyInfo info)
    {
            if(propertyIndex>=0 && propertyIndex < 0+this.AnalyticsModule.size())
            {
                info.type = UIIConfig.class;
                info.name = "AnalyticsModule";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==0+this.AnalyticsModule.size())
            {
                info.type = UIIAnalyticsEngineConfigurationExtension.class;
                info.name = "Extension";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
    }

    @Override
    public void setProperty(int arg0, java.lang.Object arg1)
    {
    }

    
}
