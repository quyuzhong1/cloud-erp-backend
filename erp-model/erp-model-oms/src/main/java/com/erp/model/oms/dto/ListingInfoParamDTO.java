package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
     * sku id 列表
     */
    private List<String> skuIdList;

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

    /**
     * 是否已过期(空==所有)
     */
    private Boolean isExpire;

    /**
     * 最后过期的时间(空=查询最新映射关系)
     */
    private LocalDateTime lastExpireDate;

    /**
     * 平台spu no 列表
     */
    private List<String> platformSpuNoList;
}
