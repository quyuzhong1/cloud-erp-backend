package com.erp.model.tms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName LogisticsBillDetailQueryDTO
 * @description: TODO
 * @date 2023年11月17日
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogisticsBillDetailQueryDTO implements Serializable {

    /**
     * 每页显示条数，默认 10
     */
    private long size = 10;

    /**
     * 当前页
     */
    private long current = 1;
    /**
     * 查询方式
     */
    private String trackQueryMode;
    /**
     * 注册状态 0 未注册 1 已注册
     */
    private Integer registerStatus;
    /**
     * 是否需要查询物流轨迹
     */
    private Boolean trackEnable;
    /**
     * 店铺id
     */
    private String shopId;

    /**
     * 物流运输类型（LogisticsTransportTypeEnum）
     */
    private String transportType;
}
