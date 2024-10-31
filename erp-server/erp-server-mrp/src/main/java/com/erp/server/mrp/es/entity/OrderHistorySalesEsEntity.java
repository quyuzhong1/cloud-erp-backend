package com.erp.server.mrp.es.entity;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;

/**
 * 建议历史销量数据
 */
@Getter
@Setter
@Document(indexName = "order_history_sales")
public class OrderHistorySalesEsEntity extends BaseEsEntity {

    /**
     * 建议 id
     */
    @Field(type = FieldType.Keyword)
    private String replenishmentId;
    /**
     * 日期
     */
    @Field(type = FieldType.Date, format = DateFormat.basic_date,pattern = "yyyy-MM-dd")
    private LocalDate date;
    /**
     * 原始销量
     */
    @Field(type = FieldType.Keyword)
    private Integer originalSalesQty;
    /**
     * 订单类型
     */
    @Field(type = FieldType.Keyword)
    private String orderType;

    public static OrderHistorySalesEsEntity createOrderHistorySales(String replenishmentId, LocalDate billDate, Integer qty) {
        OrderHistorySalesEsEntity entity = new OrderHistorySalesEsEntity();
        entity.setId(IdWorker.getIdStr());
        entity.setReplenishmentId(replenishmentId);
        entity.setDate(billDate);
        entity.setOriginalSalesQty(qty);
        return entity;
    }


    public static OrderHistorySalesEsEntity updateOrderHistorySales(String id, Integer qty) {
        OrderHistorySalesEsEntity entity = new OrderHistorySalesEsEntity();
        entity.setId(id);
        entity.setOriginalSalesQty(qty);
        return entity;
    }
}
