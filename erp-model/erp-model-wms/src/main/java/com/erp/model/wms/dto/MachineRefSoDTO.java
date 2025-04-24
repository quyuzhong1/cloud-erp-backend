package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 加工单和销售订单关联表请求响应实体
 * </p>
 *
 * @author will
 * @since 2023-12-06
*/
@Data
@NoArgsConstructor
public class MachineRefSoDTO implements Serializable {




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
        * 销售订单明细id
        */
        private String soDetailId;

        /**
        * 销售订单id
        */
        private String soId;

        /**
         * 销售订单编码
         */
        private String soCode;

        /**
        * 加工单明细id
        */
        private String machineDetailId;

        /**
        * 加工单id
        */
        private String machineId;


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
        * 销售订单明细id
        */
        @NotBlank(message = "销售订单明细id不能为空")
        @Size(max = 19,message = "销售订单明细id最大长度不能超过19位")
        private String soDetailId;

        /**
        * 销售订单id
        */
        @NotBlank(message = "销售订单id不能为空")
        @Size(max = 19,message = "销售订单id最大长度不能超过19位")
        private String soId;

        /**
         * 销售订单编码
         */
        @NotBlank(message = "销售订单编码不能为空")
        @Size(max = 19,message = "销售订单编码最大长度不能超过32位")
        private String soCode;

        /**
        * 加工单明细id
        */
        @NotBlank(message = "加工单明细id不能为空")
        @Size(max = 19,message = "加工单明细id最大长度不能超过19位")
        private String machineDetailId;

        /**
        * 加工单id
        */
        @NotBlank(message = "加工单id不能为空")
        @Size(max = 19,message = "加工单id最大长度不能超过19位")
        private String machineId;


    }


}