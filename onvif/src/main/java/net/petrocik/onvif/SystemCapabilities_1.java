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

public  class SystemCapabilities_1 extends AttributeContainer implements KvmSerializable
{

    
    private transient java.lang.Object __source;    
    
    public Boolean DiscoveryResolve=false;
    
    public Boolean DiscoveryBye=false;
    
    public Boolean RemoteDiscovery=false;
    
    public Boolean SystemBackup=false;
    
    public Boolean SystemLogging=false;
    
    public Boolean FirmwareUpgrade=false;
    
    public Boolean HttpFirmwareUpgrade=false;
    
    public Boolean HttpSystemBackup=false;
    
    public Boolean HttpSystemLogging=false;
    
    public Boolean HttpSupportInformation=false;

    

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

		if (inObj.hasAttribute("DiscoveryResolve"))
        {	
            java.lang.Object j = inObj.getAttribute("DiscoveryResolve");
            if (j != null)
            {
                DiscoveryResolve = Boolean.valueOf(j.toString());
            }
        }
		if (inObj.hasAttribute("DiscoveryBye"))
        {	
            java.lang.Object j = inObj.getAttribute("DiscoveryBye");
            if (j != null)
            {
                DiscoveryBye = Boolean.valueOf(j.toString());
            }
        }
		if (inObj.hasAttribute("RemoteDiscovery"))
        {	
            java.lang.Object j = inObj.getAttribute("RemoteDiscovery");
            if (j != null)
            {
                RemoteDiscovery = Boolean.valueOf(j.toString());
            }
        }
		if (inObj.hasAttribute("SystemBackup"))
        {	
            java.lang.Object j = inObj.getAttribute("SystemBackup");
            if (j != null)
            {
                SystemBackup = Boolean.valueOf(j.toString());
            }
        }
		if (inObj.hasAttribute("SystemLogging"))
        {	
            java.lang.Object j = inObj.getAttribute("SystemLogging");
            if (j != null)
            {
                SystemLogging = Boolean.valueOf(j.toString());
            }
        }
		if (inObj.hasAttribute("FirmwareUpgrade"))
        {	
            java.lang.Object j = inObj.getAttribute("FirmwareUpgrade");
            if (j != null)
            {
                FirmwareUpgrade = Boolean.valueOf(j.toString());
            }
        }
		if (inObj.hasAttribute("HttpFirmwareUpgrade"))
        {	
            java.lang.Object j = inObj.getAttribute("HttpFirmwareUpgrade");
            if (j != null)
            {
                HttpFirmwareUpgrade = Boolean.valueOf(j.toString());
            }
        }
		if (inObj.hasAttribute("HttpSystemBackup"))
        {	
            java.lang.Object j = inObj.getAttribute("HttpSystemBackup");
            if (j != null)
            {
                HttpSystemBackup = Boolean.valueOf(j.toString());
            }
        }
		if (inObj.hasAttribute("HttpSystemLogging"))
        {	
            java.lang.Object j = inObj.getAttribute("HttpSystemLogging");
            if (j != null)
            {
                HttpSystemLogging = Boolean.valueOf(j.toString());
            }
        }
		if (inObj.hasAttribute("HttpSupportInformation"))
        {	
            java.lang.Object j = inObj.getAttribute("HttpSupportInformation");
            if (j != null)
            {
                HttpSupportInformation = Boolean.valueOf(j.toString());
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
        return 10;
    }
    
    @Override
    public void getAttributeInfo(int index, AttributeInfo info) {
if(index==0)
        {
            info.name = "DiscoveryResolve";
            info.namespace= "";
            if(this.DiscoveryResolve!=null)
            {
                info.setValue(this.DiscoveryResolve);
            }
        
        }
        if(index==1)
        {
            info.name = "DiscoveryBye";
            info.namespace= "";
            if(this.DiscoveryBye!=null)
            {
                info.setValue(this.DiscoveryBye);
            }
        
        }
        if(index==2)
        {
            info.name = "RemoteDiscovery";
            info.namespace= "";
            if(this.RemoteDiscovery!=null)
            {
                info.setValue(this.RemoteDiscovery);
            }
        
        }
        if(index==3)
        {
            info.name = "SystemBackup";
            info.namespace= "";
            if(this.SystemBackup!=null)
            {
                info.setValue(this.SystemBackup);
            }
        
        }
        if(index==4)
        {
            info.name = "SystemLogging";
            info.namespace= "";
            if(this.SystemLogging!=null)
            {
                info.setValue(this.SystemLogging);
            }
        
        }
        if(index==5)
        {
            info.name = "FirmwareUpgrade";
            info.namespace= "";
            if(this.FirmwareUpgrade!=null)
            {
                info.setValue(this.FirmwareUpgrade);
            }
        
        }
        if(index==6)
        {
            info.name = "HttpFirmwareUpgrade";
            info.namespace= "";
            if(this.HttpFirmwareUpgrade!=null)
            {
                info.setValue(this.HttpFirmwareUpgrade);
            }
        
        }
        if(index==7)
        {
            info.name = "HttpSystemBackup";
            info.namespace= "";
            if(this.HttpSystemBackup!=null)
            {
                info.setValue(this.HttpSystemBackup);
            }
        
        }
        if(index==8)
        {
            info.name = "HttpSystemLogging";
            info.namespace= "";
            if(this.HttpSystemLogging!=null)
            {
                info.setValue(this.HttpSystemLogging);
            }
        
        }
        if(index==9)
        {
            info.name = "HttpSupportInformation";
            info.namespace= "";
            if(this.HttpSupportInformation!=null)
            {
                info.setValue(this.HttpSupportInformation);
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
