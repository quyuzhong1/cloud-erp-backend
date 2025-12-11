package com.erp.model.oms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * KOL回片费用表请求响应实体
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-01
*/
@Data
@NoArgsConstructor
public class KolFeedbackCostDTO implements Serializable {

    /**
     * 分页查询参数
     */
    @Data
    @NoArgsConstructor
    public static class ParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        /**
         * 权限SQL
         */
        private String permissionSql;
        /**
         * 勾选的id集合
         */
        private List<String> ids;


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
        private String id;

        /**
         * 回片链接（完整链接）
         */
        private String url;

        /**
         * 回片链接哈希值（MD5或SHA256，用于唯一键）
         */
        private String urlHash;

        /**
         * 费用名称
         */
        private String costType;

        /**
         * 费用名称ID
         */
        private String costTypeId;

        /**
         * 付费币别
         */
        private String currency;

        /**
         * 付费币别名称
         */
        private String currencyName;

        /**
         * 汇率
         */
        private BigDecimal exchangeRate;

        /**
         * 金额（原币）
         */
        private BigDecimal originalAmount;

        /**
         * 金额（原币）显示（symbol + 金额）
         */
        private String originalAmountDisplay;

        /**
         * 金额（本位币）
         */
        private BigDecimal baseAmount;

        /**
         * 金额（本位币）显示（CNY symbol + 金额）
         */
        private String baseAmountDisplay;

        /**
         * 备注
         */
        private String remark;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 创建人
         */
        private String createUserName;

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
        * 回片链接（完整链接）
        */
        private String url;

        /**
        * 回片链接哈希值（MD5或SHA256，用于唯一键）
        */
        private String urlHash;

        /**
        * 费用名称
        */
        private String costType;

        /**
        * 费用名称ID
        */
        private String costTypeId;

        /**
        * 付费币别
        */
        private String currency;

        /**
        * 汇率
        */
        private BigDecimal exchangeRate;

        /**
        * 金额（原币）
        */
        private BigDecimal originalAmount;

        /**
        * 金额（本位币）
        */
        private BigDecimal baseAmount;

        /**
        * 备注
        */
        private String remark;


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

    /**
    * 批量新增
    */
    @Data
    @NoArgsConstructor
    public static class BatchAddDTO {

        /**
        * 新增列表
        */
        @NotNull(message = "新增列表不能为空")
        @Size(min = 1, message = "至少需要一条数据")
        private java.util.List<AddDTO> list;

    }

    /**
    * 批量修改
    */
    @Data
    @NoArgsConstructor
    public static class BatchUpdateDTO {

        /**
        * 修改列表
        */
        @NotNull(message = "修改列表不能为空")
        @Size(min = 1, message = "至少需要一条数据")
        private java.util.List<UpdateDTO> list;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 回片链接（完整链接）
        */
        @NotBlank(message = "回片链接不能为空")
        private String url;

        /**
        * 回片链接哈希值（MD5或SHA256，用于唯一键）
        */
        @Size(max = 64,message = "回片链接哈希值（MD5或SHA256，用于唯一键）最大长度不能超过64位")
        private String urlHash;

        /**
        * 费用名称
        */
        @Size(max = 100,message = "费用名称最大长度不能超过100位")
        private String costType;

        /**
        * 费用名称ID
        */
        @NotBlank(message = "费用名称ID不能为空")
        @Size(max = 19,message = "费用名称ID最大长度不能超过19位")
        private String costTypeId;

        /**
        * 付费币别
        */
        @NotBlank(message = "付费币别不能为空")
        @Size(max = 30,message = "付费币别最大长度不能超过30位")
        private String currency;

        /**
        * 汇率
        */
        @NotNull(message = "汇率不能为空")
        @Digits(integer = 10, fraction = 6, message = "汇率整数位不能超过10位，小数位不能超过6位")
        private BigDecimal exchangeRate;

        /**
        * 金额（原币）
        */
        @NotNull(message = "金额（原币）不能为空")
        @Digits(integer = 12, fraction = 6, message = "金额（原币）整数位不能超过12位，小数位不能超过6位")
        private BigDecimal originalAmount;

        /**
        * 金额（本位币）
        */
        @Digits(integer = 12, fraction = 6, message = "金额（本位币）整数位不能超过12位，小数位不能超过6位")
        private BigDecimal baseAmount;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;


    }


}