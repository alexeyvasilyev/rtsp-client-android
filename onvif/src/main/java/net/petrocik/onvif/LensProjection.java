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

public  class LensProjection extends AttributeContainer implements KvmSerializable
{

    
    private transient java.lang.Object __source;    
    
    public Float Angle=0f;
    
    public Float Radius=0f;
    
    public Float Transmittance;
    
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
        if (info.name.equals("Angle"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.Angle = Float.valueOf(j.toString());
                    }
                }
                else if (obj instanceof Float){
                    this.Angle = (Float)obj;
                }
            }
            return true;
        }
        if (info.name.equals("Radius"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.Radius = Float.valueOf(j.toString());
                    }
                }
                else if (obj instanceof Float){
                    this.Radius = (Float)obj;
                }
            }
            return true;
        }
        if (info.name.equals("Transmittance"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.Transmittance = Float.valueOf(j.toString());
                    }
                }
                else if (obj instanceof Float){
                    this.Transmittance = (Float)obj;
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
            return this.Angle;
        }
        else if(propertyIndex==1)
        {
            return this.Radius;
        }
        else if(propertyIndex==2)
        {
            return this.Transmittance!=null?this.Transmittance:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex>=3 && propertyIndex < 3+this.any.size())
        {
            return this.any.get(propertyIndex-(3)).getValue();
        }
        return null;
    }


    @Override
    public int getPropertyCount() {
        return 3+ any.size();
    }

    @Override
    public void getPropertyInfo(int propertyIndex, Hashtable arg1, PropertyInfo info)
    {
            if(propertyIndex==0)
            {
                info.type = Float.class;
                info.name = "Angle";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==1)
            {
                info.type = Float.class;
                info.name = "Radius";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==2)
            {
                info.type = Float.class;
                info.name = "Transmittance";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex>=3 && propertyIndex < 3+this.any.size())
            {
                PropertyInfo j=this.any.get(propertyIndex-(3));
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
