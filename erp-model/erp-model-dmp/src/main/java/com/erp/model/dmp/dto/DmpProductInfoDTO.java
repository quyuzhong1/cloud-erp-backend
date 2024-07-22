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
 * 产品spu信息请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-07-22
*/
@Data
@NoArgsConstructor
public class DmpProductInfoDTO implements Serializable {




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
        * 来源系统：gyy，kingdee，mabang
        */
        private String sourceSystem;

        /**
        * 订单来源平台（编码）：Amazon，AliExpress，shopify，...
        */
        private String sourcePlatform;

        /**
        * 产品id
        */
        private String spuId;

        /**
        * 产品编码
        */
        private String spuNo;

        /**
        * 产品名称
        */
        private String spuName;


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
        * 来源系统：gyy，kingdee，mabang
        */
        @NotBlank(message = "来源系统：gyy，kingdee，mabang不能为空")
        @Size(max = 32,message = "来源系统：gyy，kingdee，mabang最大长度不能超过32位")
        private String sourceSystem;

        /**
        * 订单来源平台（编码）：Amazon，AliExpress，shopify，...
        */
        @NotBlank(message = "订单来源平台（编码）：Amazon，AliExpress，shopify，...不能为空")
        @Size(max = 32,message = "订单来源平台（编码）：Amazon，AliExpress，shopify，...最大长度不能超过32位")
        private String sourcePlatform;

        /**
        * 产品id
        */
        @NotBlank(message = "产品id不能为空")
        @Size(max = 64,message = "产品id最大长度不能超过64位")
        private String spuId;

        /**
        * 产品编码
        */
        @NotBlank(message = "产品编码不能为空")
        @Size(max = 64,message = "产品编码最大长度不能超过64位")
        private String spuNo;

        /**
        * 产品名称
        */
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 500,message = "产品名称最大长度不能超过500位")
        private String spuName;


    }


}