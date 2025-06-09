package com.erp.server.dmp.config;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.util.ObjectUtils;


public abstract class AbstractMongoConfig {
	public MongoDatabaseFactory mongoDbFactory(ConfigurableEnvironment env, String dbName) {
        String uri = getPropertyUriAsString(env, dbName, "");
        return new SimpleMongoClientDatabaseFactory(uri.replace("%", "%25"));
    }


	private String getPropertyUriAsString(ConfigurableEnvironment env, String key, String defaultVal) {
		String k = "spring.data."+key+".mongodb.uri";
		return ObjectUtils.isEmpty(env.getProperty(k))?defaultVal:env.getProperty(k);
	}

    /**
     * Factory method to create the MongoTemplate
     *
     **/
	public abstract MongoTemplate getMongoTemplate();
}
