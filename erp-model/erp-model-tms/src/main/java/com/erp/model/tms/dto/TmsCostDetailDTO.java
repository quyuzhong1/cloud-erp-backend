package com.erp.model.tms.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 自发货费用明细请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-03-20
*/
@Data
@NoArgsConstructor
public class TmsCostDetailDTO implements Serializable {





    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class CostCompareDTO {

        /**
         * 预估的主键id
         */
        private String id;

        /**
         * 预估的主表id
         */
        private String mainId;

        /**
         * 系统配置id
         */
        private String cfgCostId;

        /**
         * 费用编码
         */
        private String costCode;

        /**
         * 费用名称
         */
        private String costName;

        /**
         * 预估费用
         */
        private BigDecimal estimatedFee;

        /**
         * 实际费用
         */
        private BigDecimal actualFee;

        /**
         * 差异
         */
        private BigDecimal feeDifference;

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

        /**
        * 汇率
        */
        private BigDecimal exchangeRate;

        /**
        * 费用设置id
        */
        private String cfgCostId;


    }
    
    /**
     *  费用明细
     */
    @Data
    @NoArgsConstructor
    public static class DetailDTO {
    	
    	/**
    	 * 费用类型id  http://172.16.100.11:3002/project/128/interface/api/31391  dictCostAttribution=selfDeliver
    	 */
    	@NotNull(message = "费用类型不能为空")
    	private String cfgCostId;
    	
    	/**
    	 * 预估金额
    	 */
    	private BigDecimal estimatedValue;
    	
    	/**
    	 * 实际金额
    	 */
    	@NotNull(message = "实际金额不能为空")
    	private BigDecimal costValue;
    	
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
    @EqualsAndHashCode(callSuper = true)
    public static class UpdateDTO extends CommonDTO implements Serializable{

        /**
        * 主键id
        */
        private String id;

        /**
         * 当前是否已更新
         */
        private boolean hasUpdate;

        /**
         * 费用分类（字典dictCostCategory）
         */
        private String dictCostCategory;

        /**
         * 来源类型
         */
        private String sourceType;

        public UpdateDTO(String id, String dictCostCategory, BigDecimal costValue, String cfgCostId, String type, String sourceType) {
            this.id = id;
            this.hasUpdate = false;
            this.dictCostCategory = dictCostCategory;
            this.sourceType = sourceType;
            super.costValue = costValue;
            super.cfgCostId = cfgCostId;
            super.type = type;
        }
    }


    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 费用值
        */
        @NotNull(message = "费用值不能为空")
        @Digits(integer = 12, fraction = 4, message = "费用值整数位不能超过12位，小数位不能超过4位")
        private BigDecimal costValue;

        /**
        * 费用设置id
        */
        @NotBlank(message = "费用设置id不能为空")
        @Size(max = 19,message = "费用设置id最大长度不能超过19位")
        private String cfgCostId;

        /**
         * 类型（estimated预估、actual实际）
         */
        private String type;

        /**
         * 来源类型，SourceTypeEnum枚举
         */
        private String sourceType;
    }


    /**
     * 费用信息
     */
    @Data
    @NoArgsConstructor
    public static class CostViewDTO {

        /**
         * 主表id
         */
        private String id;

        /**
         * 主表id
         */
        private String mainId;

        /**
         * 费用归属（字典dictCostAttribution）
         */
        private String dictCostAttribution;

        /**
         * 费用分类（字典dictCostCategory）
         */
        private String dictCostCategory;

        /**
         * 费用名称
         */
        private String costName;
        /**
         * 费用ID
         */
        private String cfgCostId;

        /**
         * 费用值
         */
        private BigDecimal costValue;

        /**
         * 币别
         */
        private String currency;

        /**
         * 类型
         */
        private String type;
    }
}