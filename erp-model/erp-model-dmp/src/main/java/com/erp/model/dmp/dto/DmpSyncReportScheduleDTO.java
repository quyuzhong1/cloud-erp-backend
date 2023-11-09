package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class DmpSyncReportScheduleDTO {

    @NotBlank(message = "店铺不能为空")
    private String shopId;

    /**
     * 平台的选项
     */
    @NotBlank(message = "平台不能为空")
    private String dictPlatform;

    /**
     * 店铺名称
     */
    @NotBlank(message = "店铺名称不能为空")
    private String name;


    /**
     * 国家id
     */
    @NotBlank(message = "国家id不能为空")
    private String dictCountryCode;

    /**
     * 国家名
     */
    @NotBlank(message = "国家名不能为空")
    private String countryName;


    /**
     * 授权状态
     */
    private String authStatus;

    /**
     * 授权时间
     */
    private LocalDateTime authTime;

    /**
     * 是否已生成调度任务
     */
    private Boolean isGenTask;

    /**
     * 店铺仓库id
     */
    @NotBlank(message = "国家名不能为空")
    private String warehouseId;

    /**
     * 店铺仓库名称
     */
    @NotBlank(message = "店铺仓库名称不能为空")
    private String warehouseName;


}