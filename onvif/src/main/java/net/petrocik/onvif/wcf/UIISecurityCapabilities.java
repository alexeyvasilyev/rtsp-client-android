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

public  class UIISecurityCapabilities extends AttributeContainer implements KvmSerializable
{

    
    private transient java.lang.Object __source;    
    
    public Boolean TLS1_x002E_1=false;
    
    public Boolean TLS1_x002E_2=false;
    
    public Boolean OnboardKeyGeneration=false;
    
    public Boolean AccessPolicyConfig=false;
    
    public Boolean X_x002E_509Token=false;
    
    public Boolean SAMLToken=false;
    
    public Boolean KerberosToken=false;
    
    public Boolean RELToken=false;
    
    public UIISecurityCapabilitiesExtension Extension;

    

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
        if (info.name.equals("TLS1.1"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.TLS1_x002E_1 = Boolean.valueOf(j.toString());
                    }
                }
                else if (obj instanceof Boolean){
                    this.TLS1_x002E_1 = (Boolean)obj;
                }
            }
            return true;
        }
        if (info.name.equals("TLS1.2"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.TLS1_x002E_2 = Boolean.valueOf(j.toString());
                    }
                }
                else if (obj instanceof Boolean){
                    this.TLS1_x002E_2 = (Boolean)obj;
                }
            }
            return true;
        }
        if (info.name.equals("OnboardKeyGeneration"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.OnboardKeyGeneration = Boolean.valueOf(j.toString());
                    }
                }
                else if (obj instanceof Boolean){
                    this.OnboardKeyGeneration = (Boolean)obj;
                }
            }
            return true;
        }
        if (info.name.equals("AccessPolicyConfig"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.AccessPolicyConfig = Boolean.valueOf(j.toString());
                    }
                }
                else if (obj instanceof Boolean){
                    this.AccessPolicyConfig = (Boolean)obj;
                }
            }
            return true;
        }
        if (info.name.equals("X.509Token"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.X_x002E_509Token = Boolean.valueOf(j.toString());
                    }
                }
                else if (obj instanceof Boolean){
                    this.X_x002E_509Token = (Boolean)obj;
                }
            }
            return true;
        }
        if (info.name.equals("SAMLToken"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.SAMLToken = Boolean.valueOf(j.toString());
                    }
                }
                else if (obj instanceof Boolean){
                    this.SAMLToken = (Boolean)obj;
                }
            }
            return true;
        }
        if (info.name.equals("KerberosToken"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.KerberosToken = Boolean.valueOf(j.toString());
                    }
                }
                else if (obj instanceof Boolean){
                    this.KerberosToken = (Boolean)obj;
                }
            }
            return true;
        }
        if (info.name.equals("RELToken"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.RELToken = Boolean.valueOf(j.toString());
                    }
                }
                else if (obj instanceof Boolean){
                    this.RELToken = (Boolean)obj;
                }
            }
            return true;
        }
        if (info.name.equals("Extension"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.Extension = (UIISecurityCapabilitiesExtension)__envelope.get(j,UIISecurityCapabilitiesExtension.class,false);
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
            return this.TLS1_x002E_1;
        }
        else if(propertyIndex==1)
        {
            return this.TLS1_x002E_2;
        }
        else if(propertyIndex==2)
        {
            return this.OnboardKeyGeneration;
        }
        else if(propertyIndex==3)
        {
            return this.AccessPolicyConfig;
        }
        else if(propertyIndex==4)
        {
            return this.X_x002E_509Token;
        }
        else if(propertyIndex==5)
        {
            return this.SAMLToken;
        }
        else if(propertyIndex==6)
        {
            return this.KerberosToken;
        }
        else if(propertyIndex==7)
        {
            return this.RELToken;
        }
        else if(propertyIndex==8)
        {
            return this.Extension!=null?this.Extension:SoapPrimitive.NullSkip;
        }
        return null;
    }


    @Override
    public int getPropertyCount() {
        return 9;
    }

    @Override
    public void getPropertyInfo(int propertyIndex, Hashtable arg1, PropertyInfo info)
    {
            if(propertyIndex==0)
            {
                info.type = PropertyInfo.BOOLEAN_CLASS;
                info.name = "TLS1.1";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==1)
            {
                info.type = PropertyInfo.BOOLEAN_CLASS;
                info.name = "TLS1.2";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==2)
            {
                info.type = PropertyInfo.BOOLEAN_CLASS;
                info.name = "OnboardKeyGeneration";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==3)
            {
                info.type = PropertyInfo.BOOLEAN_CLASS;
                info.name = "AccessPolicyConfig";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==4)
            {
                info.type = PropertyInfo.BOOLEAN_CLASS;
                info.name = "X.509Token";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==5)
            {
                info.type = PropertyInfo.BOOLEAN_CLASS;
                info.name = "SAMLToken";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==6)
            {
                info.type = PropertyInfo.BOOLEAN_CLASS;
                info.name = "KerberosToken";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==7)
            {
                info.type = PropertyInfo.BOOLEAN_CLASS;
                info.name = "RELToken";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
            else if(propertyIndex==8)
            {
                info.type = UIISecurityCapabilitiesExtension.class;
                info.name = "Extension";
                info.namespace= "http://www.onvif.org/ver10/schema";
            }
    }

    @Override
    public void setProperty(int arg0, java.lang.Object arg1)
    {
    }

    
}
