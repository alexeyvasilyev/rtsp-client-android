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

public  class RotateOptions extends AttributeContainer implements KvmSerializable
{

    
    private transient java.lang.Object __source;    
    
    public java.util.ArrayList< Enums.RotateMode> Mode =new java.util.ArrayList<Enums.RotateMode >();
    
    public IntItems DegreeList;
    
    public RotateOptionsExtension Extension;
    
    public Boolean Reboot=false;

    

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

		if (inObj.hasAttribute("Reboot"))
        {	
            java.lang.Object j = inObj.getAttribute("Reboot");
            if (j != null)
            {
                Reboot = Boolean.valueOf(j.toString());
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
                    this.Mode = new java.util.ArrayList< Enums.RotateMode>();
                }
                java.lang.Object j =obj;
                Enums.RotateMode j1= Enums.RotateMode.fromString(j.toString());
                this.Mode.add(j1);
            }
            return true;
        }
        if (info.name.equals("DegreeList"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.DegreeList = (IntItems)__envelope.get(j,IntItems.class,false);
            }
            return true;
        }
        if (info.name.equals("Extension"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.Extension = (RotateOptionsExtension)__envelope.get(j,RotateOptionsExtension.class,false);
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
            Enums.RotateMode Mode = this.Mode.get(propertyIndex-(0));
            return Mode!=null?Mode.toString():SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==0+this.Mode.size())
        {
            return this.DegreeList!=null?this.DegreeList:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==1+this.Mode.size())
        {
            return this.Extension!=null?this.Extension:SoapPrimitive.NullSkip;
        }
        return null;
    }


    @Override
    public int getPropertyCount() {
        return 2+Mode.size();
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
                info.type = PropertyInfo.VECTOR_CLASS;
                info.name = "DegreeList";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==1+this.Mode.size())
            {
                info.type = RotateOptionsExtension.class;
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
            info.name = "Reboot";
            info.namespace= "";
            if(this.Reboot!=null)
            {
                info.setValue(this.Reboot);
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
