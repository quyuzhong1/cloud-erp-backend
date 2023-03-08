package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname AuditParamDTO
 * @Description TODO
 * @Date 2023-01-29 18:42
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class AuditParamDTO implements Serializable {


    private String id;

    /**
     * 意见
     */
    private String comment;
}
