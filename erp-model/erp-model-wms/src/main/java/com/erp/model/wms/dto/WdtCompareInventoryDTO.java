package com.erp.model.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WdtCompareInventoryDTO implements Serializable {

    private List<String> skuNoList;

    private List<String> skuIdList;

    private List<String> erpWarehouseList;

    /**
     * 分货单ID
     */
    private String id;

    private String code;

    /**
     * 同步id
     */
    private String dmpSyncTaskId;

}