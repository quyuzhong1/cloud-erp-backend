package com.erp.server.msg.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.util.ObjectUtils;


@Slf4j
public abstract class AbstractMongoConfig {
	public MongoDatabaseFactory mongoDbFactory(ConfigurableEnvironment env, String dbName) {
        String uri = getPropertyUriAsString(env, dbName, "");
        return new SimpleMongoClientDatabaseFactory(uri.replace("%", "%25"));
    }


	private String getPropertyUriAsString(ConfigurableEnvironment env, String key, String defaultVal) {
		String k = "spring.data."+key+".mongodb.uri";
        log.info("环境：{},数据库：{},{}",env.getProperty("spring.profiles.active"),key,k);
		return ObjectUtils.isEmpty(env.getProperty(k))?defaultVal:env.getProperty(k);
	}

    /**
     * Factory method to create the MongoTemplate
     *
     **/
    abstract public MongoTemplate getMongoTemplate();
}
