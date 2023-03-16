package com.erp.model.scm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/16 11:15
 */
@Data
@NoArgsConstructor
public class PurchaseOrderPagingViewDTO implements Serializable {


    /**
     * 采购单号
     */
    private String code;


}
