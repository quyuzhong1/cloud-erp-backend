package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * FBA库存预留信息请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2023-11-01
*/
@Data
@NoArgsConstructor
public class FbaInventoryReservedDTO implements Serializable {




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
        * FBA库存id
        */
        private String mainId;

        /**
        * 亚马逊FBA货件单号
        */
        private String fbaShipmentId;

        /**
        * 待调仓数量
        */
        private Integer reservedTransfersQty;

        /**
        * 调仓中数量
        */
        private Integer reservedProcessingQty;

        /**
        * 买家订单数量
        */
        private Integer reservedOrderQty;


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
        * FBA库存id
        */
        @NotBlank(message = "FBA库存id不能为空")
        @Size(max = 19,message = "FBA库存id最大长度不能超过19位")
        private String mainId;

        /**
        * 亚马逊FBA货件单号
        */
        @NotBlank(message = "亚马逊FBA货件单号不能为空")
        @Size(max = 19,message = "亚马逊FBA货件单号最大长度不能超过19位")
        private String fbaShipmentId;

        /**
        * 待调仓数量
        */
        @NotNull(message = "待调仓数量不能为空")
        private Integer reservedTransfersQty;

        /**
        * 调仓中数量
        */
        @NotNull(message = "调仓中数量不能为空")
        private Integer reservedProcessingQty;

        /**
        * 买家订单数量
        */
        @NotNull(message = "买家订单数量不能为空")
        private Integer reservedOrderQty;


    }


}