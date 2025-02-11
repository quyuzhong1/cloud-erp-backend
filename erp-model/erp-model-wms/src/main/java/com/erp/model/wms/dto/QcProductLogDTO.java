package com.erp.model.wms.dto;

import com.erp.model.wms.entity.QcProductEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class QcProductLogDTO extends QcProductEntity implements Serializable {
    /**
     * sku名称
     */
    private String skuName;
}