package com.erp.model.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class FsCallbackApiRespDTO {
    private Integer code = 0;
    private String msg ="success";
    private String message="";
    private Boolean without_prefix = false ;
}
