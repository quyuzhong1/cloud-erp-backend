package com.erp.model.bi.dto;

import com.erp.common.dto.base.BaseSearchDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

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
     * 创建时间-从
     */
    private Date createTime_begin;

    /**
     * 创建时间-到
     */
    private Date createTime_end;
}
