package com.erp.model.oms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * B2C寄样申请单请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-12-04
*/
@Data
@NoArgsConstructor
public class KolB2cApplicationDTO implements Serializable {


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

         /**
          * 勾选的id集合
          */
         private List<String> ids;

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
        * 作废状态
        */
        private Boolean invalidStatus;
        private String invalidStatusName;

        /**
        * 作废备注
        */
        private String invalidRemark;

        /**
        * 备注
        */
        private String remark;

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
        * 申请单号
        */
        private String code;

        /**
        * 申请日期
        */
        private LocalDate applyDate;

        /**
        * 寄样类型
        */
        private String sampleType;
        private String sampleTypeName;

        /**
        * 业务类型（是否国际）
        */
        private Boolean isInternational;
        private String isInternationalName;

        /**
        * 店铺ID
        */
        private String shopId;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 币别
        */
        private String currency;
        private String currencyName;

        /**
        * 发货仓库ID
        */
        private String warehouseId;

        /**
        * 发货仓库名称
        */
        private String warehouseName;

        /**
        * 物流渠道id
        */
        private String logisticsChannelId;

        /**
        * 物流渠道
        */
        private String logisticsChannelName;

        /**
        * 申请人ID
        */
        private String applyUserId;

        /**
        * 申请人姓名
        */
        private String applyUserName;

        /**
        * 申请部门ID
        */
        private String applyDeptId;

        /**
        * 申请部门名称
        */
        private String applyDeptName;

        /**
        * 申请理由
        */
        private String applyRemark;


        /**
        * 创建时间
        */
        private LocalDateTime createTime;

        /**
        * 创建人名称
        */
        private String createUserId;
        private String createUserName;

        /**
         * 销售订单关联状态
         */
        private String orderStatus;
        private String orderStatusName;
        /**
         * 发货状态
         */
        private String deliveryStatus;
        private String deliveryStatusName;
        /**
         * 跟踪号
         */
        private String trackNo;

        /**
         * 明细id
         */
        private String  detailId;


        /**
         * SKU ID
         */
        private String skuId;

        /**
         * SKU编码
         */
        private String skuNo;
        /**
         * SKU名称
         */
        private String productName;

        /**
         * 品牌id
         */
        private String brandId;

        /**
         * 品牌
         */
        private String brandName;

        /**
         * 达人ID
         */
        private String partnerId;

        /**
         * 达人昵称
         */
        private String nickname;

        /**
         * 申请数量
         */
        private Integer applyQty;
        /**
         * 备注
         */
        private String detailRemark;

        /**
         * 项目标签
         */
        private String projectTag;
        private String projectTagName;

        /**
         * 登记回片数
         */
        private Integer feedbackQty;
        /**
         * 抓取回片数
         */
        private Integer checkFeedbackQty;
        /**
         * 预计回片日期
         */
        private LocalDate planFeedbackDate;
        /**
         * 回片链接
         */
        private String url;


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
        * 作废状态
        */
        private Boolean invalidStatus;
        private String invalidStatusName;

        /**
        * 作废备注
        */
        private String invalidRemark;

        /**
        * 备注
        */
        private String remark;

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
        * 申请单号
        */
        private String code;

        /**
        * 申请日期
        */
        private LocalDate applyDate;

        /**
        * 寄样类型
        */
        private String sampleType;
        private String sampleTypeName;

        /**
        * 业务类型（是否国际）
        */
        private Boolean isInternational;
        private String isInternationalName;

        /**
        * 店铺ID
        */
        private String shopId;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 币别
        */
        private String currency;
        private String currencyName;

        /**
        * 发货仓库ID
        */
        private String warehouseId;

        /**
        * 发货仓库名称
        */
        private String warehouseName;

        /**
        * 物流渠道id
        */
        private String logisticsChannelId;

        /**
        * 物流渠道
        */
        private String logisticsChannelName;

        /**
        * 申请人ID
        */
        private String applyUserId;

        /**
        * 申请人姓名
        */
        private String applyUserName;

        /**
        * 申请部门ID
        */
        private String applyDeptId;

        /**
        * 申请部门名称
        */
        private String applyDeptName;

        /**
        * 申请理由
        */
        private String applyRemark;

        /**
         * 明细列表
         */
        private List<KolB2cApplicationDetailDTO.UpdateDTO> detailList;
        /**
         * 地址明细
         */
        private List<KolB2cApplicationAddressDTO.UpdateDTO> addressList;

    }


    /**
     * 回片登记下推明细详情
     */
    @Data
    @NoArgsConstructor
    public static class DetailViewDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String  id;

        /**
         * 明细id
         */
        @NotBlank(message = "明细id不能为空")
        private String detailId;

        /**
         * 申请单号
         */
        private String code;
        /**
         * skuId
         */
        @NotBlank(message = "skuId不能为空")
        private String skuId;
        /**
         * sku
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;

        /**
         * 达人ID
         */
        private String partnerId;

        /**
         * 达人昵称
         */
        private String nickname;

        /**
         * 申请数量
         */
        private Integer applyQty;


        /**
         * 回片链接（完整链接）
         */
        @NotBlank(message = "回片链接不能为空")
        private String url;

        /**
         * 发布形式  /oms/drop/down/dict/list?key=publishType
         */
        private String publishType;
        /**
         * 发布日期
         */
        private LocalDate publishDate;
        /**
         * 备注
         */
        private String remark;
    }


    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 明细列表
         */
        private List<KolB2cApplicationDetailDTO.@Valid  AddDTO> detailList;
        /**
         * 地址明细
         */
        private List<KolB2cApplicationAddressDTO.@Valid  AddDTO> addressList;
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
         * 明细列表
         */
        @NotEmpty(message = "产品明细不能为空")
        private List<KolB2cApplicationDetailDTO.@Valid  UpdateDTO> detailList;
        /**
         * 地址明细
         */
        @NotEmpty(message = "地址明细不能为空")
        private List<KolB2cApplicationAddressDTO.@Valid UpdateDTO> addressList;

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
        * 申请日期
        */
        @NotNull(message = "申请日期不能为空")
        private LocalDate applyDate;

        /**
        * 寄样类型
        */
        @NotBlank(message = "寄样类型不能为空")
        private String sampleType;

        /**
        * 业务类型（业务类型（是否国际））
        */
        @NotNull(message = "业务类型不能为空")
        private Boolean isInternational;

        /**
        * 店铺ID
        */
        @NotBlank(message = "店铺ID不能为空")
        private String shopId;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 币别
        */
        @NotBlank(message = "币别不能为空")
        private String currency;

        /**
        * 发货仓库ID
        */
        private String warehouseId;

        /**
        * 发货仓库名称
        */
        private String warehouseName;

        /**
        * 物流渠道id
        */
        private String logisticsChannelId;

        /**
        * 物流渠道
        */
        private String logisticsChannelName;

        /**
        * 申请人ID
        */
        @NotBlank(message = "申请人ID不能为空")
        @Size(max = 19,message = "申请人ID最大长度不能超过19位")
        private String applyUserId;

        /**
        * 申请人姓名
        */
        private String applyUserName;

        /**
        * 申请部门ID
        */
        private String applyDeptId;

        /**
        * 申请部门名称
        */
        private String applyDeptName;

        /**
        * 申请理由
        */
        @Size(max = 200,message = "申请理由最大长度不能超过200位")
        private String applyRemark;


    }


}