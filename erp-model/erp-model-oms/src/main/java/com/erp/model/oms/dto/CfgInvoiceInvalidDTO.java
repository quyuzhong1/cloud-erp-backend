package com.erp.model.oms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 作废发票号请求响应实体
 * </p>
 *
 * @author hcg
 * @since 2025-04-14
*/
@Data
@NoArgsConstructor
public class CfgInvoiceInvalidDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
         * 发票账号
         */
        private String  cfgInvoiceSettingCompanyName;

        /**
        * 发票设置id
        */
        private String cfgInvoiceSettingId;

        /**
        * 序列号
        */
        private String no;
        /**
         * 起始发票号
         */
        private String startInvoiceNo;
        /**
         * 截止发票号
         */
        private String endInvoiceNo;
        /**
        * 作废发票号
        */
        private String deactivateInvoiceNo;

        /**
        * 作废原因
        */
        private String reason;

        /**
         * 作废时间
         */
        private LocalDateTime updateTime;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 发票设置id
        */
        @NotBlank(message = "发票设置id不能为空")
        @Size(max = 19,message = "发票设置id最大长度不能超过19位")
        private String cfgInvoiceSettingId;

        /**
        * 序列号
        */
        @NotBlank(message = "序列号不能为空")
        @Size(max = 50,message = "序列号最大长度不能超过50位")
        private String no;

        /**
        * 起始发票号
        */
        @NotBlank(message = "起始发票号不能为空")
        @Size(max = 50,message = "起始发票号最大长度不能超过50位")
        private String startInvoiceNo;

        /**
        * 截止发票号
        */
        @NotBlank(message = "截止发票号不能为空")
        @Size(max = 50,message = "截止发票号最大长度不能超过50位")
        private String endInvoiceNo;

        /**
        * 作废原因
        */
        @NotBlank(message = "作废原因不能为空")
        @Size(max = 255,message = "作废原因最大长度不能超过255位")
        private String reason;


    }

    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList = new ArrayList<>();
        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;
    }

    @Data
    @NoArgsConstructor
    public static class DropDownDTO extends SortDTO {
        /**
         * 发票设置companyName
         */
        private String code;

        /**
         * 发票设置id
         */
        private String value;

        /**
         * disabled
         */
        private Boolean disabled;
    }
}