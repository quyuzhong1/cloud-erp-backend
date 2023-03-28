package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 *  采购价导入结果
 * @author Lambda
 * @Classname PuchasePriceExportResultDTO
 * @Description TODO
 * @Date 2023-03-16 15:14
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class PurchasePriceExportResultDTO implements Serializable {


    /**
     * 成功的数据
     */
    private List<PurchasePriceDetailDTO> succeedList;


    /**
     * 失败的链接
     */
    private String errorUrl;
}
