package com.erp.model.dmp.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 手工出入库待同步数据表请求响应实体
 * </p>
 *
 * @author Cloud
 * @since 2023-06-25
*/
@Data
@NoArgsConstructor
public class DmpOutInStockDTO implements Serializable {




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
        * 仓库编码
        */
        private String warehouseCode;

        /**
        * 仓库名称
        */
        private String warehouseName;

        /**
        * 出库类型
        */
        private String typeName;

        /**
        * 负责人名称
        */
        private String chargeUserName;

        /**
        * 备注
        */
        private String remark;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 来源平台 自研erp，马帮，管易云，金蝶云星空
        */
        private String platformSign;

        /**
        * 同步马帮状态
        */
        private String syncMbStatus;

        /**
        * 同步时间
        */
        private LocalDateTime lastSyncMbTime;

        /**
        * 类型，入库：in 出库 : out
        */
        private String type;

        /**
        * 目标平台
        */
        private String targetPlatformSign;

        /**
        * 目标平台单据号
        */
        private String targetOrderCode;


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
        * 仓库编码
        */
        @NotBlank(message = "仓库编码不能为空")
        @Size(max = 16,message = "仓库编码最大长度不能超过16位")
        private String warehouseCode;

        /**
        * 仓库名称
        */
        @NotBlank(message = "仓库名称不能为空")
        @Size(max = 32,message = "仓库名称最大长度不能超过32位")
        private String warehouseName;

        /**
        * 出库类型
        */
        @NotBlank(message = "出库类型不能为空")
        @Size(max = 16,message = "出库类型最大长度不能超过16位")
        private String typeName;

        /**
        * 负责人名称
        */
        @NotBlank(message = "负责人名称不能为空")
        @Size(max = 16,message = "负责人名称最大长度不能超过16位")
        private String chargeUserName;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 32,message = "来源类型最大长度不能超过32位")
        private String sourceType;

        /**
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 32,message = "来源id最大长度不能超过32位")
        private String sourceId;

        /**
        * 来源平台 自研erp，马帮，管易云，金蝶云星空
        */
        @NotBlank(message = "来源平台 自研erp，马帮，管易云，金蝶云星空不能为空")
        @Size(max = 32,message = "来源平台 自研erp，马帮，管易云，金蝶云星空最大长度不能超过32位")
        private String platformSign;

        /**
        * 同步马帮状态
        */
        @NotBlank(message = "同步马帮状态不能为空")
        @Size(max = 16,message = "同步马帮状态最大长度不能超过16位")
        private String syncMbStatus;

        /**
        * 同步时间
        */
        @NotNull(message = "同步时间不能为空")
        private LocalDateTime lastSyncMbTime;

        /**
        * 类型，入库：in 出库 : out
        */
        @NotBlank(message = "类型，入库：in 出库 : out不能为空")
        @Size(max = 16,message = "类型，入库：in 出库 : out最大长度不能超过16位")
        private String type;

        /**
        * 目标平台
        */
        @NotBlank(message = "目标平台不能为空")
        @Size(max = 16,message = "目标平台最大长度不能超过16位")
        private String targetPlatformSign;

        /**
        * 目标平台单据号
        */
        @NotBlank(message = "目标平台单据号不能为空")
        @Size(max = 32,message = "目标平台单据号最大长度不能超过32位")
        private String targetOrderCode;


    }


}