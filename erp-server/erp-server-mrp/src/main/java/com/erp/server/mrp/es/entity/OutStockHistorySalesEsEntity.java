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
@Document(indexName = "out_stock_history_sales")
public class OutStockHistorySalesEsEntity extends BaseEsEntity {

    /**
     * 建议 id
     */
    @Field(type = FieldType.Keyword)
    private String replenishmentId;
    /**
     * 日期
     */
    @Field(type = FieldType.Keyword, format = DateFormat.basic_date)
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

    public static OutStockHistorySalesEsEntity createOutStockHistorySales(String replenishmentId, LocalDate billDate, Integer qty) {
        OutStockHistorySalesEsEntity entity = new OutStockHistorySalesEsEntity();
        entity.setId(IdWorker.getIdStr());
        entity.setReplenishmentId(replenishmentId);
        entity.setDate(billDate);
        entity.setOriginalSalesQty(qty);
        return entity;
    }


    public static OutStockHistorySalesEsEntity updateHistoryInventory(String id, Integer qty) {
        OutStockHistorySalesEsEntity entity = new OutStockHistorySalesEsEntity();
        entity.setId(id);
        entity.setOriginalSalesQty(qty);
        return entity;
    }
}
