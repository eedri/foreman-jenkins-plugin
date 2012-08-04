package org.eedri.jenkins.plugins;

import java.io.IOException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

public class ForemanHttpController {
	private String url;
	private int    port;
	private String user;
	private String password;
	private HttpClient httpClient;
	
	// const
	public ForemanHttpController(String url, int port, String user, String pass) {
		this.url = url;
		this.port = port;
		this.user = user;
		this.password = pass;
		
		InitHttpClient();
	}

	private void InitHttpClient() {
		this.httpClient = new HttpClient();
		
		// setup credentails auth for foreman url
		this.httpClient.getParams().setAuthenticationPreemptive(true);
		Credentials defaultcreds = new UsernamePasswordCredentials(this.user, this.password);
		AuthScope authScope = new AuthScope(this.url, this.port, AuthScope.ANY_REALM);
		this.httpClient.getState().setCredentials(authScope, defaultcreds);
	}
	
	public void validateServerConn() throws HttpException, IOException {
		GetMethod get = new GetMethod(this.url + "/hosts");
		get.setDoAuthentication( true );
		
		try {
				// check if we need to handle SSL CERTS
				//TODO: implement this
				if (this.port == 443) {
					HandleSSLCertValidation();
				}
			
				// execute the GET
				int status = this.httpClient.executeMethod( get );
				
				// print the status and response
				System.out.println(status + "\n" + get.getResponseBodyAsString());
		}
		
		finally {
		    // release any connection resources used by the method
			get.releaseConnection();
		}
		
	}

	private void HandleSSLCertValidation() {
		// TODO remove this once we handle SSL certs
		
	}
}
