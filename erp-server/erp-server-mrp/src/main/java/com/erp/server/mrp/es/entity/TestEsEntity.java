package com.erp.server.mrp.es.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.Data;

@Data
@Document(indexName = "test")
public class TestEsEntity  extends BaseEsEntity {
	@Field(type = FieldType.Keyword)
    private String code;
	@Field(type = FieldType.Keyword)
    private String name;
    @Field(type = FieldType.Date, format = DateFormat.basic_date,pattern = "yyyy-MM-dd")
    private LocalDate date;
    @Field(type = FieldType.Date, format = DateFormat.basic_date_time,pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateTime;
}
