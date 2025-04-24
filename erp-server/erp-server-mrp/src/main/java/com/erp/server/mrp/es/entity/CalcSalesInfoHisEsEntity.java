package com.erp.server.mrp.es.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;

@Getter
@Setter
@Document(indexName = "calc_sales_info_his")
public class CalcSalesInfoHisEsEntity extends BaseEsEntity {
    /**
     * SKU
     */
    @Field(type = FieldType.Keyword)
    private String skuId;
    /**
     * SKU
     */
    @Field(type = FieldType.Keyword)
    private String skuNo;
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
     * 日期
     */
    @Field(type = FieldType.Date, format = DateFormat.basic_date, pattern = "yyyy-MM-dd")
    private LocalDate date;
    /**
     * 数量
     */
    @Field(type = FieldType.Keyword)
    private Integer qty;
    /**
     * 试算配置id
     */
    @Field(type = FieldType.Keyword)
    private String cfgRuleCalcId;


    public static CalcSalesInfoHisEsEntity buildCalcSalesInfoHis(OrderHistorySalesEsEntity historySales) {
        CalcSalesInfoHisEsEntity entity = new CalcSalesInfoHisEsEntity();
        entity.setDate(historySales.getDate());
        entity.setQty(historySales.getOriginalSalesQty());
        entity.setSkuId(historySales.getSkuId());
        entity.setShopId(historySales.getShopId());
        return entity;
    }
}
