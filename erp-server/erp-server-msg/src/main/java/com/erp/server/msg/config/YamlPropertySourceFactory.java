package com.erp.server.msg.config;

import org.springframework.beans.factory.config.YamlProcessor;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class YamlPropertySourceFactory extends YamlProcessor implements PropertySourceFactory {

	@Override
	public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
		Resource res = resource.getResource();
        setResources(res);
        Map<String, Object> result = new HashMap<>();
        this.process((properties, map) -> {
            result.putAll(this.getFlattenedMap(map));
        });

        return new MapPropertySource(name == null ? getNameForResource(res) : name, result);
	}
	
	String getNameForResource(Resource resource) {
        String name = resource.getDescription();
        if (!StringUtils.hasText(name)) {
            name = resource.getClass().getSimpleName() + "@" + System.identityHashCode(resource);
        }
        return name;
    }

}
