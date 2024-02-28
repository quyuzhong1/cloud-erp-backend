package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * ListingInfo信息查询DTO
 *
 * @author Jim
 * @date 2023/11/2
 */
@Data
@NoArgsConstructor
public class ListingInfoParamDTO {

    /**
     * 平台sku no 列表
     */
    private List<String> platformSkuNoList;

    /**
     * sku no 列表
     */
    private List<String> skuNoList;

    /**
     * 平台
     */
    private String platform;

    /**
     * 匹配结果吧true 已匹配 false 未匹配
     */
    private Boolean matchResult;

    /**
     * 类型: warehouse,platform
     */
    private String type;

    /**
     * 店铺ID列表
     */
    private List<String> shopIdList;

    /**
     * 仓库ID列表
     */
    private List<String> warehouseIdList;
}
