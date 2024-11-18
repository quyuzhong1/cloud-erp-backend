package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-08-14
*/
@Data
@NoArgsConstructor
public class PdaVersionDTO implements Serializable {


    /**
    * 列表查询
    */
    @Data
    @NoArgsConstructor
    public static class PagingDTO {
        /**
         * 主键
         */
        private String id;

        /**
         * 下拉获取地址：sys/common/enumDropDown?type=SysType
         * 发版类型：ALL PC PDA
         */
        private String type;

        /**
         * 通知类型
         */
        private String releaseType;

        /**
         * 类型名称
         **/
        private String typeName;

        /**
         * pda版本
         */
        private String pdaVersion;

        /**
         * 升级内容描述
         */
        private String remark;

        /**
         * 是否强制更新
         */
        private Boolean force;

        /**
         * 升级包地址
         */
        private String url;

        /**
         * 升级时间
         */
        private LocalDateTime upgradeTime;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 修改人名称
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
    }



    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO {
        private String searchKeyword;
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
        * pda版本
        */
        private String pdaVersion;

        /**
        * 升级内容描述
        */
        private String remark;

        /**
        * 是否强制更新
        */
        private Boolean force;

        /**
        * 升级包地址
        */
        private String url;


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
        * pda版本
        */
        @NotBlank(message = "pda版本不能为空")
        @Size(max = 255,message = "pda版本最大长度不能超过255位")
        private String pdaVersion;

        /**
        * 升级内容描述
        */
        @NotBlank(message = "升级内容描述不能为空")
        private String remark;

        /**
        * 是否强制更新
        */
        @NotNull(message = "是否强制更新不能为空")
        private Boolean force;

        /**
        * 升级包地址
        */
        @NotBlank(message = "升级包地址不能为空")
        private String url;

        /**
        * 升级类型
        */
        @NotBlank(message = "类型不能为空")
        private String type;

        /**
        * 通知类型：1 系统通知  0 升级通知
        */
        @NotNull(message = "通知类型")
        private Integer releaseType;

        /**
        * 升级时间
        */
        private LocalDateTime upgradeTime;

    }

}