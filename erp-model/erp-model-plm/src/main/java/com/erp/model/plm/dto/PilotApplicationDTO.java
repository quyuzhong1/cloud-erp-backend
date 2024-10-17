package com.erp.model.plm.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.SortDTO;
import java.util.List;

import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.plm.entity.PlmAttachmentEntity;
import com.erp.model.workflow.dto.ProcessTaskManagementAttachmentDTO;
import com.erp.model.workflow.vo.ApproveNodeRecordVO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import com.common.business.dto.AdvanceQueryDTO;

import java.util.Map;

/**
 * <p>
 * 试产申请请求响应实体
 * </p>
 *
 * @author tmj
 * @since 2024-08-27
*/
@Data
@NoArgsConstructor
public class PilotApplicationDTO implements Serializable {
     /**
     * 状态统计
     */
     @Data
     @NoArgsConstructor
     @AllArgsConstructor
     public static class TabListDTO {

         /**
         * tab类型
         */
         private String tabFlag;

         /**
          * tab类型名称
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
     @EqualsAndHashCode(callSuper = true)
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
          * 主键id集合
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
        * 单据编码【可排序】
        */
        private String code;

        /**
        * 审核状态【可排序】
        */
        private String approveStatus;

        /**
         * 审核状态名称
         * ApproveStatusEnum
         */
        private String approveStatusName;

        /**
        * 订单状态【可排序】
        */
        private String orderStatus;

        /**
         * 订单状态名称
         */
        private String orderStatusName;

        private String skuId;

        /**
         * skuNo【可排序】
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 下单类型：试产/量产【可排序】
         * /api/plm/common/enumDropDown?type=PilotApplicationType
         */
        private String type;

        /**
         * 下单类型名称
         * PilotApplicationTypeEnum
         */
        private String typeName;

        /**
         * 关联任务
         */
        private String refTask;

        /**
         * 申请数量【可排序】
         */
        private int applyQty;

        /**
         * 批准数量【可排序】
         */
        private int approveQty;

        /**
         * 采购申请量
         */
        private int purchaseApplyQty;

        /**
         * 采购入库量
         */
        private int stockInQty;

        /**
         * 业务类型
         */
        private String businessType;

        /**
         * 一级供应商ID【可排序】
         */
        private String mainSupplierId;

        /**
         * 一级供应商名称
         */
        private String mainSupplierName;

        /**
         * 目标含税成本
         */
        private String targetTaxCost;

        /**
         * 实际含税成本
         */
        private String actualTaxCost;

        /**
        * 单据备注
        */
        private String remark;

        /**
         * 明细备注
         */
        private String detailRemark;

        /**
        * 最新审核人ID【可排序】
        */
        private String approveUserId;

        /**
         * 最新审核人名称
         */
        private String approveUserName;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
         * 创建人ID
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
         * 产品明细ID
         */
        private String detailId;
    }

    /**
    * 导出Excel
    */
    @EqualsAndHashCode(callSuper = true)
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
        * 单据编码
        */
        private String code;

        /**
         * 单据日期
         */
        private LocalDate billDate;

        /**
        * 审核状态
        */
        private ApproveStatusEnum approveStatus;

        /**
         * 审核状态名称
         */
        private String approveStatusName;

        /**
        * 单据备注
        */
        private String remark;

        /**
         * 产品明细
         */
        private List<PilotApplicationDetailDTO.ViewDTO> productDetailList;

        /**
         * 关联任务
         */
        private List<PilotApplicationRefTaskDTO.ViewDTO> taskList;

        /**
         * 审核记录
         */
        private List<PilotApplicationDTO.AuditorHandleDTO> approveFlowList;

        /**
         * 附件
         */
        private List<PlmAttachmentEntity> attachmentList;
    }

    /**
     * 审核记录
     */
    @Data
    public static class ApproveFlowDTO {
        /**
         * 审核人ID
         */
        private String userId;
        /**
         * 审核人名称
         */
        private String userName;
        /**
         * 审核状态
         */
        private String approveStatus;
        /**
         * 审核状态名称
         */
        private String approveStatusName;
        /**
         * 审批意见
         */
        private String comment;
        /**
         * 审批时间
         */
        private String approveTime;
        /**
         * 审批时间描述
         */
        private String approveTimeDesc;
        /**
         * 附件名称集合
         */
        private List<String> attachNameList;
        /**
         * 附件URL集合
         */
        private List<String> attachUrlList;
    }

    /**
    * 新增
    */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO{
        /**
         * 单据日期
         */
        private LocalDate billDate;

        /**
         * 审核状态
         */
        private ApproveStatusEnum approveStatus = ApproveStatusEnum.WAIT_SUBMIT;

        /**
         * 产品明细集合
         */
        private List<PilotApplicationDetailDTO.AddDTO> productDetailList;

        /**
         * 关联的任务集合
         */
        private List<PilotApplicationRefTaskDTO.AddDTO> taskList;

        /**
         * 附件
         */
        @NotEmpty(message = "请上传试产/量产报告")
        private List<AttachmentDTO> attachmentList;
    }

    /**
    * 修改
    */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {
        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 产品明细集合
         */
        private List<PilotApplicationDetailDTO.UpdateDTO> productDetailList;

        /**
         * 关联的任务集合
         */
        private List<PilotApplicationRefTaskDTO.UpdateDTO> taskList;
        /**
         * 附件
         */
        private List<AttachmentDTO> attachmentList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {
        /**
        * 订单状态
        */
        private String orderStatus;

        /**
        * 单据备注
        */
        private String remark;
    }

    /**
     * 下推采购申请
     */
    @Data
    @NoArgsConstructor
    public static class PushPurchaseApplicationDTO {
        /**
         * 试产量产单ID
         */
        private String id;

        /**
         * 试产量产单号
         */
        private String code;

        /**
         * 明细ID
         */
        private String detailId;

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
         * 下单类型
         */
        private String type;

        /**
         * 下单类型名称
         */
        private String typeName;

        /**
         * 批准数量
         */
        private int approveQty;

        /**
         * 待申请量
         */
        private int spareApplyQty;

        /**
         * 采购申请量
         */
        private int purchaseApplyQty;

        /**
         * 目的仓库ID
         */
        private String toWarehouseId;

        /**
         * 采购组织ID
         */
        private String purchaseOrgId;

        /**
         * 计划交期
         */
        private LocalDate planDeliveryDate;
    }

    /**
     * 审批参数
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class ApproveDTO extends BaseApproveParamDTO {
        /**
         * 附件
         */
        private List<AttachmentDTO> attachmentList;
        /**
         * 产品明细
         */
        private List<PilotApplicationDetailDTO.ViewDTO> productDetailList;
    }

    @Data
    public static class ApproveProductDTO{
        /**
         * skuId
         */
        private String skuId;
        /**
         * 批准数量
         */
        private int qty;
    }

    @Data
    public static class PurchaseApplicationParamDTO {
        /**
         * 产品明细ID集合
         */
        @NotEmpty(message = "产品明细ID不能为空")
        private List<String> detailIds;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WarehouseDTO{
        /**
         * 仓库ID
         */
        private String id;
        /**
         * 仓库名称
         */
        private String name;
    }

    /**
     * 统计采购申请单中sku的数量
     */
    @Data
    public static class PurchaseSkuQtyDTO{
        /**
         * skuID
         */
        private String skuId;
        /**
         * 该sku已申请的数量
         */
        private int qty;
    }

    /**
     * 关联任务
     */
    @Data
    public static class RefTaskDTO {
        /**
         * 单据ID
         */
        private String id;
        /**
         * 任务ID
         */
        @NotEmpty(message = "关联任务ID不能为空")
        private List<String> taskIds;
    }

    /**
     * 产品明细
     */
    @Data
    public static class ProductDTO{
        /**
         * 单据ID
         */
        private String id;
        /**
         * 产品明细ID
         */
        @NotEmpty(message = "产品明细ID不能为空")
        private List<String> productDetailIds;
    }

    /**
     * 审批记录
     */
    @Data
    public static class AuditorHandleDTO{

        /**
         * 审核人ID
         */
        private String userId;

        /**
         * 审核人名称
         */
        private String userName;

        /**
         * 审批结果
         */
        private String result;

        /**
         * 审批结果名称
         */
        private String resultName;

        /**
         * 审批意见
         */
        private String comment;

        /**
         * 审批时间
         */
        private String time;

        /**
         * 审批时间描述
         */
        private String timeDesc;

        /**
         * 附件
         */
        private List<ProcessTaskManagementAttachmentDTO.CommonDTO> attachmentList;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AttachmentDTO{
        /**
         * 文件名称
         */
        private String attachName;
        /**
         * 文件url
         */
        private String attachUrl;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ApprovePilotNoticeDTO{
        /**
         * 试产量产主键id
         */
        private String id;
        /**
         * 试产量产单号
         */
        private String code;
        /**
         * 任务id
         */
        private List<String> taskId;
        /**
         * 产品id
         */
        private String productId;
        /**
         *
         */
        private String spuNo;
        /**
         * 产品名称
         */
        private String spuName;
        /**
         *
         */
        private String skuId;
        /**
         *
         */
        private String skuNo;
        /**
         *
         */
        private String skuName;
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
         * 操作人
         */
        private String userName;
    }
}