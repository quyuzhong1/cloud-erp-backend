package com.erp.tms.aliexpress.model.handover;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName Features
 * @description: 扩展字段
 * @date 2024年02月01日
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Features implements Serializable {
    /**
     *货好时间时间戳
     */
    @JSONField(name = "gmt_ready_to_ship")
    private Long gmtReadyToShip;
    /**
     *托盘数量
     */
    @JSONField(name = "pallet_quantity")
    private Integer palletQuantity;
    /**
     *容器类型(1、托盘;2、大包或盒子3、散装)
     */
    @JSONField(name = "container_type")
    private String containerType;
    /**
     *是否预先组大包，true：是。false：否
     */
    @JSONField(name = "pre_package")
    private String prePackage;
}
