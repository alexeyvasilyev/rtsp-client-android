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

public  class WhiteBalanceOptions20 extends AttributeContainer implements KvmSerializable
{

    
    private transient java.lang.Object __source;    
    
    public java.util.ArrayList< Enums.WhiteBalanceMode> Mode =new java.util.ArrayList<Enums.WhiteBalanceMode >();
    
    public FloatRange YrGain;
    
    public FloatRange YbGain;
    
    public WhiteBalanceOptions20Extension Extension;

    

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
        if (info.name.equals("Mode"))
        {
            if(obj!=null)
            {
                if(this.Mode==null)
                {
                    this.Mode = new java.util.ArrayList< Enums.WhiteBalanceMode>();
                }
                java.lang.Object j =obj;
                Enums.WhiteBalanceMode j1= Enums.WhiteBalanceMode.fromString(j.toString());
                this.Mode.add(j1);
            }
            return true;
        }
        if (info.name.equals("YrGain"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.YrGain = (FloatRange)__envelope.get(j,FloatRange.class,false);
            }
            return true;
        }
        if (info.name.equals("YbGain"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.YbGain = (FloatRange)__envelope.get(j,FloatRange.class,false);
            }
            return true;
        }
        if (info.name.equals("Extension"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.Extension = (WhiteBalanceOptions20Extension)__envelope.get(j,WhiteBalanceOptions20Extension.class,false);
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
        if(propertyIndex>=0 && propertyIndex < 0+this.Mode.size())
        {
            Enums.WhiteBalanceMode Mode = this.Mode.get(propertyIndex-(0));
            return Mode!=null?Mode.toString():SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==0+this.Mode.size())
        {
            return this.YrGain!=null?this.YrGain:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==1+this.Mode.size())
        {
            return this.YbGain!=null?this.YbGain:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==2+this.Mode.size())
        {
            return this.Extension!=null?this.Extension:SoapPrimitive.NullSkip;
        }
        return null;
    }


    @Override
    public int getPropertyCount() {
        return 3+Mode.size();
    }

    @Override
    public void getPropertyInfo(int propertyIndex, Hashtable arg1, PropertyInfo info)
    {
            if(propertyIndex>=0 && propertyIndex < 0+this.Mode.size())
            {
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "Mode";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==0+this.Mode.size())
            {
                info.type = FloatRange.class;
                info.name = "YrGain";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==1+this.Mode.size())
            {
                info.type = FloatRange.class;
                info.name = "YbGain";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==2+this.Mode.size())
            {
                info.type = WhiteBalanceOptions20Extension.class;
                info.name = "Extension";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
    }

    @Override
    public void setProperty(int arg0, java.lang.Object arg1)
    {
    }

    
}
