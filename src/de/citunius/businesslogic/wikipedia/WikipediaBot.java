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
package de.citunius.businesslogic.wikipedia;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import de.citunius.bbp.im.gm.common.util.Emoji;
import de.citunius.bbp.im.gm.common.util.Util;
import de.citunius.bbp.im.gm.objects.Filter;
import de.citunius.bbp.im.gm.objects.Message;
import de.citunius.bbp.im.gm.objects.PluginReturnMessage;
import de.citunius.bbp.im.gm.objects.WebMessage;
import de.citunius.bbp.objects.AnonymousUserAccount;
import de.citunius.bbp.objects.ChatbotCallback;
import de.citunius.bbp.objects.MobileUserAccount;
import de.citunius.bbp.objects.Userstate;
import de.citunius.businesslogic.wikipedia.services.WikiWebService;
import de.citunius.businesslogicapi.common.ConstantsCommon;
import de.citunius.businesslogicapi.common.ConstantsPlugin;
import de.citunius.businesslogicapi.common.Utilities;
import de.citunius.businesslogicapi.common.services.LocalisationService;
import de.citunius.businesslogicapi.common.services.TemplateService;
import de.citunius.businesslogicapi.common.services.WebService;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * WikipediaBot is the class represents the business logic Wikipedia use case.
 * This business logic interacts with the mobile user and manages all user requests and responses.
 *
 * @author me
 * @version %I%, %G%
 * @since   1.0
*/
public class WikipediaBot {
    
	static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());
	private static String defaultLanguage = "en";
    
    public static LocalisationService ls = null;
    public String tenantId = null;
    public String accountId = null;
    public HashMap<String, String> pluginMap = null;
    
    public String apiId = null;
    public String apiKey = null;
    
    public WikipediaBot(String tenantId, String accountId, HashMap<String, String> pluginMap) {
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
     * 
     * @param message
     * @param chatId
     * @param filter
     * @param username
     * @return
     */
    public PluginReturnMessage managePageRequestForMessageObjectResult(Message message, String userId, Filter filter, String username) {    	
		try {
			// Create the corresponding message
			if (null != filter.getView() && filter.getView().equals(ConstantsBot.VIEW_DEFAULT) ) {
				// Print results using the default template for a common message
				return composePageForDefaultObjectList(userId, filter, message, username);
			} else {
				// Print results using standard common message
				return composePageForDefaultObjectList(userId, filter, message, username);
			}
		} catch (TemplateException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		return null;
    }
    
    /**
     * This function composes the page for the list of default objects using a template
     *
     * @param userId  the chat identifier
     * @param filter  the filter object
     * @param message  the message object
     * @return the <code>SendMessage</code> method
     * @exception IOException On input error.
     * @exception TemplateException On template error.
     * @see IOException
     * @see TemplateException
     */
    private PluginReturnMessage composePageForDefaultObjectList(String userId, Filter filter, Message message, String username) throws TemplateException, IOException {    	
    	TemplateService ts = new TemplateService(pluginMap);
	    Template templatePage = ts.getTemplate("list.ftl");
	    Map<String, Object> dataPage = new HashMap<String, Object>();
	    Writer outPage = new StringWriter();
	    dataPage.put("title", ls.getString("Generic.Results.PageTitle", defaultLanguage));
		dataPage.put("listoutput", getMessageObjectResultForDefaultList(userId, filter, message, username));
		templatePage.process(dataPage, outPage);
		
		PluginReturnMessage pluginReturnMessage = new PluginReturnMessage(null, outPage.toString(), null);
	    return pluginReturnMessage;	   
    }
    
    /**
     * This function composes the data for default objects
     *
     * @param userId  the chat identifier
     * @param job  the job object
     * @param message  the message object
     * @return the composed list of default objects
     */
    private String getMessageObjectResultForDefaultList(String userId, Filter filter, Message message, String username) {    	
    	String text = "";
	    logger.info("MessageResult field content: ["+message.getData().getResult()+"]");
	    
	    if (message.getData() != null && message.getData().getResult() != null) {
	    	JSONArray jsonArray = null;
	    	try {
				jsonArray = new JSONArray(message.getData().getResult());
			} catch(Exception e) {
				logger.error(e);
			}
			if (jsonArray != null && jsonArray.length() != 0) {
				logger.info("jsonArray: ["+jsonArray+"] jsonArray.length(): ["+jsonArray.length()+"]");
				for (int i = 0; i < jsonArray.length(); i++) {				
					WebMessage wm = new WebMessage(jsonArray.getJSONObject(i));
					logger.info("WebMessage: Owner: ["+wm.getOwner()+"] Content: ["+wm.getContent()+"]");
					text += String.format(ls.getString("NewMsgCompose.TextHeader", defaultLanguage), Util.replaceRestrictedChars((username != null ? username : "UnknownUser"))) + "\n\n";
					text += "--------------------------\n";
					text += Util.replaceRestrictedChars(wm.getContent())+"\n";		    	
			    	text += "--------------------------\n\n";
			    	text += ls.getString("NewMsgCompose.TextFooter", defaultLanguage);
				}
			} else {
				//text += ConstantsGeneric.ERROR_INTERNALPROCESSING;
				text += ls.getString("Common.NoResultsFound", defaultLanguage);
			}
	    } else {
	    	text += ls.getString("Common.NoResultsFound", defaultLanguage);
	    }
		// Add attachments
		if (message.getAudio() != null) {
			logger.info("Adding audio to message");
			text += ConstantsCommon.FILEUUID_TAG_BEGIN + message.getAudio().getFileUUID() + ConstantsCommon.FILEUUID_TAG_END;
		}
		if (message.getDocument() != null) {
			logger.info("Adding document to message");
			text += ConstantsCommon.FILEUUID_TAG_BEGIN + message.getDocument().getFileUUID() + ConstantsCommon.FILEUUID_TAG_END;
		}
		if (message.getLocation() != null) {
			logger.info("Adding location to message");
			text += ConstantsCommon.LOCATION_TAG_BEGIN + message.getLocation().getLatitude() + ConstantsCommon.LOCATION_TAG_DELIMITER + message.getLocation().getLongitude() + ConstantsCommon.LOCATION_TAG_END;
		}
		if (message.getPhoto() != null && message.getPhoto().size() != 0) {
			logger.info("Adding photos to message");
			for (int p=0; p<message.getPhoto().size(); p++) {
				text += ConstantsCommon.FILEUUID_TAG_BEGIN + message.getPhoto().get(p).getFileUUID() + ConstantsCommon.FILEUUID_TAG_END;
			}			
		}
		if (message.getVideo() != null) {
			logger.info("Adding video to message");
			text += ConstantsCommon.FILEUUID_TAG_BEGIN + message.getVideo().getFileUUID() + ConstantsCommon.FILEUUID_TAG_END;
		}
		if (message.getVoice() != null) {
			logger.info("Adding voice to message");
			text += ConstantsCommon.FILEUUID_TAG_BEGIN + message.getVoice().getFileUUID() + ConstantsCommon.FILEUUID_TAG_END;
		}
		
		return text;		
    }

	private static String getCommandAbout() {
	    return String.format(ls.getString("Common.About", defaultLanguage), Emoji.ABOUT.toString());
	}

	private static String getCommandBack() {
	    return String.format(ls.getString("Common.Back", defaultLanguage), Emoji.BACK_WITH_LEFTWARDS_ARROW_ABOVE.toString());
	}

	private static String getCommandCancel() {
	    return String.format(ls.getString("Common.Cancel", defaultLanguage), Emoji.DISCUSSION_NOREPLY.toString());
	}

	private static String getCommandLanguages() {
	    return String.format(ls.getString("Languages", defaultLanguage), Emoji.GLOBE_WITH_MERIDIANS.toString());
	}

	private String getMessageLanguage() {
	    String baseString = ls.getString("Language.SelectLanguage", defaultLanguage);
	    return String.format(baseString, defaultLanguage);
	}

	/**
	 * 
	 * @param tenantId
	 * @param accountId
	 * @param jsonMessage
	 * @param jsonMobileUserAccount
	 * @return
	 */
	public String handleIncomingMessage(String tenantId, String accountId, String jsonMessage, boolean anonymousUserAccountExists, String jsonAnonymousUserAccount, boolean mobileUserAccountExists, String jsonMobileUserAccount) {		
		logger.info("Plugin: New jsonMessage: ["+jsonMessage+"]");
		JSONObject jsonObjectMessage = new JSONObject(jsonMessage);
		Message message = new Message(jsonObjectMessage);
		AnonymousUserAccount anonymousUserAccount = null;
		if (anonymousUserAccountExists) {
			JSONObject jsonObjectAnonymousUserAccount = new JSONObject(jsonAnonymousUserAccount);
			anonymousUserAccount = new AnonymousUserAccount(jsonObjectAnonymousUserAccount);
		}
		MobileUserAccount mobileUserAccount = null;
		if (mobileUserAccountExists) {
			JSONObject jsonObjectMobileUserAccount = new JSONObject(jsonMobileUserAccount);
			mobileUserAccount = new MobileUserAccount(jsonObjectMobileUserAccount);
		}
		logger.info("Plugin: New message: ["+message.getText()+"]");
		logger.info("From UserId: ["+message.getFrom().getId()+"] First-/Lastname: ["+message.getFrom().getFirstName()+" "+message.getFrom().getLastName()+"]");
		logger.info("message text: ["+message.hasText()+"]");

		logger.info("Message received from user: ["+message.getFrom().getUserName()+"]");
		if (mobileUserAccount != null) {
			logger.info("Username: ["+mobileUserAccount.getUsername()+"]");
		}
		
		String replySubject = ls.getString("Common.DefaultReplySubject", defaultLanguage);
		String replyMessage = ls.getString("Common.NoResultsFound", defaultLanguage);
		String replyErrorMessage = ls.getString("Common.Error", defaultLanguage);

    	try {
			Userstate userstate = Utilities.getUserState(tenantId, accountId, ConstantsPlugin.PLUGIN_NAME, pluginMap, message.getFrom().getUserName());
			// Get user language to default language
	    	if (userstate != null && userstate.getUserLanguageCode() != null) {
	    		defaultLanguage = userstate.getUserLanguageCode();
	    		logger.info("Set user language code to ["+userstate.getUserLanguageCode()+"]");
	    	} else {
	    		logger.info("No user state found for user ["+message.getFrom().getUserName()+"]@["+ConstantsPlugin.PLUGIN_NAME+"]");
	    	}
		} catch(Exception e) {
			logger.error("Exception: "+e.getMessage());
			logger.error(e);
			return new PluginReturnMessage(replySubject, replyErrorMessage, null).toJson().toString();
		}		
    	
    	if (message.getText() != null && message.getText().length() != 0) {
    		WikiWebService ws = new WikiWebService(pluginMap);
        	String result = ws.getWikiResult(message.getText());
        	
        	if (result != null) {
        		replyMessage = result;
        	} else {
        		replyMessage = ls.getString("Common.NoResultsFound", defaultLanguage);
        	}
    	} else {
    		logger.info("The user message does not contain an article name.");
    		replyMessage = ls.getString("Wikipedia.NoArticleName", defaultLanguage);
    	}  
    	return new PluginReturnMessage(replySubject, replyMessage, null).toJson().toString();
	}
	
	/**
	 * 
	 * @param message
	 * @param filter
	 * @param mobileUserAccount
	 * @return
	 */
	public String handleIncomingCallback(String tenantId, String accountId, String message, boolean anonymousUserAccountExists, String jsonAnonymousUserAccount, boolean mobileUserAccountExists, String jsonMobileUserAccount) {
		logger.info("handleIncomingCallback() called");
		
		String replyMessage = "NOT_IMPLEMENTED";
		
		PluginReturnMessage pluginReturnMessage = new PluginReturnMessage(null, "Callback message: ["+replyMessage+"]", null);
		return pluginReturnMessage.toJson().toString();
	}
	
	/**
	 * Call webservice to schedule a callback for this plugin
	 * 
	 * @param mobileUserAccount  the mobile user account object
	 * @return
	 */
	public boolean scheduleCallback(boolean hasAnonymousUserAccount, AnonymousUserAccount anonymousUserAccount, boolean hasMobileUserAccount, MobileUserAccount mobileUserAccount) {
		String cronSchedule = "0 0/1 * * * ? *";
		String callbackMessage = "reminder-xy";
		ChatbotCallback chatbotCallback = new ChatbotCallback(
				Integer.parseInt(pluginMap.get(ConstantsPlugin.BOTID)), 
				Integer.parseInt(pluginMap.get(ConstantsPlugin.PLUGIN_ID)), 
				hasAnonymousUserAccount,
				anonymousUserAccount,
				hasMobileUserAccount,
				mobileUserAccount, 
				cronSchedule, 
				callbackMessage);
		
		// Register chatbot callback to BBP webservices
		String receiptId = null;
		try {
			WebService ws = new WebService(pluginMap);
			receiptId = ws.registerChatbotCallback(chatbotCallback);
		} catch (Exception e) {
			logger.error("Unable to register chatbot callback ["+chatbotCallback.getCallbackMessage()+"] to BBP webservices: "+e.getMessage());
			return false;
		}
		if (receiptId != null && receiptId.length() != 0) {
			logger.info("Chatbot callback ["+chatbotCallback.getCallbackMessage()+"] has been registered to BBP webservices successfully. Receipt Id: ["+receiptId+"]");
		} else {
			logger.error("Failed to register chatbot callback ["+chatbotCallback.getCallbackMessage()+"] to BBP webservices");
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @param message
	 * @param filter
	 * @param mobileUserAccount
	 * @return
	 */
	public String sendTextMessage(String tenantId, String accountId, String jsonMessage, String jsonFilter, boolean anonymousUserAccountExists, String jsonAnonymousUserAccount, boolean mobileUserAccountExists, String jsonMobileUserAccount) {
		logger.info("sendTextMessage() called");
		JSONObject jsonObjectMessage = new JSONObject(jsonMessage);
		Message message = new Message(jsonObjectMessage);
		JSONObject jsonObjectFilter = new JSONObject(jsonFilter);
		Filter filter = new Filter(jsonObjectFilter);
		
		logger.info("Plugin: New message: ["+message.getText()+"] from UserId: ["+message.getFrom().getUserName()+"] First-/Lastname: ["+message.getFrom().getFirstName()+" "+message.getFrom().getLastName()+"]");
		AnonymousUserAccount anonymousUserAccount = null;
		MobileUserAccount mobileUserAccount = null;
		String userId = null;
		if (anonymousUserAccountExists) {
			JSONObject jsonObjectAnonymousUserAccount = new JSONObject(jsonAnonymousUserAccount);
			anonymousUserAccount = new AnonymousUserAccount(jsonObjectAnonymousUserAccount);
			logger.info("Managing request with AnonymousUserAccount: userId: ["+anonymousUserAccount.getUserId()+"]");
			userId = anonymousUserAccount.getUserId();
		}
		if (mobileUserAccountExists) {
			JSONObject jsonObjectMobileUserAccount = new JSONObject(jsonMobileUserAccount);
			mobileUserAccount = new MobileUserAccount(jsonObjectMobileUserAccount);
			logger.info("Managing request with MobileUserAccount: Id: ["+mobileUserAccount.getId()+"]");
			userId = Utilities.getMobileAppSettingValue(Utilities.getMobileAppSettingParameterNameUserId(tenantId, accountId, pluginMap), mobileUserAccount.getPrimaryMobileDevice().getPrimaryMobileApp().getMobileAppSettings());
		}
		
		if (userId == null) {
			logger.info("userId not found");
			return null;
		} else {
			logger.info("Using userId: ["+userId+"]");
		}
		
		PluginReturnMessage pluginReturnMessage = managePageRequestForMessageObjectResult(message, userId, filter, (mobileUserAccountExists ? mobileUserAccount.getUsername() : null));
		// Manage attachments
		PluginReturnMessage pluginReturnMessageWithAttachments = manageAttachments(pluginReturnMessage, message);
		return pluginReturnMessageWithAttachments.toJson().toString();
    }
	
	/**
	 * Manage attachments 
	 * 
	 * @param pluginReturnMessage  the plugin return message
	 * @return the plugin return message with attachments
	 */
	public PluginReturnMessage manageAttachments(PluginReturnMessage pluginReturnMessage, Message message) {
		// Add media such as photos and documents (from incoming message)
		if (message.getAudio() != null) {
			logger.info("Adding audio to pluginReturnMessage");
			pluginReturnMessage.setAudio(message.getAudio());
		}
		if (message.getDocument() != null) {
			logger.info("Adding document to pluginReturnMessage");
			pluginReturnMessage.setDocument(message.getDocument());
		}
		if (message.getLocation() != null) {
			logger.info("Adding location to pluginReturnMessage");
			pluginReturnMessage.setLocation(message.getLocation());
		}
		if (message.getPhoto() != null && message.getPhoto().size() != 0) {
			logger.info("Adding photo to pluginReturnMessage");
			pluginReturnMessage.setPhoto(message.getPhoto());
		}
		if (message.getVideo() != null) {
			logger.info("Adding video to pluginReturnMessage");
			pluginReturnMessage.setVideo(message.getVideo());
		}
		if (message.getVoice() != null) {
			logger.info("Adding voice to pluginReturnMessage");
			pluginReturnMessage.setVoice(message.getVoice());
		}		
		return pluginReturnMessage;
	}
}
