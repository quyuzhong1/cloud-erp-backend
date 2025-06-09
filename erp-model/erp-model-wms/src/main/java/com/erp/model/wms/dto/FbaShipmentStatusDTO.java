package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * FBA货件状态信息请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2023-11-01
*/
@Data
@NoArgsConstructor
public class FbaShipmentStatusDTO implements Serializable {




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
        * FBA货件id
        */
        private String mainId;

        /**
        * 平台货件状态
        */
        private String platformShipmentStatus;

        /**
        * 亚马逊FBA货件单号
        */
        private String fbaShipmentId;


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
        * FBA货件id
        */
        @NotBlank(message = "FBA货件id不能为空")
        @Size(max = 19,message = "FBA货件id最大长度不能超过19位")
        private String mainId;

        /**
        * 平台货件状态
        */
        @NotBlank(message = "平台货件状态不能为空")
        @Size(max = 255,message = "平台货件状态最大长度不能超过255位")
        private String platformShipmentStatus;

        /**
        * 亚马逊FBA货件单号
        */
        @NotBlank(message = "亚马逊FBA货件单号不能为空")
        @Size(max = 19,message = "亚马逊FBA货件单号最大长度不能超过19位")
        private String fbaShipmentId;


    }


}