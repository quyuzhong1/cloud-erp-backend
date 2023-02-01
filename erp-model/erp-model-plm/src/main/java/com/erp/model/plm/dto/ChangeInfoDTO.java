package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 变更信息
 *
 * @Classname
 * @Description TODO
 * @Date 2023-01-31 15:06
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ChangeInfoDTO implements Serializable {

    /**
     * name
     */
    private String name;


    /**
     * 来源id
     */
    private String sourceId;

    private String skuNo;

    private String productId;






}
