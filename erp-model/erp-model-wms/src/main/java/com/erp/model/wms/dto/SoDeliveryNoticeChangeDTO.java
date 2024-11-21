package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
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
 * <p>
 * 发货通知变更单请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-10-23
*/
@Data
@NoArgsConstructor
public class SoDeliveryNoticeChangeDTO implements Serializable {


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
          * tab名称
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
    public static class ProductAddDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        /**
         * 销售id
         */
        @NotBlank(message = "销售id不能为空")
        private String  soId;
        /**
         * 发货通知单id
         */
        @NotBlank(message = "发货通知单不能为空")
        private String  noticeId;

        /**
         * sku,快粘贴传
         */
        private List<String> skuNoList;


        /**
         * 发货通知单明细id
         */
        private List<String> detailIds;
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
         * 明细Id
         */
        private String detailId;
        /**
        * 单据编号
        */
        private String code;

        /**
         * 发货通知id
         */
        private String noticeId;

        /**
         * 发货通知单号
         */
        private String noticeCode;

        /**
         * 销售id
         */
        private String soId;

        /**
         * 销售单号
         */
        private String soCode;

        /**
        * 审核状态 
        */
        private String approveStatus;
        /**
         * 审核状态名称
         */
        private String approveStatusName;

        /**
         * 作废状态
         */
        private Boolean invalidStatus;

        /**
         * 作废状态名称
         */
        private String invalidStatusName;

        /**
         * 客户名称
         */
        private String customerName;

        /**
         * 通知单明细id
         */
        private String sourceDetailId;
        /**
         * 销售明细id
         */
        private String soDetailId;

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
         * 销售数量
         */
        private Integer saleQty;

        /**
         * 已下推发货通知数量
         */
        private Integer allNoticeQty;

        /**
         * 变更类型
         */
        private String changeType;

        /**
         * 变更类型名称
         */
        private String changeTypeName;

        /**
         * 原发货通知数量
         */
        private Integer originQty;

        /**
         * 已拣货数量
         */
        private Integer pickedQty;

        /**
         * 新发货通知数量
         */
        private Integer newQty;

        /**
        * 创建人名称
        */
        private String createUserName;

        /**
         * 审核人名称
         */
        private String approveUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 审核时间
         */
        private LocalDateTime approveTime;

    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ViewIdDTO {
        /**
         * 发货通知单id 或者是发货通知变更单id
         */
        @NotBlank(message = "id不能为空")
        private String id;

        /**
         * 发货通知单明细id
         */
        private List<String> detailIds;
        /**
         * 类型，pushDown：下推, edit：编辑
         */
        @NotBlank(message = "类型不能为空")
        private String type;
    }

    /**
     * 产品dto
     */
    @Data
    @NoArgsConstructor
    public static class ProductDTO {

        /**
         * 来源明细Id
         */
        private String sourceDetailId;
        /**
         * 销售明细Id
         */
        private String soDetailId;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编号
         */
        private String skuNo;
        /**
         * bom版本
         */
        private String bomVersion;

        /**
         * 客户sku
         */
        private String platformSkuNo;
        /**
         * 产品名称
         */
        private String productName;

        /**
         * 销售数量
         */
        private Integer saleQty;

        /**
         * 已下推发货通知数量
         */
        private Integer allNoticeQty;

        /**
         * 原发货通知数量
         */
        private Integer currentNoticeQty;

        /**
         * 已拣货
         */
        private Integer pickedQty;

        /**
         * 最大可变更数量
         */
        private Integer maxCanChangeQty;

        /**
         * 备注
         */
        private String remark;
    }

    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 发货通知变更单id
        */
        private String  id;

        /**
         * 发货通知单号id
         */
        @NotBlank(message = "发货通知id不能为空")
        private String  noticeId;
        /**
        * 发货通知变更单号
        */
        private String code;

        /**
         * 发货通知单号
         */
        @NotBlank(message = "发货通知单号不能为空")
        private String noticeCode;

        /**
         * 审核状态
         */
        private String approveStatus;
        /**
         * 审核状态名称
         */
        private String approveStatusName;
        /**
         * 单据类型
         */
        private String type;
        /**
         * 单据类型名称
         */
        private String typeName;

        /**
         * 销售id
         */
        @NotBlank(message = "销售id不能为空")
        private String soId;
        /**
         * 销售单号
         */
        @NotBlank(message = "销售单号不能为空")
        private String soCode;

        /**
         * 销售组织id
         */
        private String salesOrgId;
        /**
         * 销售组织名称
         */
        private String salesOrgName;
        /**
         * 销售部门id
         */
        private String salesDeptId;
        /**
         * 销售部门名称
         */
        private String salesDeptName;
        /**
         * 销售员id
         */
        private String sellerId;
        /**
         * 销售员名称
         */
        private String sellerName;

        /**
         * 实际发货日期
         */
        private LocalDate actualDeliveryDate;

        /**
         * 要货日期
         */
        private LocalDate requireDate;

        /**
         * 预计发货日期
         */
        private LocalDate planDeliveryDate;

        /**
         * 完成打包日期
         */
        private LocalDate packDate;

        /**
         * 出货仓库
         */
        private String warehouseId;
        /**
         * 出货仓库名称
         */
        private String warehouseName;

        /**
         * 库存组织id
         */
        private String warehouseOrgId;
        /**
         * 库存组织名称
         */
        private String warehouseOrgName;
        /**
         * 承运商id
         */
        private String carrierId;
        /**
         * 承运商名称
         */
        private String carrierName;
        /**
         * 运输单号
         */
        private String trackNo;

        /**
         * 变更原因
         */
        @NotBlank(message = "变更原因不能为空")
        private String changeReason;
        /**
         * 客户id
         */
        private String customerId;
        /**
         * 客户名称
         */
        private String customerName;
        /**
         * 收货人
         */
        private String receiverName;
        /**
         * 联系电话
         */
        private String telNumber;
        /**
         * 交货方式 oms/common/enumDropDown?type=DeliveryMode
         * 描述：deliverGoods（发货）selfExtraction（自提）
         */
        private String deliveryModeDict;
        /**
         * 交货方式名称
         */
        private String deliveryModeDictName;
        /**
         * 收货地址
         */
        private String receiveAddress;

        /**
         * 明细
         */
        @NotEmpty(message = "明细不能为空")
        @Valid
        private List<ViewDetail> viewDetailList;
    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class ViewDetail {

        /**
         * 明细Id
         */
        private String detailId;

        /**
         * 来源明细Id
         */
        private String sourceDetailId;

        /**
         * 销售明细Id
         */
        @NotBlank(message = "销售明细Id不能为空")
        private String soDetailId;

        /**
         * skuId
         */
        @NotBlank(message = "skuId不能为空")
        private String skuId;

        /**
         * sku编号
         */
        @NotBlank(message = "sku编号不能为空")
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 变更类型
         */
        @NotBlank(message = "变更类型不能为空")
        private String changeType;
        /**
         * 变更类型名称
         */
        private String changeTypeName;

        /**
         * 销售数量
         */
        private Integer saleQty;

        /**
         * 已下推发货通知数量
         */
        private Integer allNoticeQty;

        /**
         * 原发货通知数量
         */
        private Integer currentNoticeQty;

        /**
         * 已拣货
         */
        private Integer pickedQty;

        /**
         * 最大可变更数量
         */
        private Integer maxCanChangeQty;

        /**
         * 新发货通知数量
         */
        private Integer newNoticeQty;

        /**
         * 备注
         */
        private String remark;

        /**
         * bom版本
         */
        private String bomVersion;

        /**
         * 客户sku
         */
        private String platformSkuNo;
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
    public static class UpdateDTO extends ViewDTO {

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
        * 来源单号
        */
        @NotBlank(message = "来源单号不能为空")
        @Size(max = 50,message = "来源单号最大长度不能超过50位")
        private String sourceCode;

        /**
        * 来源Id
        */
        @NotBlank(message = "来源Id不能为空")
        @Size(max = 50,message = "来源Id最大长度不能超过50位")
        private String sourceId;

        /**
        * 销售订单编号
        */
        @NotBlank(message = "销售订单编号不能为空")
        @Size(max = 50,message = "销售订单编号最大长度不能超过50位")
        private String soCode;

        /**
        * 销售订单id
        */
        @NotBlank(message = "销售订单id不能为空")
        @Size(max = 50,message = "销售订单id最大长度不能超过50位")
        private String soId;

        /**
        * 客户表id
        */
        @NotBlank(message = "客户表id不能为空")
        @Size(max = 19,message = "客户表id最大长度不能超过19位")
        private String customerId;

        /**
        * 客户名称
        */
        @NotBlank(message = "客户名称不能为空")
        @Size(max = 255,message = "客户名称最大长度不能超过255位")
        private String customerName;

        /**
        * 变更原因
        */
        @NotBlank(message = "变更原因不能为空")
        @Size(max = 255,message = "变更原因最大长度不能超过255位")
        private String changeReason;


    }


}