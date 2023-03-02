package com.erp.model.plm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author Cloud
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreTemplateTaskUpdateDTO {


    @NotBlank(message = "模板id不能为空")
    private String templateId;

    @NotEmpty(message = "修改列表不能为空")
    private List<@Valid PreTaskUpdateDTO> list;

}