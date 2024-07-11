package com.sdk.wangdian.dto;

import com.common.business.dto.CleanBaseDTO;
import lombok.Data;

/**
 * 旺店通仓库数据
 * @date 2024-05-22
 * @author tanmujin
 */
@Data
public class WdtVirtualWarehouseDto extends CleanBaseDTO {
    /**
     * 仓库id, 仓库唯一键
     */
    private Integer virtual_warehouse_id;

    /**
     * 模式：0：普通店铺 1：分销模式
     */
    private String virtual_warehouse_type;
    /**
     * 仓库编号
     */
    private String virtual_warehouse_no;

    /**
     * 仓库名称
     */
    private String virtual_warehouse_name;

    /**
     * 备注
     */
    private String remark;


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
    private String warehouse_list;
}
