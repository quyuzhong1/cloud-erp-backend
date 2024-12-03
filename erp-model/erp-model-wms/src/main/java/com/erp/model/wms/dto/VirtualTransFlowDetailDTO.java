package com.erp.model.wms.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 虚拟仓库存流水明细请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-12-03
*/
@Data
@NoArgsConstructor
public class VirtualTransFlowDetailDTO implements Serializable {




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
        * 数量
        */
        private Integer qty;

        /**
        * 后数量
        */
        private Integer curInventoryQty;

        /**
        * 虚拟仓流水id
        */
        private String virtualTransFlowId;

        /**
        * 虚拟仓库存明细id
        */
        private String virtualInventoryDetailId;

        /**
        * 操作时间
        */
        private LocalDateTime tradeTime;


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
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer qty;

        /**
        * 后数量
        */
        @NotNull(message = "后数量不能为空")
        private Integer curInventoryQty;

        /**
        * 虚拟仓流水id
        */
        @NotBlank(message = "虚拟仓流水id不能为空")
        @Size(max = 255,message = "虚拟仓流水id最大长度不能超过255位")
        private String virtualTransFlowId;

        /**
        * 虚拟仓库存明细id
        */
        @NotBlank(message = "虚拟仓库存明细id不能为空")
        @Size(max = 255,message = "虚拟仓库存明细id最大长度不能超过255位")
        private String virtualInventoryDetailId;

        /**
        * 操作时间
        */
        private LocalDateTime tradeTime;


    }


}