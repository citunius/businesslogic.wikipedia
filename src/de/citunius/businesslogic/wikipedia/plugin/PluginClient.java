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
package de.citunius.businesslogic.wikipedia.plugin;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.pf4j.RuntimeMode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.citunius.bbp.objects.PluginPreparationReturnMessage;
import de.citunius.businesslogic.plugin.api.BusinessLogicInterface;
import de.citunius.businesslogic.wikipedia.ConstantsBot;
import de.citunius.businesslogic.wikipedia.WikipediaBot;
import de.citunius.businesslogic.wikipedia.botbuilder.ActionHandler;
import de.citunius.businesslogicapi.common.ConstantsPlugin;
import de.citunius.businesslogicapi.objects.BusinessBotBuilderPluginFunction;
import de.citunius.businesslogicapi.objects.BusinessBotBuilderPluginFunctionParameter;

/**
 * The business logic plugin.
 *
 * @author me
 * @version %I%, %G%
 * @since   1.0
 */
public class PluginClient extends Plugin {
	static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());	
	
	/**
	 * If you want to use the dialogue designer of the business bot platform, 
	 * you have to use true and must implement the required functions
	 */
	private final static boolean isBotBuilderModelSupported = true;
	/**
	 * If a license for this business logic is required
	 */
	private final static boolean isLicenseRequired = false;
	/**
	 * If you want to use the web UI business logic of the business bot platform, 
	 * you have to use true and must implement the required functions
	 */
	private final static boolean isWebUISupported = false;
	
    public PluginClient(PluginWrapper wrapper) {
        super(wrapper);
    }

    /**
     * This function is called when starting the plugin (business logic)
     */
    @Override
    public void start() {
        logger.debug("Plugin ["+ConstantsBot.PLUGIN_NAME+"] started");
        // for testing the development mode
        if (RuntimeMode.DEVELOPMENT.equals(wrapper.getRuntimeMode())) {
        	logger.debug("Plugin ["+ConstantsBot.PLUGIN_NAME+"] started in Development Mode");
        }
    }

    /**
     * This function is called when stopping the plugin (business logic)
     */
    @Override
    public void stop() {
    	logger.debug("Plugin ["+ConstantsBot.PLUGIN_NAME+"] stopped");
    }
    
    /**
     * 
     * Business Logic classes
     *
     */
    @Extension(ordinal=1)
    public static class BusinessLogic implements BusinessLogicInterface {
    	/**
    	 * This hashmap is provided by the business bot platform and contains some plugin information
    	 */
    	public static HashMap<String, String> pluginMap = null;
    	
    	/**
    	 * Prepare business chatbot for operation
    	 * 
    	 * <p>Preparation of this business chatbot may include database setup,
    	 * configuration and other tasks</p>
    	 * 
    	 * @param tenantId  the tenant identifier
    	 * @param accountId  the account identifier
    	 * @param pluginMap  the plugin hashmap
    	 * @param jsonMessage  the JSON formatted message
    	 * 
    	 * @return the JSON formatted message as reply
    	 */
    	@Override
		public String prepare(String tenantId, String accountId, HashMap<String, String> pluginMap, String jsonMessage) {
    		logger.info("Plugin -> jsonMessage: ["+jsonMessage+"]");
			BusinessLogic.pluginMap = pluginMap;
			PluginPreparationReturnMessage pluginReturnMessage = new PluginPreparationReturnMessage(ConstantsPlugin.PLUGIN_PREPARATIONSTATUS_SUCCESS, "Plugin has been prepared");
			return pluginReturnMessage.toJson().toString();	        
		}
    	
    	/**
    	 * Manage incoming message
    	 * 
    	 * @param tenantId  the tenant identifier
    	 * @param accountId  the account identifier
    	 * @param pluginMap  the plugin hashmap
    	 * @param jsonMessage  the JSON formatted message
    	 * @param jsonMobileUserAccount  the JSON formatted mobile user account
    	 * 
    	 * @return the JSON formatted message as reply
    	 */
    	@Override
		public String handleIncomingMessage(String tenantId, String accountId, HashMap<String, String> pluginMap, String jsonMessage, boolean anonymousUserAccountExists, String jsonAnonymousUserAccount, boolean mobileUserAccountExists, String jsonMobileUserAccount) {
    		logger.info("Plugin -> jsonMessage: ["+jsonMessage+"]");
			BusinessLogic.pluginMap = pluginMap;
			WikipediaBot wikiBotHandlers = new WikipediaBot(tenantId, accountId, pluginMap);
			String sendMessageJsonStr = wikiBotHandlers.handleIncomingMessage(tenantId, accountId, jsonMessage, anonymousUserAccountExists, jsonAnonymousUserAccount, mobileUserAccountExists, jsonMobileUserAccount);			
			return sendMessageJsonStr;
		}

    	/**
    	 * Manage callback request
    	 * 
    	 * @param tenantId  the tenant identifier
    	 * @param accountId  the account identifier
    	 * @param pluginMap  the plugin hashmap
    	 * @param jsonMessage  the JSON formatted message
    	 * @param jsonMobileUserAccount  the JSON formatted mobile user account
    	 * 
    	 * @return the JSON formatted message as reply
    	 */
		@Override
		public String handleIncomingCallback(String tenantId, String accountId, HashMap<String, String> pluginMap, String message, boolean anonymousUserAccountExists, String jsonAnonymousUserAccount, boolean mobileUserAccountExists, String jsonMobileUserAccount) {
			logger.info("Plugin -> callback message: ["+message+"] tenantId:["+tenantId+"] accountId:["+accountId+"] pluginMap size:["+(pluginMap != null ? pluginMap.size() : "NULL")+"] message:["+message+"]");
			BusinessLogic.pluginMap = pluginMap;
			
			WikipediaBot wikiBotHandlers = new WikipediaBot(tenantId, accountId, pluginMap);
			String sendMessageJsonStr = wikiBotHandlers.handleIncomingCallback(tenantId, accountId, message, anonymousUserAccountExists, jsonAnonymousUserAccount, mobileUserAccountExists, jsonMobileUserAccount);
			return sendMessageJsonStr;
		}
		
    	/**
    	 * Manage request to send a text message (without previous message from mobile user)
    	 * 
    	 * @param tenantId  the tenant identifier
    	 * @param accountId  the account identifier
    	 * @param pluginMap  the plugin hashmap
    	 * @param jsonMessage  the JSON formatted message
    	 * @param jsonFilter  the JSON formatted filter
    	 * @param jsonMobileUserAccount  the JSON formatted mobile user account
    	 * 
    	 * @return the JSON formatted message as reply
    	 */    
		@Override
		public String sendMessage(String tenantId, String accountId, HashMap<String, String> pluginMap, String jsonMessage, String jsonFilter, boolean anonymousUserAccountExists, String jsonAnonymousUserAccount, boolean mobileUserAccountExists, String jsonMobileUserAccount) {
			logger.info("Plugin -> jsonMessage: "+jsonMessage);
			BusinessLogic.pluginMap = pluginMap;
			
			WikipediaBot wikiBotHandlers = new WikipediaBot(tenantId, accountId, pluginMap);
			String sendMessageJsonStr = wikiBotHandlers.sendTextMessage(tenantId, accountId, jsonMessage, jsonFilter, anonymousUserAccountExists, jsonAnonymousUserAccount, mobileUserAccountExists, jsonMobileUserAccount);
			return sendMessageJsonStr;
		}

		/**
    	 * This function is called when using the dialogue designer model
    	 * 
    	 * @param tenantId  the tenant identifier
    	 * @param accountId  the account identifier
    	 * @param pluginMap  the plugin hashmap
    	 * @param jsonMessage  the JSON formatted message
    	 * @param jsonMobileUserAccount  the JSON formatted mobile user account
    	 * 
    	 * @return the text message
    	 */
		@Override
		public String callBotBuilderFunction(String tenantId, String accountId, HashMap<String, String> pluginMap, String jsonMessage, boolean anonymousUserAccountExists, String jsonAnonymousUserAccount, boolean mobileUserAccountExists, String jsonMobileUserAccount) {
			BusinessLogic.pluginMap = pluginMap;
			ActionHandler ah = new ActionHandler(tenantId, accountId, pluginMap);
			String sendMessageStr = ah.performAction(tenantId, accountId, jsonMessage, anonymousUserAccountExists, jsonAnonymousUserAccount, mobileUserAccountExists, jsonMobileUserAccount);			
			return sendMessageStr;
		}

		/**
		 * Returns the plugin functions for the bot model support
		 * 
		 * <p>This function returns the supported plugin function in order to use 
		 * the dialogue designer of the Business Bot platform. According to the pluginFunction.xml, 
		 * the Java function getMathResult() must be defined which allow the business bot platform to call this function</p>
		 */
		@Override
		public HashMap<String, String> getBotBuilderPluginFunctions(String pluginResourcesPath) {
			if (isBotBuilderModelSupported()) {
				HashMap<String, String> functionList = new HashMap<String, String>();
				
				String filenamePluginFunctionsXML = "pluginFunctions.xml";
				String filenamePluginFunctionsXSD = "pluginFunctions.xsd";
				
				String botBuilderPath = pluginResourcesPath + File.separator + "botbuilder" + File.separator;
				logger.info("Using botBuilderPath: "+botBuilderPath);
				
				String xmlFilePath = botBuilderPath+filenamePluginFunctionsXML;
				String xsdFilePath = botBuilderPath+filenamePluginFunctionsXSD;
				// Validate pluginFunctions XML file <pluginFunctions.xml> against <pluginFunctions.xsd>
				try {
					SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
					Schema schema = factory.newSchema(new File(xsdFilePath));
					Validator validator = schema.newValidator();
					DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					Document document = parser.parse(new File(xmlFilePath));
					validator.validate(new DOMSource(document));
					logger.info("["+filenamePluginFunctionsXML+"] validation OK");
				} catch (Exception e) {
					logger.error("["+filenamePluginFunctionsXML+"] validation FAILED");
					logger.error(e);
					return null;
				}
				
				// Read pluginFunctions from XML file <pluginFunctions.xml>
				try {
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document doc = dBuilder.parse(botBuilderPath+filenamePluginFunctionsXML);
					
					// optional, but recommended, read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
					doc.getDocumentElement().normalize();
					logger.info("Root element: [" + doc.getDocumentElement().getNodeName() + "]");
					NodeList nList = doc.getElementsByTagName("PluginFunction");

					for (int temp = 0; temp < nList.getLength(); temp++) {
						Node nNode = nList.item(temp);
						logger.info("----- Current Element [" + (temp + 1) + "/" + nList.getLength() + "]: ["+ nNode.getNodeName() + "] -----");

						if (nNode.getNodeType() == Node.ELEMENT_NODE) {
							Element eElement = (Element) nNode;
							logger.info("FunctionName: [" + eElement.getElementsByTagName("FunctionName").item(0).getTextContent() + "]");
							logger.info("FunctionDescription: [" + eElement.getElementsByTagName("FunctionDescription").item(0).getTextContent() + "]");
							logger.info("ExpectedParameters: [" + eElement.getElementsByTagName("ExpectedParameters").item(0).getTextContent() + "]");
							
							BusinessBotBuilderPluginFunction pfObj = new BusinessBotBuilderPluginFunction();
							pfObj.setFunctionName(eElement.getElementsByTagName("FunctionName").item(0).getTextContent());
							pfObj.setFunctionDescription(eElement.getElementsByTagName("FunctionDescription").item(0).getTextContent());
							pfObj.setExpectedParameters(Integer.parseInt(eElement.getElementsByTagName("ExpectedParameters").item(0).getTextContent()));
							
							NodeList nListFunctionParameter = eElement.getElementsByTagName("PluginFunctionParameter");
							if (nListFunctionParameter != null && nListFunctionParameter.getLength() != 0) {
								List<BusinessBotBuilderPluginFunctionParameter> pfpList = new ArrayList<BusinessBotBuilderPluginFunctionParameter>(); // Create parameter list for the function object
								
								for (int tempListFunctionParameter = 0; tempListFunctionParameter < nListFunctionParameter.getLength(); tempListFunctionParameter++) {
									Node nNodeFunctionParameter = nListFunctionParameter.item(tempListFunctionParameter);
									if (nNodeFunctionParameter.getNodeType() == Node.ELEMENT_NODE) {
										Element eElementFunctionParameter = (Element) nNodeFunctionParameter;
										logger.info("Node: [" + nNodeFunctionParameter.getNodeName() + "]");
										logger.info("\tSequenceId: [" + eElementFunctionParameter.getElementsByTagName("SequenceId").item(0).getTextContent() + "]");
										logger.info("\tParameterName: [" + eElementFunctionParameter.getElementsByTagName("ParameterName").item(0).getTextContent() + "]");
										logger.info("\tDefaultValue: [" + eElementFunctionParameter.getElementsByTagName("DefaultValue").item(0).getTextContent() + "]");
										logger.info("\tExampleValue: [" + eElementFunctionParameter.getElementsByTagName("ExampleValue").item(0).getTextContent() + "]");
										logger.info("\tDescription: [" + eElementFunctionParameter.getElementsByTagName("Description").item(0).getTextContent() + "]");
										// Create function parameter object and add to function parameter list
										pfpList.add(new BusinessBotBuilderPluginFunctionParameter(null, null, 0, 
												Integer.parseInt(eElementFunctionParameter.getElementsByTagName("SequenceId").item(0).getTextContent()), 
												eElementFunctionParameter.getElementsByTagName("ParameterName").item(0).getTextContent(), 
												eElementFunctionParameter.getElementsByTagName("DefaultValue").item(0).getTextContent(), 
												eElementFunctionParameter.getElementsByTagName("ExampleValue").item(0).getTextContent(), 
												eElementFunctionParameter.getElementsByTagName("Description").item(0).getTextContent()));
										
									}
								}
								pfObj.setFunctionParameters(pfpList); // Add function parameter list to function object
							} else {
								logger.info("FunctionName ["+ eElement.getElementsByTagName("FunctionName").item(0).getTextContent()+ "] has no function parameters");
							}	
							
							// Add plugin function object to returned function list
							functionList.put(eElement.getElementsByTagName("FunctionName").item(0).getTextContent(), pfObj.toJson().toString());
						}
					}
				} catch (Exception e) {
					logger.error("Can not parse XML file ["+filenamePluginFunctionsXML+"]");
					logger.error(e);
					return null;
				}				
				return functionList;
			} else {
				return null;
			}			
		}
		
		/**
		 * Returns the state of the bot model support of this plugin
		 * 
		 * <p>The bot model support of this plugin must be true in order to use 
		 * the dialogue designer of the Business Bot platform</p>
		 */
		@Override
		public boolean isBotBuilderModelSupported() {
			return isBotBuilderModelSupported;
		}
		
		/**
		 * Returns the state of the web UI support of this plugin
		 * 
		 * <p>The web UI support of this plugin must be true in order to use 
		 * the web UI logic of the Business Bot platform</p>
		 */
		@Override
		public boolean isWebUISupported() {
			return isWebUISupported;
		}

		/**
		 * Process the request and returns the requested webpage content
		 * 
		 * <p>The incoming request is processed (e.g. save form) and returns
		 * the requested webpage to the Business Bot platform in order to display the
		 * webpage of this business chatbot logic to the user</p>
		 */
		@Override
		public String getWebUIPage(String tenantId, String accountId, HashMap<String, String> pluginMap, Map<String, String[]> parameterMap) {
			return null;
		}
		
		/**
		 * Is license required for this business logic
		 * 
		 * <p>Returns <code>true</code> if a license is required for this business logic; otherwise <code>false</code></p>
		 */
		@Override
		public boolean isLicenseRequired() {
			return isLicenseRequired;
		}
    }
}