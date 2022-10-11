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

public  class UIIImagingSettings20 extends AttributeContainer implements KvmSerializable
{

    
    private transient java.lang.Object __source;    
    
    public UIIBacklightCompensation20 BacklightCompensation;
    
    public Float Brightness;
    
    public Float ColorSaturation;
    
    public Float Contrast;
    
    public UIIExposure20 Exposure;
    
    public UIIFocusConfiguration20 Focus;
    
    public UIIEnums.IrCutFilterMode IrCutFilter;
    
    public Float Sharpness;
    
    public UIIWideDynamicRange20 WideDynamicRange;
    
    public UIIWhiteBalance20 WhiteBalance;
    
    public UIIImagingSettingsExtension20 Extension;

    

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
        if (info.name.equals("BacklightCompensation"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.BacklightCompensation = (UIIBacklightCompensation20)__envelope.get(j,UIIBacklightCompensation20.class,false);
            }
            return true;
        }
        if (info.name.equals("Brightness"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.Brightness = Float.valueOf(j.toString());
                    }
                }
                else if (obj instanceof Float){
                    this.Brightness = (Float)obj;
                }
            }
            return true;
        }
        if (info.name.equals("ColorSaturation"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.ColorSaturation = Float.valueOf(j.toString());
                    }
                }
                else if (obj instanceof Float){
                    this.ColorSaturation = (Float)obj;
                }
            }
            return true;
        }
        if (info.name.equals("Contrast"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.Contrast = Float.valueOf(j.toString());
                    }
                }
                else if (obj instanceof Float){
                    this.Contrast = (Float)obj;
                }
            }
            return true;
        }
        if (info.name.equals("Exposure"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.Exposure = (UIIExposure20)__envelope.get(j,UIIExposure20.class,false);
            }
            return true;
        }
        if (info.name.equals("Focus"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.Focus = (UIIFocusConfiguration20)__envelope.get(j,UIIFocusConfiguration20.class,false);
            }
            return true;
        }
        if (info.name.equals("IrCutFilter"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.IrCutFilter = UIIEnums.IrCutFilterMode.fromString(j.toString());
                    }
                }
                else if (obj instanceof UIIEnums.IrCutFilterMode){
                    this.IrCutFilter = (UIIEnums.IrCutFilterMode)obj;
                }
            }
            return true;
        }
        if (info.name.equals("Sharpness"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.Sharpness = Float.valueOf(j.toString());
                    }
                }
                else if (obj instanceof Float){
                    this.Sharpness = (Float)obj;
                }
            }
            return true;
        }
        if (info.name.equals("WideDynamicRange"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.WideDynamicRange = (UIIWideDynamicRange20)__envelope.get(j,UIIWideDynamicRange20.class,false);
            }
            return true;
        }
        if (info.name.equals("WhiteBalance"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.WhiteBalance = (UIIWhiteBalance20)__envelope.get(j,UIIWhiteBalance20.class,false);
            }
            return true;
        }
        if (info.name.equals("Extension"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.Extension = (UIIImagingSettingsExtension20)__envelope.get(j,UIIImagingSettingsExtension20.class,false);
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
            return this.BacklightCompensation!=null?this.BacklightCompensation:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==1)
        {
            return this.Brightness!=null?this.Brightness:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==2)
        {
            return this.ColorSaturation!=null?this.ColorSaturation:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==3)
        {
            return this.Contrast!=null?this.Contrast:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==4)
        {
            return this.Exposure!=null?this.Exposure:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==5)
        {
            return this.Focus!=null?this.Focus:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==6)
        {
            return this.IrCutFilter!=null?this.IrCutFilter.toString():SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==7)
        {
            return this.Sharpness!=null?this.Sharpness:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==8)
        {
            return this.WideDynamicRange!=null?this.WideDynamicRange:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==9)
        {
            return this.WhiteBalance!=null?this.WhiteBalance:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==10)
        {
            return this.Extension!=null?this.Extension:SoapPrimitive.NullSkip;
        }
        return null;
    }


    @Override
    public int getPropertyCount() {
        return 11;
    }

    @Override
    public void getPropertyInfo(int propertyIndex, Hashtable arg1, PropertyInfo info)
    {
            if(propertyIndex==0)
            {
                info.type = UIIBacklightCompensation20.class;
                info.name = "BacklightCompensation";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==1)
            {
                info.type = Float.class;
                info.name = "Brightness";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==2)
            {
                info.type = Float.class;
                info.name = "ColorSaturation";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==3)
            {
                info.type = Float.class;
                info.name = "Contrast";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==4)
            {
                info.type = UIIExposure20.class;
                info.name = "Exposure";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==5)
            {
                info.type = UIIFocusConfiguration20.class;
                info.name = "Focus";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==6)
            {
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "IrCutFilter";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==7)
            {
                info.type = Float.class;
                info.name = "Sharpness";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==8)
            {
                info.type = UIIWideDynamicRange20.class;
                info.name = "WideDynamicRange";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==9)
            {
                info.type = UIIWhiteBalance20.class;
                info.name = "WhiteBalance";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==10)
            {
                info.type = UIIImagingSettingsExtension20.class;
                info.name = "Extension";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
    }

    @Override
    public void setProperty(int arg0, java.lang.Object arg1)
    {
    }

    
}
