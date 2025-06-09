package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
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
 * 委外发料单请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-01-08
*/
@Data
@NoArgsConstructor
public class SubcontractIssueDTO implements Serializable {


     /**
     * 状态统计
     */
     @Data
     @NoArgsConstructor
     @AllArgsConstructor
     public static class TabListDTO {

         /**
         * 类型 ,(waitSubmit待提交,approveIng待审核,approve已审核,reject不通过)
         */
         private String tabFlag;

         /**
          * 类型
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
        private String  detailId;

        /**
        * 委外发料单号【可排序】
        */
        private String code;

        /**
         * 委外订单号【可排序】
         */
        private String subcontractOrderCode;

        /**
         * 来源编码（委外订单号）【可排序】
         */
        private String sourceCode;

        /**
        * 单据状态【可排序】
        */
        private ApproveStatusEnum approveStatus;

        /**
         * 单据状态名称
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
        * 发料类型【可排序】
        */
        private String type;

        /**
         * 发料类型名称
         */
        private String typeName;

        /**
         * skuId【可排序】
         */
        private String skuId;

        /**
         * sku编码【可排序】
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
        * 发料日期【可排序】
        */
        private LocalDate date;

        /**
         * 发料数量【可排序】
         */
        private Integer issueQty;

        /**
         * 收货数量【可排序】
         */
        private Integer receiveQty;

        /**
         * 仓库id【可排序】
         */
        private String warehouseId;

        /**
         * 仓库名称【可排序】
         */
        private String warehouseName;

        /**
         * 仓位【可排序】
         */
        private String warehouseLocation;

        /**
         * 仓位名称
         */
        private String warehouseLocationName;

        /**
         * 备注【可排序】
         */
        private String remark;

        /**
        * 最新审核人名称
        */
        private String approveUserName;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
        * 创建时间【可排序】
        */
        private LocalDateTime createTime;

        /**
         * 审核完成时间【可排序】
         */
        private LocalDateTime approveTime;

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
        * 委外发料单号
        */
        private String code;

        /**
        * 单据审核状态
        */
        private ApproveStatusEnum approveStatus;

        /**
         * 单据审核状态
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
        * 发料类型
        */
        private String type;

        /**
         * 发料类型名称
         */
        private String typeName;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
        * 发料日期
        */
        private LocalDate date;

        /**
         * 委外订单Id
         */
        private String subcontractOrderId;

        /**
         * 委外订单编码
         */
        private String subcontractOrderCode;


        /**
        * 来源id
        *  /scm/subcontractOrder/list,get请求
        */
        private String sourceId;

        /**
        * 来源编号
        */
        private String sourceCode;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
         * 发料明细
         */
        private List<SubcontractIssueDetailDTO.ViewDTO> detailList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 委外发料明细不能为空
         */
        @NotEmpty(message = "委外发料明细不能为空")
        @Valid
        private List<SubcontractIssueDetailDTO.AddDTO> detailList;

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
         * 委外发料明细不能为空
         */
        @NotEmpty(message = "委外发料明细不能为空")
        @Valid
        private List<SubcontractIssueDetailDTO.UpdateDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 发料类型
        * /wms/dict/drop/down?type=issueType
        */
        @NotBlank(message = "发料类型不能为空")
        @Size(max = 32,message = "发料类型最大长度不能超过32位")
        private String type;

        /**
        * 发料日期
        */
        @NotNull(message = "发料日期不能为空")
        private LocalDate date;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 委外订单Id
         */
        @NotBlank(message = "委外订单Id不能为空")
        @Size(max = 19,message = "委外订单Id最大长度不能超过19位")
        private String subcontractOrderId;

        /**
        * 来源id
        * /scm/subcontractOrder/list,get请求
        */
        @Size(max = 19,message = "来源id最大长度不能超过19位")
        private String sourceId;

        /**
         * 供应商id
         */
        private String supplierId;
    }


    /**
     * 委外明细分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class DetailPagingParamDTO {

        /**
         * 来源id（现只有委外订单id）
         */
        @NotBlank(message = "来源id不能为空")
        private String sourceId;

        /**
         * sku编码集合
         */
        private List<String>  skuNoList;

    }

    /**
     * 委外明细分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class SubcontractDetailListDTO{

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 父级来源明细id
         */
        private String parentSourceDetailId;

        /**
         * 委外订单id
         */
        private String subcontractOrderId;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编号
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 图片
         */
        private String imagesUrl;

        /**
         * SPU编号
         */
        private String spuNo;

        /**
         * sku状态
         */
        private String statusName;

        /**
         * 产品分类
         */
        private String categoryName;

        /**
         * 品牌
         */
        private String brandName;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 委外订单明细
         */
        private List<SubcontractIssueDetailDTO.ListSourceDetailDTO> detailList;
    }


    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AutoAddDTO {
       /**
        * 新增参数
        */
       @NotNull(message = "新增数据不能为空")
       @Valid
       private AddDTO addDTO;

        /**
         * 是否审核
         */
       @NotNull(message = "是否审核标识不能为空")
       private Boolean isApprove;

    }
}