package com.erp.server.dmp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.util.ObjectUtils;


public abstract class AbstractMongoConfig {
	public MongoDatabaseFactory mongoDbFactory(ConfigurableEnvironment env, String dbName) throws Exception {
//       String url = getUrl(env, dbName);
//      return new SimpleMongoClientDatabaseFactory(url.replace("%", "%25"));
        String uri = getPropertyUriAsString(env, dbName, "");
        return new SimpleMongoClientDatabaseFactory(uri.replace("%", "%25"));
    }
/*
	public String getUrl(ConfigurableEnvironment env, String dbName) {
		String host=getPropertyAsString(env,dbName, "host", "127.0.0.1");
		String database=getPropertyAsString(env,dbName, "database", "");
		String username=getPropertyAsString(env,dbName, "username", "");
		String password=getPropertyAsString(env,dbName, "password", "");
		int port = getPropertyAsInt(env, dbName, "port", 27017);
		return "mongodb://"+username+":"+password+"@"+host+":"+port+"/"+database;
	}*/

/*	private int getPropertyAsInt(ConfigurableEnvironment env, String key,String key1, int defaultVal) {
		try {
			return Integer.parseInt(env.getProperty("spring.data."+key+".mongodb."+key1));
		} catch (Exception e) {
			return defaultVal;
		}
	}*/

	private String getPropertyUriAsString(ConfigurableEnvironment env, String key, String defaultVal) {
		String k = "spring.data."+key+".mongodb.uri";
		return ObjectUtils.isEmpty(env.getProperty(k))?defaultVal:env.getProperty(k);
	}
//
/*
	private String getPropertyAsString(ConfigurableEnvironment env, String key,String key1, String defaultVal) {
		String k = "spring.data."+key+".mongodb."+key1;
		return ObjectUtils.isEmpty(env.getProperty(k))?defaultVal:env.getProperty(k);
	}
*/

    /*
     * Factory method to create the MongoTemplate
     */
    abstract public MongoTemplate getMongoTemplate() throws Exception;
}
