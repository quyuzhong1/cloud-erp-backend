package com.erp.model.scm.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 供应商资质信息
 * @author Lambda
 * @Classname SupplierCredentialDTO

 * @Date 2023-03-17 15:06
 * @Created by yl
 */
public class SupplierCredentialDTO  implements Serializable {


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO extends CommonDTO {
        private String id;
    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class AddListDTO {

        @NotEmpty(message = "新增列表不能为空")
        List<SupplierCredentialDTO. @Valid AddDTO> list;

    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
         * 主键id
         */
        private String id;

    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateListDTO {

        @NotEmpty(message = "修改列表不能为空")
        List<SupplierCredentialDTO. @Valid UpdateDTO> list;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 资质编码
         */
        @NotBlank(message = "资质不能为空")
        private String code;
        /**
         * 名称
         */
        private String name;

        /**
         * 资质有效期起
         */
        private LocalDate effectiveDate;

        /**
         * 失效日期
         */
        private LocalDate expireDate;

        /**
         * 供应商表id
         */
        private String supplierId;

        /**
         * 备注
         */
        @Size(max = 250,message = "资质备注最大250字符")
        private String remark;


        /**
         * SupplierCredentialStatusEnum 生效状态：notEffective=未生效,effective=生效中,expired=失效
         */
        private String status;
        private String statusName;

        private List<String> attachmentUrlList;

        private List<String> attachmentNameList;

    }

    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO extends BaseDTO {


        /**
         * 资质编码
         */
        private String code;
        /**
         * 名称
         */
        private String name;

        /**
         * 资质有效期起
         */
        private LocalDate effectiveDate;

        /**
         * 失效日期
         */
        private LocalDate expireDate;

        /**
         * 供应商表id
         */
        private String supplierId;
        private String supplierCode;
        private String supplierName;
        private String supplierStatus;
        private String supplierStatusName;

        /**
         * 备注
         */
        private String remark;


        /**
         * SupplierCredentialStatusEnum 生效状态：notEffective=未生效,effective=生效中,expired=失效
         */
        private String status;
        private String statusName;

        private List<String> attachmentUrlList;
        private List<String> attachmentNameList;

    }

    @Data
    @NoArgsConstructor
    public static class BaseDTO {
        /**
         * id
         */
        private String id;

        /**
         * 创建人id
         */
        private String createUserId;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 修改人id
         */
        private String updateUserId;

        /**
         * 修改人名称
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {
        private String tabFlag;

        private String tabFlagName;

        private Integer count;
    }



    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        /**
         * 主键id
         */
        private List<String> ids;

    }



    @Data
    @NoArgsConstructor
    public  static class ImportAddDTO {

        /**
         * 主键id
         */
        private String id;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private String supplierName;
        /**
         * 名称
         */
        @Size(max = 64,message = "最大64字符")
        private String code;
        /**
         * 名称
         */
        @Size(max = 50,message = "最大50字符")
        private String name;


        /**
         * 有效时间
         */
        private LocalDate effectiveDate;


        /**
         * 失效时间
         */
        private LocalDate expireDate;

        /**
         * 备注
         */
        @Size(max = 255,message = "最大255字符")
        private String remark;


    }


    /**
     * 资质字典dto
     */
    @Data
    @NoArgsConstructor
    public static class DictCredentialDTO  {

        @NotBlank(message = "自定义资质名称不能为空")
        @Size(max = 50,message = "自定义资质名称最大50字符")
        private String credentialName;
    }

}
