package com.erp.model.dmp.mabang.item;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @CreateTime: 2023-06-29  16:12
 * @Author: zhangchunlin
 */
@Data
@NoArgsConstructor
@ToString
public class ShipmentItemLocalInfoEntity implements Serializable {

    /**
     * 本地库存编号
     */
    private String stockId;

    /**
     * 本地库存sku
     */
    private String stockSku;

    /**
     * 平台库存(ful)
     */
    private String quantity;

}