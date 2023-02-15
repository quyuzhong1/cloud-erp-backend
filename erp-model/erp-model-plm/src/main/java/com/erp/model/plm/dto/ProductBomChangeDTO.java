package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname ProductBomChangeDTO
 * @Description TODO
 * @Date 2023-01-31 16:48
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductBomChangeDTO  implements Serializable {

    /**
     *
     */
    private String id;

    /**
     * 类型
     */
    private String type;


    /**
     * 来源的信息
     */
    private String sourceId;

    private BomDTO info;
}
