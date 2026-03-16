package com.erp.model.plm.dto;

import java.math.BigDecimal;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import com.common.business.dto.base.SuperDTO;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;
import javax.validation.constraints.Digits;

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
        * sku编号
        */
        private String skuNo;

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


        /**
        * 审核状态名称
        */
        private String approveStatusName;


        /**
        * 创建时间
        */
        private LocalDateTime createTime;

        /**
        * 创建人名称
        */
        private String createUserName;

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
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;

        /**
        * 币别
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


    }


}