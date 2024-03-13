package com.erp.model.wms.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * <p>
 * 库存关账记录表请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2023-10-12
*/
@Data
@NoArgsConstructor
public class InventoryClosedRecordDTO implements Serializable {




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
        * 关账日期
        */
        private LocalDate closedDate;

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


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
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
        * 关账日期
        */
        private LocalDate closedDate;

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


    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClosedParamDTO {

        /**
         * 组织id
         */
        @NotBlank(message = "组织id不能为空")
        private String orgId;

        /**
         * 单据日期
         */
        @NotNull(message = "单据日期不能为空")
        private LocalDate billDate;
    }

}