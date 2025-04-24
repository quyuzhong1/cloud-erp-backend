package com.erp.model.wms.dto.third;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ThirdWarehouseProductReq {

    /**
     * 第三方sku（必填）
     */
    private List<String> skuNoList;
}
