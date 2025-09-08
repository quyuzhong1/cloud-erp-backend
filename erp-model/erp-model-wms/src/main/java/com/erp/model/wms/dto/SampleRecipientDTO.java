package com.erp.model.wms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;
import javax.validation.constraints.Digits;
import org.apache.commons.lang3.StringUtils;

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
          * 名称
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
        * 审批状态(waitSubmit=待提交, approved=已批准, rejected=已驳回) /wms/drop/down/approveStatus/list
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
        * 用途 /wms/dict/drop/down?type=sampleUsage
        */
        private String usage;

        /**
        * 发货仓库ID
        */
        private String warehouseId;

        /**
         * 发货仓库名称
         */
        private String warehouseName;

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
        * 使用方式 公司内部使用/公司外部使用 /wms/dict/drop/down?type=sampleUsageScope
        */
        private String usageScope;

        /**
        * 使用方id /wms/dict/saveOrUpdateBatch 保存 /wms/dict/drop/down?type=sampleUseUser 查询
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
        /**
         * 领用数量
         */
        @NotNull(message = "领用数量不能为空")
        private Integer recipientQty;

        /**
         * 已出库数量
         */
        @NotNull(message = "已出库数量不能为空")
        private Integer deliveryQty;
        /**
         * 待出库数量
         */
        private Integer reservedQty;

        /**
         * 明细ID
         */
        private String detailId;

        /**
         * SKU编号
         */
        private String skuNo;

        /**
         * SKU ID
         */
        private String skuId;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 执行状态 /wms/dict/drop/down?type=executionStatus
         */
        private String execStatus;

        /**
         * 执行状态名称
         */
        private String execStatusName;

        /**
         * 明细备注
         */
        private String detailRemark;

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
        * 审批状态(waitSubmit=待提交, approved=已批准, rejected=已驳回) /wms/drop/down/approveStatus/list
        */
        private String approveStatus;


        /**
        * 样品领用单号
        */
        private String code;

        /**
        * 领用日期
        */
        private LocalDate recipientDate;

        /**
        * 用途  /wms/dict/drop/down?type=sampleUsage
        */
        private String usage;

        /**
        * 发货仓库ID 接口：warehouse/list
        */
        private String warehouseId;

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
        * 使用方式 公司内部使用/公司外部使用 /wms/dict/drop/down?type=sampleUsageScope
        */
        private String usageScope;

        /**
        * 使用方id /wms/dict/saveOrUpdateBatch 保存 /wms/dict/drop/down?type=sampleUseUser 查询
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
        * SKU成本合计
        */
        private BigDecimal skuTotalCost;
        /**
         * 附件名称集合
         */
        private List<String> attachmentNameList;

        /**
         * 附件URL集合
         */
        private List<String> attachmentUrlList;

        /**
         * 产品列表
         */
        private List<ProductDTO>detailList;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 详情
         */
        private List<ProductDTO> detailList;

        /**
         * 附件名称集合
         */
        private List<String> attachmentNameList;

        /**
         * 附件URL集合
         */
        private List<String> attachmentUrlList;

    }

    @Data
    @NoArgsConstructor
    public static class ProductDTO {

        /**
         * id 新增这个字段为空
         */
        private String id;
        
        /**
         * SKU编码
         */
        @NotBlank(message = "SKU不能为空")
        private String skuNo;

        /**
         * SKU ID
         */
        @NotBlank(message = "SKU不能为空")
        private String skuId;
        
        /**
         * 产品名称
         */
        private String productName;
        
        /**
         * 领用数量
         */
        @NotNull(message = "领用数量不能为空")
        @Min(value = 1, message = "领用数量必须大于0")
        private Integer quantity;

        /**
         * 可领用库存
         */
        private Integer usableQty;
        
        /**
         * 备注
         */
        @Size(max = 200, message = "备注最大长度不能超过200位")
        private String remark;

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

        /**
         * 详情
         */
        private List<ProductDTO> detailList;

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
        @NotNull(message = "领用日期不允许为空")
        private LocalDate recipientDate;

        /**
         * 用途 /wms/dict/drop/down?type=sampleUsage
         */
        @NotBlank(message = "用途 不能为空")
        private String usage;

        /**
         * 发货仓库ID  接口： warehouse/list
         */
        @NotBlank(message = "发货仓库ID不能为空")
        @Size(max = 19, message = "发货仓库ID最大长度不能超过19位")
        private String warehouseId;

        /**
         * 领用人ID
         */
        @NotBlank(message = "领用人ID不能为空")
        @Size(max = 19, message = "领用人ID最大长度不能超过19位")
        private String userId;

        /**
         * 领用部门ID
         */
        @NotBlank(message = "领用部门ID不能为空")
        @Size(max = 19, message = "领用部门ID最大长度不能超过19位")
        private String deptId;

        /**
         * 领料组织ID
         */
        @NotBlank(message = "领料组织ID不能为空")
        @Size(max = 19, message = "领料组织ID最大长度不能超过19位")
        private String pickOrgId;


        /**
         * 使用方式 公司内部使用/公司外部使用 /wms/dict/drop/down?type=sampleUsageScope
         */
        @NotBlank(message = "使用方式 公司内部使用/公司外部使用不能为空")
        private String usageScope;

        /**
         * 使用方id /wms/dict/saveOrUpdateBatch 保存 /wms/dict/drop/down?type=sampleUseUser 查询
         */
        @NotBlank(message = "使用方id不能为空")
        @Size(max = 50, message = "使用方id最大长度不能超过50位")
        private String useUserId;

        /**
         * 备注
         */
        @Size(max = 200, message = "备注最大长度不能超过200位")
        private String remark;

        /**
         * 是否邮寄
         */
        @NotNull(message = "是否邮寄不能为空")
        private Boolean isDelivery;

        /**
         * 收货地址
         */
        @Size(max = 200, message = "收货地址最大长度不能超过200位")
        private String receiveAddress;

        /**
         * 收货人
         */
        @Size(max = 50, message = "收货人最大长度不能超过50位")
        private String receiverName;

        /**
         * 联系电话
         */
        @Size(max = 20, message = "联系电话最大长度不能超过20位")
        private String receivePhone;

        /**
         * SKU成本合计
         */
        @Digits(integer = 13, fraction = 2, message = "SKU成本合计整数位不能超过13位，小数位不能超过2位")
        private BigDecimal skuTotalCost;

        /**
         * 附件名称集合
         */
        private List<String> attachmentNameList;

        /**
         * 附件URL集合
         */
        private List<String> attachmentUrlList;

        /**
         * 验证：当选择邮寄时，收货地址必填
         */
        @AssertTrue(message = "选择邮寄时，收货地址不能为空")
        public boolean isReceiveAddressValid() {
            // 如果不邮寄，则收货地址可以为空
            if (isDelivery == null || !isDelivery) {
                return true;
            }
            // 如果邮寄，则收货地址不能为空
            return StringUtils.isNotBlank(receiveAddress);
        }

        /**
         * 验证：当选择邮寄时，收货人必填
         */
        @AssertTrue(message = "选择邮寄时，收货人不能为空")
        public boolean isReceiverNameValid() {
            // 如果不邮寄，则收货人可以为空
            if (isDelivery == null || !isDelivery) {
                return true;
            }
            // 如果邮寄，则收货人不能为空
            return StringUtils.isNotBlank(receiverName);
        }

        /**
         * 验证：当选择邮寄时，联系电话必填
         */
        @AssertTrue(message = "选择邮寄时，联系电话不能为空")
        public boolean isReceivePhoneValid() {
            // 如果不邮寄，则联系电话可以为空
            if (isDelivery == null || !isDelivery) {
                return true;
            }
            // 如果邮寄，则联系电话不能为空
            return StringUtils.isNotBlank(receivePhone);
        }


    }
    /**
     * SKU成本查询请求参数
     */
    @Data
    @NoArgsConstructor
    public static class SkuCostQueryDTO {
        /**
         * SKU成本查询明细列表
         */
        @NotEmpty(message = "SKU成本查询明细列表不能为空")
        private List<SkuCostQueryDetailDTO> detailList;
    }
    
    /**
     * SKU成本查询明细
     */
    @Data
    @NoArgsConstructor
    public static class SkuCostQueryDetailDTO {
        /**
         * SKU编号
         */
        @NotBlank(message = "SKU编号不能为空")
        private String skuNo;

        /**
         * SKU ID
         */
        @NotBlank(message = "SKU ID不能为空")
        private String skuId;
        
        /**
         * 仓库ID
         */
        @NotBlank(message = "仓库ID不能为空")
        private String warehouseId;

        /**
         * 领料组织ID
         */
        @NotBlank(message = "领料组织ID不能为空")
        private String orgId;


    }

    /**
     * SKU成本信息
     */
    @Data
    @NoArgsConstructor
    public static class SkuDTO {
        /**
         * SKU编号
         */
        private String skuNo;

        /**
         * SKU ID
         */
        private String skuId;

        /**
         * SKU成本
         */
        private BigDecimal skuCost;

        /**
         * 币种
         */
        private String currency;
    }

    /**
     * 下推其他出库单查询响应
     */
    @Data
    @NoArgsConstructor
    public static class ViewGenerateOutboundOrderDTO {
        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 来源类型
         */
        private String sourceType;
        
        /**
         * SKU编号
         */
        private String skuNo;
        
        /**
         * 产品名称
         */
        private String productName;
        
        /**
         * 发货仓库
         */
        private String warehouseName;
        /**
         * 发货仓库ID
         */
        private String warehouseId;
        
        /**
         * 领用人
         */
        private String userName;
        
        /**
         * 领用人ID
         */
        private String userId;
        
        /**
         * 待出库数量
         */
        private Integer reservedQty;
        
        /**
         * 已出库数量
         */
        private Integer deliveryQty;
        
        /**
         * 出库数量
         */
        private Integer outQty;
        
        /**
         * 出库日期
         */
        private LocalDate billDate;
        
        /**
         * 仓位
         */
        private String warehouseLocation;
        
        /**
         * 即时库存
         */
        private Integer curInventoryQty;
        
        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 下推其他出库单保存请求
     */
    @Data
    @NoArgsConstructor
    public static class ListGenerateOutboundOrderDTO {
        /**
         * 下推其他出库单列表
         */
        @NotEmpty(message = "下推其他出库单列表不能为空")
        private List<ViewGenerateOutboundOrderDTO> list;
    }
    
    /**
     * SKU列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class SkuListQueryDTO {
        /**
         * 搜索关键词（SKU编号、产品名称等）
         */
        private String searchKeyword;
        
        /**
         * 仓库ID（用于查询库存）
         */
        private String warehouseId;
        
        /**
         * 高级查询条件
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;
        
        /**
         * 当前页码
         */
        private Integer currPage = 1;
        
        /**
         * 每页大小
         */
        private Integer pageSize = 20;
    }
    
    /**
     * SKU列表响应
     */
    @Data
    @NoArgsConstructor
    public static class SkuListResponseDTO {
        /**
         * SKU ID
         */
        private String skuId;
        
        /**
         * SKU编号
         */
        private String skuNo;
        
        /**
         * 产品名称
         */
        private String productName;
        
        /**
         * SPU编号
         */
        private String spuNo;
        
        /**
         * 标准零售价
         */
        private BigDecimal retailPrice;
        
        /**
         * 可用库存
         */
        private Integer availableQty;
        
        /**
         * 冻结库存
         */
        private Integer frozenQty;
        
        /**
         * 总库存
         */
        private Integer totalQty;
    }

    /**
     * SKU可用库存查询参数
     */
    @Data
    @NoArgsConstructor
    public static class SkuAvailableStockQueryDTO {
        /**
         * SKU成本查询明细列表
         */
        @NotEmpty(message = "SKU成本查询明细列表不能为空")
        private List<SkuCostQueryDetailDTO> detailList;
        
    }
    
    /**
     * SKU可用库存响应
     */
    @Data
    @NoArgsConstructor
    public static class SkuAvailableStockDTO {
        /**
         * SKU ID
         */
        private String skuId;
        
        /**
         * SKU编号
         */
        private String skuNo;
        
        /**
         * 产品名称
         */
        private String productName;
        
        /**
         * 仓库ID
         */
        private String warehouseId;
        
        /**
         * 仓库名称
         */
        private String warehouseName;
        
        /**
         * 可用库存
         */
        private Integer availableQty;
        
    }

    /**
     * 结束领用DTO
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class FinishRecipientDTO extends PermissionsDTO {
        /**
         * 样品领用单ID列表
         */
        @NotEmpty(message = "样品领用单ID列表不能为空")
        private List<String> ids;
        
        /**
         * 结束领用原因
         */
        @Size(max = 200, message = "结束领用原因不能超过200字符")
        private String reason;
    }

}