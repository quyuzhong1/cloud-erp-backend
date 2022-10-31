package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description 产品信息-主页列表-导出excel
 * @Author Luo_WG
 * @Date 2022/9/28 14:21
 **/
@Data
@NoArgsConstructor
public class ProductSkuExcelDTO {

    /**
     * sku/spu/编号
     */
    private String no;

    /**
     * 产品SPU表id
     */
    private String productId;

    /**
     * 产品状态 1:未开发 2:开发中 3:开发完成 4:中止开发 5:暂停开发
     */
    private List<Integer> stateList;

    /**
     * 类别id
     */
    private List<String> categoryIds;

    /**
     * 负责人id
     */
    private List<String> chargeIds;

    /**
     * skuId集合
     */
    private List<String> skuIds;
}
