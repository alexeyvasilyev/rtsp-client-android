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

public  class IPv6Configuration extends AttributeContainer implements KvmSerializable
{

    
    private transient java.lang.Object __source;    
    
    public Boolean AcceptRouterAdvert;
    
    public Enums.IPv6DHCPConfiguration DHCP=Enums.IPv6DHCPConfiguration.Auto;
    
    public java.util.ArrayList< PrefixedIPv6Address> Manual =new java.util.ArrayList<PrefixedIPv6Address >();
    
    public java.util.ArrayList< PrefixedIPv6Address> LinkLocal =new java.util.ArrayList<PrefixedIPv6Address >();
    
    public java.util.ArrayList< PrefixedIPv6Address> FromDHCP =new java.util.ArrayList<PrefixedIPv6Address >();
    
    public java.util.ArrayList< PrefixedIPv6Address> FromRA =new java.util.ArrayList<PrefixedIPv6Address >();
    
    public IPv6ConfigurationExtension Extension;

    

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
        if (info.name.equals("DHCP"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.DHCP = Enums.IPv6DHCPConfiguration.fromString(j.toString());
                    }
                }
                else if (obj instanceof Enums.IPv6DHCPConfiguration){
                    this.DHCP = (Enums.IPv6DHCPConfiguration)obj;
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
                    this.Manual = new java.util.ArrayList< PrefixedIPv6Address>();
                }
                java.lang.Object j =obj;
                PrefixedIPv6Address j1= (PrefixedIPv6Address)__envelope.get(j,PrefixedIPv6Address.class,false);
                this.Manual.add(j1);
            }
            return true;
        }
        if (info.name.equals("LinkLocal"))
        {
            if(obj!=null)
            {
                if(this.LinkLocal==null)
                {
                    this.LinkLocal = new java.util.ArrayList< PrefixedIPv6Address>();
                }
                java.lang.Object j =obj;
                PrefixedIPv6Address j1= (PrefixedIPv6Address)__envelope.get(j,PrefixedIPv6Address.class,false);
                this.LinkLocal.add(j1);
            }
            return true;
        }
        if (info.name.equals("FromDHCP"))
        {
            if(obj!=null)
            {
                if(this.FromDHCP==null)
                {
                    this.FromDHCP = new java.util.ArrayList< PrefixedIPv6Address>();
                }
                java.lang.Object j =obj;
                PrefixedIPv6Address j1= (PrefixedIPv6Address)__envelope.get(j,PrefixedIPv6Address.class,false);
                this.FromDHCP.add(j1);
            }
            return true;
        }
        if (info.name.equals("FromRA"))
        {
            if(obj!=null)
            {
                if(this.FromRA==null)
                {
                    this.FromRA = new java.util.ArrayList< PrefixedIPv6Address>();
                }
                java.lang.Object j =obj;
                PrefixedIPv6Address j1= (PrefixedIPv6Address)__envelope.get(j,PrefixedIPv6Address.class,false);
                this.FromRA.add(j1);
            }
            return true;
        }
        if (info.name.equals("Extension"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.Extension = (IPv6ConfigurationExtension)__envelope.get(j,IPv6ConfigurationExtension.class,false);
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
            return this.AcceptRouterAdvert!=null?this.AcceptRouterAdvert:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==1)
        {
            return this.DHCP!=null?this.DHCP.toString():SoapPrimitive.NullSkip;
        }
        else if(propertyIndex>=2 && propertyIndex < 2+this.Manual.size())
        {
            PrefixedIPv6Address Manual = this.Manual.get(propertyIndex-(2));
            return Manual!=null?Manual:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex>=2+this.Manual.size() && propertyIndex < 2+this.Manual.size()+this.LinkLocal.size())
        {
            PrefixedIPv6Address LinkLocal = this.LinkLocal.get(propertyIndex-(2+this.Manual.size()));
            return LinkLocal!=null?LinkLocal:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex>=2+this.Manual.size()+this.LinkLocal.size() && propertyIndex < 2+this.Manual.size()+this.LinkLocal.size()+this.FromDHCP.size())
        {
            PrefixedIPv6Address FromDHCP = this.FromDHCP.get(propertyIndex-(2+this.Manual.size()+this.LinkLocal.size()));
            return FromDHCP!=null?FromDHCP:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex>=2+this.Manual.size()+this.LinkLocal.size()+this.FromDHCP.size() && propertyIndex < 2+this.Manual.size()+this.LinkLocal.size()+this.FromDHCP.size()+this.FromRA.size())
        {
            PrefixedIPv6Address FromRA = this.FromRA.get(propertyIndex-(2+this.Manual.size()+this.LinkLocal.size()+this.FromDHCP.size()));
            return FromRA!=null?FromRA:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex==2+this.Manual.size()+this.LinkLocal.size()+this.FromDHCP.size()+this.FromRA.size())
        {
            return this.Extension!=null?this.Extension:SoapPrimitive.NullSkip;
        }
        return null;
    }


    @Override
    public int getPropertyCount() {
        return 3+Manual.size()+LinkLocal.size()+FromDHCP.size()+FromRA.size();
    }

    @Override
    public void getPropertyInfo(int propertyIndex, Hashtable arg1, PropertyInfo info)
    {
            if(propertyIndex==0)
            {
                info.type = PropertyInfo.BOOLEAN_CLASS;
                info.name = "AcceptRouterAdvert";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==1)
            {
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "DHCP";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex>=2 && propertyIndex < 2+this.Manual.size())
            {
                info.type = PrefixedIPv6Address.class;
                info.name = "Manual";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex>=2+this.Manual.size() && propertyIndex < 2+this.Manual.size()+this.LinkLocal.size())
            {
                info.type = PrefixedIPv6Address.class;
                info.name = "LinkLocal";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex>=2+this.Manual.size()+this.LinkLocal.size() && propertyIndex < 2+this.Manual.size()+this.LinkLocal.size()+this.FromDHCP.size())
            {
                info.type = PrefixedIPv6Address.class;
                info.name = "FromDHCP";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex>=2+this.Manual.size()+this.LinkLocal.size()+this.FromDHCP.size() && propertyIndex < 2+this.Manual.size()+this.LinkLocal.size()+this.FromDHCP.size()+this.FromRA.size())
            {
                info.type = PrefixedIPv6Address.class;
                info.name = "FromRA";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==2+this.Manual.size()+this.LinkLocal.size()+this.FromDHCP.size()+this.FromRA.size())
            {
                info.type = IPv6ConfigurationExtension.class;
                info.name = "Extension";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
    }

    @Override
    public void setProperty(int arg0, java.lang.Object arg1)
    {
    }

    
}
