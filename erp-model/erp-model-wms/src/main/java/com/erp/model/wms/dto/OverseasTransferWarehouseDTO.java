package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 海外仓签收记录请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
*/
@Data
@NoArgsConstructor
public class OverseasTransferWarehouseDTO implements Serializable {




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
        * 平台类型
        */
        private String dictPlatform;

        /**
        * 平台仓库代号
        */
        private String platformWarehouseCode;

        /**
        * 名称
        */
        private String name;

        /**
        * 平台海外仓状态
        */
        private String platformStatus;


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
        * 平台类型
        */
        @NotBlank(message = "平台类型不能为空")
        @Size(max = 30,message = "平台类型最大长度不能超过30位")
        private String dictPlatform;

        /**
        * 平台仓库代号
        */
        @NotBlank(message = "平台仓库代号不能为空")
        @Size(max = 30,message = "平台仓库代号最大长度不能超过30位")
        private String platformWarehouseCode;

        /**
        * 名称
        */
        @NotBlank(message = "名称不能为空")
        @Size(max = 255,message = "名称最大长度不能超过255位")
        private String name;

        /**
        * 平台海外仓状态
        */
        @NotBlank(message = "平台海外仓状态不能为空")
        @Size(max = 30,message = "平台海外仓状态最大长度不能超过30位")
        private String platformStatus;


    }


}