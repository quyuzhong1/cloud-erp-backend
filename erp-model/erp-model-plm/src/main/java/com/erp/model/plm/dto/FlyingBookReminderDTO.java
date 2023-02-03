package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 飞书提醒DTO
 * @date 2023/2/1 14:11
 */
@Data
@NoArgsConstructor
public class FlyingBookReminderDTO {

    /**
     * 提醒人id
     */
    @NotEmpty(message = "提醒人不能为空")
    private List<String> userIds;

    /**
     * 提醒内容
     */
    @NotBlank(message = "提醒内容不能为空")
    @Size(max = 100, message = "提醒内容最大100字符")
    private String content;

    /**
     * 任务id
     */
    @NotEmpty(message = "任务id不能为空")
    private List<String> taskIds;
}
