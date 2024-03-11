package com.erp.model.wms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * <p>
 * 系统配置管理请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-01-08
*/
@Data
@NoArgsConstructor
public class CfgSettingValueDTO implements Serializable {




    /**
     * 委外发料单设置
     */
    @Data
    @NoArgsConstructor
    public static class SubcontractIssueSettingDTO {

        /**
        * 发料单生成类型,/wms/dict/drop/down?type=createType
        */
        @NotBlank(message = "发料单生成类型不能为空")
        private String createType;
    }

    /**
     * 采购退货单设置
     */
    @Data
    @NoArgsConstructor
    public static class PoReturnSettingDTO {

        /**
         * 是否启用退货确认
         */
        private Boolean isReturnConfirm;

        /**
         * 数量处理岗位id,/sys/post/list,get请求
         */
        private String qtyHandlePostId;

        /**
         * 其他处理岗位id,/sys/post/list,get请求
         */
        private String otherHandlePostId;
    }

    /**
     * 采购对账单设置
     */
    @Data
    @NoArgsConstructor
    public static class PoReconciliationSettingDTO {

        /**
         * 对账周期生成类型,/wms/dict/drop/down?type=reconciliationType
         */
        @NotBlank(message = "对账周期生成类型不能为空")
        private String reconciliationType;

        /**
         * 截止日期
         */
        private String endDate;
    }

    @Data
    @NoArgsConstructor
    public static class LogisticsProductDestDeclarePrice{
        /**
         * 含税采购价（>）
         */
        @Digits(integer = 12, fraction = 4, message = "含税采购价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal startPrice;
        /**
         * 含税采购价(<=)
         */
        @Digits(integer = 12, fraction = 4, message = "含税采购价整数位不能超过12位，小数位不能超过4位")
        private BigDecimal endPrice;
        /**
         * 比例
         */
        @Digits(integer = 12, fraction = 2, message = "比例整数位不能超过12位，小数位不能超过2位")
        private BigDecimal rate;
    }

}