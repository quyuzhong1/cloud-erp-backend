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
 * 库存流水重算时间范围记录请求响应实体
 * </p>
 *
 * @author cloud
 * @since 2024-08-09
*/
@Data
@NoArgsConstructor
public class InventoryFlowOverrideRecordDTO implements Serializable {




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
        * 开始时间
        */
        private LocalDateTime startTime;

        /**
        * 结束时间
        */
        private LocalDateTime endTime;

        /**
        * 库存组织id
        */
        private String inventoryOrgId;

        /**
        * 库存组织名称
        */
        private String inventoryOrgName;

        /**
        * 库存组织描述
        */
        private String inventoryOrgDesc;

        /**
        * 类型: auto=自动生成，manual=手动触发
        */
        private String type;


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
        * 开始时间
        */
        @NotNull(message = "开始时间不能为空")
        private LocalDateTime startTime;

        /**
        * 结束时间
        */
        @NotNull(message = "结束时间不能为空")
        private LocalDateTime endTime;

        /**
        * 库存组织id
        */
        @NotBlank(message = "库存组织id不能为空")
        @Size(max = 19,message = "库存组织id最大长度不能超过19位")
        private String inventoryOrgId;

        /**
        * 库存组织名称
        */
        @NotBlank(message = "库存组织名称不能为空")
        @Size(max = 100,message = "库存组织名称最大长度不能超过100位")
        private String inventoryOrgName;

        /**
        * 库存组织描述
        */
        @NotBlank(message = "库存组织描述不能为空")
        @Size(max = 255,message = "库存组织描述最大长度不能超过255位")
        private String inventoryOrgDesc;

        /**
        * 类型: auto=自动生成，manual=手动触发
        */
        @NotBlank(message = "类型: auto=自动生成，manual=手动触发不能为空")
        @Size(max = 16,message = "类型: auto=自动生成，manual=手动触发最大长度不能超过16位")
        private String type;


    }


}