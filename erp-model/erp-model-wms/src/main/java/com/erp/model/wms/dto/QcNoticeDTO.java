package com.erp.model.wms.dto;

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
 * 质检通知单请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-04-21
*/
@Data
@NoArgsConstructor
public class QcNoticeDTO implements Serializable {


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
        * 单据状态
        */
        private String approveStatus;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 最新审核人ID
        */
        private String approveUserId;

        /**
        * 最新审核人
        */
        private String approveUserName;

        /**
        * 质检通知单号
        */
        private String code;

        /**
        * 质检仓库
        */
        private String qcWarehouseId;

        /**
        * 上架仓库
        */
        private String putawayWarehouseId;

        /**
        * 质检类型 stockIn 入库质检   outsideQc 外检质检  insideQc  在库质检   newProductStockIn 新品入库质检  b2bOutsideQc  B2B外检  
        */
        private String qcType;

        /**
        * 备注
        */
        private String remark;


        /**
        * 审核状态名称
        */
        private String approveStatusName;

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
        * 单据状态
        */
        private String approveStatus;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 最新审核人ID
        */
        private String approveUserId;

        /**
        * 最新审核人
        */
        private String approveUserName;

        /**
        * 质检通知单号
        */
        private String code;

        /**
        * 质检仓库
        */
        private String qcWarehouseId;

        /**
        * 上架仓库
        */
        private String putawayWarehouseId;

        /**
        * 质检类型 stockIn 入库质检   outsideQc 外检质检  insideQc  在库质检   newProductStockIn 新品入库质检  b2bOutsideQc  B2B外检  
        */
        private String qcType;

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


        private List<QcNoticeDetailDTO.@Valid AddDTO> detailList;
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

        private List< QcNoticeDetailDTO.@Valid UpdateDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 质检仓库
        */
        @NotBlank(message = "质检仓库不能为空")
        @Size(max = 255,message = "质检仓库最大长度不能超过255位")
        private String qcWarehouseId;

        /**
        * 上架仓库
        */
        @NotBlank(message = "上架仓库不能为空")
        @Size(max = 255,message = "上架仓库最大长度不能超过255位")
        private String putawayWarehouseId;

        /**
        * 质检类型 stockIn 入库质检   outsideQc 外检质检  insideQc  在库质检   newProductStockIn 新品入库质检  b2bOutsideQc  B2B外检  
        */
        @NotBlank(message = "质检类型不能为空")
        @Size(max = 32,message = "质检类型最大长度不能超过32位")
        private String qcType;

        /**
        * 备注
        */
        @Size(max = 500,message = "质检类型最大长度不能超过255位")
        private String remark;

    }
}