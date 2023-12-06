package com.erp.model.dmp.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName SyncDataReportVO
 * @description: 同步报表函数
 * @date 2023年12月05日
 * @version: 1.0
 */
@Data
public class SyncDataReportVO implements Serializable {
    /**
     * 函数编码
     */
    private String code;
    /**
     * 函数名称
     */
    private String name;
    /**
     * SyncStatusEnum
     */
    private String status;
    private String msg;
}
