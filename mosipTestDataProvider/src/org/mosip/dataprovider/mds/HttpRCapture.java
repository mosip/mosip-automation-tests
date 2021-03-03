package org.mosip.dataprovider.mds;
import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

public class HttpRCapture extends HttpEntityEnclosingRequestBase
//HttpRequestBase 
{

	    String METHOD_NAME ;

	    public void setMethod(String method) {
	    	METHOD_NAME = method;
	    }
	    public HttpRCapture() {
	        super();
	        METHOD_NAME = "RCAPTURE";
	    }

	    @Override
	    public String getMethod() {
	        return METHOD_NAME;  
	    }

	    public HttpRCapture(final String uri) {
	        super();
	        setURI(URI.create(uri));
	        METHOD_NAME = "RCAPTURE";
	    }

	    public String getName() {
	        return METHOD_NAME;
	    }
}

