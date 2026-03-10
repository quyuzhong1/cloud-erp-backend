package com.erp.model.oms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * B2B寄样申请主表请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-12-01
*/
@Data
@NoArgsConstructor
public class KolB2bApplicationDTO implements Serializable {


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
        * 主键id【可排序】
        */
        private String  id;
        /**
         * 明细主键id【可排序】
         */
        private String detailId;

        /**
        * 申请单号【可排序】
        */
        private String code;

        /**
        * 申请日期【可排序】
        */
        private LocalDate date;

        /**
        * 审核状态【可排序】
        */
        private String approveStatus;
        /**
         * 审核状态名称
         */
        private String approveStatusName;


        /**
        * 作废状态【可排序】
        */
        private Boolean invalidStatus;

        /**
         * 作废状态名称
         */
        private String invalidStatusName;

        /**
        * 寄样类型【可排序】
        */
        private String type;

        /**
        * 客户id【可排序】
        */
        private String customerId;

        /**
         * 客户名称
         */
        private String customerName;

        /**
        * 申请说明【可排序】
        */
        private String applyRemark;

        /**
        * 申请人id【可排序】
        */
        private String applyUserId;
        /**
         * 申请人名称
         */
        private String applyUserName;

        /**
        * 申请部门id【可排序】
        */
        private String applyDeptId;
        /**
         * 申请部门名称
         */
        private String applyDeptName;

        /**
         * B2B销售订单关联状态名称
         */
        private String b2bRefStatusName;
        /**
         * 发货状态名称
         */
        private String deliveryStatusName;
        /**
         * 跟踪号
         */
        private String trackNo;
        /**
         * skuId【可排序】
         */
        private String skuId;
        /**
         * sku编号【可排序】
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         *  申请数量【可排序】
         */
        private Integer qty;
        /**
         * 已出库时间
         */
        private Integer outstockQty;
        /**
         * 登记回片数
         */
        private Integer feedbackQty;
        /**
         * 抓取回片数
         */
        private Integer captureFeedbackQty;
        /**
         * 预计回片日期【可排序】
         */
        private LocalDate planFeedbackDate;
        /**
         * 回片链接
         */
        private String feedbackUrl;
        /**
         * 项目名称
         */
        private List<String> projectTag;
        private String projectTags;

        /**
         * 明细备注【可排序】
         */
        private String detailRemark;

        /**
         * 审核人名称
         */
        private String approveUserName;

        /**
         * 审核时间【可排序】
         */
        private LocalDateTime approveTime;

        /**
        * 创建时间【可排序】
        */
        private LocalDateTime createTime;

        /**
        * 创建人名称
        */
        private String createUserName;
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
        * 申请单号
        */
        private String code;

        /**
        * 申请日期
        */
        private LocalDate date;

        /**
        * 审核状态
        */
        private String approveStatus;
        /**
         * 审核状态名称
         */
        private String approveStatusName;

        /**
        * 寄样类型
        */
        private String type;

        /**
        * 客户id
        */
        private String customerId;

        /**
         * 客户名称
         */
        private String customerName;

        /**
        * 申请说明
        */
        private String applyRemark;

        /**
        * 申请人id
        */
        private String applyUserId;

        /**
         * 申请人名称
         */
        private String applyUserName;

        /**
        * 申请部门id
        */
        private String applyDeptId;

        /**
         * 申请部门名称
         */
        private String applyDeptName;

        /**
        * 收货国家编码
        */
        private String countryId;

        /**
        * 收货国家名称
        */
        private String countryName;

        /**
        * 收货人
        */
        private String receiverName;

        /**
        * 联系电话
        */
        private String telNumber;

        /**
         * 收货地址id
         */
        private String receiveAddressId;

        /**
        * 地址类型，CustomerAddressTypeEnum枚举
        */
        private String addressType;
        /**
         * 地址类型名称
         */
        private String addressTypeName;

        /**
        * 备注
        */
        private String remark;
        /**
         * 附件名集合
         */
        private List<String> attachNameList;

        /**
         * 附件url集合
         */
        private List<String> attachUrlList;

        /**
         * 明细信息
         */
        private List<KolB2bApplicationDetailDTO.ViewDTO> detailList;
    }

    /**
     * 关联单据
     */
    @Data
    @NoArgsConstructor
    public static class RefBillDTO {
        /**
         * b2b销售单
         */
        private List<B2bSoInfoDTO> soInfoList;
    }


    /**
     * 关联单据
     */
    @Data
    @NoArgsConstructor
    public static class  B2bSoInfoDTO {
        /**
         * 销售订单id
         */
        private String soId;
        /**
         * 销售订单明细id
         */
        private String soDetailId;
        /**
         * 销售订单号
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
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * 发货状态
         */
        private String deliveryStatus;
        /**
         * 发货状态名称
         */
        private String deliveryStatusName;
        /**
         * skuId
         */
        private String skuId;
        /**
         * sku编码
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 销售数量
         */
        private Integer qty;
        /**
         * 明细备注
         */
        private String detailRemark;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        /**
         * 明细信息
         */
        @NotEmpty(message = "明细信息不能为空")
        @Valid
        private List<KolB2bApplicationDetailDTO.AddDTO> detailList;
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
         * 明细信息
         */
        @NotEmpty(message = "明细信息不能为空")
        @Valid
        private List<KolB2bApplicationDetailDTO.UpdateDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 申请日期
        */
        @NotNull(message = "申请日期不能为空")
        private LocalDate date;

        /**
        * 寄样类型，/api/oms/cfgKolOption/select?type=kolSampleType
        */
        @NotBlank(message = "寄样类型不能为空")
        @Size(max = 64,message = "寄样类型最大长度不能超过64位")
        private String type;

        /**
        * 客户id，/api/oms/customer/listEnable
        */
        @NotBlank(message = "客户id不能为空")
        @Size(max = 19,message = "客户id最大长度不能超过19位")
        private String customerId;

        /**
        * 申请说明
        */
        @Size(max = 255,message = "申请说明最大长度不能超过255位")
        private String applyRemark;

        /**
        * 申请人id，/api/plm/common/findUserList
        */
        @NotBlank(message = "申请人id不能为空")
        @Size(max = 19,message = "申请人id最大长度不能超过19位")
        private String applyUserId;

        /**
        * 申请部门id，/api/sys/department/drop/down
        */
        @Size(max = 19,message = "申请部门id最大长度不能超过19位")
        private String applyDeptId;

        /**
        * 收货国家编码
        */
        @Size(max = 32,message = "收货国家编码最大长度不能超过32位")
        private String countryId;


        /**
        * 收货人
        */
        @Size(max = 64,message = "收货人最大长度不能超过64位")
        private String receiverName;

        /**
        * 联系电话
        */
        @Size(max = 64,message = "联系电话最大长度不能超过64位")
        private String telNumber;

        /**
        * 收货地址，/api/oms/customer/listCustomerAddress?customerId=
        */
        @NotBlank(message = "收货地址不能为空")
        @Size(max = 255,message = "收货地址最大长度不能超过255位")
        private String receiveAddressId;

        /**
        * 地址类型，/api/oms/common/enumDropDown?type=CustomerAddressType
        */
        @NotBlank(message = "地址类型不能为空")
        @Size(max = 32,message = "地址类型，CustomerAddressTypeEnum枚举最大长度不能超过32位")
        private String addressType;

        /**
        * 备注
        */
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
         * 附件名集合
         */
        private List<String> attachNameList;

        /**
         * 附件url集合
         */
        private List<String> attachUrlList;
    }

    /**
     * 生成销售订单信息
     */
    @Data
    @NoArgsConstructor
    public static class GenerateSoInfoDTO {

        /**
         * B2B寄样申请明细Id
         */
        private String detailId;
        /**
         * 销售组织Id,/api/sys/company/list
         */
        private String soOrgId;
        /**
         * 仓库Id,/api/wms/warehouse/list
         * http://172.16.100.11:3002/project/92/interface/api/42243
         */
        private String warehouseId;
    }

    /**
     * 生成回片登记信息
     */
    @Data
    @NoArgsConstructor
    public static class GenerateFeedbackDTO {
        /**
         * B2B寄样申请明细Id
         */
        private String detailId;
        /**
         * 达人Id,http://172.16.100.11:3002/project/110/interface/api/41971
         */
        private String partnerId;
        /**
         * 回片链接
         */
        private String url;
        /**
         * 回片发布日期,
         */
        private LocalDate publishDate;
        /**
         * 发布形式,/oms/drop/down/dict/list?key=publishType
         */
        private String publishType;
        /**
         * 备注
         */
        private String remark;
    }
}