package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 装箱任务表请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2024-07-02
*/
@Data
@NoArgsConstructor
public class PackingTaskDTO implements Serializable {




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
        * 装箱任务编码
        */
        private String code;

        /**
        * 关联订单id
        */
        private String sourceId;

        /**
        * 关联订单编号
        */
        private String sourceCode;

        /**
        * 单据类型(B2B,FBA,third)
        */
        private String sourceType;

        /**
        * 发货数量
        */
        private Integer deliveryQty;

        /**
        * 发货仓库id
        */
        private String warehouseId;

        /**
        * 发货仓库名称
        */
        private String warehouseName;


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
        * 关联订单id
        */
        @NotBlank(message = "关联订单id不能为空")
        @Size(max = 19,message = "关联订单id最大长度不能超过19位")
        private String sourceId;

        /**
        * 关联订单编号
        */
        @NotBlank(message = "关联订单编号不能为空")
        @Size(max = 50,message = "关联订单编号最大长度不能超过50位")
        private String sourceCode;

        /**
        * 单据类型(B2B,FBA,third)
        */
        @NotBlank(message = "单据类型(B2B,FBA,third)不能为空")
        @Size(max = 30,message = "单据类型(B2B,FBA,third)最大长度不能超过30位")
        private String sourceType;

        /**
        * 发货数量
        */
        @NotNull(message = "发货数量不能为空")
        private Integer deliveryQty;

        /**
        * 发货仓库id
        */
        @NotBlank(message = "发货仓库id不能为空")
        @Size(max = 19,message = "发货仓库id最大长度不能超过19位")
        private String warehouseId;

        /**
        * 发货仓库名称
        */
        @NotBlank(message = "发货仓库名称不能为空")
        @Size(max = 50,message = "发货仓库名称最大长度不能超过50位")
        private String warehouseName;


    }


    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {
        /**
         * 装箱任务id
         */
        private String id;
        /**
         * 装箱任务编码
         */
        private String code;
        /**
         * 关联订单id
         */
        private String sourceId;

        /**
         * 关联订单编号
         */
        private String sourceCode;

        /**
         * 单据类型(B2B,FBA,third)
         * PickingSourceTypeEnum
         */
        private String sourceType;

        /**
         * 发货数量
         */
        private Integer deliveryQty;

        /**
         * 发货仓库id
         */
        private String warehouseId;

        /**
         * 发货仓库名称
         */
        private String warehouseName;
    }

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
        private Map<String, String> sqlMap;
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

    @Data
    @NoArgsConstructor
    public static class TypeCountDTO {
        /**
         * 类型
         */
        private String type;

        /**
         * 数量
         */
        private Integer count;
    }
}