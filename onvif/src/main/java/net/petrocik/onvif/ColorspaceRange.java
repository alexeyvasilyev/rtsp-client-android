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

public  class ColorspaceRange extends AttributeContainer implements KvmSerializable
{

    
    private transient java.lang.Object __source;    
    
    public FloatRange X;
    
    public FloatRange Y;
    
    public FloatRange Z;
    
    public String Colorspace;

    

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
        if (info.name.equals("X"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.X = (FloatRange)__envelope.get(j,FloatRange.class,false);
            }
            return true;
        }
        if (info.name.equals("Y"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.Y = (FloatRange)__envelope.get(j,FloatRange.class,false);
            }
            return true;
        }
        if (info.name.equals("Z"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.Z = (FloatRange)__envelope.get(j,FloatRange.class,false);
            }
            return true;
        }
        if (info.name.equals("Colorspace"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.Colorspace = j.toString();
                    }
                }
                else{
                    this.Colorspace = obj.toString();
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
            return this.X;
        }
        else if(propertyIndex==1)
        {
            return this.Y;
        }
        else if(propertyIndex==2)
        {
            return this.Z;
        }
        else if(propertyIndex==3)
        {
            return this.Colorspace;
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
                info.type = FloatRange.class;
                info.name = "X";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==1)
            {
                info.type = FloatRange.class;
                info.name = "Y";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==2)
            {
                info.type = FloatRange.class;
                info.name = "Z";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==3)
            {
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "Colorspace";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
    }

    @Override
    public void setProperty(int arg0, java.lang.Object arg1)
    {
    }

    
}
