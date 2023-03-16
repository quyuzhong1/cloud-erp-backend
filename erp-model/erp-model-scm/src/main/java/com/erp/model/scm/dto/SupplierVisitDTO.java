package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Lambda
 * @Classname SupplierVisitDTO
 * @Description TODO
 * @Date 2023-03-16 14:31
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SupplierVisitDTO  implements Serializable {

    /**
     * 表id
     */
    private String id;


    /**
     * 供应商id
     */
    @NotBlank(message = "供应商不能为空")
    private String supplierId;

    /**
     * 类型
     */
    @NotBlank(message = "供应商拜访类型不能为空")
    private String type;

    /**
     * 拜访时间
     */
    private LocalDate visitTime;

    /**
     * 拜访人
     */
    private String people;

    /**
     * 内容
     */
    private String content;


    /**
     * 结果
     */
    private String result;


    /**
     * 附件url
     */
    private List<String> visitAttachmentList;


    /**
     * 物料sku 集合
     */
    private List<String> skuIdList;


}
