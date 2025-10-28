package com.erp.model.plm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * <p>
 * 模具监控请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-10-22
*/
@Data
@NoArgsConstructor
public class MoldMonitorDTO implements Serializable {


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
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 创建人id
         */
        private String createUserId;
        /**
         * 创建人名称
         */
        private String createUserName;
        /**
         * 更新时间
         */
        private LocalDateTime updateTime;

        /**
         * 更新人id
         */
        private String updateUserId;
        /**
         * 更新人名称
         */
        private String updateUserName;


        /**
         * 来源id
         */
        private String sourceId;
        /**
         * 来源明细id
         */
        private String sourceDetailId;
        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 统计状态：counting=统计中 , finish=统计完成  枚举：MoldMonitorStatusEnum
         */
        private String status;
        private String statusName;
        /**
         * 返还状态：underachieved=未达量 , notReturned=未返 , returned=已返  枚举：MoldMonitorReturnStatusEnum
         */
        private String returnStatus;
        private String returnStatusName;
        /**
         * 寿命状态：healthy=健康 , alert=预警 , exhausted=耗尽  枚举：MoldMonitorLifeStatusEnum
         */
        private String lifeStatus;
        private String lifeStatusName;
        /**
         * 采购下单数量
         */
        private Integer purchaseOrderQty;
        /**
         * 采购收货数量
         */
        private Integer warehouseReceiveQty;
        /**
         * 采购入库数量
         */
        private Integer poInstockQty;
        /**
         * 实际返还金额
         */
        private BigDecimal actualReturnPrice;
        /**
         * 返还人id
         */
        private String returnUserId;
        /**
         * 返还人
         */
        private String returnUserName;
        /**
         * 返还日期
         */
        private LocalDate returnDate;
        /**
         * 返还说明
         */
        private String remark;


        /**
         * 模具id
         */
        private String moldId;
        /**
         * 模具编码
         */
        private String moldCode;
        /**
         * 模具名称
         */
        private String moldName;
        /**
         * 供应商id
         */
        private String supplierId;
        /**
         * 供应商编号
         */
        private String supplierCode;
        /**
         * 供应商名称
         */
        private String supplierName;
        /**
         * 开始日期
         */
        private LocalDate startDate;
        /**
         * 结束日期
         */
        private LocalDate endDate;
        /**
         * 标准：purchaseOrder=以采购下单数量 ,warehouseReceive=以采购收货数量 ,poInstock=以采购入库数量  枚举：CfgMoldReturnAlertRuleCountDimEnum
         */
        private String countDim;
        private String countDimName;

        /**
         * 是否禁用
         */
        private Boolean disabled;
        private String disabledName;

        //返还
        /**
         * 返还数量上限
         */
        private Integer returnQtyLimit;
        /**
         * 返回金额
         */
        private BigDecimal returnPrice;

        //预警
        /**
         * 寿命数量
         */
        private Integer lifeQty;
        /**
         * 剩余寿命数量
         */
        private Integer remainingQty;
        /**
         * 预警寿命（数量）
         */
        private Integer alertLifeQty;
        /**
         * 预警寿命（%）
         */
        private BigDecimal alertLifeRate;


    }

    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class TabDTO extends PermissionsDTO {
        /**
         * 来源类型 cfgMoldReturnAlertrRule =模具返还策略 ， cfgMoldAlertrRule = 模具预警策略
         */
        @NotBlank(message = "来源类型不能为空")
        private String sourceType;

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
         * 勾选的id集合
         */
        private List<String> ids;
        /**
         * 来源类型 cfgMoldReturnAlertrRule =模具返还策略 ， cfgMoldAlertrRule = 模具预警策略
         */
        @NotBlank(message = "来源类型不能为空")
        private String sourceType;

    }

    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO extends ListDTO {

        /**
         * 附件集合
         */
        private List<String> attachmentNameList;
        private List<String> attachmentUrlList;

    }


    /**
    * 关联订单参数
    */
    @Data
    @NoArgsConstructor
    public static class RefOrderParamsDTO  {

        /**
         * 类型  purchaseOrder =采购单  ,warehouseReceive = 收货单 , poInstock = 入库单
         */
        @NotBlank(message = "单据类型不能为空")
        private String businessType;
        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;
    }

    /**
    * 关联订单
    */
    @Data
    @NoArgsConstructor
    public static class RefOrderDTO  {

        /**
         * 主键id
         */
        private String id;
        /**
         * 明细id
         */
        private String detailId;
        /**
         * 单据编号
         */
        private String code;
        /**
         * 供应商id
         */
        private String supplierId;
        /**
         * 供应商名称
         */
        private String supplierName;
        /**
         * 审核状态
         */
        private String approveStatus;
        private String approveStatusName;

        /**
         * 作废状态（false未作废，true已作废）
         */
        private Boolean invalidStatus;
        private String invalidStatusName;
        /**
         * skuId
         */
        private String skuId;
        /**
         * skuNo
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 日期
         */
        private LocalDate date;
        /**
         * 数量
         */
        private Integer qty;
    }



    /**
     * 返还确认
     */
    @Data
    @NoArgsConstructor
    public static class UpdateReturnParamsDTO  {
        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;
        /**
         * 实际返还金额
         */
        @NotNull(message = "实际返还金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "实际返还金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal actualReturnPrice;
        /**
         * 返还人id
         */
        @NotBlank(message = "返还人不能为空")
        private String returnUserId;
        /**
         * 返还日期
         */
        @NotNull(message = "返还日期不能为空")
        private LocalDate returnDate;
        /**
         * 附件集合
         */
        private String remark;

        /**
         * 附件集合
         */
        @NotEmpty(message = "附件不能为空")
        private List<String> attachmentNameList;
        private List<String> attachmentUrlList;
    }

    /**
     * 刷新统计参数
     */
    @Data
    @NoArgsConstructor
    public static class RefreshParamsDTO  {
        /**
         * 主键id
         */
        @NotEmpty(message = "主键ids不能为空")
        private List<String> ids;
    }

    /**
     * 生成监控参数
     */
    @Data
    @NoArgsConstructor
    public static class GenerateParamsDTO  {
        /**
         * 主键id
         */
        private List<String> ids;
    }

}