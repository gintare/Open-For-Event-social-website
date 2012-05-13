package com.openforevent.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javolution.util.FastMap;

import org.apache.commons.io.IOUtils;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;
import org.ofbiz.webapp.stats.VisitHandler;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.GenericServiceException;
import javax.security.auth.login.LoginException;


public class FacebookServices {
	
	public static final String module = FacebookServices.class.getName();
	public static final String access_token = "access_token";
	public static final String expires = "expires" ;

	public static Map<String, Object> storeFacebookToken(DispatchContext ctx, Map<String, ? extends Object> context) throws IOException, GenericEntityException{
		 Delegator delegator = ctx.getDelegator();
         LocalDispatcher dispatcher = ctx.getDispatcher();
         String userLoginId = "admin"; 
         String state = (String)context.get("state"); 
         String code = (String)context.get("code"); 
         String error = (String)context.get("error"); 
         String error_reason = (String)context.get("error_reason"); 
         String error_description = (String)context.get("error_description"); 
         
         Debug.logInfo("Facebook login : state="+state+", code="+code, module);
         
         if(code != null){
        	 
        	 //get long time access_token
        	 URL url = new URL("https://graph.facebook.com/oauth/access_token?" +
        	 		"client_id=283576871735609" +
        	 		"&redirect_uri=https://localhost:8443/events/control/storeFacebookToken" +
        	 		"&client_secret=5cf1fe4e531dff8de228bfac61b8fdfa" +
        	 		"&code="+code);
        	 InputStream inputStream = httpConnection(url);
             StringWriter writer = new StringWriter();
             IOUtils.copy(inputStream, writer, "UTF-8");
             String theString = writer.toString();
             String token = theString.substring(theString.indexOf(access_token+"=")+access_token.length()+1, theString.indexOf("&"));
             String expire = theString.substring(theString.indexOf(expires+"=")+expires.length()+1);
             Timestamp expireTmsp = new Timestamp(UtilDateTime.nowTimestamp().getTime() + Long.parseLong(expire) * 1000);

             Debug.logInfo("Facebook access_token string = "+theString+", token = "+token+", expireTmsp = "+expireTmsp, module);
             
             GenericValue facebookLogin = delegator.makeValue("FacebookLogin");
             facebookLogin.set("facebookLoginId", state);
             facebookLogin.set("userLoginId", userLoginId);
             facebookLogin.set("accessToken", token);
             facebookLogin.set("expireTime", expireTmsp);
             facebookLogin.set("statusId", "FB_VALID");
             delegator.createOrStore(facebookLogin);
         }
         
         return new HashMap<String, Object>();
	}
	
	private static InputStream httpConnection(URL url) throws IOException{
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
   	    urlConnection.setRequestMethod("GET");
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setUseCaches(false);
        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        int responseCode = urlConnection.getResponseCode();
        InputStream inputStream;
        if (responseCode == HttpURLConnection.HTTP_OK) {
            inputStream = urlConnection.getInputStream();
        } else {
            inputStream = urlConnection.getErrorStream();
        }
        return inputStream;
	}
	
	public static Map<String, Object> loadFacebookPrimaryImport(DispatchContext ctx, Map<String, ? extends Object> context) throws IOException, GenericServiceException{
		Delegator delegator = ctx.getDelegator();
        LocalDispatcher dispatcher = ctx.getDispatcher();
        
        FastMap<String, Object> args = FastMap.newInstance();
        dispatcher.runAsync("facebookPrimaryImport", args);       
        
		return new HashMap<String, Object>();
	}
	
	public static Map<String, Object> facebookPrimaryImport(DispatchContext ctx, Map<String, ? extends Object> context) throws IOException, GenericServiceException{
		Delegator delegator = ctx.getDelegator();
        LocalDispatcher dispatcher = ctx.getDispatcher();
        Integer rowsPerPage = 3;
        
        FastMap<String, Object> args = FastMap.newInstance();
        args.put("fromDate", UtilDateTime.nowTimestamp());
        args.put("rowsPerPage", rowsPerPage);
        Map<String, Object> results = dispatcher.runSync("importFacebookEvents", args);
        /*if(results.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_ERROR)){
        	Debug.logError((String) results.get(ModelService.ERROR_MESSAGE), module);
        }*/
        
		return new HashMap<String, Object>();
	}
	
	public static Map<String, Object> importFacebookEvents(DispatchContext ctx, Map<String, ? extends Object> context) throws LoginException, GenericEntityException, IOException{
		Delegator delegator = ctx.getDelegator();
        LocalDispatcher dispatcher = ctx.getDispatcher();
        Timestamp fromDate = (Timestamp)context.get("fromDate"); 
        Integer rowsPerPage = (Integer)context.get("rowsPerPage"); 
        String userLoginId = "admin";
        String eventsUrl = "https://graph.facebook.com/search?";
        String eventUrl = "https://graph.facebook.com/";
        int fetchedEventsTotal;
        int storedEventsTotal;
        
        Debug.logInfo("fromDate = "+fromDate+", rowsPerPage = "+rowsPerPage, module);
        
        //define search key that is given date - 1 day
        String searchKey = "2012-05-13";
        
        //obtain valid access token
        List<EntityCondition> clpConditionList = new ArrayList<EntityCondition>();
        clpConditionList.add(EntityCondition.makeCondition("userLoginId", EntityOperator.EQUALS, userLoginId));
        clpConditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "FB_VALID"));
        EntityConditionList<EntityCondition> clpConditions = EntityCondition.makeCondition(clpConditionList, EntityOperator.AND);
        EntityCondition condition = EntityCondition.makeCondition(clpConditions);
        List<GenericValue> login = delegator.findList("FacebookLogin", condition, null, null, null, true);
        if(login.size() == 0){
        	throw new LoginException("Theres no valid Facebook acccess_token for this user !");
        }
        String token = (String)EntityUtil.getFirst(login).get("accessToken");
        String facebookLoginId = (String)EntityUtil.getFirst(login).get("facebookLoginId");
        Debug.logInfo("Facebook access token - "+token, module);
        
        int pageNo = 0;
        URL url = new URL(eventsUrl+"since=now&limit="+rowsPerPage.toString()+
        		"&q="+searchKey+"&"+"type=event&access_token="+token);
        
        //Begin to collect metadata info
        GenericValue meta = delegator.makeValue("FacebookImportMetadata");
        meta.put("facebookImportMetadataId", delegator.getNextSeqId("FacebookImportMetadata"));
        meta.put("facebookLoginId", facebookLoginId);
        meta.put("pageUrl", url.toString());
        meta.put("pageNo", Integer.toString(pageNo));
        meta.put("searchKey", searchKey);
        meta.put("rowsToFech", Long.parseLong(rowsPerPage.toString()));
        meta.put("startTime", fromDate);
        delegator.createOrStore(meta);
        
        //obtain events data 
        InputStream inputStream = httpConnection(url);
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer, "UTF-8");
        String theString = writer.toString();
        Debug.logInfo("Events fetched = "+theString, module);       
        
		return new HashMap<String, Object>();
	}

}
