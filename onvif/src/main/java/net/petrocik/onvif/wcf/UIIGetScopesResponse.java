package net.petrocik.onvif.wcf;

//----------------------------------------------------
//
// Generated by www.easywsdl.com
// Version: 6.0.1.1
//
// Created by Quasar Development 
//
//----------------------------------------------------



import org.ksoap2.serialization.*;
import java.util.Vector;
import java.util.Hashtable;

public class UIIGetScopesResponse extends Vector<UIIScope> implements KvmSerializable
{
    private transient java.lang.Object __source;

    public UIIGetScopesResponse()
    {
    }

    public UIIGetScopesResponse(int initialCapactiy)
    {
        super(initialCapactiy);
    }

    public UIIGetScopesResponse(java.util.Collection< UIIScope> initialCapactiy)
    {
        super(initialCapactiy);
    }

    public void loadFromSoap(java.lang.Object inObj,UIIExtendedSoapSerializationEnvelope __envelope)
    {
        if (inObj == null)
            return;
        __source=inObj;
        SoapObject soapObject=(SoapObject)inObj;
        int size = soapObject.getPropertyCount();
        for (int i0=0;i0< size;i0++)
        {
            java.lang.Object obj = soapObject.getProperty(i0);
            if (obj!=null && obj instanceof AttributeContainer)
            {
                AttributeContainer j =(AttributeContainer) soapObject.getProperty(i0);
                UIIScope j1= (UIIScope)__envelope.get(j,UIIScope.class,false);
                add(j1);
                
            }
        }
    }

    public java.lang.Object getSourceObject()
    {
        return __source;
    }
    
    @Override
    public java.lang.Object getProperty(int arg0) {
        return this.get(arg0)!=null?this.get(arg0):SoapPrimitive.NullNilElement;
    }
    
    @Override
    public int getPropertyCount() {
        return this.size();
    }
    
    @Override
    public void getPropertyInfo(int index, Hashtable arg1, PropertyInfo info) {
        info.name = "Scopes";
        info.type = UIIScope.class;
    	info.namespace= "http://www.onvif.org/ver10/device/wsdl";
    }
    
    @Override
    public void setProperty(int arg0, java.lang.Object arg1) {
    }

    
}