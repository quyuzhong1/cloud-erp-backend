package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author Administrator
 * @Classname SysAccountingCompanyDTO
 * @Description TODO
 * @Date 2022-07-12 10:03
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SysAccountingCompanyDTO {


    private String id;

    /**
     * 公司名
     */
    @NotBlank(message = "公司名不能为空")
    @Size(min = 0, max = 50, message = "公司名称长度不能超过50个字符")
    private String companyName;

    /**
     * 公司地址
     */
    @NotBlank(message = "公司地址不能为空")
    private String companyAddress;

    @NotBlank(message = "联系人电话不能为空")
    private String contactMobile;

    /**
     * 联系人名字
     */
    @NotBlank(message = "联系人不能为空")
    private String contactName;

    /**
     * 联系人地址
     */
    @NotBlank(message = "联系人地址不能为空")
    private String contactAddress;

    @NotBlank(message = "币种不能为空")
    private String currencyId;


    /**
     * 列表展示
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * ID
         */
        private String id;

        /**
         * 名称
         */
        private String companyName;

        /**
         * 是否禁用
         * true 禁用
         */
        private Boolean disabled;

    }


}
