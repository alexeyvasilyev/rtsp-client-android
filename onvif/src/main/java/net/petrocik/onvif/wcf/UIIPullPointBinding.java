package net.petrocik.onvif.wcf;

//----------------------------------------------------
//
// Generated by www.easywsdl.com
// Version: 6.0.1.1
//
// Created by Quasar Development 
//
//----------------------------------------------------


import java.math.BigInteger;

import org.ksoap2.HeaderProperty;
import org.ksoap2.serialization.*;
import org.ksoap2.transport.*;
import org.kxml2.kdom.Element;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class UIIPullPointBinding
{
    interface UIIIWcfMethod
    {
        UIIExtendedSoapSerializationEnvelope CreateSoapEnvelope() throws java.lang.Exception;

        java.lang.Object ProcessResult(UIIExtendedSoapSerializationEnvelope __envelope,java.lang.Object result) throws java.lang.Exception;
    }

    String url="";

    int timeOut=60000;
    
    public List< HeaderProperty> httpHeaders= new ArrayList< HeaderProperty>();
    public boolean enableLogging;

    UIIIServiceEvents callback;

    public UIIPullPointBinding(){}

    public UIIPullPointBinding (UIIIServiceEvents callback)
    {
        this.callback = callback;
    }
    public UIIPullPointBinding(UIIIServiceEvents callback,String url)
    {
        this.callback = callback;
        this.url = url;
    }

    public UIIPullPointBinding(UIIIServiceEvents callback,String url,int timeOut)
    {
        this.callback = callback;
        this.url = url;
        this.timeOut=timeOut;
    }

    protected org.ksoap2.transport.Transport createTransport()
    {
        try
        {
            java.net.URI uri = new java.net.URI(url);
            if(uri.getScheme().equalsIgnoreCase("https"))
            {
                int port=uri.getPort()>0?uri.getPort():443;
                String path=uri.getPath();
                if(uri.getQuery()!=null && uri.getQuery()!="")
                {
                    path+="?"+uri.getQuery();
                }
                return new com.easywsdl.exksoap2.transport.AdvancedHttpsTransportSE(uri.getHost(), port, path, timeOut);
            }
            else
            {
                return new com.easywsdl.exksoap2.transport.AdvancedHttpTransportSE(url,timeOut);
            }

        }
        catch (java.net.URISyntaxException e)
        {
        }
        return null;
    }

    protected UIIExtendedSoapSerializationEnvelope createEnvelope()
    {
        UIIExtendedSoapSerializationEnvelope envelope= new UIIExtendedSoapSerializationEnvelope(UIIExtendedSoapSerializationEnvelope.VER12);
        envelope.enableLogging = enableLogging;
    
        return envelope;
    }

    protected java.util.List sendRequest(String methodName,UIIExtendedSoapSerializationEnvelope envelope,org.ksoap2.transport.Transport transport ,com.easywsdl.exksoap2.ws_specifications.profile.WS_Profile profile )throws java.lang.Exception
    {
        if(transport instanceof com.easywsdl.exksoap2.transport.AdvancedHttpTransportSE )
        {
            return ((com.easywsdl.exksoap2.transport.AdvancedHttpTransportSE)transport).call(methodName, envelope,httpHeaders,null,profile);
        }
        else
        {
            return ((com.easywsdl.exksoap2.transport.AdvancedHttpsTransportSE)transport).call(methodName, envelope,httpHeaders,null,profile);
        }
    }

    java.lang.Object getResult(java.lang.Class destObj,java.lang.Object source,String resultName,UIIExtendedSoapSerializationEnvelope __envelope) throws java.lang.Exception
    {
        if(source==null)
        {
            return null;
        }
        if(source instanceof SoapPrimitive)
        {
            SoapPrimitive soap =(SoapPrimitive)source;
            if(soap.getName().equals(resultName))
            {
                java.lang.Object instance=__envelope.get(source,destObj,false);
                return instance;
            }
        }
        else
        {
            SoapObject soap = (SoapObject)source;
            if (soap.hasProperty(resultName))
            {
                java.lang.Object j=soap.getProperty(resultName);
                if(j==null)
                {
                    return null;
                }
                java.lang.Object instance=__envelope.get(j,destObj,false);
                return instance;
            }
            else if( soap.getName().equals(resultName)) 
            {
                java.lang.Object instance=__envelope.get(source,destObj,false);
                return instance;
            }
        }

        return null;
    }

    
    
    
    public UIINotificationMessageHolderType GetMessages(final BigInteger MaximumNumber) throws java.lang.Exception
    {
        com.easywsdl.exksoap2.ws_specifications.profile.WS_Profile __profile = new com.easywsdl.exksoap2.ws_specifications.profile.WS_Profile();
        return (UIINotificationMessageHolderType)execute(new UIIIWcfMethod()
        {
            @Override
            public UIIExtendedSoapSerializationEnvelope CreateSoapEnvelope(){
                UIIExtendedSoapSerializationEnvelope __envelope = createEnvelope();
                SoapObject __soapReq = new SoapObject("http://docs.oasis-open.org/wsn/b-2", "GetMessages");
                __envelope.setOutputSoapObject(__soapReq);
                
                PropertyInfo __info=null;
                __info = new PropertyInfo();
                __info.namespace="http://docs.oasis-open.org/wsn/b-2";
                __info.name="MaximumNumber";
                __info.type=PropertyInfo.STRING_CLASS;
                __info.setValue(MaximumNumber!=null?MaximumNumber.toString():SoapPrimitive.NullSkip);
                __soapReq.addProperty(__info);
                return __envelope;
            }
            
            @Override
            public java.lang.Object ProcessResult(UIIExtendedSoapSerializationEnvelope __envelope,java.lang.Object __result)throws java.lang.Exception {
                return (UIINotificationMessageHolderType)getResult(UIINotificationMessageHolderType.class,__result,"NotificationMessage",__envelope);
            }
        },"http://docs.oasis-open.org/wsn/bw-2/PullPoint/GetMessagesRequest",__profile);
    }
    
    
    
    public android.os.AsyncTask< Void, Void, UIIOperationResult< UIINotificationMessageHolderType>> GetMessagesAsync(final BigInteger MaximumNumber)
    {
        return executeAsync(new UIIFunctions.IFunc< UIINotificationMessageHolderType>() {
            public UIINotificationMessageHolderType Func() throws java.lang.Exception {
                return GetMessages( MaximumNumber);
            }
        },"GetMessages");
    }
    
    
    
    public void DestroyPullPoint() throws java.lang.Exception
    {
        com.easywsdl.exksoap2.ws_specifications.profile.WS_Profile __profile = new com.easywsdl.exksoap2.ws_specifications.profile.WS_Profile();
        execute(new UIIIWcfMethod()
        {
            @Override
            public UIIExtendedSoapSerializationEnvelope CreateSoapEnvelope(){
                UIIExtendedSoapSerializationEnvelope __envelope = createEnvelope();
                SoapObject __soapReq = new SoapObject("http://docs.oasis-open.org/wsn/b-2", "DestroyPullPoint");
                __envelope.setOutputSoapObject(__soapReq);
                
                PropertyInfo __info=null;
                return __envelope;
            }
            
            @Override
            public java.lang.Object ProcessResult(UIIExtendedSoapSerializationEnvelope __envelope,java.lang.Object __result)throws java.lang.Exception {
                return null;
            }
        },"http://docs.oasis-open.org/wsn/bw-2/PullPoint/DestroyPullPointRequest",__profile);
    }
    
    
    
    public android.os.AsyncTask< Void, Void, UIIOperationResult< Void>> DestroyPullPointAsync()
    {
        return executeAsync(new UIIFunctions.IFunc< Void>()
        {
            @Override
            public Void Func() throws java.lang.Exception {
                DestroyPullPoint( );
                return null;
            }
        },"DestroyPullPoint") ;
    }
    
	/**
	* This method is available in Premium account only. To test if generated classes work correctly with your webservice, please use different method. Check http://EasyWsdl.com/Payment/PremiumAccountDetails to see all benefits of Premium account.
	*/
    
    
    public void Notify_1(final String Notify) throws java.lang.Exception
    {
        
/*This feature is available in Premium account. To test if generated classes work correctly with your webservice, please use different method. Check https://EasyWsdl.com/Payment/PremiumAccountDetails to see all benefits of Premium account.*/
        throw new java.lang.UnsupportedOperationException("This feature is available in Premium account. To test if generated classes work correctly with your webservice, please use different method. Check https://EasyWsdl.com/Payment/PremiumAccountDetails to see all benefits of Premium account.");
    }
    
	/**
	* This method is available in Premium account only. To test if generated classes work correctly with your webservice, please use different method. Check http://EasyWsdl.com/Payment/PremiumAccountDetails to see all benefits of Premium account.
	*/
    
    
    public android.os.AsyncTask< Void, Void, UIIOperationResult< Void>> Notify_1Async(final String Notify)
    {
        return executeAsync(new UIIFunctions.IFunc< Void>()
        {
            @Override
            public Void Func() throws java.lang.Exception {
                Notify_1( Notify);
                return null;
            }
        },"Notify_1") ;
    }

    protected java.lang.Object execute(UIIIWcfMethod wcfMethod,String methodName,com.easywsdl.exksoap2.ws_specifications.profile.WS_Profile profile) throws java.lang.Exception
    {
        org.ksoap2.transport.Transport __httpTransport=createTransport();
        __httpTransport.debug=enableLogging;
        UIIExtendedSoapSerializationEnvelope __envelope=wcfMethod.CreateSoapEnvelope();
        try
        {
            sendRequest(methodName, __envelope, __httpTransport,profile);
        }
        finally {
            if (__httpTransport.debug) {
                if (__httpTransport.requestDump != null) {
                    android.util.Log.i("requestDump",__httpTransport.requestDump);

                }
                if (__httpTransport.responseDump != null) {
                    android.util.Log.i("responseDump",__httpTransport.responseDump);
                }
            }
        }
        java.lang.Object __retObj = __envelope.bodyIn;
        if (__retObj instanceof org.ksoap2.SoapFault){
            org.ksoap2.SoapFault __fault = (org.ksoap2.SoapFault)__retObj;
            throw convertToException(__fault,__envelope);
        }else{
            return wcfMethod.ProcessResult(__envelope,__retObj);
        }
    }

    protected < T> android.os.AsyncTask< Void, Void, UIIOperationResult< T>>  executeAsync(final UIIFunctions.IFunc< T> func, final String methodName)
    {
        return new android.os.AsyncTask< Void, Void, UIIOperationResult< T>>()
        {
            @Override
            protected void onPreExecute() {
                callback.Starting();
            }
            @Override
            protected UIIOperationResult< T> doInBackground(Void... params) {
                UIIOperationResult< T> result = new UIIOperationResult< T>();
                try
                {
                    result.MethodName=methodName;
                    result.Result= func.Func();
                }
                catch(java.lang.Exception ex)
                {
                    ex.printStackTrace();
                    result.Exception=ex;
                }
                return result;
            }
            
            @Override
            protected void onPostExecute(UIIOperationResult< T> result)
            {
                callback.Completed(result);
            }
        }.execute();
    }

    protected java.lang.Exception convertToException(org.ksoap2.SoapFault fault,UIIExtendedSoapSerializationEnvelope envelope)
    {
        org.ksoap2.SoapFault newException = fault;
        
        if(fault.detail!=null && fault.detail.getChildCount()>0)
        {
            Element detailsNode=(Element)fault.detail.getChild(0);
            try
            {
                SoapObject exceptionObject = null;
                exceptionObject=envelope.GetExceptionDetail(detailsNode,"http://docs.oasis-open.org/wsrf/r-2","ResourceUnknownFault");
                if (exceptionObject != null)
                {
                    UIIResourceUnknownFaultType ex=new UIIResourceUnknownFaultType();
                    ex.loadFromSoap(exceptionObject,envelope);
                    newException = ex;
                }
                exceptionObject=envelope.GetExceptionDetail(detailsNode,"http://docs.oasis-open.org/wsn/b-2","UnableToGetMessagesFault");
                if (exceptionObject != null)
                {
                    UIIUnableToGetMessagesFaultType ex=new UIIUnableToGetMessagesFaultType();
                    ex.loadFromSoap(exceptionObject,envelope);
                    newException = ex;
                }
                exceptionObject=envelope.GetExceptionDetail(detailsNode,"http://docs.oasis-open.org/wsn/b-2","UnableToDestroyPullPointFault");
                if (exceptionObject != null)
                {
                    UIIUnableToDestroyPullPointFaultType ex=new UIIUnableToDestroyPullPointFaultType();
                    ex.loadFromSoap(exceptionObject,envelope);
                    newException = ex;
                }
            }
            catch (java.lang.Exception e)
            {
				if(enableLogging)
				{
					android.util.Log.e(UIIExtendedSoapSerializationEnvelope.TAG,"Error occured",e);
				}
            }
            newException.detail=fault.detail;
            newException.faultactor=fault.faultactor;
            newException.faultcode=fault.faultcode;
            newException.faultstring=fault.faultstring;
            newException.version=fault.version;
        }
        return newException;
    }
}


