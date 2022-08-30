package com.cloud.erp.chrome.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.security.DenyAll;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Classname GyyShipmentsDTO
 * @Description TODO
 * @Date 2022-08-26 14:38
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class GyyShipmentsDTO {

    @NotNull(message = "任务id 不能为空")
    private Integer taskId;


    @NotBlank(message = "ossUrl 不能为空")
    private String ossUrl;
}
