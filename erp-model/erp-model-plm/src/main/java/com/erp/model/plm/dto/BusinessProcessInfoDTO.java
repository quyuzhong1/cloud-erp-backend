package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;

/**
 * @Classname BusinessProcessInfoDTO
 * @Description TODO
 * @Date 2022-11-22 12:06
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BusinessProcessInfoDTO  implements Serializable {

    /**
     * 表id
     */
    private String id;

    /**
     * 业务名称
     */
    private String businessName;


    /**
     * 业务名称
     */
    private String param;


    /**
     * 审核人数
     */
    private Integer auditorTotal;

    /**
     * 审核人是否多选
     * true 表示多选
     */
    private Boolean isMultiple=false;
}
