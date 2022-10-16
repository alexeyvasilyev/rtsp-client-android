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

public  class PresetTour extends AttributeContainer implements KvmSerializable
{

    
    private transient java.lang.Object __source;    
    
    public String Name;
    
    public PTZPresetTourStatus Status;
    
    public Boolean AutoStart=false;
    
    public PTZPresetTourStartingCondition StartingCondition;
    
    public java.util.ArrayList< PTZPresetTourSpot> TourSpot =new java.util.ArrayList<PTZPresetTourSpot >();
    
    public PTZPresetTourExtension Extension;
    
    public String token;

    

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

		if (inObj.hasAttribute("token"))
        {	
            java.lang.Object j = inObj.getAttribute("token");
            if (j != null)
            {
                token = j.toString();
            }
        }

    }

    
    protected boolean loadProperty(PropertyInfo info,SoapObject soapObject,ExtendedSoapSerializationEnvelope __envelope)
    {
        java.lang.Object obj = info.getValue();
        if (info.name.equals("Name"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.Name = j.toString();
                    }
                }
                else if (obj instanceof String){
                    this.Name = (String)obj;
                }
                else{
                    this.Name = "";
                }
            }
            return true;
        }
        if (info.name.equals("Status"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.Status = (PTZPresetTourStatus)__envelope.get(j,PTZPresetTourStatus.class,false);
            }
            return true;
        }
        if (info.name.equals("AutoStart"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.AutoStart = Boolean.valueOf(j.toString());
                    }
                }
                else if (obj instanceof Boolean){
                    this.AutoStart = (Boolean)obj;
                }
            }
            return true;
        }
        if (info.name.equals("StartingCondition"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.StartingCondition = (PTZPresetTourStartingCondition)__envelope.get(j,PTZPresetTourStartingCondition.class,false);
            }
            return true;
        }
        if (info.name.equals("TourSpot"))
        {
            if(obj!=null)
            {
                if(this.TourSpot==null)
                {
                    this.TourSpot = new java.util.ArrayList< PTZPresetTourSpot>();
                }
                java.lang.Object j =obj;
                PTZPresetTourSpot j1= (PTZPresetTourSpot)__envelope.get(j,PTZPresetTourSpot.class,false);
                this.TourSpot.add(j1);
            }
            return true;
        }
        if (info.name.equals("Extension"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.Extension = (PTZPresetTourExtension)__envelope.get(j,PTZPresetTourExtension.class,false);
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
            return this.Name!=null?this.Name:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==1)
        {
            return this.Status;
        }
        else if(propertyIndex==2)
        {
            return this.AutoStart;
        }
        else if(propertyIndex==3)
        {
            return this.StartingCondition;
        }
        else if(propertyIndex>=4 && propertyIndex < 4+this.TourSpot.size())
        {
            PTZPresetTourSpot TourSpot = this.TourSpot.get(propertyIndex-(4));
            return TourSpot!=null?TourSpot:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==4+this.TourSpot.size())
        {
            return this.Extension!=null?this.Extension:SoapPrimitive.NullSkip;
        }
        return null;
    }


    @Override
    public int getPropertyCount() {
        return 5+TourSpot.size();
    }

    @Override
    public void getPropertyInfo(int propertyIndex, Hashtable arg1, PropertyInfo info)
    {
            if(propertyIndex==0)
            {
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "Name";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==1)
            {
                info.type = PTZPresetTourStatus.class;
                info.name = "Status";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==2)
            {
                info.type = PropertyInfo.BOOLEAN_CLASS;
                info.name = "AutoStart";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==3)
            {
                info.type = PTZPresetTourStartingCondition.class;
                info.name = "StartingCondition";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex>=4 && propertyIndex < 4+this.TourSpot.size())
            {
                info.type = PTZPresetTourSpot.class;
                info.name = "TourSpot";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==4+this.TourSpot.size())
            {
                info.type = PTZPresetTourExtension.class;
                info.name = "Extension";
                info.namespace= "http://www.onvif.org/ver10/schema";
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
            info.name = "token";
            info.namespace= "";
            if(this.token!=null)
            {
                info.setValue(this.token);
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
