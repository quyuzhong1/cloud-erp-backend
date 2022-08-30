package com.cloud.erp.chrome.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @Classname YxkExWarehouseDTO
 * @Description TODO
 * @Date 2022-08-29 12:25
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class YxkExWarehouseDTO implements Serializable {

    @NotNull(message = "任务id 不能为空")
    private Integer taskId;


    @NotBlank(message = "fileUrl 不能为空")
    private String fileUrl;


    @NotBlank(message = "cookie 不能为空")
    private String cookie;
}
