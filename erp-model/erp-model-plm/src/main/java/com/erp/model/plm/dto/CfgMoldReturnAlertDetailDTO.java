package com.erp.model.plm.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
 * <p>
 * 模具返回策略明细请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-10-15
*/
@Data
@NoArgsConstructor
public class CfgMoldReturnAlertDetailDTO implements Serializable {




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
        * 明细备注
        */
        private String remark;

        /**
        * main_id
        */
        private String mainId;

        /**
        * 返还数量上限
        */
        private Integer returnQtyLimit;

        /**
        * 返回金额
        */
        private BigDecimal returnPrice;


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
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 明细备注
        */
        @Size(max = 200,message = "明细备注最大长度不能超过200位")
        private String remark;

        /**
        * main_id
        */
        private String mainId;

        /**
        * 返还数量上限
        */
        @NotNull(message = "返还数量上限不能为空")
        @Min(value = 1,message = "返还数量上限不能小于1")
        private Integer returnQtyLimit;

        /**
        * 返回金额
        */
        @NotNull(message = "返回金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "返回金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal returnPrice;


    }


}