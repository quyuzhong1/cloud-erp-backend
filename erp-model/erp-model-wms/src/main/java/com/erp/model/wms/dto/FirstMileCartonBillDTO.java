package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 发货单箱子信息明细表请求响应实体
 * </p>
 *
 * @author Luo_wg
 * @since 2023-11-16
*/
@Data
@NoArgsConstructor
public class FirstMileCartonBillDTO implements Serializable {




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
        * first_mile_carton表id
        */
        private String cartonId;

        /**
        * first_mile_carton_detail表id
        */
        private String cartonDetailId;

        /**
        * 箱号
        */
        private String boxNo;

        /**
        * 描述（sku*qty+sku*qty+...）
        */
        private String boxDesc;


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
        * first_mile_carton表id
        */
        @NotBlank(message = "first_mile_carton表id不能为空")
        @Size(max = 19,message = "first_mile_carton表id最大长度不能超过19位")
        private String cartonId;

        /**
        * first_mile_carton_detail表id
        */
        @NotBlank(message = "first_mile_carton_detail表id不能为空")
        @Size(max = 19,message = "first_mile_carton_detail表id最大长度不能超过19位")
        private String cartonDetailId;

        /**
        * 箱号
        */
        @NotBlank(message = "箱号不能为空")
        @Size(max = 19,message = "箱号最大长度不能超过19位")
        private String boxNo;

        /**
        * 描述（sku*qty+sku*qty+...）
        */
        @NotBlank(message = "描述（sku*qty+sku*qty+...）不能为空")
        @Size(max = 255,message = "描述（sku*qty+sku*qty+...）最大长度不能超过255位")
        private String boxDesc;


    }


}