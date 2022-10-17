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

public  class VideoRateControl2 extends AttributeContainer implements KvmSerializable
{

    
    private transient java.lang.Object __source;    
    
    public Float FrameRateLimit=0f;
    
    public Integer BitrateLimit=0;
    
    public Boolean ConstantBitRate=false;
    
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

		if (inObj.hasAttribute("ConstantBitRate"))
        {	
            java.lang.Object j = inObj.getAttribute("ConstantBitRate");
            if (j != null)
            {
                ConstantBitRate = Boolean.valueOf(j.toString());
            }
        }

    }

    
    protected boolean loadProperty(PropertyInfo info,SoapObject soapObject,ExtendedSoapSerializationEnvelope __envelope)
    {
        java.lang.Object obj = info.getValue();
        if (info.name.equals("FrameRateLimit"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.FrameRateLimit = Float.valueOf(j.toString());
                    }
                }
                else if (obj instanceof Float){
                    this.FrameRateLimit = (Float)obj;
                }
            }
            return true;
        }
        if (info.name.equals("BitrateLimit"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.BitrateLimit = Integer.parseInt(j.toString());
                    }
                }
                else if (obj instanceof Integer){
                    this.BitrateLimit = (Integer)obj;
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
            return this.FrameRateLimit;
        }
        else if(propertyIndex==1)
        {
            return this.BitrateLimit;
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
                info.type = Float.class;
                info.name = "FrameRateLimit";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==1)
            {
                info.type = PropertyInfo.INTEGER_CLASS;
                info.name = "BitrateLimit";
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



        public int getAttributeCount() {
        return 1;
    }
    
    @Override
    public void getAttributeInfo(int index, AttributeInfo info) {
if(index==0)
        {
            info.name = "ConstantBitRate";
            info.namespace= "";
            if(this.ConstantBitRate!=null)
            {
                info.setValue(this.ConstantBitRate);
            }
        
        }
        
    }

    @Override
    public void getAttribute(int i, AttributeInfo attributeInfo) {

    }

    @Override
    public void setAttribute(AttributeInfo attributeInfo) {

    }    
}
