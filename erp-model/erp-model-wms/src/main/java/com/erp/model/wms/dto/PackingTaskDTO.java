package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotEmpty;
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
         * 装箱任务编码【可排序】
         */
        private String code;
        /**
         * 关联订单id
         */
        private String sourceId;

        /**
         * 关联订单编号【可排序】
         */
        private String sourceCode;

        /**
         * 单据类型(B2B,FBA,third) 【可排序】
         * PickingSourceTypeEnum
         */
        private String sourceType;
        /**
         * 单据类型名称
         */
        private String sourceTypeName;
        /**
         * 装箱状态 unpacked：待装箱，packing 装箱中，packed：已装箱
         * 枚举：PackingTaskStatusEnum
         */
        private String packingStatus;
        /**
         * 装箱状态名称
         */
        private String packingStatusName;
        /**
         * 称重状态-单箱(unweighed 未称重,success 称重成功,fail 称重失败 )
         * PackingWeightStatusEnum
         * 字典接口地址
         */
        private String weightingStatus;
        /**
         * 称重状态-全部 名称
         */
        private String weightingStatusName;
        /**
         * 发货数量 【可排序】
         * 取值关联发货单/发货通知单的发货数量
         */
        private Integer deliveryQty;
        /**
         * 拣货数量
         * 取值关联发货单的拣货单的拣货数量
         */
        private Integer pickedQty;
        /**
         * 已装箱数量
         * 取值实际装箱数量，初始为0
         */
        private Integer packedQty;
        /**
         * 装箱总重（kg）
         * 取值实际装箱更新重量
         */
        private BigDecimal packageWeight;
        /**
         * 装箱总重（kg） [导出使用]
         */
        private String packageWeightStr;
        /**
         * 异常原因
         */
        private String errorMsg;

        /**
         * 发货仓库id
         */
        private String warehouseId;

        /**
         * 发货仓库名称 【可排序】
         */
        private String warehouseName;
        /**
         * 创建人 【可排序】
         * 取值单据创建人，系统生成显示为system
         */
        private String createUserName;
        /**
         * 创建时间时间 【可排序】
         */
        private LocalDateTime createTime;
        /**
         * 产品种类
         */
        private Integer productNum;
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


        /**
         * 关联单号
         */
        private String sourceCode;
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

    /**
     * 装箱状态汇总
     */
    @Data
    @NoArgsConstructor
    public static class StatusDTO {
        private String id;
        private String sourceCode;
        /**
         * 装箱状态 unpacked：待装箱，packing 装箱中，packed：已装箱
         * 枚举：PackingTaskStatusEnum
         */
        private String packingStatus;
        /**
         * 称重状态-全部(unweighed 未称重,weighing 部分称重,weighed 全部称重 )
         * PackingWeightStatusEnum
         * 字典接口地址
         */
        private String weightingStatus;

        /**
         * 异常原因
         */
        private String errorMsg;
        /**
         * 已装箱数量
         */
        private Integer packedQty;
        /**
         * 拣货数量
         */
        private Integer pickQty;
        /**
         * 装箱重量（设备更新）
         */
        private BigDecimal packingWeight;
    }

    /**
     * 导出Excel
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends FirstMileDeliveryDTO.PagingParamDTO {
        /**
         * 勾选的id集合
         */
        private List<String> ids;
    }

    @Data
    @NoArgsConstructor
    public static class DetailDTO {
        private String taskId;
        private String skuId;
        private String skuNo;
        /**
         * 发货数量
         */
        private Integer deliveryQty;
    }

    @Data
    @NoArgsConstructor
    public static class ProductNum {
        private String taskId;
        /**
         * 产品种类
         */
        private Integer productNum;
    }
    @Data
    @NoArgsConstructor
    public static class SearchSourceCodeDTO {
        /**
         * 搜索单号（支持箱号/关联单号/任务单号）
         */
        private String searchKeyword;
    }

    @Data
    @NoArgsConstructor
    public static class PackingTreeDTO {
        private String taskId;
        private String sourceId;
        private String sourceCode;
    }

    /**
     * 已装箱明细查询
     */
    @Data
    @NoArgsConstructor
    public static class PackedDetailDTO extends PermissionsDTO {
        /**
         * 任务id
         */
        @NotBlank(message = "任务id不能为空")
        private String taskId;
        /**
         * 装箱id
         */
        private List<String> cartonIds;
    }
}