package com.erp.model.wms.dto;

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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 样品领用单请求响应实体
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
*/
@Data
@NoArgsConstructor
public class SampleRecipientDTO implements Serializable {


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
        * 审批状态(waitSubmit=待提交, approved=已批准, rejected=已驳回)
        */
        private String approveStatus;

        /**
        * 审批人ID
        */
        private String approveUserId;

        /**
        * 审批人姓名
        */
        private String approveUserName;

        /**
        * 审批时间
        */
        private LocalDateTime approveTime;

        /**
        * 作废状态(false:有效,true:已作废)
        */
        private Boolean invalidStatus;

        /**
        * 作废原因
        */
        private String invalidRemark;

        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 样品领用单号
        */
        private String code;

        /**
        * 领用日期
        */
        private LocalDate recipientDate;

        /**
        * 用途 枚举类型：办公领用/拍摄/研发/抖音直播/客户领用（客户使用指导）/参展/营销样品/认证检测/供应链生产组装/用户新品体验（仓库提供）/不良品分析（从售后仓领样）/星河线下店领用/其他
        */
        private String usage;

        /**
        * 发货仓库ID
        */
        private String warehouseId;

        /**
        * 单据状态
        */
        private String status;

        /**
        * 领用人ID
        */
        private String userId;

        /**
        * 领用人姓名
        */
        private String userName;

        /**
        * 领用部门ID
        */
        private String deptId;

        /**
        * 领料组织ID
        */
        private String pickOrgId;

        /**
        * 领料组织名称
        */
        private String pickOrgName;

        /**
        * 使用方式 公司内部使用/公司外部使用
        */
        private String usageScope;

        /**
        * 使用方id
        */
        private String useUserId;

        /**
        * 使用方名称
        */
        private String useUserName;

        /**
        * 备注
        */
        private String remark;

        /**
        * 是否邮寄
        */
        private Boolean isDelivery;

        /**
        * 收货地址
        */
        private String receiveAddress;

        /**
        * 收货人
        */
        private String receiverName;

        /**
        * 联系电话
        */
        private String receivePhone;

        /**
        * 来源ID（预留字段）
        */
        private String sourceId;

        /**
        * 来源单号（预留字段）
        */
        private String sourceCode;

        /**
        * 来源类型（预留字段）
        */
        private String sourceType;

        /**
        * SKU成本合计
        */
        private BigDecimal skuTotalCost;


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
        * 审批状态(waitSubmit=待提交, approved=已批准, rejected=已驳回)
        */
        private String approveStatus;

        /**
        * 审批人ID
        */
        private String approveUserId;

        /**
        * 审批人姓名
        */
        private String approveUserName;

        /**
        * 审批时间
        */
        private LocalDateTime approveTime;

        /**
        * 作废状态(false:有效,true:已作废)
        */
        private Boolean invalidStatus;

        /**
        * 作废原因
        */
        private String invalidRemark;

        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 样品领用单号
        */
        private String code;

        /**
        * 领用日期
        */
        private LocalDate recipientDate;

        /**
        * 用途 枚举类型：办公领用/拍摄/研发/抖音直播/客户领用（客户使用指导）/参展/营销样品/认证检测/供应链生产组装/用户新品体验（仓库提供）/不良品分析（从售后仓领样）/星河线下店领用/其他
        */
        private String usage;

        /**
        * 发货仓库ID
        */
        private String warehouseId;

        /**
        * 单据状态
        */
        private String status;

        /**
        * 领用人ID
        */
        private String userId;

        /**
        * 领用人姓名
        */
        private String userName;

        /**
        * 领用部门ID
        */
        private String deptId;

        /**
        * 领料组织ID
        */
        private String pickOrgId;

        /**
        * 领料组织名称
        */
        private String pickOrgName;

        /**
        * 使用方式 公司内部使用/公司外部使用
        */
        private String usageScope;

        /**
        * 使用方id
        */
        private String useUserId;

        /**
        * 使用方名称
        */
        private String useUserName;

        /**
        * 备注
        */
        private String remark;

        /**
        * 是否邮寄
        */
        private Boolean isDelivery;

        /**
        * 收货地址
        */
        private String receiveAddress;

        /**
        * 收货人
        */
        private String receiverName;

        /**
        * 联系电话
        */
        private String receivePhone;

        /**
        * 来源ID（预留字段）
        */
        private String sourceId;

        /**
        * 来源单号（预留字段）
        */
        private String sourceCode;

        /**
        * 来源类型（预留字段）
        */
        private String sourceType;

        /**
        * SKU成本合计
        */
        private BigDecimal skuTotalCost;


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
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 领用日期
        */
        private LocalDate recipientDate;

        /**
        * 用途 枚举类型：办公领用/拍摄/研发/抖音直播/客户领用（客户使用指导）/参展/营销样品/认证检测/供应链生产组装/用户新品体验（仓库提供）/不良品分析（从售后仓领样）/星河线下店领用/其他
        */
        @NotBlank(message = "用途 枚举类型：办公领用/拍摄/研发/抖音直播/客户领用（客户使用指导）/参展/营销样品/认证检测/供应链生产组装/用户新品体验（仓库提供）/不良品分析（从售后仓领样）/星河线下店领用/其他不能为空")
        @Size(max = 200,message = "用途 枚举类型：办公领用/拍摄/研发/抖音直播/客户领用（客户使用指导）/参展/营销样品/认证检测/供应链生产组装/用户新品体验（仓库提供）/不良品分析（从售后仓领样）/星河线下店领用/其他最大长度不能超过200位")
        private String usage;

        /**
        * 发货仓库ID
        */
        @NotBlank(message = "发货仓库ID不能为空")
        @Size(max = 19,message = "发货仓库ID最大长度不能超过19位")
        private String warehouseId;

        /**
        * 单据状态
        */
        @NotBlank(message = "单据状态不能为空")
        @Size(max = 50,message = "单据状态最大长度不能超过50位")
        private String status;

        /**
        * 领用人ID
        */
        @NotBlank(message = "领用人ID不能为空")
        @Size(max = 19,message = "领用人ID最大长度不能超过19位")
        private String userId;

        /**
        * 领用人姓名
        */
        @NotBlank(message = "领用人姓名不能为空")
        @Size(max = 50,message = "领用人姓名最大长度不能超过50位")
        private String userName;

        /**
        * 领用部门ID
        */
        @NotBlank(message = "领用部门ID不能为空")
        @Size(max = 19,message = "领用部门ID最大长度不能超过19位")
        private String deptId;

        /**
        * 领料组织ID
        */
        @NotBlank(message = "领料组织ID不能为空")
        @Size(max = 19,message = "领料组织ID最大长度不能超过19位")
        private String pickOrgId;

        /**
        * 领料组织名称
        */
        @NotBlank(message = "领料组织名称不能为空")
        @Size(max = 50,message = "领料组织名称最大长度不能超过50位")
        private String pickOrgName;

        /**
        * 使用方式 公司内部使用/公司外部使用
        */
        @NotBlank(message = "使用方式 公司内部使用/公司外部使用不能为空")
        @Size(max = 200,message = "使用方式 公司内部使用/公司外部使用最大长度不能超过200位")
        private String usageScope;

        /**
        * 使用方id
        */
        @NotBlank(message = "使用方id不能为空")
        @Size(max = 50,message = "使用方id最大长度不能超过50位")
        private String useUserId;

        /**
        * 使用方名称
        */
        @NotBlank(message = "使用方名称不能为空")
        @Size(max = 50,message = "使用方名称最大长度不能超过50位")
        private String useUserName;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;

        /**
        * 是否邮寄
        */
        @NotNull(message = "是否邮寄不能为空")
        private Boolean isDelivery;

        /**
        * 收货地址
        */
        @NotBlank(message = "收货地址不能为空")
        @Size(max = 200,message = "收货地址最大长度不能超过200位")
        private String receiveAddress;

        /**
        * 收货人
        */
        @NotBlank(message = "收货人不能为空")
        @Size(max = 50,message = "收货人最大长度不能超过50位")
        private String receiverName;

        /**
        * 联系电话
        */
        @NotBlank(message = "联系电话不能为空")
        @Size(max = 20,message = "联系电话最大长度不能超过20位")
        private String receivePhone;

        /**
        * 来源ID（预留字段）
        */
        @NotBlank(message = "来源ID（预留字段）不能为空")
        @Size(max = 19,message = "来源ID（预留字段）最大长度不能超过19位")
        private String sourceId;

        /**
        * 来源单号（预留字段）
        */
        @NotBlank(message = "来源单号（预留字段）不能为空")
        @Size(max = 32,message = "来源单号（预留字段）最大长度不能超过32位")
        private String sourceCode;

        /**
        * 来源类型（预留字段）
        */
        @NotBlank(message = "来源类型（预留字段）不能为空")
        @Size(max = 50,message = "来源类型（预留字段）最大长度不能超过50位")
        private String sourceType;

        /**
        * SKU成本合计
        */
        @NotNull(message = "SKU成本合计不能为空")
        @Digits(integer = 13, fraction = 2, message = "SKU成本合计整数位不能超过13位，小数位不能超过2位")
        private BigDecimal skuTotalCost;


    }


}