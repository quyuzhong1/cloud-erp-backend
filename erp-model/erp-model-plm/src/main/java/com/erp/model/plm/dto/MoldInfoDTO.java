package com.erp.model.plm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 模具档案请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-10-10
*/
@Data
@NoArgsConstructor
public class MoldInfoDTO implements Serializable {


     /**
     * 状态统计
     */
     @Data
     @NoArgsConstructor
     @AllArgsConstructor
     public static class TabListDTO {
         /**
         * 类型
         */
         private String tabFlag;
         /**
          * 类型名称
          */
         private String tabFlagName;
         /**
         * 数量
         */
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

     }
    /**
    * 分页列表
    */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 是否作废
        */
        private Boolean invalidStatus;

        /**
        * 备注
        */
        private String remark;

        /**
        * 作废备注
        */
        private String invalidRemark;

        /**
        * 审批状态
        */
        private String approveStatus;

        /**
        * 审批时间
        */
        private LocalDateTime approveTime;

        /**
        * 审批人ID
        */
        private String approveUserId;

        /**
        * 审批人姓名
        */
        private String approveUserName;

        /**
        * 模具编号
        */
        private String code;

        /**
        * 模具名称
        */
        private String name;

        /**
        * 模具标识：first =首套模,copy =复制模
        */
        private String tag;
        private String tagName;

        /**
        * 项目编号
        */
        private String projectCode;

        /**
        * 项目名称
        */
        private String projectName;

        /**
        * 产品经理id
        */
        private String chargeId;

        /**
        * 产品经理
        */
        private String chargeName;

        /**
        * 项目经理id
        */
        private String projectChargeId;

        /**
        * 项目经理
        */
        private String projectChargeName;

        /**
        * 模具分类
        */
        private String categoryId;
        private String categoryName;

        /**
        * 模具类型
        */
        private String type;
        private String typeName;

        /**
        * 模具穴数
        */
        private String moldHoles;

        /**
        * 尺寸单位
        */
        private String sizeUnit;

        /**
        * 长(mm)
        */
        private BigDecimal productLength;

        /**
        * 宽(mm)
        */
        private BigDecimal productWidth;

        /**
        * 高(mm)
        */
        private BigDecimal productHeight;

        private String size;

        /**
        * 模具材质
        */
        private String materials;

        /**
        * 开模周期(自然日)
        */
        private Integer cycle;

        /**
        * 模具启用日期
        */
        private LocalDate activationDate;

        /**
        * 供应商id
        */
        private String supplierId;

        /**
        * 供应商编码
        */
        private String supplierCode;

        /**
        * 供应商
        */
        private String supplierName;

        /**
        * 含税单价
        */
        private BigDecimal taxPrice;

        /**
        * 税率(%)
        */
        private BigDecimal rate;

        /**
        * 结算方式
        */
        private String payMethodId;
        private String payMethodName;

        /**
        * 付款条件
        */
        private String paymentCondition;
        private String paymentConditionName;


        /**
        * 审核状态名称
        */
        private String approveStatusName;

        /**
        * 作废状态名称
        */
        private String invalidStatusName;

        /**
        * 创建时间
        */
        private LocalDateTime createTime;

        /**
        * 创建人名称
        */
        private String createUserName;
    }

    /**
    * 导出Excel
    */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
        * 勾选的id集合
        */
        private List<String> ids;
    }

    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 是否作废
        */
        private Boolean invalidStatus;
        private String invalidStatusName;

        /**
        * 备注
        */
        private String remark;

        /**
        * 作废备注
        */
        private String invalidRemark;

        /**
        * 审批状态
        */
        private String approveStatus;
        private String approveStatusName;

        /**
        * 审批时间
        */
        private LocalDateTime approveTime;

        /**
        * 审批人ID
        */
        private String approveUserId;

        /**
        * 审批人姓名
        */
        private String approveUserName;

        /**
        * 模具编号
        */
        private String code;

        /**
        * 模具名称
        */
        private String name;

        /**
        * 模具标识：first =首套模,copy =复制模
        */
        private String tag;
        private String tagName;

        /**
        * 项目编号
        */
        private String projectCode;

        /**
        * 项目名称
        */
        private String projectName;

        /**
        * 产品经理id
        */
        private String chargeId;

        /**
        * 产品经理
        */
        private String chargeName;

        /**
        * 项目经理id
        */
        private String projectChargeId;

        /**
        * 项目经理
        */
        private String projectChargeName;

        /**
        * 模具分类
        */
        private String categoryId;
        private String categoryName;



        /**
        * 模具类型
        */
        private String type;
        private String typeName;

        /**
        * 模具穴数
        */
        private String moldHoles;

        /**
        * 尺寸单位
        */
        private String sizeUnit;

        /**
        * 长(mm)
        */
        private BigDecimal productLength;

        /**
        * 宽(mm)
        */
        private BigDecimal productWidth;

        /**
        * 高(mm)
        */
        private BigDecimal productHeight;

        /**
        * 模具材质
        */
        private String materials;

        /**
        * 开模周期(自然日)
        */
        private Integer cycle;

        /**
        * 模具启用日期
        */
        private LocalDate activationDate;

        /**
        * 供应商id
        */
        private String supplierId;

        /**
        * 供应商编码
        */
        private String supplierCode;

        /**
        * 供应商
        */
        private String supplierName;

        /**
        * 含税单价
        */
        private BigDecimal taxPrice;

        /**
        * 税率(%)
        */
        private BigDecimal rate;

        /**
        * 结算方式
        */
        private String payMethodId;
        private String payMethodName;

        /**
        * 付款条件
        */
        private String paymentCondition;
        private String paymentConditionName;


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
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 备注
        */
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;

        /**
        * 模具名称
        */
        @NotBlank(message = "模具名称不能为空")
        @Size(max = 500,message = "模具名称最大长度不能超过500位")
        private String name;

        /**
         * 模具标识 MoldInfoTagEnum
         * http://172.16.100.11:3002/project/47/interface/api/28979   type = MoldInfoTag
        */
        @NotBlank(message = "模具标识不能为空")
        @Size(max = 32,message = "模具标识最大长度不能超过32位")
        private String tag;

        /**
        * 项目编号
        */
        @Size(max = 50,message = "项目编号最大长度不能超过50位")
        private String projectCode;

        /**
        * 项目名称
        */
        @NotBlank(message = "项目名称不能为空")
        @Size(max = 50,message = "项目名称最大长度不能超过50位")
        private String projectName;

        /**
        * 产品经理id http://172.16.100.11:3002/project/47/interface/api/28947
        */
        @NotBlank(message = "产品经理不能为空")
        @Size(max = 19,message = "产品经理id最大长度不能超过19位")
        private String chargeId;

        /**
        * 产品经理
        */
        @NotBlank(message = "产品经理不能为空")
        @Size(max = 50,message = "产品经理最大长度不能超过50位")
        private String chargeName;

        /**
        * 项目经理id http://172.16.100.11:3002/project/47/interface/api/28947
        */
        @Size(max = 19,message = "项目经理id最大长度不能超过19位")
        private String projectChargeId;

        /**
        * 项目经理
        */
        @Size(max = 50,message = "项目经理最大长度不能超过50位")
        private String projectChargeName;

        /**
        * 模具分类
         * http://172.16.100.11:3002/project/47/interface/api/29759 type=product
        */
        @NotBlank(message = "模具分类不能为空")
        @Size(max = 19,message = "模具分类最大长度不能超过19位")
        private String categoryId;


        /**
        * 模具类型
         * http://172.16.100.11:3002/project/47/interface/api/34549
        */
        @NotBlank(message = "模具类型不能为空")
        @Size(max = 32,message = "模具类型最大长度不能超过32位")
        private String type;

        /**
        * 模具穴数
        */
        @NotBlank(message = "模具穴数不能为空")
        @Size(max = 50,message = "模具穴数最大长度不能超过50位")
        private String moldHoles;

        /**
        * 尺寸单位
         * 默认值 = mm
        */
        private String sizeUnit;

        /**
        * 长(mm)
        */
        @NotNull(message = "长(mm)不能为空")
        @Digits(integer = 18, fraction = 2, message = "长(mm)整数位不能超过18位，小数位不能超过2位")
        @DecimalMin(value = "0.01", message = "长(mm)必须大于0")
        private BigDecimal productLength;

        /**
        * 宽(mm)
        */
        @NotNull(message = "宽(mm)不能为空")
        @Digits(integer = 18, fraction = 2, message = "宽(mm)整数位不能超过18位，小数位不能超过2位")
        @DecimalMin(value = "0.01", message = "宽(mm)必须大于0")
        private BigDecimal productWidth;

        /**
        * 高(mm)
        */
        @NotNull(message = "高(mm)不能为空")
        @Digits(integer = 18, fraction = 2, message = "高(mm)整数位不能超过18位，小数位不能超过2位")
        @DecimalMin(value = "0.01", message = "高(mm)必须大于0")
        private BigDecimal productHeight;

        /**
        * 模具材质
        */
        @NotBlank(message = "模具材质不能为空")
        @Size(max = 50,message = "模具材质最大长度不能超过50位")
        private String materials;

        /**
        * 开模周期(自然日)
        */
        @NotNull(message = "开模周期(自然日)不能为空")
        @Min(value = 1, message = "开模周期(自然日)必须大于0")
        private Integer cycle;

        /**
        * 模具启用日期
        */
        @NotNull(message = "模具启用日期不能为空")
        private LocalDate activationDate;

        /**
        * 供应商id
         * http://172.16.100.11:3002/project/83/interface/api/24307 categoryType = loan
        */
        @NotBlank(message = "供应商id不能为空")
        @Size(max = 19,message = "供应商id最大长度不能超过19位")
        private String supplierId;

        /**
        * 供应商编码
        */
        private String supplierCode;

        /**
        * 供应商
        */
        private String supplierName;

        /**
        * 含税单价
        */
        @NotNull(message = "含税单价不能为空")
        @Digits(integer = 12, fraction = 4, message = "含税单价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal taxPrice;

        /**
        * 税率(%)
        */
        @NotNull(message = "税率(%)不能为空")
        @Digits(integer = 12, fraction = 4, message = "税率(%)整数位不能超过12位，小数位不能超过4位")
        private BigDecimal rate;

        /**
        * 结算方式
         * http://172.16.100.11:3002/project/83/interface/api/7144 type = supplierPayMode
        */
        @NotBlank(message = "结算方式不能为空")
        @Size(max = 19,message = "结算方式最大长度不能超过19位")
        private String payMethodId;

        /**
        * 付款条件
         * http://172.16.100.11:3002/project/83/interface/api/31039 type=paymentCondition
        */
        @NotBlank(message = "付款条件不能为空")
        @Size(max = 64,message = "付款条件最大长度不能超过64位")
        private String paymentCondition;


    }

    /**
     * 批量关联sku
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefSkuDTO {
        /**
         * 模具id集合
         */
        @NotEmpty(message = "模具id集合不能为空")
        private List<String> ids;
        /**
         * sku集合
         */
        @NotEmpty(message = "sku集合不能为空")
        private List<String> skuNos;
    }


}