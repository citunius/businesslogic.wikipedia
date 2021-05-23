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
package de.citunius.businesslogic.wikipedia.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import de.citunius.businesslogic.wikipedia.ConstantsBot;
import de.citunius.businesslogicapi.common.ConstantsPlugin;
import de.citunius.businesslogicapi.common.Utilities;
import de.citunius.businesslogicapi.common.services.WebService;

/**
 * This class holds the Wikipedia services
 *
 * @author me
 * @version %I%, %G%
 * @since   1.0
 */
public class WikiWebService {
	static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());
	public HashMap<String, String> pluginMap = null;
	
	/**
	 * Constructor
	 * 
	 * @param pluginMap  the plugin HashMap
	 */
	public WikiWebService(HashMap<String, String> pluginMap) {
		this.pluginMap = pluginMap;
	}
	
	/**
	 * Get Wiki arctile intro
	 * 
	 * @param keyword  the search keyword
	 * @return  the article intro as string
	 */
	public String getWikiResult(String keyword) {
		// Get Wikipedia Url from properties file
		String targetURL = Utilities.getAttributeFromPropertiesFile(pluginMap, ConstantsBot.PROP_KEY_WIKIPEDIA_URL)+"&titles="+keyword;
		String urlParameters = null;
		String contentType = "application/x-www-form-urlencoded";
		String language = "en-US";
		String result = WebService.sendHttpGetRequest(targetURL, urlParameters, contentType, language);
		
		if (result != null) {
			try {
				JSONObject object = new JSONObject(result);
				JSONObject objPages = object.getJSONObject("query").getJSONObject("pages");

				// Determinate the page id
				Iterator<String> keys = objPages.keys();
				// get some_name_i_wont_know in str_Name
				String pageId = keys.next();
				// get the value i care about
				String value = objPages.optString(pageId);
				String extract = objPages.getJSONObject(pageId).getString("extract");
				extract = extract.replaceAll("<[^>]+>", "");
				logger.trace("-> intro:" + extract);
				return extract;
			} catch (JSONException e) {
				e.getMessage();	
				logger.error(e);
			}			
		}
		return null;
	}
	
	/**
	 * Get Wiki arctile intro
	 * 
	 * @return  the Wikipedia logo as <code>FileUUID</code> string
	 */
	public String getWikiLogo() {
		
		String imageUrl = "https://en.wikipedia.org/static/images/project-logos/enwiki.png";
		String filePath = null;
		try {
			filePath = saveFiletoPluginWorkDir(imageUrl);
		} catch (Exception e) {
			logger.error("Unable to save file to plugin work directory: "+e.getMessage());
			return null;
		}
		
		// Upload and register local file to BBP FileStore
		String fileUUID = null;
		try {
			WebService ws = new WebService(pluginMap);
			fileUUID = ws.registerFileToBBPFileStore(filePath);
		} catch (Exception e) {
			logger.error("Unable to register local WorkDirFile ["+filePath+"] to BBP FileStore: "+e.getMessage());
			return null;
		}
		if (fileUUID != null && fileUUID.length() != 0) {
			logger.info("Local WorkDirFile ["+filePath+"] has been registered to BBP FileStore successfully");
		} else {
			logger.error("Failed to register local WorkDirFile ["+filePath+"] to BBP FileStore");
		}	    
		return fileUUID;		
	}
	
	/**
	 * Save file to plugin's work directory
	 * 
	 * @param imageUrl		the image URL
	 * @return <code>true</code> on success; otherwise <code>false</code>
	 * @throws Exception occurs on error
	 */
	public String saveFiletoPluginWorkDir(String imageUrl) throws Exception {
		String filePath = pluginMap.get(ConstantsPlugin.PLUGIN_WORKDIR)+File.separator+"logo.jpg";
		
		URL url = new URL(imageUrl);
	    InputStream is = url.openStream();
	    OutputStream os = new FileOutputStream(filePath);

	    byte[] b = new byte[2048];
	    int length;

	    while ((length = is.read(b)) != -1) {
	        os.write(b, 0, length);
	    }

	    is.close();
	    os.close();	    
	    return filePath;
	}
}
