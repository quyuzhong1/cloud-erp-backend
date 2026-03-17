package com.erp.model.plm.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.dto.base.SuperDTO;
import com.erp.model.plm.enums.SkuStdSettingEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * sku标准零售价表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2026-03-16
*/
@Data
@NoArgsConstructor
public class SkuStdRetailPriceDTO implements Serializable {



     /**
     * 状态统计
     */
     @Data
     @NoArgsConstructor
     @AllArgsConstructor
     public static class TabListDTO {

         /**
         * 类型
         */
         private String tabFlag;
         
         /**
         * 名称
         */
        private String tabFlagName;

         /**
         * 数量
         */
         private Integer count;

     }


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
    * 分页列表
    */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * skuId
        */
        private String skuId;

        /**
        * sku
        */
        private String skuNo;
        
        
        /**
         * 产品名称
         */
         private String name;
        
        /**
         * SKU审核状态
         */
        private Integer status;
        
        /**
         * SKU审核状态名称
         */
        private String statusName;
        
        /**
         * 销售状态
         */
        private Integer saleState;
        
        /**
         * 销售状态名称
         */
        private String saleStateName;

        /**
        * 币别
        */
        private String currency;

        /**
        * 标准零售价(含税)
        */
        private BigDecimal stdRetailPriceVat;

        /**
        * 税率
        */
        private BigDecimal vatRate;
        
        /**
         * 税率，带%的
         */
        private String vatRateStr;

        /**
        * 标准零售价(不含税)
        */
        private BigDecimal stdRetailPrice;
        
        /**
         * 最近出库日期
         */
        private LocalDate lastOutstockDate;
        
        /**
         * 更新人
         */
         private String updateUserName;

        /**
        * 更新时间
        */
        private LocalDateTime updateTime;

    }


    /**
    * 导出Excel
    */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
        * 勾选的id集合
        */
        private List<String> ids;
    }

    /**
     * 分摊设置
     */
     @Data
     @NoArgsConstructor
     public static class SettingDTO {
         /**
         * 分摊类型，http://172.16.100.11:3002/project/47/interface/api/28979	type传SkuStdSetting
         */
    	 @NotNull(message = "分摊类型不能为空")
         private String skuStdSetting;
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
        * skuId
        */
        private String skuId;

        /**
        * sku编号
        */
        private String skuNo;
        
        /**
         * 产品名称
         */
         private String name;

        /**
        * 币别
        */
        private String currency;

        /**
        * 标准零售价(含税)
        */
        private BigDecimal stdRetailPriceVat;

        /**
        * 税率
        */
        private BigDecimal vatRate;

        /**
        * 标准零售价(不含税)
        */
        private BigDecimal stdRetailPrice;


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
    public static class CommonDTO extends SuperDTO {

        /**
        * skuId,参考仓位移动菜单，新增仓位移动的获取SKU下拉接口，product/detail/listSku，参数的statusList传空即可，不需要传2
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;

        /**
        * 币别，get方法，路径api/sys/currency/list，显示和提交都使用id
        */
        @NotBlank(message = "币别不能为空")
        @Size(max = 64,message = "币别最大长度不能超过64位")
        private String currency;

        /**
        * 标准零售价(含税)
        */
        @NotNull(message = "标准零售价(含税)不能为空")
        @Digits(integer = 12, fraction = 4, message = "标准零售价(含税)整数位不能超过12位，小数位不能超过4位")
        private BigDecimal stdRetailPriceVat;

        /**
        * 税率
        */
        @NotNull(message = "税率不能为空")
        @Digits(integer = 12, fraction = 4, message = "税率整数位不能超过12位，小数位不能超过4位")
        private BigDecimal vatRate;

        /**
        * 标准零售价(不含税)
        */
        @NotNull(message = "标准零售价(不含税)不能为空")
        @Digits(integer = 12, fraction = 4, message = "标准零售价(不含税)整数位不能超过12位，小数位不能超过4位")
        private BigDecimal stdRetailPrice;

        /**
         * 是否删除，内部用，前端无需处理
         */
        private Boolean isDeleted = false;
    }


}