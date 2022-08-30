package com.cloud.erp.chrome.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @Classname YxkOrderDTO
 * @Description TODO
 * @Date 2022-08-30 9:51
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class YxkOrderDTO implements Serializable {

    @NotBlank(message = "cookie不能为空")
    private String cookie;
    @NotBlank(message = "url 不能为空")
    private String url;
    @NotNull(message = "任务id 不能为空")
    private Integer taskId;
}
