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
 * 第三方仓库存请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-08-08
*/
@Data
@NoArgsConstructor
public class DmpThirdInboundDTO implements Serializable {




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
        * 入库单号
        */
        private String receivingCode;

        /**
        * 入库单状态
        */
        private String receivingStatus;

        /**
        * 明细json
        */
        private String detailListJson;

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
        * 入库单号
        */
        @NotBlank(message = "入库单号不能为空")
        @Size(max = 64,message = "入库单号最大长度不能超过64位")
        private String receivingCode;

        /**
        * 入库单状态
        */
        @NotBlank(message = "入库单状态不能为空")
        @Size(max = 64,message = "入库单状态最大长度不能超过64位")
        private String receivingStatus;

        /**
        * 明细json
        */
        private String detailListJson;

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