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
import java.util.HashMap;

import org.apache.log4j.Logger;

import de.citunius.businesslogic.wikipedia.services.WikiWebService;
import de.citunius.businesslogicapi.common.ConstantsPlugin;
import de.citunius.businesslogicapi.common.botbuilder.BotBuilderService;
import de.citunius.businesslogicapi.common.services.LocalisationService;

/**
 * BotBuilder is the class represents the services for the Dialogue Designer Model.
 * This service calls the use case specific functions of this business bot plugin.
 * This class is called using Java reflections.
 *
 * @author me
 * @version %I%, %G%
 * @since   1.0
*/
public class BotBuilder implements BotBuilderService {

	static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	// Define the syntax for function parameter field
	public static final String REQUIRED_PLUGINMAP_FUNCTION_PARAMS_FIELD = "REQUIRED_PLUGINMAP_FUNCTION_PARAMS_";
	
	/**
	 * Class constructor
	 * 
	 * @param pluginMap
	 * @param ls
	 * @param defaultLanguage
	 */
	public BotBuilder() {
		
	}
	
	/**
	 * Get Wikipedia article data
	 * 
	 * @param pluginMap			the plugin map
	 * @param ls				the LocalisationService instance
	 * @param defaultLanguage	the default language
	 * @return	the Wikipedia arctile data if exists; otherwise <code>NoResultsFound</code>
	 */
	public String getArticle(HashMap<String, String> pluginMap, LocalisationService ls, String defaultLanguage) {
		String result = null;
		
		if (pluginMap.containsKey(ConstantsPlugin.BOTBUILDER_FUNCTION_PARAM_1.toString())) {
			String searchKeyword = pluginMap.get(ConstantsPlugin.BOTBUILDER_FUNCTION_PARAM_1.toString());
			logger.info("Searching for ["+searchKeyword+"]");
			WikiWebService ws = new WikiWebService(pluginMap);
	    	result = ws.getWikiResult(searchKeyword);
		} else {
			logger.warn("Search keyword not found in pluginMap -> expected as key ["+ConstantsPlugin.BOTBUILDER_FUNCTION_PARAM_1+"]");
		}
		
    	String replyMessage = "No reply content";
    	if (result != null) {
    		replyMessage = result;
    	} else {
    		replyMessage = ls.getString("Common.NoResultsFound", defaultLanguage);
    	}
    	return replyMessage;
	}
	
	/**
	 * Get Wikipedia logo
	 * 
	 * @param pluginMap			the plugin map
	 * @param ls				the LocalisationService instance
	 * @param defaultLanguage	the default language
	 * @return	the Wikipedia logo as <code>FileUUID</code> if exists; otherwise <code>NoResultsFound</code>
	 */
	public String getWikiLogo(HashMap<String, String> pluginMap, LocalisationService ls, String defaultLanguage) {
		String fileUUID = null;
		
		if (pluginMap.containsKey(ConstantsPlugin.BBP_WEBSERVICES_URL)) {
			WikiWebService ws = new WikiWebService(pluginMap);
	    	fileUUID = ws.getWikiLogo();
		} else {
			logger.warn("BBP WebServiceUrl not found in pluginMap -> expected as key ["+ConstantsPlugin.BBP_WEBSERVICES_URL+"]");
		}
		
    	String replyMessage = fileUUID;
    	if (fileUUID != null) {
    		replyMessage = "<FileUUID:"+fileUUID+">";
    	} else {
    		replyMessage = ls.getString("Common.NoResultsFound", defaultLanguage);
    	}
    	return replyMessage;
	}
}
