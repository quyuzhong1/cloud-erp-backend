package com.erp.model.dmp.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 第三方系统店铺表请求响应实体
 * </p>
 *
 * @author hyj
 * @since 2024-05-17
 */
@Data
@NoArgsConstructor
public class ThirdShopDTO implements Serializable {


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 是否禁用/停用 true 是 false 不是
         */
        private Boolean disabled;

        /**
         * 系统类型：lingxing领星，wangdian旺店通
         */
        private String sysType;

        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 平台id
         */
        private String platformId;

        /**
         * 子平台id
         */
        private String subPlatformId;

        /**
         * 分组id
         */
        private String groupId;

        /**
         * 平台的主键（平台店铺授权时平台有推送对应值则返回，不推送则返回空字符串）
         */
        private String accountId;

        /**
         * 授权状态：0未授权 1已授权 2授权失效 3授权停用
         */
        private Integer authState;

        /**
         * 授权时间
         */
        private String authTime;

        /**
         * 编号
         */
        private String code;

        /**
         * 名称
         */
        private String name;

        /**
         * 地址
         */
        private String address;

        /**
         * 联系人
         */
        private String contacts;

        /**
         * 联系人电话
         */
        private String telNumber;

        /**
         * 固话
         */
        private String telno;

        /**
         * 邮箱
         */
        private String email;

        /**
         * 邮编
         */
        private String zip;

        /**
         * 网址
         */
        private String website;

        /**
         * 省份
         */
        private String province;

        /**
         * 城市
         */
        private String city;

        /**
         * 区县
         */
        private String district;

        /**
         * 备注
         */
        private String remark;

        /**
         * 第三方创建时间
         */
        private String created;

        /**
         * 第三方修改时间
         */
        private String modified;


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
    public static class CommonDTO {

        /**
         * 是否禁用/停用 true 是 false 不是
         */
        @NotNull(message = "是否禁用/停用 true 是 false 不是不能为空")
        private Boolean disabled;

        /**
         * 系统类型：lingxing领星，wangdian旺店通
         */
        @NotBlank(message = "系统类型：lingxing领星，wangdian旺店通不能为空")
        @Size(max = 19, message = "系统类型：lingxing领星，wangdian旺店通最大长度不能超过19位")
        private String sysType;

        /**
         * 店铺id
         */
        @NotBlank(message = "店铺id不能为空")
        @Size(max = 200, message = "店铺id最大长度不能超过200位")
        private String shopId;

        /**
         * 平台id
         */
        @NotBlank(message = "平台id不能为空")
        @Size(max = 4, message = "平台id最大长度不能超过4位")
        private String platformId;

        /**
         * 子平台id
         */
        @NotBlank(message = "子平台id不能为空")
        @Size(max = 4, message = "子平台id最大长度不能超过4位")
        private String subPlatformId;

        /**
         * 分组id
         */
        @NotBlank(message = "分组id不能为空")
        @Size(max = 100, message = "分组id最大长度不能超过100位")
        private String groupId;

        /**
         * 平台的主键（平台店铺授权时平台有推送对应值则返回，不推送则返回空字符串）
         */
        @NotBlank(message = "平台的主键（平台店铺授权时平台有推送对应值则返回，不推送则返回空字符串）不能为空")
        @Size(max = 255, message = "平台的主键（平台店铺授权时平台有推送对应值则返回，不推送则返回空字符串）最大长度不能超过255位")
        private String accountId;

        /**
         * 授权状态：0未授权 1已授权 2授权失效 3授权停用
         */
        @NotNull(message = "授权状态：0未授权 1已授权 2授权失效 3授权停用不能为空")
        private Integer authState;

        /**
         * 授权时间
         */
        @NotBlank(message = "授权时间不能为空")
        @Size(max = 50, message = "授权时间最大长度不能超过50位")
        private String authTime;

        /**
         * 名称
         */
        @NotBlank(message = "名称不能为空")
        @Size(max = 200, message = "名称最大长度不能超过200位")
        private String name;
        /**
         * 编号
         */
        @NotBlank(message = "编号不能为空")
        @Size(max = 200, message = "编号最大长度不能超过200位")
        private String code;

        /**
         * 地址
         */
        @NotBlank(message = "地址 不能为空")
        @Size(max = 200, message = "地址 最大长度不能超过200位")
        private String address;

        /**
         * 联系人
         */
        @NotBlank(message = "联系人不能为空")
        @Size(max = 50, message = "联系人最大长度不能超过50位")
        private String contacts;

        /**
         * 联系人电话
         */
        @NotBlank(message = "联系人电话不能为空")
        @Size(max = 20, message = "联系人电话最大长度不能超过20位")
        private String telNumber;

        /**
         * 固话
         */
        @NotBlank(message = "固话不能为空")
        @Size(max = 20, message = "固话最大长度不能超过20位")
        private String telno;

        /**
         * 邮箱
         */
        @NotBlank(message = "邮箱不能为空")
        @Size(max = 64, message = "邮箱最大长度不能超过64位")
        private String email;

        /**
         * 邮编
         */
        @NotBlank(message = "邮编不能为空")
        @Size(max = 255, message = "邮编最大长度不能超过255位")
        private String zip;

        /**
         * 网址
         */
        @NotBlank(message = "网址不能为空")
        @Size(max = 1024, message = "网址最大长度不能超过1,024位")
        private String website;

        /**
         * 省份
         */
        @NotBlank(message = "省份不能为空")
        @Size(max = 50, message = "省份最大长度不能超过50位")
        private String province;

        /**
         * 城市
         */
        @NotBlank(message = "城市不能为空")
        @Size(max = 50, message = "城市最大长度不能超过50位")
        private String city;

        /**
         * 区县
         */
        @NotBlank(message = "区县不能为空")
        @Size(max = 50, message = "区县最大长度不能超过50位")
        private String district;

        /**
         * 备注
         */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255, message = "备注最大长度不能超过255位")
        private String remark;

        /**
         * 第三方创建时间
         */
        @NotBlank(message = "第三方创建时间不能为空")
        @Size(max = 50, message = "第三方创建时间最大长度不能超过50位")
        private String created;

        /**
         * 第三方修改时间
         */
        @NotBlank(message = "第三方修改时间不能为空")
        @Size(max = 40, message = "第三方修改时间最大长度不能超过40位")
        private String modified;


    }

    @Data
    @NoArgsConstructor
    public static class PageDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 系统类型：lingxing领星，wangdian旺店通
         */
        private String sysType;
        /**
         * 名称
         */
        private String name;
        /**
         * 编号
         */
        private String code;
    }

    @Data
    @NoArgsConstructor
    public static class PageSelectDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 系统类型：lingxing领星，wangdian旺店通
         */
        private String sysType;
        /**
         * 名称
         */
        private String name;
        /**
         * 编号
         */
        private String code;
        /**
         * 是否可选
         */
        private Boolean canCheck = true;
        /**
         * disabled
         */
        private Boolean disabled;
    }

    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        /**
         * 系统类型：lingxing领星，wangdian旺店通
         */
        @NotBlank(message = "系统类型不能为空")
        @Size(max = 19, message = "系统类型：lingxing领星，wangdian旺店通 最大长度不能超过19位")
        private String sysType;
        /**
         * 名称
         */
        @Size(max = 200, message = "名称最大长度不能超过200位")
        private String name;
    }

    /**
     * 远程搜索
     */
    @Data
    @NoArgsConstructor
    public static class SelectDTO {
        /**
         * 系统类型
         */
        @NotBlank(message = "系统类型不能为空")
        private String sysType;

        /**
         * 关键词
         */
        private String searchKeyword;
    }
}