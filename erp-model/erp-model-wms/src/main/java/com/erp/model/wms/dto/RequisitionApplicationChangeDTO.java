package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 要货申请变更单请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-11-18
*/
@Data
@NoArgsConstructor
public class RequisitionApplicationChangeDTO implements Serializable {


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductDTO {

        /**
         * 要货申请明细id
         */
        private String requisitionDetailId;

        /**
         * 第三方sku,MSKU
         */
        private String platformSku;

        /**
         * 第三方,平台产品名称
         */
        private String platformSkuName;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku
         */
        private String skuNo;

        /**
         * 品名
         */
        private String productName;

        /**
         * FNSKU
         */
        private String fnSku;

        /**
         * ASIN
         */
        private String asin;

        /**
         * 变更类型
         */
        private String changeType;
        /**
         * 变更类型名称
         */
        private String changeTypeName;
        /**
         * 原要货数量
         */
        private Integer originRequisitionQty;

        /**
         * bom版本
         */
        private String bomVersion;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
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
         * 要货申请id
         */
        @NotBlank(message = "要货申请id不能为空")
        private String requisitionId;

        /**
         * msku, 平台sku,快粘贴传
         */
        private List<String> platformSkuNoList;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExistDTO{
        private String id;
        private String code;
        private String skuId;
        private String skuNo;
    }

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
        * 主键id
        */
        private String  id;

        /**
         * 明细id
         */
        private String  detailId;
        /**
        * 单据编号
        */
        private String code;

        /**
         * 单据类型
         */
        private String type;

        /**
         * 变更类型
         */
        private String changeType;

        /**
         * 变更类型name
         */
        private String changeTypeName;
        /**
        * 审核状态 
        */
        private String approveStatus;

        /**
        * 来源单号
        */
        private String sourceCode;

        /**
        * 来源Id
        */
        private String sourceId;

        /**
        * 业务单号
        */
        private String businessCode;

        /**
        * 业务id
        */
        private String businessId;

        /**
        * 作废状态
        */
        private Boolean invalidStatus;

        /**
        * 审核人id
        */
        private String approveUserId;

        /**
        * 审核人名称
        */
        private String approveUserName;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

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
         * 第三方sku,MSKU,平台sku
         */
        private String platformSku;

        /**
         * 第三方,平台产品名称
         */
        private String platformSkuName;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku
         */
        private String skuNo;

        /**
         * 品名
         */
        private String productName;

        /**
         * FNSKU
         */
        private String fnSku;

        /**
         * ASIN，平台产品id
         */
        private String asin;

        /**
         * 原要货数量
         */
        private Integer originRequisitionQty;
        /**
         * 变更后要货数量
         */
        private Integer newRequisitionQty;
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ViewIdDTO {
        /**
         * 要货申请id 或者是要货申请变更id
         */
        @NotBlank(message = "id不能为空")
        private String id;

        /**
         * 要货申请单明细id
         */
        private List<String> detailIds;
        /**
         * 类型，pushDown：下推, edit：编辑
         */
        @NotBlank(message = "类型不能为空")
        private String type;
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
        private String id;

        /**
         * 变更单code
         */
        private String code;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源单号code
         */
        private String sourceCode;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 业务id
         */
        private String businessId;

        /**
         * 业务code
         */
        private String businessCode;
        /**
        * 单据状态
        */
        private String approveStatus;

        /**
         * 单据状态Name
         */
        private String approveStatusName;

        /**
        * 发货计划单号
        */
        private String deliveryPlanCode;

        /**
        * 单据类型
        */
        private String type;

        /**
         * 单据类型名称
         */
        private String typeName;

        /**
        * 店铺/目的仓库
        */
        private String channelId;

        /**
         * 店铺/目的仓库 名称
         */
        private String channelName;

        /**
         * 要货仓库id
         */
        private String requisitionWarehouseId;
        /**
         * 要货仓库中文名
         */
        private String requisitionWarehouseName;

        /**
         * 明细
         */
        @Valid
        private List<RequisitionApplicationChangeDTO.ViewDetailDTO> viewDetailList;

    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class ViewDetailDTO {

        /**
         * 明细id
         */
        private String detailId;

        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 要货申请明细id
         */
        private String requisitionDetailId;

        /**
         * 第三方sku,MSKU,平台sku
         */
        private String platformSku;

        /**
         * 第三方,平台产品名称
         */
        private String platformSkuName;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku
         */
        private String skuNo;

        /**
         * 品名
         */
        private String productName;

        /**
         * FNSKU
         */
        private String fnSku;

        /**
         * ASIN，平台产品id
         */
        private String asin;

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
         * 原要货数量
         */
        private Integer originRequisitionQty;
        /**
         * 变更后要货数量
         */
        @Min(value = 1, message = "变更后要货数量最少为1")
        @NotNull(message = "变更后数量不能为空")
        private Integer newRequisitionQty;
        /**
         * 备注
         */
        private String remark;

        /**
         * bom版本
         */
        private String bomVersion;

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
        * 业务单号
        */
        @NotBlank(message = "业务单号不能为空")
        @Size(max = 50,message = "业务单号最大长度不能超过50位")
        private String businessCode;

        /**
        * 业务id
        */
        @NotBlank(message = "业务id不能为空")
        @Size(max = 50,message = "业务id最大长度不能超过50位")
        private String businessId;


    }

}