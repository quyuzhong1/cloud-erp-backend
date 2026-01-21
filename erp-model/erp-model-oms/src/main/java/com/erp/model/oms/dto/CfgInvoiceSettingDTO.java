package com.erp.model.oms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author hcg
 * @since 2025-04-09
 */
@Data
@NoArgsConstructor
public class CfgInvoiceSettingDTO implements Serializable {
    @Data
    @NoArgsConstructor
    public static class CommonDTO {
        /**
         * 发票类型（必填）
         */
        @NotBlank(message = "发票类型不能为空")
        @Size(max = 50, message = "发票类型最大长度不能超过50位")
        private String type;

        /**
         * 公司名称（必填）
         */
        @NotBlank(message = "公司名称不能为空")
        @Size(max = 255, message = "公司名称最大长度不能超过255位")
        private String companyName;

        /**
         * 法人国家经济号（必填）
         */
        @NotBlank(message = "法人国家经济号不能为空")
        @Size(max = 50, message = "法人国家经济号最大长度不能超过50位")
        private String leiCode;

        /**
         * 启用禁用状态（true禁用，false启用，必填）
         */
        private Boolean disabled;

        /**
         * 税务类型（必填）
         */
        @NotBlank(message = "税务类型不能为空")
        @Size(max = 50, message = "税务类型最大长度不能超过50位")
        private String taxType;

        /**
         * 公司类型（必填）
         */
        @NotBlank(message = "公司类型不能为空")
        @Size(max = 50, message = "公司类型最大长度不能超过50位")
        private String dictCompanyType;

        /**
         * 州税号（必填）
         */
        @NotBlank(message = "州税号不能为空")
        @Size(max = 50, message = "州税号最大长度不能超过50位")
        private String stateTaxNo;

        /**
         * 邮箱（必填，格式校验）
         */
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱最大长度不能超过100位")
        private String email;

        /**
         * 邮编（必填）
         */
        @NotBlank(message = "邮编不能为空")
        @Size(max = 20, message = "邮编最大长度不能超过20位")
        private String postCode;

        /**
         * 地址（必填）
         */
        @NotBlank(message = "地址不能为空")
        @Size(max = 255, message = "地址最大长度不能超过255位")
        private String address;

        /**
         * 门牌号
         */
        @NotBlank(message = "门牌号不能为空")
        @Size(max = 50, message = "门牌号最大长度不能超过50位")
        private String doorplateNo;

        /**
         * 区（必填）
         */
        @NotBlank(message = "区不能为空")
        @Size(max = 50, message = "区最大长度不能超过50位")
        private String district;

        /**
         * 城市（必填）
         */
        @NotBlank(message = "城市不能为空")
        @Size(max = 50, message = "城市最大长度不能超过50位")
        private String city;

        /**
         * 州
         */
        @Size(max = 50, message = "州最大长度不能超过50位")
        private String state;

        /**
         * 序列号（必填）
         */
        @NotNull(message = "序列号不能为空")
        private Integer no;

        /**
         * 起始编号（必填）
         */
        @NotBlank(message = "起始编号不能为空")
        @Size(max = 50, message = "起始编号最大长度不能超过50位")
        private String startCode;

        /**
         * A1证书链接（必填）
         */
        private String certificateUrl;

        /**
         * A1证书密码（必填，敏感字段建议加密传输）
         */
        @NotBlank(message = "证书密码不能为空")
        @Size(max = 100, message = "证书密码最大长度不能超过100位")
        private String certificatePassword;

        /**
         * 附件地址
         */
        @NotEmpty(message = "证书地址列表不能为空")
        @Size(max = 1, message = "最多只能上传1个附件地址")
        @Valid
        private List<String> attachmentUrlList;

        /**
         * 附件名
         */
        @NotEmpty(message = "证书名列表不能为空")
        @Size(max = 1, message = "最多只能上传1个附件名")
        @Valid
        private List<String> attachmentNameList;

    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CfgInvoiceSettingDTO.CommonDTO {

    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CfgInvoiceSettingDTO.CommonDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 发票类型
         */
        private String type;

        /**
         * 操作人
         */
        private String updateUserName;

        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;

        /**
         * 操作时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updateTime;

        /**
         * 公司名称
         */
        private String companyName;

        /**
         * 法人国家经济号
         */
        private String leiCode;

        /**
         * 启用禁用 true禁用 false启用
         */
        private Boolean disabled;

        /**
         * 税务类型
         */
        private String taxType;

        /**
         * 公司类型
         */
        private String dictCompanyType;

        /**
         * 州税号
         */
        private String stateTaxNo;

        /**
         * 邮箱
         */
        private String email;

        /**
         * 邮编
         */
        private String postCode;

        /**
         * 地址
         */
        private String address;

        /**
         * 门牌号
         */
        private String doorplateNo;

        /**
         * 区
         */
        private String district;

        /**
         * 城市
         */
        private String city;

        /**
         * 州
         */
        private String state;

        /**
         * 序列号
         */
        private Integer no;

        /**
         * 起始编号
         */
        private String startCode;

        /**
         * A1证书链接
         */
        private String certificateUrl;

        /**
         * A1证书密码
         */
        private String certificatePassword;

        /**
         * 税种ID（关联cfg_tax_category.category_id）
         */
        private String taxCategoryId;

        /**
         * 税种描述（名称）
         */
        private String taxCategoryName;

        /**
         * name、value、
         */
        private List<ShopInfoDTO> shopList;
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
    public static class ShopInfoDTO {
        /**
         * 平台
         */
        private String dictPlatform;
        /**
         * 平台
         */
        private String dictPlatformName;
        /**
         * 店铺名称
         */
        private String name;
    }

    @Data
    @NoArgsConstructor
    public static class ViewDTO extends CommonDTO {
        /**
         * 主键id
         */
        private String id;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateStatusDTO {
        /**
         * 主键Id
         */
        @NotBlank(message = "主键Id不能为空")
        private String id;

        /**
         * 是否启用标识
         */
        @NotNull(message = "是否启用标识不能为空")
        private Boolean disabled;
    }
    @Data
    @NoArgsConstructor
    public static class UpdateSerialDTO {
        /**
         * 主键Id
         */
        @NotBlank(message = "主键Id不能为空")
        private String id;
        /**
         * 序列号
         */
        @NotNull(message = "序列号不能为空")
        private Integer no;

        /**
         * 起始编号
         */
        @NotBlank(message = "起始编号不能为空")
        private String startCode;
    }

    @Data
    @NoArgsConstructor
    public static class RuleMatchDTO {
        //是否匹配
        Boolean isPass = Boolean.FALSE;
    }
}