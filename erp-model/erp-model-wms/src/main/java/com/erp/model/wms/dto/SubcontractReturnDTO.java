package com.erp.model.wms.dto;

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

/**
 * <p>
 * 委外退料单请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2024-09-15
*/
@Data
@NoArgsConstructor
public class SubcontractReturnDTO implements Serializable {


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
         * 明细id
         */
        private String  detailId;

        /**
        * 委外退料单号
        */
        private String code;

        /**
        * 单据审核状态
        */
        private String approveStatus;

        /**
        * 退料类型
        */
        private String type;

        /**
        * 退料日期
        */
        private LocalDateTime billDate;

        /**
        * 来源id
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
        * 作废状态（false未作废，true已作废）
        */
        private Boolean invalidStatus;

        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 作废原因
        */
        private String invalidRemark;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 审核人名称
        */
        private String approveUserName;

        /**
        * 审核人id
        */
        private String approveUserId;

        /**
        * 金蝶数据id
        */
        private String syncKingdeeId;

        /**
        * 供应商id
        */
        private String supplierId;

        /**
        * 供应商名称
        */
        private String supplierName;

        /**
        * 委外订单id
        */
        private String subcontractOrderId;

        /**
        * 委外订单编号
        */
        private String subcontractOrderCode;


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
        * 委外退料单号
        */
        private String code;

        /**
        * 单据审核状态
        */
        private String approveStatus;

        /**
        * 退料类型
        */
        private String type;

        /**
        * 退料日期
        */
        private LocalDateTime billDate;

        /**
        * 来源id
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
        * 作废状态（false未作废，true已作废）
        */
        private Boolean invalidStatus;

        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 作废原因
        */
        private String invalidRemark;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 审核人名称
        */
        private String approveUserName;

        /**
        * 审核人id
        */
        private String approveUserId;

        /**
        * 金蝶数据id
        */
        private String syncKingdeeId;

        /**
        * 供应商id
        */
        private String supplierId;

        /**
        * 供应商名称
        */
        private String supplierName;

        /**
        * 委外订单id
        */
        private String subcontractOrderId;

        /**
        * 委外订单编号
        */
        private String subcontractOrderCode;


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
        * 退料类型
        */
//        @NotBlank(message = "退料类型不能为空")
//        @Size(max = 32,message = "退料类型最大长度不能超过32位")
        private String type;

        /**
        * 退料日期
        */
        private LocalDateTime billDate;

        /**
        * 来源id
        */
//        @NotBlank(message = "来源id不能为空")
//        @Size(max = 19,message = "来源id最大长度不能超过19位")
        private String sourceId;

        /**
        * 来源编号
        */
//        @NotBlank(message = "来源编号不能为空")
//        @Size(max = 32,message = "来源编号最大长度不能超过32位")
        private String sourceCode;

        /**
        * 来源类型
        */
//        @NotBlank(message = "来源类型不能为空")
//        @Size(max = 32,message = "来源类型最大长度不能超过32位")
        private String sourceType;

        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 金蝶数据id
        */
//        @NotBlank(message = "金蝶数据id不能为空")
//        @Size(max = 19,message = "金蝶数据id最大长度不能超过19位")
        private String syncKingdeeId;

        /**
        * 供应商id
        */
        @NotBlank(message = "供应商id不能为空")
        @Size(max = 19,message = "供应商id最大长度不能超过19位")
        private String supplierId;

        /**
        * 供应商名称
        */
//        @NotBlank(message = "供应商名称不能为空")
//        @Size(max = 200,message = "供应商名称最大长度不能超过200位")
        private String supplierName;

        /**
        * 委外订单id
        */
        @NotBlank(message = "委外订单id不能为空")
        @Size(max = 19,message = "委外订单id最大长度不能超过19位")
        private String subcontractOrderId;

        /**
        * 委外订单编号
        */
//        @NotBlank(message = "委外订单编号不能为空")
//        @Size(max = 32,message = "委外订单编号最大长度不能超过32位")
        private String subcontractOrderCode;


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
        private List<SubcontractReturnDetailDTO.ListSourceDetailDTO> detailList;
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
}