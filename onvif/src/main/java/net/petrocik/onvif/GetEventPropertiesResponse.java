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

public  class GetEventPropertiesResponse extends AttributeContainer implements KvmSerializable
{

    
    private transient java.lang.Object __source;    
    
    public java.util.ArrayList< String> TopicNamespaceLocation =new java.util.ArrayList<String >();
    
    public Boolean FixedTopicSet=false;
    
    public TopicSetType TopicSet;
    
    public java.util.ArrayList< String> TopicExpressionDialect =new java.util.ArrayList<String >();
    
    public java.util.ArrayList< String> MessageContentFilterDialect =new java.util.ArrayList<String >();
    
    public java.util.ArrayList< String> ProducerPropertiesFilterDialect =new java.util.ArrayList<String >();
    
    public java.util.ArrayList< String> MessageContentSchemaLocation =new java.util.ArrayList<String >();
    
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


    }

    
    protected boolean loadProperty(PropertyInfo info,SoapObject soapObject,ExtendedSoapSerializationEnvelope __envelope)
    {
        java.lang.Object obj = info.getValue();
        if (info.name.equals("TopicNamespaceLocation"))
        {
            if(obj!=null)
            {
                if(this.TopicNamespaceLocation==null)
                {
                    this.TopicNamespaceLocation = new java.util.ArrayList< String>();
                }
                java.lang.Object j =obj;
                String j1= j.toString();
                this.TopicNamespaceLocation.add(j1);
            }
            return true;
        }
        if (info.name.equals("FixedTopicSet"))
        {
            if(obj!=null)
            {
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    if(j.toString()!=null)
                    {
                        this.FixedTopicSet = Boolean.valueOf(j.toString());
                    }
                }
                else if (obj instanceof Boolean){
                    this.FixedTopicSet = (Boolean)obj;
                }
            }
            return true;
        }
        if (info.name.equals("TopicSet"))
        {
            if(obj!=null)
            {
                java.lang.Object j = obj;
                this.TopicSet = (TopicSetType)__envelope.get(j,TopicSetType.class,false);
            }
            return true;
        }
        if (info.name.equals("TopicExpressionDialect"))
        {
            if(obj!=null)
            {
                if(this.TopicExpressionDialect==null)
                {
                    this.TopicExpressionDialect = new java.util.ArrayList< String>();
                }
                java.lang.Object j =obj;
                String j1= j.toString();
                this.TopicExpressionDialect.add(j1);
            }
            return true;
        }
        if (info.name.equals("MessageContentFilterDialect"))
        {
            if(obj!=null)
            {
                if(this.MessageContentFilterDialect==null)
                {
                    this.MessageContentFilterDialect = new java.util.ArrayList< String>();
                }
                java.lang.Object j =obj;
                String j1= j.toString();
                this.MessageContentFilterDialect.add(j1);
            }
            return true;
        }
        if (info.name.equals("ProducerPropertiesFilterDialect"))
        {
            if(obj!=null)
            {
                if(this.ProducerPropertiesFilterDialect==null)
                {
                    this.ProducerPropertiesFilterDialect = new java.util.ArrayList< String>();
                }
                java.lang.Object j =obj;
                String j1= j.toString();
                this.ProducerPropertiesFilterDialect.add(j1);
            }
            return true;
        }
        if (info.name.equals("MessageContentSchemaLocation"))
        {
            if(obj!=null)
            {
                if(this.MessageContentSchemaLocation==null)
                {
                    this.MessageContentSchemaLocation = new java.util.ArrayList< String>();
                }
                java.lang.Object j =obj;
                String j1= j.toString();
                this.MessageContentSchemaLocation.add(j1);
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
        if(propertyIndex>=0 && propertyIndex < 0+this.TopicNamespaceLocation.size())
        {
            String TopicNamespaceLocation = this.TopicNamespaceLocation.get(propertyIndex-(0));
            return TopicNamespaceLocation;
        }
        else if(propertyIndex==0+this.TopicNamespaceLocation.size())
        {
            return this.FixedTopicSet;
        }
        else if(propertyIndex==1+this.TopicNamespaceLocation.size())
        {
            return this.TopicSet;
        }
        else if(propertyIndex>=2+this.TopicNamespaceLocation.size() && propertyIndex < 2+this.TopicNamespaceLocation.size()+this.TopicExpressionDialect.size())
        {
            String TopicExpressionDialect = this.TopicExpressionDialect.get(propertyIndex-(2+this.TopicNamespaceLocation.size()));
            return TopicExpressionDialect;
        }
        else if(propertyIndex>=2+this.TopicNamespaceLocation.size()+this.TopicExpressionDialect.size() && propertyIndex < 2+this.TopicNamespaceLocation.size()+this.TopicExpressionDialect.size()+this.MessageContentFilterDialect.size())
        {
            String MessageContentFilterDialect = this.MessageContentFilterDialect.get(propertyIndex-(2+this.TopicNamespaceLocation.size()+this.TopicExpressionDialect.size()));
            return MessageContentFilterDialect;
        }
        else if(propertyIndex>=2+this.TopicNamespaceLocation.size()+this.TopicExpressionDialect.size()+this.MessageContentFilterDialect.size() && propertyIndex < 2+this.TopicNamespaceLocation.size()+this.TopicExpressionDialect.size()+this.MessageContentFilterDialect.size()+this.ProducerPropertiesFilterDialect.size())
        {
            String ProducerPropertiesFilterDialect = this.ProducerPropertiesFilterDialect.get(propertyIndex-(2+this.TopicNamespaceLocation.size()+this.TopicExpressionDialect.size()+this.MessageContentFilterDialect.size()));
            return ProducerPropertiesFilterDialect!=null?ProducerPropertiesFilterDialect:SoapPrimitive.NullSkip;
        }
        else if(propertyIndex>=2+this.TopicNamespaceLocation.size()+this.TopicExpressionDialect.size()+this.MessageContentFilterDialect.size()+this.ProducerPropertiesFilterDialect.size() && propertyIndex < 2+this.TopicNamespaceLocation.size()+this.TopicExpressionDialect.size()+this.MessageContentFilterDialect.size()+this.ProducerPropertiesFilterDialect.size()+this.MessageContentSchemaLocation.size())
        {
            String MessageContentSchemaLocation = this.MessageContentSchemaLocation.get(propertyIndex-(2+this.TopicNamespaceLocation.size()+this.TopicExpressionDialect.size()+this.MessageContentFilterDialect.size()+this.ProducerPropertiesFilterDialect.size()));
            return MessageContentSchemaLocation;
        }
        else if(propertyIndex>=2+this.TopicNamespaceLocation.size()+this.TopicExpressionDialect.size()+this.MessageContentFilterDialect.size()+this.ProducerPropertiesFilterDialect.size()+this.MessageContentSchemaLocation.size() && propertyIndex < 2+this.TopicNamespaceLocation.size()+this.TopicExpressionDialect.size()+this.MessageContentFilterDialect.size()+this.ProducerPropertiesFilterDialect.size()+this.MessageContentSchemaLocation.size()+this.any.size())
        {
            return this.any.get(propertyIndex-(2+this.TopicNamespaceLocation.size()+this.TopicExpressionDialect.size()+this.MessageContentFilterDialect.size()+this.ProducerPropertiesFilterDialect.size()+this.MessageContentSchemaLocation.size())).getValue();
        }
        return null;
    }


    @Override
    public int getPropertyCount() {
        return 2+TopicNamespaceLocation.size()+TopicExpressionDialect.size()+MessageContentFilterDialect.size()+ProducerPropertiesFilterDialect.size()+MessageContentSchemaLocation.size()+ any.size();
    }

    @Override
    public void getPropertyInfo(int propertyIndex, Hashtable arg1, PropertyInfo info)
    {
            if(propertyIndex>=0 && propertyIndex < 0+this.TopicNamespaceLocation.size())
            {
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "TopicNamespaceLocation";
                info.namespace= "http://www.onvif.org/ver10/events/wsdl";
            }
            else if(propertyIndex==0+this.TopicNamespaceLocation.size())
            {
                info.type = PropertyInfo.BOOLEAN_CLASS;
                info.name = "FixedTopicSet";
                info.namespace= "http://docs.oasis-open.org/wsn/b-2";
            }
            else if(propertyIndex==1+this.TopicNamespaceLocation.size())
            {
                info.type = TopicSetType.class;
                info.name = "TopicSet";
                info.namespace= "http://docs.oasis-open.org/wsn/t-1";
            }
            else if(propertyIndex>=2+this.TopicNamespaceLocation.size() && propertyIndex < 2+this.TopicNamespaceLocation.size()+this.TopicExpressionDialect.size())
            {
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "TopicExpressionDialect";
                info.namespace= "http://docs.oasis-open.org/wsn/b-2";
            }
            else if(propertyIndex>=2+this.TopicNamespaceLocation.size()+this.TopicExpressionDialect.size() && propertyIndex < 2+this.TopicNamespaceLocation.size()+this.TopicExpressionDialect.size()+this.MessageContentFilterDialect.size())
            {
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "MessageContentFilterDialect";
                info.namespace= "http://www.onvif.org/ver10/events/wsdl";
            }
            else if(propertyIndex>=2+this.TopicNamespaceLocation.size()+this.TopicExpressionDialect.size()+this.MessageContentFilterDialect.size() && propertyIndex < 2+this.TopicNamespaceLocation.size()+this.TopicExpressionDialect.size()+this.MessageContentFilterDialect.size()+this.ProducerPropertiesFilterDialect.size())
            {
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "ProducerPropertiesFilterDialect";
                info.namespace= "http://www.onvif.org/ver10/events/wsdl";
            }
            else if(propertyIndex>=2+this.TopicNamespaceLocation.size()+this.TopicExpressionDialect.size()+this.MessageContentFilterDialect.size()+this.ProducerPropertiesFilterDialect.size() && propertyIndex < 2+this.TopicNamespaceLocation.size()+this.TopicExpressionDialect.size()+this.MessageContentFilterDialect.size()+this.ProducerPropertiesFilterDialect.size()+this.MessageContentSchemaLocation.size())
            {
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "MessageContentSchemaLocation";
                info.namespace= "http://www.onvif.org/ver10/events/wsdl";
            }
            else if(propertyIndex>=2+this.TopicNamespaceLocation.size()+this.TopicExpressionDialect.size()+this.MessageContentFilterDialect.size()+this.ProducerPropertiesFilterDialect.size()+this.MessageContentSchemaLocation.size() && propertyIndex < 2+this.TopicNamespaceLocation.size()+this.TopicExpressionDialect.size()+this.MessageContentFilterDialect.size()+this.ProducerPropertiesFilterDialect.size()+this.MessageContentSchemaLocation.size()+this.any.size())
            {
                PropertyInfo j=this.any.get(propertyIndex-(2+this.TopicNamespaceLocation.size()+this.TopicExpressionDialect.size()+this.MessageContentFilterDialect.size()+this.ProducerPropertiesFilterDialect.size()+this.MessageContentSchemaLocation.size()));
                info.type = j.type;
                info.name = j.name;
                info.namespace= j.namespace;
            }
    }

    @Override
    public void setProperty(int arg0, java.lang.Object arg1)
    {
    }

    
}
