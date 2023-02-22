package com.erp.server.dmp.config;

import com.erp.server.dmp.bean.YamlPropertySourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.interceptor.TransactionInterceptor;

@Configuration
@PropertySource(value= {"/bootstrap.yml"},factory= YamlPropertySourceFactory.class)
public class DatacenterMongo extends AbstractMongoConfig {
	@Autowired
	private ConfigurableEnvironment env;

	@Primary
  	@Bean(name = "reportTemplate")
	@Override
	public MongoTemplate getMongoTemplate(){
		return new MongoTemplate(mongoDatabaseFactory());
	}

	@Bean
	public MongoDatabaseFactory mongoDatabaseFactory(){
		return mongoDbFactory(env,"");
	}
	@Bean("mongoTransactionManager")
	public MongoTransactionManager mongoTransactionManager() {
		return new MongoTransactionManager(mongoDatabaseFactory());
	}


}
