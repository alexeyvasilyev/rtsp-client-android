package net.petrocik.onvif;

//----------------------------------------------------
//
// Generated by www.easywsdl.com
// Version: 6.0.1.1
//
// Created by Quasar Development 
//
//----------------------------------------------------



import org.ksoap2.HeaderProperty;
import org.ksoap2.serialization.*;
import org.ksoap2.transport.*;
import org.kxml2.kdom.Element;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class PullPointBinding
{
    interface IWcfMethod
    {
        ExtendedSoapSerializationEnvelope CreateSoapEnvelope() throws java.lang.Exception;

        java.lang.Object ProcessResult(ExtendedSoapSerializationEnvelope __envelope,java.lang.Object result) throws java.lang.Exception;
    }

    String url="";

    int timeOut=60000;
    
    public List< HeaderProperty> httpHeaders= new ArrayList< HeaderProperty>();
    public boolean enableLogging;
    public boolean createClassesForAny = false;

    IServiceEvents callback;

    public PullPointBinding(){}

    public PullPointBinding (IServiceEvents callback)
    {
        this.callback = callback;
    }
    public PullPointBinding(IServiceEvents callback,String url)
    {
        this.callback = callback;
        this.url = url;
    }

    public PullPointBinding(IServiceEvents callback,String url,int timeOut)
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

    protected ExtendedSoapSerializationEnvelope createEnvelope()
    {
        ExtendedSoapSerializationEnvelope envelope= new ExtendedSoapSerializationEnvelope(ExtendedSoapSerializationEnvelope.VER12);
        envelope.enableLogging = enableLogging;
        envelope.createClassesForAny = createClassesForAny;
    
        return envelope;
    }

    protected java.util.List sendRequest(String methodName,ExtendedSoapSerializationEnvelope envelope,org.ksoap2.transport.Transport transport ,com.easywsdl.exksoap2.ws_specifications.profile.WS_Profile profile )throws java.lang.Exception
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

    java.lang.Object getResult(java.lang.Class destObj,java.lang.Object source,String resultName,ExtendedSoapSerializationEnvelope __envelope) throws java.lang.Exception
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

    
    
    
    public GetMessagesResponse GetMessages(final GetMessages GetMessages) throws java.lang.Exception
    {
        com.easywsdl.exksoap2.ws_specifications.profile.WS_Profile __profile = new com.easywsdl.exksoap2.ws_specifications.profile.WS_Profile();
        return (GetMessagesResponse)execute(new IWcfMethod()
        {
            @Override
            public ExtendedSoapSerializationEnvelope CreateSoapEnvelope(){
                ExtendedSoapSerializationEnvelope __envelope = createEnvelope();
                __envelope.addMapping("http://docs.oasis-open.org/wsn/b-2","GetMessages",GetMessages.class);
                __envelope.setOutputSoapObject(GetMessages);
                return __envelope;
            }
            
            @Override
            public java.lang.Object ProcessResult(ExtendedSoapSerializationEnvelope __envelope,java.lang.Object __result)throws java.lang.Exception {
                return (GetMessagesResponse)getResult(GetMessagesResponse.class,__result,"GetMessagesResponse",__envelope);
            }
        },"http://docs.oasis-open.org/wsn/bw-2/PullPoint/GetMessagesRequest",__profile);
    }
    
    
    
    public android.os.AsyncTask< Void, Void, OperationResult< GetMessagesResponse>> GetMessagesAsync(final GetMessages GetMessages)
    {
        return executeAsync(new Functions.IFunc< GetMessagesResponse>() {
            public GetMessagesResponse Func() throws java.lang.Exception {
                return GetMessages( GetMessages);
            }
        },"GetMessages");
    }
    
    
    
    public String DestroyPullPoint(final DestroyPullPoint DestroyPullPoint) throws java.lang.Exception
    {
        com.easywsdl.exksoap2.ws_specifications.profile.WS_Profile __profile = new com.easywsdl.exksoap2.ws_specifications.profile.WS_Profile();
        return (String)execute(new IWcfMethod()
        {
            @Override
            public ExtendedSoapSerializationEnvelope CreateSoapEnvelope(){
                ExtendedSoapSerializationEnvelope __envelope = createEnvelope();
                __envelope.addMapping("http://docs.oasis-open.org/wsn/b-2","DestroyPullPoint",DestroyPullPoint.class);
                __envelope.setOutputSoapObject(DestroyPullPoint);
                return __envelope;
            }
            
            @Override
            public java.lang.Object ProcessResult(ExtendedSoapSerializationEnvelope __envelope,java.lang.Object __result)throws java.lang.Exception {
                SoapObject __soap=(SoapObject)__result;
                java.lang.Object obj = __soap.getProperty("any");        
                if (obj instanceof SoapPrimitive)
                {
                    SoapPrimitive j =(SoapPrimitive) obj;
                    return j.toString();
                }
                else if (obj!= null){
                    return obj.toString();
                }
                return null;
            }
        },"http://docs.oasis-open.org/wsn/bw-2/PullPoint/DestroyPullPointRequest",__profile);
    }
    
    
    
    public android.os.AsyncTask< Void, Void, OperationResult< String>> DestroyPullPointAsync(final DestroyPullPoint DestroyPullPoint)
    {
        return executeAsync(new Functions.IFunc< String>() {
            public String Func() throws java.lang.Exception {
                return DestroyPullPoint( DestroyPullPoint);
            }
        },"DestroyPullPoint");
    }
    
    
    
    public void Notify_1(final Notify Notify) throws java.lang.Exception
    {
        com.easywsdl.exksoap2.ws_specifications.profile.WS_Profile __profile = new com.easywsdl.exksoap2.ws_specifications.profile.WS_Profile();
        execute(new IWcfMethod()
        {
            @Override
            public ExtendedSoapSerializationEnvelope CreateSoapEnvelope(){
                ExtendedSoapSerializationEnvelope __envelope = createEnvelope();
                __envelope.addMapping("http://docs.oasis-open.org/wsn/b-2","Notify",Notify.class);
                __envelope.setOutputSoapObject(Notify);
                return __envelope;
            }
            
            @Override
            public java.lang.Object ProcessResult(ExtendedSoapSerializationEnvelope __envelope,java.lang.Object __result)throws java.lang.Exception {
                return null;
            }
        },"http://docs.oasis-open.org/wsn/bw-2/PullPoint/Notify",__profile);
    }
    
    
    
    public android.os.AsyncTask< Void, Void, OperationResult< Void>> Notify_1Async(final Notify Notify)
    {
        return executeAsync(new Functions.IFunc< Void>()
        {
            @Override
            public Void Func() throws java.lang.Exception {
                Notify_1( Notify);
                return null;
            }
        },"Notify_1") ;
    }

    protected java.lang.Object execute(IWcfMethod wcfMethod,String methodName,com.easywsdl.exksoap2.ws_specifications.profile.WS_Profile profile) throws java.lang.Exception
    {
        org.ksoap2.transport.Transport __httpTransport=createTransport();
        __httpTransport.debug=enableLogging;
        ExtendedSoapSerializationEnvelope __envelope=wcfMethod.CreateSoapEnvelope();
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

    protected < T> android.os.AsyncTask< Void, Void, OperationResult< T>>  executeAsync(final Functions.IFunc< T> func, final String methodName)
    {
        return new android.os.AsyncTask< Void, Void, OperationResult< T>>()
        {
            @Override
            protected void onPreExecute() {
                callback.Starting();
            }
            @Override
            protected OperationResult< T> doInBackground(Void... params) {
                OperationResult< T> result = new OperationResult< T>();
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
            protected void onPostExecute(OperationResult< T> result)
            {
                callback.Completed(result);
            }
        }.execute();
    }

    protected java.lang.Exception convertToException(org.ksoap2.SoapFault fault,ExtendedSoapSerializationEnvelope envelope)
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
                    ResourceUnknownFaultType ex=new ResourceUnknownFaultType();
                    ex.loadFromSoap(exceptionObject,envelope);
                    newException = ex;
                }
                exceptionObject=envelope.GetExceptionDetail(detailsNode,"http://docs.oasis-open.org/wsn/b-2","UnableToGetMessagesFault");
                if (exceptionObject != null)
                {
                    UnableToGetMessagesFaultType ex=new UnableToGetMessagesFaultType();
                    ex.loadFromSoap(exceptionObject,envelope);
                    newException = ex;
                }
                exceptionObject=envelope.GetExceptionDetail(detailsNode,"http://docs.oasis-open.org/wsn/b-2","UnableToDestroyPullPointFault");
                if (exceptionObject != null)
                {
                    UnableToDestroyPullPointFaultType ex=new UnableToDestroyPullPointFaultType();
                    ex.loadFromSoap(exceptionObject,envelope);
                    newException = ex;
                }
            }
            catch (java.lang.Exception e)
            {
				if(enableLogging)
				{
					android.util.Log.e(ExtendedSoapSerializationEnvelope.TAG,"Error occured",e);
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


