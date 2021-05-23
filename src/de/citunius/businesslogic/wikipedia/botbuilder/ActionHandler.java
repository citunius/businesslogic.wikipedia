/*
 * Copyright: (c) 2015-2021, Citunius GmbH. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * Licence: This program contains proprietary and trade secret information of Citunius GmbH.
 *          Copyright notice is precautionary only and does not evidence any actual or intended 
 *          publication of such program
 *          See: https://www.citunius.de/en/legal
 *
 * Requires: JDK 1.8+
 *
 */
package de.citunius.businesslogic.wikipedia.botbuilder;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import de.citunius.bbp.im.gm.objects.Message;
import de.citunius.bbp.objects.AnonymousUserAccount;
import de.citunius.bbp.objects.MobileUserAccount;
import de.citunius.bbp.objects.Userstate;
import de.citunius.businesslogicapi.common.ConstantsCommon;
import de.citunius.businesslogicapi.common.ConstantsPlugin;
import de.citunius.businesslogicapi.common.Utilities;
import de.citunius.businesslogicapi.common.services.LocalisationService;

/**
 * Action handler of the bot builder service
 *
 * @author me
 * @version %I%, %G%
 * @since   1.0
 */
public class ActionHandler {
	static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());
	private static String defaultLanguage = "en";
	public static LocalisationService ls = null;
	private static final String CLASS_NAME_BOTBUILDERSERVICE = de.citunius.businesslogic.wikipedia.botbuilder.BotBuilder.class.getName();
	
	public String tenantId = null;
    public String accountId = null;
    public HashMap<String, String> pluginMap = null;
    
    public String apiId = null;
    public String apiKey = null;
    
    public ActionHandler(String tenantId, String accountId, HashMap<String, String> pluginMap) {
        logger.info("called");
        this.tenantId = tenantId;
        this.accountId = accountId;
        this.pluginMap = pluginMap;
        
        this.apiId = Utilities.getAttributeFromPropertiesFile(pluginMap, ConstantsCommon.BUSINESSBOTPLATFORM_ACCOUNT_APIID);
        this.apiKey = Utilities.getAttributeFromPropertiesFile(pluginMap, ConstantsCommon.BUSINESSBOTPLATFORM_ACCOUNT_APIKEY);
        
        logger.info("tenantId: ["+tenantId+"] accountId: ["+accountId+"]");
        ls = new LocalisationService(pluginMap);
    }
    
	/**
	 * Performs the action requested by the bot builder service
	 * 
	 * @param tenantId  the tenant identifier
	 * @param accountId  the account identifier
	 * @param jsonMessage the JSON message contains the Message object 
	 * @param jsonMobileUserAccount the JSON message contains the MobileUserAccount object
	 * @return
	 */
	public String performAction(String tenantId, String accountId, String jsonMessage, boolean anonymousUserAccountExists, String jsonAnonymousUserAccount, boolean mobileUserAccountExists, String jsonMobileUserAccount) {		
		logger.info("Plugin: New jsonMessage: ["+jsonMessage+"]");
		JSONObject jsonObjectMessage = new JSONObject(jsonMessage);
		Message message = new Message(jsonObjectMessage);
		logger.info("Plugin: New message: ["+message.getText()+"] from UserId: ["+message.getFrom().getUserName()+"] First-/Lastname: ["+message.getFrom().getFirstName()+" "+message.getFrom().getLastName()+"]");
		AnonymousUserAccount anonymousUserAccount = null;
		MobileUserAccount mobileUserAccount = null;
		if (anonymousUserAccountExists) {
			JSONObject jsonObjectAnonymousUserAccount = new JSONObject(jsonAnonymousUserAccount);
			anonymousUserAccount = new AnonymousUserAccount(jsonObjectAnonymousUserAccount);
			logger.info("Managing request with AnonymousUserAccount: userId: ["+anonymousUserAccount.getUserId()+"]");
		}
		if (mobileUserAccountExists) {
			JSONObject jsonObjectMobileUserAccount = new JSONObject(jsonMobileUserAccount);
			mobileUserAccount = new MobileUserAccount(jsonObjectMobileUserAccount);
			logger.info("Managing request with MobileUserAccount: Id: ["+mobileUserAccount.getId()+"]");
		}
		
		// Set user language to default language
		Userstate userstate = Utilities.getUserState(tenantId, accountId, ConstantsPlugin.PLUGIN_NAME, pluginMap, message.getFrom().getUserName());
		if (userstate != null && userstate.getUserLanguageCode() != null) {
    		defaultLanguage = userstate.getUserLanguageCode();
    		logger.info("Set user language code to ["+userstate.getUserLanguageCode()+"]");
    	} else {
    		logger.info("No user state found for user ["+message.getFrom().getUserName()+"]@["+ConstantsPlugin.PLUGIN_NAME+"]");
    	}
		
    	// Call the Bot Builder Service
    	String className = CLASS_NAME_BOTBUILDERSERVICE;
    	logger.info("Class to invoke: ["+className+"]");
    	
    	if (pluginMap.containsKey(ConstantsPlugin.BOTBUILDER_FUNCTION_NAME.toString())) {
			String methodName = pluginMap.get(ConstantsPlugin.BOTBUILDER_FUNCTION_NAME.toString());
			logger.info("Extracted method to invoke: ["+methodName+"]");
			Class<?>[] parameterTypes = {HashMap.class, LocalisationService.class, String.class};
			logger.info("parameterTypes to invoke: ["+parameterTypes.toString()+"]");
			
			try {
				Class<?> c = Class.forName(className);
				Method  method = c.getDeclaredMethod (methodName, parameterTypes);
				Object returnedObj = method.invoke (c.newInstance(), pluginMap, ls, defaultLanguage);
				String returnedStr = (String)returnedObj;										
				return returnedStr;
			} catch (InstantiationException e) {
	    		logger.error("InstantiationException for class ["+className+"]");
	    		logger.error(e);
				return null;
	    	} catch (ClassNotFoundException e) {
	    		logger.error("Bot Builder function is not implemented");
	    		logger.error(e);
				return null;
	    	} catch (NoSuchMethodException e) {
	    		logger.error("Bot Builder function is not implemented");
	    		logger.error(e);
				return null;
	    	} catch (InvocationTargetException e) {
	    		logger.error("Bot Builder function is not implemented");
	    		logger.error(e);
				return null;
	    	} catch (IllegalAccessException e) {
	    		logger.error("Bot Builder function is not implemented");
	    		logger.error(e);
				return null;
	    	}
		} else {
			logger.error("Bot Builder function is not implemented");
			return null;
		}
    	
	}
}
