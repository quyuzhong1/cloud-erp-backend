package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 达人合作平台信息请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-12-02
*/
@Data
@NoArgsConstructor
public class KolCooperationPlatformDTO implements Serializable {




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
        * main_id
        */
        private String mainId;

        /**
        * 合作平台名称
        */
        private String platformName;

        /**
        * 平台ID
        */
        private String platformAccountId;

        /**
        * 账号名称
        */
        private String platformAccountName;

        /**
        * 粉丝数量
        */
        private Integer followerCount;

        /**
        * 主页链接
        */
        private String homepageUrl;

        /**
        * 平台备注
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
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * main_id
        */
        private String mainId;

        /**
        * 合作平台名称
        */
        @NotBlank(message = "合作平台名称不能为空")
        @Size(max = 100,message = "合作平台名称最大长度不能超过100位")
        private String platformName;

        /**
        * 平台ID
        */
        @Size(max = 50,message = "平台ID最大长度不能超过50位")
        private String platformAccountId;

        /**
        * 账号名称
        */
        @Size(max = 100,message = "账号名称最大长度不能超过100位")
        private String platformAccountName;

        /**
        * 粉丝数量
        */
        private Integer followerCount;

        /**
        * 主页链接
        */
        @NotBlank(message = "主页链接不能为空")
        @Size(max = 500,message = "主页链接最大长度不能超过500位")
        private String homepageUrl;

        /**
        * 平台备注
        */
        @Size(max = 500,message = "平台备注最大长度不能超过500位")
        private String remark;


    }


}