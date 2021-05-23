/*
 * Copyright: (c) 2017-2021, Citunius GmbH. All rights reserved.
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

/**
 * This class holds all constants
 * 
 * @author me
 * @version %I%, %G%
 * @since   1.0
 */
public class ConstantsBot {
	// Plugin name
	public static final String PLUGIN_NAME = "GM.Wikipedia";
	
	// Event Types and Event Names
	public static final String EVENTTYPE_MSGGATEWAY_NOTIFICATION = "Notification";
	public static final String EVENTNAME_MSGGATEWAY_NOTIFICATION_PLAINTEXT = "plainText";
	
	// View names to know which telegram message template should be used for an incoming system event
	public static final String VIEW_DEFAULT = "default";
	
	// Properties file: key names
	public static final String PROP_KEY_WIKIPEDIA_URL = "Wikipedia.URL";
}
