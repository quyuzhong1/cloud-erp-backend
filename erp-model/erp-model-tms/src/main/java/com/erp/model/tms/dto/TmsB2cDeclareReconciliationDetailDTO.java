package com.erp.model.tms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.erp.model.tms.enums.TmsB2cDeclareReconciliationImportEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * b2c报关对账单明细请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-03-19
*/
@Data
@NoArgsConstructor
public class TmsB2cDeclareReconciliationDetailDTO implements Serializable {


    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

    }

    /**
     * 列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {


        /**
         * 主键id
         */
        private String  id;

        /**
         * 主表id
         */
        private String mainId;

        /**
         * 对账单编码
         */
        private String code;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源编码
         */
        private String sourceCode;

        /**
         * 销售订单id
         */
        private String soId;

        /**
         * 销售订单明细id
         */
        private String soDetailId;

        /**
         * 销售订单编码
         */
        private String soCode;

        /**
         * 入库预报日期
         */
        private LocalDate date;

        /**
         * 订单预报状态
         */
        private String instockForecastStatus;

        /**
         * 订单预报状态名称
         */
        private String instockForecastStatusName;

        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 国家
         */
        private String country;

        /**
         * 国家名称
         */
        private String countryName;

        /**
         * 物流渠道id
         */
        private String logisticsChannelId;

        /**
         * 物流渠道名称
         */
        private String logisticsChannelName;

        /**
         * 产品数量
         */
        private Integer qty;

        /**
         * 预估重量
         */
        private BigDecimal estimateWeight;

        /**
         * 预估重量单位
         */
        private String estimateWeightUnit;

        /**
         * 实际重量
         */
        private BigDecimal actualWeight;

        /**
         * 实际重量单位
         */
        private String actualWeightUnit;

        /**
         * 实际计费重
         */
        private BigDecimal actualBillingWeight;

        /**
         * 实际物流运费
         */
        private BigDecimal actualShippingCost;

        /**
         * 实际报关费
         */
        private BigDecimal actualDeclareCost;

        /**
         * 实际其他费
         */
        private BigDecimal actualOtherCost;

        /**
         * 备注
         */
        private String remark;

        /**
         * 对账状态
         */
        private String status;

        /**
         * 对账状态名称
         */
        private String statusName;

    }


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
        * 来源id
        */
        private String sourceId;

        /**
        * 来源明细id
        */
        private String sourceDetailId;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 来源编码
        */
        private String sourceCode;

        /**
        * 销售订单id
        */
        private String soId;

        /**
        * 销售订单明细id
        */
        private String soDetailId;

        /**
        * 销售订单编码
        */
        private String soCode;

        /**
        * 入库预报日期
        */
        private LocalDate date;

        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
        * 国家
        */
        private String country;
        /**
         * 国家名称
         */
        private String countryName;
        /**
        * 物流渠道id
        */
        private String logisticsChannelId;

        /**
        * 物流渠道名称
        */
        private String logisticsChannelName;

        /**
        * 产品数量
        */
        private Integer qty;

        /**
        * 预估重量
        */
        private BigDecimal estimateWeight;

        /**
        * 预估重量单位
        */
        private String estimateWeightUnit;

        /**
        * 实际重量
        */
        private BigDecimal actualWeight;

        /**
        * 实际重量单位
        */
        private String actualWeightUnit;

        /**
        * 实际计费重
        */
        private BigDecimal actualBillingWeight;

        /**
        * 实际物流运费
        */
        private BigDecimal actualShippingCost;

        /**
        * 实际报关费
        */
        private BigDecimal actualDeclareCost;

        /**
        * 实际其他费
        */
        private BigDecimal actualOtherCost;

        /**
         * 币别符号
         */
        private String currencySymbol;

        /**
        * 备注
        */
        private String remark;

        /**
        * 对账状态,/tms/drop/down/dict/list?key=declareReconciliationStatus
        */
        private String status;


        /**
         * 对账状态名称
         */
        private String statusName;

        /**
        * 确认时间
        */
        private LocalDate confirmDate;

        /**
        * 确认人id
        */
        private String confirmUserId;

        /**
        * 确认人名称
        */
        private String confirmUserName;
        
        /**
         * 实际物流运费币别
         */
         private String actualShippingCurrency;

         /**
         * 实际报关费币别
         */
         private String actualDeclareCurrency;

         /**
         * 实际其他费币别
         */
         private String actualOtherCurrency;
         /**
          * 实际物流运费币别符号
          */
         private String actualShippingCurrencySymbol;
         
         /**
          * 实际报关费币别符号
          */
         private String actualDeclareCurrencySymbol;
         
         /**
          * 实际其他费币别符号
          */
         private String actualOtherCurrencySymbol;
         
        /**
         * 费用编辑（导入数据返回）
         */
        private List<TmsCostDetailDTO.UpdateDTO> updateList;
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
    public static class UpdateDTO {

        /**
        * 主键id
        */
        private String id;


        /**
         * 备注
         */
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
         * 对账状态
         */
        private String status;

        /**
         * 实际重量
         */
        private BigDecimal actualWeight;

        /**
         * 实际重量单位
         */
        private String actualWeightUnit;

        /**
         * 实际计费重
         */
        private BigDecimal actualBillingWeight;
        /**
         * 实际计费重
         */
        private BigDecimal estimateWeight;
        
        /**
         * 实际计费重
         */
        private String estimateWeightUnit;
        /**
         * 实际计费重
         */
        private String logisticsChannelId;
        /**
         * 实际计费重
         */
        private String sourceDetailId;

        /**
         * 费用编辑（导入数据返回）
         */
        private List<TmsCostDetailDTO.UpdateDTO> updateList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 来源明细id
        */
        @NotBlank(message = "来源明细id不能为空")
        @Size(max = 19,message = "来源明细id最大长度不能超过19位")
        private String sourceDetailId;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 32,message = "来源类型最大长度不能超过32位")
        private String sourceType;

        /**
        * 备注
        */
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

    }
    @Data
    @NoArgsConstructor
    public static class ExportDTO {

        /**
         * 对账单id
         */
        @NotEmpty(message = "选中对账单id不能为空")
        private List<String> mainIdList;
    }


    @Data
    @NoArgsConstructor
    public static class UpdateStatusDTO {

        /**
         * 对账明细id
         */
        @NotBlank(message = "对账明细id不能为空")
        private List<String> ids;

        /**
         * 对账状态,/tms/drop/down/dict/list?key=declareReconciliationStatus
         */
        @NotBlank(message = "对账状态不能为空")
        private String status;
    }

    @Data
    @NoArgsConstructor
    public static class ExcelImportDTO {

        /**
         * 导入类型，standard标准，config配置
         */
        @NotNull(message = "导入类型不能为空")
        private TmsB2cDeclareReconciliationImportEnum typeEnum;


        /**
         * 导入文件
         */
        @NotNull(message = "导入文件不能为空")
        private MultipartFile excelFile;

        /**
         * 主表id
         */
        @NotBlank(message = "主表id")
        private String id;
    }

    @Data
    @NoArgsConstructor
    public static class ImportDTO {

        /**
         * 成功返回数据
         */
        private List<TmsB2cDeclareReconciliationDetailDTO.ViewDTO> successList;

        /**
         * 错误url
         */
        private String errorUrl;
    }

    @Data
    @NoArgsConstructor
    public static class ExcelDownloadTemplateDTO {

        /**
         * 导入类型，standard标准，config配置
         */
        @NotNull(message = "导入类型不能为空")
        private TmsB2cDeclareReconciliationImportEnum typeEnum;

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;
    }

}