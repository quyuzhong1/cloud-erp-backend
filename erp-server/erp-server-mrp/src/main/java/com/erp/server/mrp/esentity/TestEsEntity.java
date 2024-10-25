package com.erp.server.mrp.esentity;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import lombok.Data;

@Data
@Document(indexName = "test")
public class TestEsEntity {
	@Id
    private String id;
    
    private String code;
    private String name;
}
