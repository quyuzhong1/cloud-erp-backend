package com.common.business.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
public class CreateJobDTO {

    @NotBlank(message = "平台类型不能为空")
    private String dictPlatform;

    @NotBlank(message = "业务类型不能为空")
    private String billType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

}
