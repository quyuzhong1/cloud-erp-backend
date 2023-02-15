package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname ChangeScheduleDTO
 * @Description TODO
 * @Date 2023-02-14 15:41
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ChangeScheduleDTO implements Serializable {

    /**
     * 产品id
     */
    @NotBlank(message = "产品id不能为空")
    private String productId;

    @Valid
    @NotNull(message = "变更任务不能为空")
    @Size(min = 1,message = "至少要有一个任务")
    private List<ChangeTaskScheduleDTO> changeTaskList;
}
