package com.erp.server.mrp.es.entity;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;

@Getter
@Setter
@Document(indexName = "history_inventory")
public class HistoryInventoryEsEntity extends BaseEsEntity {

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
     * 原始库存
     */
    @Field(type = FieldType.Keyword)
    private Integer originalInventQty;


    public static HistoryInventoryEsEntity createHistoryInventory(String replenishmentId, LocalDate billDate, Integer qty) {
        HistoryInventoryEsEntity entity = new HistoryInventoryEsEntity();
        entity.setId(IdWorker.getIdStr());
        entity.setReplenishmentId(replenishmentId);
        entity.setDate(billDate);
        entity.setOriginalInventQty(qty);
        return entity;
    }


    public static HistoryInventoryEsEntity updateHistoryInventory(String id, Integer qty) {
        HistoryInventoryEsEntity entity = new HistoryInventoryEsEntity();
        entity.setId(id);
        entity.setOriginalInventQty(qty);
        return entity;
    }
}
