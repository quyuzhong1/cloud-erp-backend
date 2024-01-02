package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * B2C销售订单合并拆分关联表请求响应实体
 *
 * @author Will
 * @since 2023-08-18
*/
@Data
@NoArgsConstructor
public class SoB2cRefDTO implements Serializable {




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
        * 关联类型（拆分/合并）
        */
        private String type;

        /**
        * 来源单据id
        */
        private String sourceId;

        /**
        * 目标单据id
        */
        private String targetId;

        /**
         * 来源单据明细id
         */
        private String sourceDetailId;

        /**
         * 目标单据明细id
         */
        private String targetDetailId;
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
        * 关联类型（拆分/合并）
        */
        @NotBlank(message = "关联类型（拆分/合并）不能为空")
        @Size(max = 32,message = "关联类型（拆分/合并）最大长度不能超过32位")
        private String type;

        /**
        * 来源单据id
        */
        @NotBlank(message = "来源单据id不能为空")
        @Size(max = 19,message = "来源单据id最大长度不能超过19位")
        private String sourceId;

        /**
        * 目标单据id
        */
        @NotBlank(message = "目标单据id不能为空")
        @Size(max = 19,message = "目标单据id最大长度不能超过19位")
        private String targetId;

        /**
         * 来源单据明细id
         */
        @NotBlank(message = "来源单据明细id不能为空")
        @Size(max = 19,message = "来源单据明细id最大长度不能超过19位")
        private String sourceDetailId;

        /**
         * 目标单据明细id
         */
        @NotBlank(message = "目标单据明细id不能为空")
        @Size(max = 19,message = "目标单据明细id最大长度不能超过19位")
        private String targetDetailId;
    }


}