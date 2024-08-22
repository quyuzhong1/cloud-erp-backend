package com.erp.model.dmp.dto;

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
 * 第三方中转仓库请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-08-07
*/
@Data
@NoArgsConstructor
public class DmpTransferWarehouseInfoDTO implements Serializable {




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
        * 仓库平台类型
        */
        private String warehousePlatformType;

        /**
        * 来源平台（编码）：goodcang、iml
        */
        private String sourcePlatform;

        /**
        * erp授权Id
        */
        private String authId;

        /**
        * 物流渠道编码
        */
        private String logisticsChannelCode;

        /**
        * 物流渠道名称
        */
        private String logisticsChannelName;

        /**
        * 中转仓编码
        */
        private String transferWarehouseCode;

        /**
        * 中转仓名称
        */
        private String transferWarehouseName;

        /**
        * 目的仓编码
        */
        private String destinationWarehouseCode;

        /**
        * 目的仓名称
        */
        private String destinationWarehouseName;

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

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;


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
        * 仓库平台类型
        */
        @NotBlank(message = "仓库平台类型不能为空")
        @Size(max = 32,message = "仓库平台类型最大长度不能超过32位")
        private String warehousePlatformType;

        /**
        * 来源平台（编码）：goodcang、iml
        */
        @NotBlank(message = "来源平台（编码）：goodcang、iml不能为空")
        @Size(max = 32,message = "来源平台（编码）：goodcang、iml最大长度不能超过32位")
        private String sourcePlatform;

        /**
        * erp授权Id
        */
        @NotBlank(message = "erp授权Id不能为空")
        @Size(max = 64,message = "erp授权Id最大长度不能超过64位")
        private String authId;

        /**
        * 物流渠道编码
        */
        @NotBlank(message = "物流渠道编码不能为空")
        @Size(max = 64,message = "物流渠道编码最大长度不能超过64位")
        private String logisticsChannelCode;

        /**
        * 物流渠道名称
        */
        @NotBlank(message = "物流渠道名称不能为空")
        @Size(max = 500,message = "物流渠道名称最大长度不能超过500位")
        private String logisticsChannelName;

        /**
        * 中转仓编码
        */
        @NotBlank(message = "中转仓编码不能为空")
        @Size(max = 64,message = "中转仓编码最大长度不能超过64位")
        private String transferWarehouseCode;

        /**
        * 中转仓名称
        */
        @NotBlank(message = "中转仓名称不能为空")
        @Size(max = 500,message = "中转仓名称最大长度不能超过500位")
        private String transferWarehouseName;

        /**
        * 目的仓编码
        */
        @NotBlank(message = "目的仓编码不能为空")
        @Size(max = 64,message = "目的仓编码最大长度不能超过64位")
        private String destinationWarehouseCode;

        /**
        * 目的仓名称
        */
        @NotBlank(message = "目的仓名称不能为空")
        @Size(max = 500,message = "目的仓名称最大长度不能超过500位")
        private String destinationWarehouseName;

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

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;


    }


}