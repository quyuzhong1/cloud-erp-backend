package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @Classname 批量保存任务阶段名
 * @Description TODO
 * @Date 2022-09-14 17:48
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BatchTaskPhaseDTO {

    @NotBlank(message = "产品id不能为空")
    private String productId;


    @Valid
    List<TaskPhaseDTO> taskPhases;
}
