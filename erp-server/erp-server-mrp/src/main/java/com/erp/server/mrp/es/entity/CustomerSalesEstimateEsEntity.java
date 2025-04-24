package com.erp.server.mrp.es.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Document(indexName = "customer_sales_estimate")
public class CustomerSalesEstimateEsEntity extends BaseEsEntity {

    /**
     * sku id + 店铺id
     */
    @Field(type = FieldType.Keyword)
    private String shopSkuId;
    /**
     * 平台
     */
    @Field(type = FieldType.Keyword)
    private String platform;
    /**
     * 平台
     */
    @Field(type = FieldType.Keyword)
    private String platformName;
    /**
     * 店铺
     */
    @Field(type = FieldType.Keyword)
    private String shopId;
    /**
     * 店铺
     */
    @Field(type = FieldType.Keyword)
    private String shopName;
    /**
     * SKU
     */
    @Field(type = FieldType.Keyword)
    private String skuNo;
    /**
     * SKU
     */
    @Field(type = FieldType.Keyword)
    private String skuId;

    /**
     * 日期
     */
    @Field(type = FieldType.Date, format = DateFormat.basic_date,pattern = "yyyy-MM-dd")
    private LocalDate date;

    /**
     * 预估日销量
     */
    @Field(type = FieldType.Keyword)
    private BigDecimal salesQty;

}
