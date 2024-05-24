package com.sdk.wangdian.dto;

import com.common.business.dto.CleanBaseDTO;
import com.common.business.dto.PlatformOrderDTO;
import lombok.Data;

/**
 * 旺店通仓库数据
 * @date 2024-05-22
 * @author tanmujin
 */
@Data
public class WdtWarehouseDto extends CleanBaseDTO {
    /**
     * 仓库id, 仓库唯一键
     */
    private Integer warehouse_id;

    /**
     * 仓库编号
     */
    private String warehouse_no;

    /**
     * 仓库名称
     */
    private String name;

    /**
     * 邮编
     */
    private String zip;

    /**
     * 地址
     */
    private String address;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 手机
     */
    private String mobile;

    /**
     * 备注
     */
    private String remark;

    /**
     * 固话
     */
    private String telno;


    /**
     * 联系人
     */
    private String contact;

    /**
     * 修改时间
     */
    private String modified;

    /**
     * 停用
     */
    private Boolean is_disabled;

    /**
     * 创建时间
     */
    private String created;
}
