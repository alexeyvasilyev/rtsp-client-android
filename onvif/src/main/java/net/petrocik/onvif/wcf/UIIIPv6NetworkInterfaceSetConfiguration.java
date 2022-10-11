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
import java.util.ArrayList;
import org.ksoap2.serialization.PropertyInfo;

public  class UIIIPv6NetworkInterfaceSetConfiguration extends AttributeContainer implements KvmSerializable
{

    
    private transient java.lang.Object __source;    
    
    public Boolean Enabled;
    
    public Boolean AcceptRouterAdvert;
    
    public java.util.ArrayList< UIIPrefixedIPv6Address> Manual =new java.util.ArrayList<UIIPrefixedIPv6Address >();
    
    public UIIEnums.IPv6DHCPConfiguration DHCP;

    

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
        if (info.name.equals("Enabled"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.Enabled = Boolean.valueOf(j.toString());
                    }
                }
                else if (obj instanceof Boolean){
                    this.Enabled = (Boolean)obj;
                }
            }
            return true;
        }
        if (info.name.equals("AcceptRouterAdvert"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.AcceptRouterAdvert = Boolean.valueOf(j.toString());
                    }
                }
                else if (obj instanceof Boolean){
                    this.AcceptRouterAdvert = (Boolean)obj;
                }
            }
            return true;
        }
        if (info.name.equals("Manual"))
        {
            if(obj!=null)
            {
                if(this.Manual==null)
                {
                    this.Manual = new java.util.ArrayList< UIIPrefixedIPv6Address>();
                }
                java.lang.Object j =obj;
                UIIPrefixedIPv6Address j1= (UIIPrefixedIPv6Address)__envelope.get(j,UIIPrefixedIPv6Address.class,false);
                this.Manual.add(j1);
            }
            return true;
        }
        if (info.name.equals("DHCP"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.DHCP = UIIEnums.IPv6DHCPConfiguration.fromString(j.toString());
                    }
                }
                else if (obj instanceof UIIEnums.IPv6DHCPConfiguration){
                    this.DHCP = (UIIEnums.IPv6DHCPConfiguration)obj;
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
            return this.Enabled!=null?this.Enabled:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==1)
        {
            return this.AcceptRouterAdvert!=null?this.AcceptRouterAdvert:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex>=2 && propertyIndex < 2+this.Manual.size())
        {
            UIIPrefixedIPv6Address Manual = this.Manual.get(propertyIndex-(2));
            return Manual!=null?Manual:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==2+this.Manual.size())
        {
            return this.DHCP!=null?this.DHCP.toString():SoapPrimitive.NullSkip;
        }
        return null;
    }


    @Override
    public int getPropertyCount() {
        return 3+Manual.size();
    }

    @Override
    public void getPropertyInfo(int propertyIndex, Hashtable arg1, PropertyInfo info)
    {
            if(propertyIndex==0)
            {
                info.type = PropertyInfo.BOOLEAN_CLASS;
                info.name = "Enabled";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==1)
            {
                info.type = PropertyInfo.BOOLEAN_CLASS;
                info.name = "AcceptRouterAdvert";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex>=2 && propertyIndex < 2+this.Manual.size())
            {
                info.type = UIIPrefixedIPv6Address.class;
                info.name = "Manual";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==2+this.Manual.size())
            {
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "DHCP";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
    }

    @Override
    public void setProperty(int arg0, java.lang.Object arg1)
    {
    }

    
}
