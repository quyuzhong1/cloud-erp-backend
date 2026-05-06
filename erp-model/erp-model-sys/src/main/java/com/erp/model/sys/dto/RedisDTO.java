package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName RedisDTO
 * @description: TODO
 * @date 2026年04月02日
 * @version: 1.0
 */
public class RedisDTO implements Serializable {

    @Data
    @NoArgsConstructor
    @Valid
    public static class OperationDTO {
        @NotBlank(message = "sourcePrefix不能为空")
        private String sourcePrefix;
        @NotBlank(message = "targetPrefix不能为空")
        private String targetPrefix;
    }
    @Data
    @NoArgsConstructor
    @Valid
    public static class BatchOperationDTO {
        @NotEmpty(message = "操作集合不能为空")
        private List<OperationDTO> operationDTOList;
    }
}
