package com.erp.model.scm.dto;

import com.common.core.anno.RegularValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author yl
 * @Classname
 * @Description TODO
 * @Date 2023-03-15 16:36
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SupplierBaseDTO implements Serializable {

    /**
     * 表id
     */
    private String id;


    /**
     * 名称
     */
    @NotBlank(message = "供应商名称不能为空")
    @Size(max = 50, message = "最大50字符")
    private String name;


    /**
     * 分类id
     */
    @NotBlank(message = "分类id不能为空")
    private String categoryId;

    /**
     * 等级id
     */
    @NotBlank(message = "等级id")
    private String gradeId;


    /**
     * 采购员id
     */
    private String purchaseUserId;


    /**
     * 公司地址
     */
    @Size(max = 100, message = "最大50字符")
    private String companyAddress;


    /**
     * 公司网址
     */
    @Size(max = 100, message = "最大50字符")
    @RegularValid(formatPattern= FieldFormatPatternTypeEnum.URL,message = "网址有误")
    private String companyWebsite;


    /**
     * 生命周期
     */
    @NotBlank(message = "阶段不能为空")
    private String phase;


    /**
     * 结算付款方式
     */
    private String payMethod;

    /**
     * 结算付款币种
     */
    private String payCurrency;


    /**
     * true 启用   false 禁用
     */
    private Boolean openStatus;

}
