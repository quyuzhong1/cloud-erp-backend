package com.erp.model.dmp.dto;

import java.time.LocalDateTime;
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
 * 中台店铺表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2024-08-07
*/
@Data
@NoArgsConstructor
public class DmpShopInfoDTO implements Serializable {




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
        * 平台创建时间
        */
        private LocalDateTime platformCreateTime;

        /**
        * 平台修改时间
        */
        private LocalDateTime platformUpdateTime;

        /**
        * 来源类型：管易，金蝶，马帮
        */
        private String sourceType;

        /**
        * 订单来源平台（编码）：Amazon，AliExpress，shopify，...
        */
        private String sourcePlatform;

        /**
        * 单据编号（唯一）
        */
        private String billCode;

        /**
        * 销售平台原始单号
        */
        private String platformOriginalCode;

        /**
        * 平台店铺账户
        */
        private String accountUserName;

        /**
        * 店铺名称
        */
        private String name;

        /**
        * site
站点
        */
        private String site;

        /**
        * 店铺状态:true 禁用 false 启用
        */
        private String disabled;

        /**
        * 企业id
        */
        private String companyId;

        /**
        * 企业名称
        */
        private String companyName;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;

        /**
        * 输入任务id
        */
        private String inputTaskId;

        /**
        * 转换id
        */
        private String convertId;

        /**
        * 下一层级id
        */
        private String nextLevelId;


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
        * 平台创建时间
        */
        private LocalDateTime platformCreateTime;

        /**
        * 平台修改时间
        */
        private LocalDateTime platformUpdateTime;

        /**
        * 来源类型：管易，金蝶，马帮
        */
        @NotBlank(message = "来源类型：管易，金蝶，马帮不能为空")
        @Size(max = 32,message = "来源类型：管易，金蝶，马帮最大长度不能超过32位")
        private String sourceType;

        /**
        * 订单来源平台（编码）：Amazon，AliExpress，shopify，...
        */
        @NotBlank(message = "订单来源平台（编码）：Amazon，AliExpress，shopify，...不能为空")
        @Size(max = 32,message = "订单来源平台（编码）：Amazon，AliExpress，shopify，...最大长度不能超过32位")
        private String sourcePlatform;

        /**
        * 单据编号（唯一）
        */
        @NotBlank(message = "单据编号（唯一）不能为空")
        @Size(max = 64,message = "单据编号（唯一）最大长度不能超过64位")
        private String billCode;

        /**
        * 销售平台原始单号
        */
        @NotBlank(message = "销售平台原始单号不能为空")
        @Size(max = 64,message = "销售平台原始单号最大长度不能超过64位")
        private String platformOriginalCode;

        /**
        * 平台店铺账户
        */
        @NotBlank(message = "平台店铺账户不能为空")
        @Size(max = 255,message = "平台店铺账户最大长度不能超过255位")
        private String accountUserName;

        /**
        * 店铺名称
        */
        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 255,message = "店铺名称最大长度不能超过255位")
        private String name;

        /**
        * site站点
        */
        @NotBlank(message = "site站点不能为空")
        @Size(max = 32,message = "site站点最大长度不能超过32位")
        private String site;

        /**
        * 店铺状态:true 禁用 false 启用
        */
        @NotBlank(message = "店铺状态:true 禁用 false 启用不能为空")
        @Size(max = 32,message = "店铺状态:true 禁用 false 启用最大长度不能超过32位")
        private String disabled;

        /**
        * 企业id
        */
        @NotBlank(message = "企业id不能为空")
        @Size(max = 32,message = "企业id最大长度不能超过32位")
        private String companyId;

        /**
        * 企业名称
        */
        @NotBlank(message = "企业名称不能为空")
        @Size(max = 255,message = "企业名称最大长度不能超过255位")
        private String companyName;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;

        /**
        * 输入任务id
        */
        @NotBlank(message = "输入任务id不能为空")
        @Size(max = 19,message = "输入任务id最大长度不能超过19位")
        private String inputTaskId;

        /**
        * 转换id
        */
        @NotBlank(message = "转换id不能为空")
        @Size(max = 19,message = "转换id最大长度不能超过19位")
        private String convertId;

        /**
        * 下一层级id
        */
        @NotBlank(message = "下一层级id不能为空")
        @Size(max = 19,message = "下一层级id最大长度不能超过19位")
        private String nextLevelId;


    }


}