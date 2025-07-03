package com.erp.model.scm.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.core.anno.StateEnumValue;
import com.erp.model.scm.entity.SupplierVisitSkuEntity;
import com.erp.model.scm.enums.SupplierVisitEnum;
import com.erp.model.scm.enums.SupplierVisitResultEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author Lambda
 * @Classname SupplierVisitDTO

 * @Date 2023-03-16 14:31
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SupplierVisitDTO implements Serializable {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {
        private String tabFlag;

        private String tabFlagName;

        private Integer count;
    }

    /**
     * 供应商拜访信息
     */
    @Data
    @NoArgsConstructor
    @Valid
    public static class AddDTO extends CommonDTO{


    }

    /**
     * 供应商拜访信息
     */
    @Data
    @NoArgsConstructor
    @Valid
    public static class UpdateDTO extends CommonDTO{

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;
    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO{
        private String id;
        /**
         * 供应商名
         */
        private String  supplierId;
        /**
         * 供应商名
         */
        private String  supplierCode;
        /**
         * 供应商名
         */
        private String  supplierName;

        /**
         * 类型
         */
        private String visitType;
        private String visitTypeName;

        /**
         * 拜访时间
         */
        private LocalDate visitTime;

        /**
         * 拜访人 集合
         */
        private List<String> peopleList;
        private List<String> peopleNameList;

        /**
         * 内容
         */
        private String content;


        /**
         * 结果
         */
        private String result;
        private String resultName;

        /**
         * 附件url
         */
        private List<String> attachmentUrlList;

        /**
         * 附件名
         */
        private List<String> attachmentNameList;

        /**
         * 物料sku 集合
         */
        private List<String> skuIdList;

        private List<String> skuNoList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 供应商id
         */
        @NotBlank(message = "供应商不能为空")
        private String supplierId;

        /**
         * 类型
         */
        @NotBlank(message = "供应商拜访类型不能为空")
        @StateEnumValue(strValues = {"newProduct", "access", "other"}, message = "拜访类型有误")
        private String visitType;

        /**
         * 拜访时间
         */
        @NotNull(message = "拜访时间不能为空")
        private LocalDate visitTime;

        /**
         * 拜访人 集合
         */
        @NotNull(message = "拜访人不能为空")
        @Size(min = 1, message = "拜访人不能为空")
        private List<String> peopleList;

        /**
         * 内容
         */
        @NotBlank(message = "拜访记录不能为空")
        private String content;


        /**
         * 结果
         */
        @NotBlank(message = "拜访结果不能为空")
        @StateEnumValue(strValues = {"conformity", "nonconformity", "pending"}, message = "拜访结果有误")
        private String result;

        /**
         * 附件url
         */
        private List<String> attachmentUrlList;

        /**
         * 附件名
         */
        private List<String> attachmentNameList;


        /**
         * 物料sku 集合
         */
        private List<String> skuIdList;
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

        private String id;

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

    /**
     * 供应商拜访分页信息
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO extends BaseDTO {
        /**
         * 供应商名
         */
        private String  supplierCode;
        /**
         * 供应商名
         */
        private String  supplierName;


        /**
         * 表id
         */
        private String visitType;


        /**
         * 类型名称
         */
        private String visitTypeName;

        /**
         * 拜访时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate visitTime;

        /**
         * 拜访人
         */
        private String people;

        /**
         * 拜访人
         */
        private String peopleName;


        /**
         * 物料信息
         */
        private String skuInfo;

        /**
         * 结果
         */
        private String result;

        /**
         * 结果
         */
        private String resultName;

        /**
         * 内容
         */
        private String content;

        /**
         * 附件地址
         */
        private List<String> attachmentUrlList;

        /**
         * 附件名
         */
        private List<String> attachmentNameList;
    }

    /**
     * 供应商拜访分页信息
     */
    @Data
    @NoArgsConstructor
    @Valid
    public static class PagingViewDTO {
        /**
         * 表id
         */
        private String id;


        /**
         * 供应商名
         */
        private String  supplierName;


        /**
         * 表id
         */
        private SupplierVisitEnum visitType;


        /**
         * 类型名称
         */
        private String visitTypeName;

        /**
         * 拜访时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate visitTime;

        /**
         * 拜访人
         */
        private String people;

        /**
         * 拜访人
         */
        private String peopleName;


        /**
         * 物料信息
         */
        private String skuInfo;

        /**
         * 结果
         */
        private SupplierVisitResultEnum result;

        /**
         * 结果
         */
        private String resultName;

        /**
         * 内容
         */
        private String content;

        /**
         * 附件地址
         */
        private List<String> attachmentUrlList;

        /**
         * 附件名
         */
        private List<String> attachmentNameList;
    }


    /**
     * 现场考察导入dto
     */
    @Data
    @NoArgsConstructor
    public static class ImportAddDTO{
        /**
         * id
         */
        private String  id;
        /**
         * 供应商名
         */
        private String  supplierId;

        /**
         * 类型
         */
        private String visitType;

        /**
         * 拜访时间
         */
        private LocalDate visitTime;

        /**
         * 拜访人 集合
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
         * 物料
         */
        private List<SupplierVisitSkuEntity> supplierVisitSkuEntityList;

    }


}
