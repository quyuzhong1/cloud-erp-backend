package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * FBA调拨发货请求响应实体
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-29
*/
@Data
@NoArgsConstructor
public class DmpShipmentDTO implements Serializable {




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
        * 申报id
        */
        private String shippNo;

        /**
        * 批次
        */
        private String batchNo;

        /**
        * 申报名称
        */
        private String shippName;

        /**
        * 货件状态
        */
        private String shipmentStatus;

        /**
        * 1:等待发货;2:已发货;3:已签收;4已删除
        */
        private String status;

        /**
        * 发货时间
        */
        private String expressTime;

        /**
        * 备注
        */
        private String content;

        /**
        * 发货仓库id
        */
        private String warehouseId;

        /**
        * 发货仓库编码
        */
        private String warehouseCode;

        /**
        * 目的仓库id(fba仓库)
        */
        private String fbaWarehouseId;

        /**
        * 货件编号
        */
        private String shipmentId;

        /**
        * 是否完结1完结2未完结
        */
        private Integer isOver;

        /**
        * 完结时间
        */
        private String overTime;

        /**
        * 城市编码
        */
        private String cityCode;

        /**
        * 始发港编码
        */
        private String startportCode;

        /**
        * 目的港编码
        */
        private String endportCode;


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
        * 申报id
        */
        @NotBlank(message = "申报id不能为空")
        @Size(max = 64,message = "申报id最大长度不能超过64位")
        private String shippNo;

        /**
        * 批次
        */
        @NotBlank(message = "批次不能为空")
        @Size(max = 64,message = "批次最大长度不能超过64位")
        private String batchNo;

        /**
        * 申报名称
        */
        @NotBlank(message = "申报名称不能为空")
        @Size(max = 255,message = "申报名称最大长度不能超过255位")
        private String shippName;

        /**
        * 货件状态
        */
        @NotBlank(message = "货件状态不能为空")
        @Size(max = 32,message = "货件状态最大长度不能超过32位")
        private String shipmentStatus;

        /**
        * 1:等待发货;2:已发货;3:已签收;4已删除
        */
        @NotBlank(message = "1:等待发货;2:已发货;3:已签收;4已删除不能为空")
        @Size(max = 32,message = "1:等待发货;2:已发货;3:已签收;4已删除最大长度不能超过32位")
        private String status;

        /**
        * 发货时间
        */
        @NotBlank(message = "发货时间不能为空")
        @Size(max = 32,message = "发货时间最大长度不能超过32位")
        private String expressTime;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String content;

        /**
        * 发货仓库id
        */
        @NotBlank(message = "发货仓库id不能为空")
        @Size(max = 64,message = "发货仓库id最大长度不能超过64位")
        private String warehouseId;

        /**
        * 发货仓库编码
        */
        @NotBlank(message = "发货仓库编码不能为空")
        @Size(max = 64,message = "发货仓库编码最大长度不能超过64位")
        private String warehouseCode;

        /**
        * 目的仓库id(fba仓库)
        */
        @NotBlank(message = "目的仓库id(fba仓库)不能为空")
        @Size(max = 64,message = "目的仓库id(fba仓库)最大长度不能超过64位")
        private String fbaWarehouseId;

        /**
        * 货件编号
        */
        @NotBlank(message = "货件编号不能为空")
        @Size(max = 100,message = "货件编号最大长度不能超过100位")
        private String shipmentId;

        /**
        * 是否完结1完结2未完结
        */
        @NotNull(message = "是否完结1完结2未完结不能为空")
        private Integer isOver;

        /**
        * 完结时间
        */
        @NotBlank(message = "完结时间不能为空")
        @Size(max = 32,message = "完结时间最大长度不能超过32位")
        private String overTime;

        /**
        * 城市编码
        */
        @NotBlank(message = "城市编码不能为空")
        @Size(max = 64,message = "城市编码最大长度不能超过64位")
        private String cityCode;

        /**
        * 始发港编码
        */
        @NotBlank(message = "始发港编码不能为空")
        @Size(max = 64,message = "始发港编码最大长度不能超过64位")
        private String startportCode;

        /**
        * 目的港编码
        */
        @NotBlank(message = "目的港编码不能为空")
        @Size(max = 64,message = "目的港编码最大长度不能超过64位")
        private String endportCode;


    }


}