package com.erp.model.oms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author wtr
 * @since 2025-11-24
*/
@Data
@NoArgsConstructor
public class DeliveryBoxRuleDTO implements Serializable {


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
        * skuId
        */
        private String skuId;

        /**
        * sku编码
        */
        private String skuNo;

        /**
        * sku名称
        */
        private String productName;


        private List<DeliveryBoxRuleDetailDTO.ViewDTO> deliveryBoxRuleDetailDTOList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        @NotNull(message = "发货箱规明细不能为空")
        private List<DeliveryBoxRuleDetailDTO.AddDTO> deliveryBoxRuleDetailDTOList;
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


        @NotNull(message = "发货箱规明细不能为空")
        private List<DeliveryBoxRuleDetailDTO.UpdateDTO> deliveryBoxRuleDetailDTOList;
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
         * skuId
         */
        private String  skuId;

        /**
         * sku编码
         */
        private String  skuNo;

        /**
         * sku名称
         */
        private String  productName;

        /**
         * 发货skuId
         */
        private String  deliverySkuId;

        /**
         * 发货sku编码
         */
        private String  deliverySkuNo;

        /**
         * 发货sku名称
         */
        private String  deliveryProductName;

        /**
         * 单箱数量
         */
        private Integer  perBoxQty;

        /**
         * 优先级
         */
        private Integer sort;

        /**
         * 作废状态
         */
        private Boolean invalidStatus;

        /**
         * 作废状态名称
         */
        private String invalidStatusName;

        /**
         * 创建用户id
         */
        private String  createUserId;


        /**
         * 创建用户名称
         */
        private String  createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime  createTime;

        /**
         * 更新用户id
         */
        private String  updateUserId;

        /**
         * 更新用户名称
         */
        private String  updateUserName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;


    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        private String skuId;

        /**
         * sku编码
         */
        @NotBlank(message = "sku编码不能为空")
        private String skuNo;

        /**
        * sku名称
        */
        @NotBlank(message = "sku名称不能为空")
        private String productName;


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
    public static class SkuDTO {

        /**
         * sku编码
         */
        @NotBlank(message = "sku编码不能为空")
        private String skuNo;


    }

    @Data
    @NoArgsConstructor
    public static class ImportDTO {
        /**
         * skuId
         */
        private String skuId;
        /**
         * sku编码(相同的为一张单)
         */
        private String skuNo;
        /**
         * sku名称
         */
        private String productName;
        /**
         * 明细
         */
        private List<DeliveryBoxRuleDetailDTO.DetailImportDTO> detailImportDTOList;

    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ListBoxRuleBySkuDTO {

        /**
         * id
         */
        private String id;
        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * sku名称
         */
        private String productName;


        private List<DeliveryBoxRuleDetailDTO.ListBoxRuleBySkuDetailDTO> deliveryBoxRuleDetailDTOList;
    }

}