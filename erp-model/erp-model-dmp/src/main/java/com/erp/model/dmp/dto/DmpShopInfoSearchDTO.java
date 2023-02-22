package com.erp.model.dmp.dto;

import com.common.business.dto.base.BaseSearchDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/14 14:54
 */
@Data
@NoArgsConstructor
public class DmpShopInfoSearchDTO extends BaseSearchDTO {

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 平台名称
     */
    private String platformName;

    /**
     * 店铺站点
     */
    private String site;

    /**
     * 创建时间-从
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime_begin;

    /**
     * 创建时间-到
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime_end;
}
