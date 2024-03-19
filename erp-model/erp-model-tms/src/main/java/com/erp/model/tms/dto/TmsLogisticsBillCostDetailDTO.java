package com.erp.model.tms.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-03-19
*/
@Data
@NoArgsConstructor
public class TmsLogisticsBillCostDetailDTO implements Serializable {




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
        * 主表id
        */
        private String mainId;

        /**
        * 费用编码
        */
        private String costCode;

        /**
        * 费用名称
        */
        private String costName;

        /**
        * 费用值
        */
        private BigDecimal costValue;

        /**
        * 币别
        */
        private String currency;


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
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        private String mainId;

        /**
        * 费用编码
        */
        @NotBlank(message = "费用编码不能为空")
        @Size(max = 32,message = "费用编码最大长度不能超过32位")
        private String costCode;

        /**
        * 费用名称
        */
        @NotBlank(message = "费用名称不能为空")
        @Size(max = 100,message = "费用名称最大长度不能超过100位")
        private String costName;

        /**
        * 费用值
        */
        @NotNull(message = "费用值不能为空")
        @Digits(integer = 12, fraction = 4, message = "费用值整数位不能超过12位，小数位不能超过4位")
        private BigDecimal costValue;

        /**
        * 币别
        */
        @NotBlank(message = "币别不能为空")
        @Size(max = 32,message = "币别最大长度不能超过32位")
        private String currency;


    }


}