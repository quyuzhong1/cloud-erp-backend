package com.erp.model.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * 飞书质检通知DTO
     */
    @Data
    @NoArgsConstructor
    public static class FsQcNoticeDTO{

        /**
         * 岗位id集合,post请求,/api/sys/post/list
         */
        private List<String> postIdList;

        /**
         * 抄送人员id集合,post请求,/api/plm/common/findUserList
         */
        private List<String> userIdList;

        /**
         * 发送时间
         */
        private LocalTime sendTime;
    }
    /**
     * 发货拦截设置DTO
     */
    @Data
    @NoArgsConstructor
    public static class B2cDeliveryInterceptDTO{
        /**
         * B2C发货拦截 （组包后不允许拦截）
         */
        private Boolean b2cDeliveryIntercept;
    }
    /**
     * 组包设置DTO
     */
    @Data
    @NoArgsConstructor
    public static class PackageSettingDTO{
        /**
         * 发货物流商Id
         */
        private List<String> supplierIds;
    }

    /**
     * 中转设置DTO
     */
    @Data
    @NoArgsConstructor
    public static class TransitSettingDTO{

        /**
         * 中转仓库Id
         */
        private String warehouseId;
    }


    /**
     * 装箱完成通知
     */
    @Data
    @NoArgsConstructor
    public static class FinishPackingNoticeDTO{
        /**
         * 岗位id集合,post请求,/api/sys/post/list
         */
        private List<String> postIdList;

        /**
         * 抄送人员id集合,post请求,/api/plm/common/findUserList
         */
        private List<String> userIdList;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CfgPrintDetail {
        /**
         * 纸张大小
         */
        private String paperSize;

        /**
         * 打印机名称
         */
        private String printerName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CfgPrint {
        /**
         * 打印配置详情
         */
        private List<CfgSettingValueDTO.CfgPrintDetail> cfgPrintDetails = new ArrayList<>();
    }

}