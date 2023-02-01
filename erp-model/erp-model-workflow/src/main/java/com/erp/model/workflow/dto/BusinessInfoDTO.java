package com.erp.model.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname BusinessInfoDTO
 * @Description TODO
 * @Date 2023-01-31 15:39
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BusinessInfoDTO implements Serializable {

    /**
     * 业务名称
     */
    private String businessName;


    /**
     * 业务key
     */
    private String businessKey;


    /**
     *流程自定义的key
     */
    private String processDefinitionKey;


    /**
     * 参数
     */
    private String param;


    private List<String> paramList;


}
