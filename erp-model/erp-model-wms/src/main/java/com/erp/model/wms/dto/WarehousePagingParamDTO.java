package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author Lambda
 * @Classname WarehousePagingParamDTO
 * @Description TODO
 * @Date 2023-03-16 16:52
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class WarehousePagingParamDTO implements Serializable {


    /**
     * 仓库组织id 集合
     */
    private List<String> orgIdList;


    /**
     * 联系人
     */
    private String contacts;


    /**
     * 创建人id
     */
    private List<String> createUserIdList;


    /**
     * 状态
     */
    private boolean status;

    /**
     * 类型id 集合
     */
    private List<String> typeIdList;


}
