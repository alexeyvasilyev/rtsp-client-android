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

public  class MaximumNumberOfOSDs extends AttributeContainer implements KvmSerializable
{

    
    private transient java.lang.Object __source;    
    
    public Integer Total=0;
    
    public Integer Image=0;
    
    public Integer PlainText=0;
    
    public Integer Date=0;
    
    public Integer Time=0;
    
    public Integer DateAndTime=0;

    

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

		if (inObj.hasAttribute("Total"))
        {	
            java.lang.Object j = inObj.getAttribute("Total");
            if (j != null)
            {
                Total = Integer.parseInt(j.toString());
            }
        }
		if (inObj.hasAttribute("Image"))
        {	
            java.lang.Object j = inObj.getAttribute("Image");
            if (j != null)
            {
                Image = Integer.parseInt(j.toString());
            }
        }
		if (inObj.hasAttribute("PlainText"))
        {	
            java.lang.Object j = inObj.getAttribute("PlainText");
            if (j != null)
            {
                PlainText = Integer.parseInt(j.toString());
            }
        }
		if (inObj.hasAttribute("Date"))
        {	
            java.lang.Object j = inObj.getAttribute("Date");
            if (j != null)
            {
                Date = Integer.parseInt(j.toString());
            }
        }
		if (inObj.hasAttribute("Time"))
        {	
            java.lang.Object j = inObj.getAttribute("Time");
            if (j != null)
            {
                Time = Integer.parseInt(j.toString());
            }
        }
		if (inObj.hasAttribute("DateAndTime"))
        {	
            java.lang.Object j = inObj.getAttribute("DateAndTime");
            if (j != null)
            {
                DateAndTime = Integer.parseInt(j.toString());
            }
        }

    }

    
    protected boolean loadProperty(PropertyInfo info,SoapObject soapObject,ExtendedSoapSerializationEnvelope __envelope)
    {
        java.lang.Object obj = info.getValue();
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
        return null;
    }


    @Override
    public int getPropertyCount() {
        return 0;
    }

    @Override
    public void getPropertyInfo(int propertyIndex, Hashtable arg1, PropertyInfo info)
    {
    }

    @Override
    public void setProperty(int arg0, java.lang.Object arg1)
    {
    }



        public int getAttributeCount() {
        return 6;
    }
    
    @Override
    public void getAttributeInfo(int index, AttributeInfo info) {
if(index==0)
        {
            info.name = "Total";
            info.namespace= "";
            if(this.Total!=null)
            {
                info.setValue(this.Total);
            }
        
        }
        if(index==1)
        {
            info.name = "Image";
            info.namespace= "";
            if(this.Image!=null)
            {
                info.setValue(this.Image);
            }
        
        }
        if(index==2)
        {
            info.name = "PlainText";
            info.namespace= "";
            if(this.PlainText!=null)
            {
                info.setValue(this.PlainText);
            }
        
        }
        if(index==3)
        {
            info.name = "Date";
            info.namespace= "";
            if(this.Date!=null)
            {
                info.setValue(this.Date);
            }
        
        }
        if(index==4)
        {
            info.name = "Time";
            info.namespace= "";
            if(this.Time!=null)
            {
                info.setValue(this.Time);
            }
        
        }
        if(index==5)
        {
            info.name = "DateAndTime";
            info.namespace= "";
            if(this.DateAndTime!=null)
            {
                info.setValue(this.DateAndTime);
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
