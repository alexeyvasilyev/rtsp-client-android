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

public  class PTZSpaces extends AttributeContainer implements KvmSerializable
{

    
    private transient java.lang.Object __source;    
    
    public java.util.ArrayList< Space2DDescription> AbsolutePanTiltPositionSpace =new java.util.ArrayList<Space2DDescription >();
    
    public java.util.ArrayList< Space1DDescription> AbsoluteZoomPositionSpace =new java.util.ArrayList<Space1DDescription >();
    
    public java.util.ArrayList< Space2DDescription> RelativePanTiltTranslationSpace =new java.util.ArrayList<Space2DDescription >();
    
    public java.util.ArrayList< Space1DDescription> RelativeZoomTranslationSpace =new java.util.ArrayList<Space1DDescription >();
    
    public java.util.ArrayList< Space2DDescription> ContinuousPanTiltVelocitySpace =new java.util.ArrayList<Space2DDescription >();
    
    public java.util.ArrayList< Space1DDescription> ContinuousZoomVelocitySpace =new java.util.ArrayList<Space1DDescription >();
    
    public java.util.ArrayList< Space1DDescription> PanTiltSpeedSpace =new java.util.ArrayList<Space1DDescription >();
    
    public java.util.ArrayList< Space1DDescription> ZoomSpeedSpace =new java.util.ArrayList<Space1DDescription >();
    
    public PTZSpacesExtension Extension;

    

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
        if (info.name.equals("AbsolutePanTiltPositionSpace"))
        {
            if(obj!=null)
            {
                if(this.AbsolutePanTiltPositionSpace==null)
                {
                    this.AbsolutePanTiltPositionSpace = new java.util.ArrayList< Space2DDescription>();
                }
                java.lang.Object j =obj;
                Space2DDescription j1= (Space2DDescription)__envelope.get(j,Space2DDescription.class,false);
                this.AbsolutePanTiltPositionSpace.add(j1);
            }
            return true;
        }
        if (info.name.equals("AbsoluteZoomPositionSpace"))
        {
            if(obj!=null)
            {
                if(this.AbsoluteZoomPositionSpace==null)
                {
                    this.AbsoluteZoomPositionSpace = new java.util.ArrayList< Space1DDescription>();
                }
                java.lang.Object j =obj;
                Space1DDescription j1= (Space1DDescription)__envelope.get(j,Space1DDescription.class,false);
                this.AbsoluteZoomPositionSpace.add(j1);
            }
            return true;
        }
        if (info.name.equals("RelativePanTiltTranslationSpace"))
        {
            if(obj!=null)
            {
                if(this.RelativePanTiltTranslationSpace==null)
                {
                    this.RelativePanTiltTranslationSpace = new java.util.ArrayList< Space2DDescription>();
                }
                java.lang.Object j =obj;
                Space2DDescription j1= (Space2DDescription)__envelope.get(j,Space2DDescription.class,false);
                this.RelativePanTiltTranslationSpace.add(j1);
            }
            return true;
        }
        if (info.name.equals("RelativeZoomTranslationSpace"))
        {
            if(obj!=null)
            {
                if(this.RelativeZoomTranslationSpace==null)
                {
                    this.RelativeZoomTranslationSpace = new java.util.ArrayList< Space1DDescription>();
                }
                java.lang.Object j =obj;
                Space1DDescription j1= (Space1DDescription)__envelope.get(j,Space1DDescription.class,false);
                this.RelativeZoomTranslationSpace.add(j1);
            }
            return true;
        }
        if (info.name.equals("ContinuousPanTiltVelocitySpace"))
        {
            if(obj!=null)
            {
                if(this.ContinuousPanTiltVelocitySpace==null)
                {
                    this.ContinuousPanTiltVelocitySpace = new java.util.ArrayList< Space2DDescription>();
                }
                java.lang.Object j =obj;
                Space2DDescription j1= (Space2DDescription)__envelope.get(j,Space2DDescription.class,false);
                this.ContinuousPanTiltVelocitySpace.add(j1);
            }
            return true;
        }
        if (info.name.equals("ContinuousZoomVelocitySpace"))
        {
            if(obj!=null)
            {
                if(this.ContinuousZoomVelocitySpace==null)
                {
                    this.ContinuousZoomVelocitySpace = new java.util.ArrayList< Space1DDescription>();
                }
                java.lang.Object j =obj;
                Space1DDescription j1= (Space1DDescription)__envelope.get(j,Space1DDescription.class,false);
                this.ContinuousZoomVelocitySpace.add(j1);
            }
            return true;
        }
        if (info.name.equals("PanTiltSpeedSpace"))
        {
            if(obj!=null)
            {
                if(this.PanTiltSpeedSpace==null)
                {
                    this.PanTiltSpeedSpace = new java.util.ArrayList< Space1DDescription>();
                }
                java.lang.Object j =obj;
                Space1DDescription j1= (Space1DDescription)__envelope.get(j,Space1DDescription.class,false);
                this.PanTiltSpeedSpace.add(j1);
            }
            return true;
        }
        if (info.name.equals("ZoomSpeedSpace"))
        {
            if(obj!=null)
            {
                if(this.ZoomSpeedSpace==null)
                {
                    this.ZoomSpeedSpace = new java.util.ArrayList< Space1DDescription>();
                }
                java.lang.Object j =obj;
                Space1DDescription j1= (Space1DDescription)__envelope.get(j,Space1DDescription.class,false);
                this.ZoomSpeedSpace.add(j1);
            }
            return true;
        }
        if (info.name.equals("Extension"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.Extension = (PTZSpacesExtension)__envelope.get(j,PTZSpacesExtension.class,false);
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
        if(propertyIndex>=0 && propertyIndex < 0+this.AbsolutePanTiltPositionSpace.size())
        {
            Space2DDescription AbsolutePanTiltPositionSpace = this.AbsolutePanTiltPositionSpace.get(propertyIndex-(0));
            return AbsolutePanTiltPositionSpace!=null?AbsolutePanTiltPositionSpace:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex>=0+this.AbsolutePanTiltPositionSpace.size() && propertyIndex < 0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size())
        {
            Space1DDescription AbsoluteZoomPositionSpace = this.AbsoluteZoomPositionSpace.get(propertyIndex-(0+this.AbsolutePanTiltPositionSpace.size()));
            return AbsoluteZoomPositionSpace!=null?AbsoluteZoomPositionSpace:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex>=0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size() && propertyIndex < 0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()+this.RelativePanTiltTranslationSpace.size())
        {
            Space2DDescription RelativePanTiltTranslationSpace = this.RelativePanTiltTranslationSpace.get(propertyIndex-(0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()));
            return RelativePanTiltTranslationSpace!=null?RelativePanTiltTranslationSpace:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex>=0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()+this.RelativePanTiltTranslationSpace.size() && propertyIndex < 0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()+this.RelativePanTiltTranslationSpace.size()+this.RelativeZoomTranslationSpace.size())
        {
            Space1DDescription RelativeZoomTranslationSpace = this.RelativeZoomTranslationSpace.get(propertyIndex-(0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()+this.RelativePanTiltTranslationSpace.size()));
            return RelativeZoomTranslationSpace!=null?RelativeZoomTranslationSpace:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex>=0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()+this.RelativePanTiltTranslationSpace.size()+this.RelativeZoomTranslationSpace.size() && propertyIndex < 0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()+this.RelativePanTiltTranslationSpace.size()+this.RelativeZoomTranslationSpace.size()+this.ContinuousPanTiltVelocitySpace.size())
        {
            Space2DDescription ContinuousPanTiltVelocitySpace = this.ContinuousPanTiltVelocitySpace.get(propertyIndex-(0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()+this.RelativePanTiltTranslationSpace.size()+this.RelativeZoomTranslationSpace.size()));
            return ContinuousPanTiltVelocitySpace!=null?ContinuousPanTiltVelocitySpace:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex>=0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()+this.RelativePanTiltTranslationSpace.size()+this.RelativeZoomTranslationSpace.size()+this.ContinuousPanTiltVelocitySpace.size() && propertyIndex < 0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()+this.RelativePanTiltTranslationSpace.size()+this.RelativeZoomTranslationSpace.size()+this.ContinuousPanTiltVelocitySpace.size()+this.ContinuousZoomVelocitySpace.size())
        {
            Space1DDescription ContinuousZoomVelocitySpace = this.ContinuousZoomVelocitySpace.get(propertyIndex-(0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()+this.RelativePanTiltTranslationSpace.size()+this.RelativeZoomTranslationSpace.size()+this.ContinuousPanTiltVelocitySpace.size()));
            return ContinuousZoomVelocitySpace!=null?ContinuousZoomVelocitySpace:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex>=0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()+this.RelativePanTiltTranslationSpace.size()+this.RelativeZoomTranslationSpace.size()+this.ContinuousPanTiltVelocitySpace.size()+this.ContinuousZoomVelocitySpace.size() && propertyIndex < 0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()+this.RelativePanTiltTranslationSpace.size()+this.RelativeZoomTranslationSpace.size()+this.ContinuousPanTiltVelocitySpace.size()+this.ContinuousZoomVelocitySpace.size()+this.PanTiltSpeedSpace.size())
        {
            Space1DDescription PanTiltSpeedSpace = this.PanTiltSpeedSpace.get(propertyIndex-(0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()+this.RelativePanTiltTranslationSpace.size()+this.RelativeZoomTranslationSpace.size()+this.ContinuousPanTiltVelocitySpace.size()+this.ContinuousZoomVelocitySpace.size()));
            return PanTiltSpeedSpace!=null?PanTiltSpeedSpace:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex>=0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()+this.RelativePanTiltTranslationSpace.size()+this.RelativeZoomTranslationSpace.size()+this.ContinuousPanTiltVelocitySpace.size()+this.ContinuousZoomVelocitySpace.size()+this.PanTiltSpeedSpace.size() && propertyIndex < 0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()+this.RelativePanTiltTranslationSpace.size()+this.RelativeZoomTranslationSpace.size()+this.ContinuousPanTiltVelocitySpace.size()+this.ContinuousZoomVelocitySpace.size()+this.PanTiltSpeedSpace.size()+this.ZoomSpeedSpace.size())
        {
            Space1DDescription ZoomSpeedSpace = this.ZoomSpeedSpace.get(propertyIndex-(0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()+this.RelativePanTiltTranslationSpace.size()+this.RelativeZoomTranslationSpace.size()+this.ContinuousPanTiltVelocitySpace.size()+this.ContinuousZoomVelocitySpace.size()+this.PanTiltSpeedSpace.size()));
            return ZoomSpeedSpace!=null?ZoomSpeedSpace:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()+this.RelativePanTiltTranslationSpace.size()+this.RelativeZoomTranslationSpace.size()+this.ContinuousPanTiltVelocitySpace.size()+this.ContinuousZoomVelocitySpace.size()+this.PanTiltSpeedSpace.size()+this.ZoomSpeedSpace.size())
        {
            return this.Extension!=null?this.Extension:SoapPrimitive.NullSkip;
        }
        return null;
    }


    @Override
    public int getPropertyCount() {
        return 1+AbsolutePanTiltPositionSpace.size()+AbsoluteZoomPositionSpace.size()+RelativePanTiltTranslationSpace.size()+RelativeZoomTranslationSpace.size()+ContinuousPanTiltVelocitySpace.size()+ContinuousZoomVelocitySpace.size()+PanTiltSpeedSpace.size()+ZoomSpeedSpace.size();
    }

    @Override
    public void getPropertyInfo(int propertyIndex, Hashtable arg1, PropertyInfo info)
    {
            if(propertyIndex>=0 && propertyIndex < 0+this.AbsolutePanTiltPositionSpace.size())
            {
                info.type = Space2DDescription.class;
                info.name = "AbsolutePanTiltPositionSpace";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex>=0+this.AbsolutePanTiltPositionSpace.size() && propertyIndex < 0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size())
            {
                info.type = Space1DDescription.class;
                info.name = "AbsoluteZoomPositionSpace";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex>=0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size() && propertyIndex < 0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()+this.RelativePanTiltTranslationSpace.size())
            {
                info.type = Space2DDescription.class;
                info.name = "RelativePanTiltTranslationSpace";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex>=0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()+this.RelativePanTiltTranslationSpace.size() && propertyIndex < 0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()+this.RelativePanTiltTranslationSpace.size()+this.RelativeZoomTranslationSpace.size())
            {
                info.type = Space1DDescription.class;
                info.name = "RelativeZoomTranslationSpace";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex>=0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()+this.RelativePanTiltTranslationSpace.size()+this.RelativeZoomTranslationSpace.size() && propertyIndex < 0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()+this.RelativePanTiltTranslationSpace.size()+this.RelativeZoomTranslationSpace.size()+this.ContinuousPanTiltVelocitySpace.size())
            {
                info.type = Space2DDescription.class;
                info.name = "ContinuousPanTiltVelocitySpace";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex>=0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()+this.RelativePanTiltTranslationSpace.size()+this.RelativeZoomTranslationSpace.size()+this.ContinuousPanTiltVelocitySpace.size() && propertyIndex < 0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()+this.RelativePanTiltTranslationSpace.size()+this.RelativeZoomTranslationSpace.size()+this.ContinuousPanTiltVelocitySpace.size()+this.ContinuousZoomVelocitySpace.size())
            {
                info.type = Space1DDescription.class;
                info.name = "ContinuousZoomVelocitySpace";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex>=0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()+this.RelativePanTiltTranslationSpace.size()+this.RelativeZoomTranslationSpace.size()+this.ContinuousPanTiltVelocitySpace.size()+this.ContinuousZoomVelocitySpace.size() && propertyIndex < 0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()+this.RelativePanTiltTranslationSpace.size()+this.RelativeZoomTranslationSpace.size()+this.ContinuousPanTiltVelocitySpace.size()+this.ContinuousZoomVelocitySpace.size()+this.PanTiltSpeedSpace.size())
            {
                info.type = Space1DDescription.class;
                info.name = "PanTiltSpeedSpace";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex>=0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()+this.RelativePanTiltTranslationSpace.size()+this.RelativeZoomTranslationSpace.size()+this.ContinuousPanTiltVelocitySpace.size()+this.ContinuousZoomVelocitySpace.size()+this.PanTiltSpeedSpace.size() && propertyIndex < 0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()+this.RelativePanTiltTranslationSpace.size()+this.RelativeZoomTranslationSpace.size()+this.ContinuousPanTiltVelocitySpace.size()+this.ContinuousZoomVelocitySpace.size()+this.PanTiltSpeedSpace.size()+this.ZoomSpeedSpace.size())
            {
                info.type = Space1DDescription.class;
                info.name = "ZoomSpeedSpace";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==0+this.AbsolutePanTiltPositionSpace.size()+this.AbsoluteZoomPositionSpace.size()+this.RelativePanTiltTranslationSpace.size()+this.RelativeZoomTranslationSpace.size()+this.ContinuousPanTiltVelocitySpace.size()+this.ContinuousZoomVelocitySpace.size()+this.PanTiltSpeedSpace.size()+this.ZoomSpeedSpace.size())
            {
                info.type = PTZSpacesExtension.class;
                info.name = "Extension";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
    }

    @Override
    public void setProperty(int arg0, java.lang.Object arg1)
    {
    }

    
}
