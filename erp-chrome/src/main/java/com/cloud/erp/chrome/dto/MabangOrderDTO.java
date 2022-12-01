package com.cloud.erp.chrome.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

/**
 * @Classname MabaOrderDTO
 * @Description TODO
 * @Date 2022-08-26 9:15
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class MabangOrderDTO {

    @NotNull(message = "任务id 不能为空")
    private Integer taskId;

    private Boolean exportData;

    private MultipartFile  file;

}
