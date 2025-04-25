package com.erp.model.dmp.dto;

import java.time.LocalDateTime;
import java.util.List;

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
 * 中台物流单主表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2025-04-07
*/
@Data
@NoArgsConstructor
public class DmpSoLogisticsDTO implements Serializable {




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
        * 来源平台：gyy，kingdee，mabang
        */
        private String sourceSystem;

        /**
        * 第三方物流id
        */
        private String thirdLogisticsId;

        /**
        * 第三方物流单据编号
        */
        private String thirdLogisticsCode;

        /**
        * 第三方创建时间
        */
        private LocalDateTime thirdCreateTime;

        /**
        * 第三方更新时间
        */
        private LocalDateTime thirdUpdateTime;

        /**
        * 发货时间
        */
        private LocalDateTime deliveryTime;

        /**
        * 签收时间
        */
        private LocalDateTime signTime;

        /**
        * 出库单号
        */
        private String outstockCode;

        /**
        * 物流商编码
        */
        private String logisticCompanyCode;

        /**
        * 物流商名称
        */
        private String logisticCompanyName;

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

        /**
         * 明细数据
         */
        private List<DmpSoLogisticsDetailDTO.ViewDTO> detailList; 
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
        * 来源平台：gyy，kingdee，mabang
        */
        @NotBlank(message = "来源平台：gyy，kingdee，mabang不能为空")
        @Size(max = 25,message = "来源平台：gyy，kingdee，mabang最大长度不能超过25位")
        private String sourceSystem;

        /**
        * 第三方物流id
        */
        @NotBlank(message = "第三方物流id不能为空")
        @Size(max = 64,message = "第三方物流id最大长度不能超过64位")
        private String thirdLogisticsId;

        /**
        * 第三方物流单据编号
        */
        @NotBlank(message = "第三方物流单据编号不能为空")
        @Size(max = 64,message = "第三方物流单据编号最大长度不能超过64位")
        private String thirdLogisticsCode;

        /**
        * 第三方创建时间
        */
        private LocalDateTime thirdCreateTime;

        /**
        * 第三方更新时间
        */
        private LocalDateTime thirdUpdateTime;

        /**
        * 发货时间
        */
        private LocalDateTime deliveryTime;

        /**
        * 签收时间
        */
        private LocalDateTime signTime;

        /**
        * 出库单号
        */
        @NotBlank(message = "出库单号不能为空")
        @Size(max = 64,message = "出库单号最大长度不能超过64位")
        private String outstockCode;

        /**
        * 物流商编码
        */
        @NotBlank(message = "物流商编码不能为空")
        @Size(max = 64,message = "物流商编码最大长度不能超过64位")
        private String logisticCompanyCode;

        /**
        * 物流商名称
        */
        @NotBlank(message = "物流商名称不能为空")
        @Size(max = 255,message = "物流商名称最大长度不能超过255位")
        private String logisticCompanyName;

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