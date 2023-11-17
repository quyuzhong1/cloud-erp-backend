package com.erp.model.wms.dto;

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
 * 要货申请单请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
*/
@Data
@NoArgsConstructor
public class RequisitionApplicationDTO implements Serializable {




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
        * code
        */
        private String code;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 来源单号
        */
        private String sourceCode;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 单据状态
        */
        private String status;

        /**
        * 作废状态
        */
        private String invalidStatus;

        /**
        * 作废备注
        */
        private String invalidRemark;

        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 类型
        */
        private String type;

        /**
        * 要货渠道id
        */
        private String channelId;

        /**
        * 要货渠道中文名
        */
        private String channelName;

        /**
        * 要货仓库id
        */
        private String requisitionWarehouseId;

        /**
        * 要货仓库中文名
        */
        private String requisitionWarehouseName;

        /**
        * 调入仓库id
        */
        private String toWarehouseId;

        /**
        * 调入仓库中文名
        */
        private String toWarehouseName;

        /**
        * 调出仓库id
        */
        private String fromWarehouseId;

        /**
        * 调出仓库中文名
        */
        private String fromWarehouseName;

        /**
        * 处理人id
        */
        private String handleUserId;

        /**
        * 处理人中文名
        */
        private String handleUserName;

        /**
        * 处理时间
        */
        private LocalDateTime handleTime;


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
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 19,message = "来源id最大长度不能超过19位")
        private String sourceId;

        /**
        * 来源单号
        */
        @NotBlank(message = "来源单号不能为空")
        @Size(max = 50,message = "来源单号最大长度不能超过50位")
        private String sourceCode;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 50,message = "来源类型最大长度不能超过50位")
        private String sourceType;

        /**
        * 单据状态
        */
        @NotBlank(message = "单据状态不能为空")
        @Size(max = 255,message = "单据状态最大长度不能超过255位")
        private String status;

        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 类型
        */
        @NotBlank(message = "类型不能为空")
        @Size(max = 255,message = "类型最大长度不能超过255位")
        private String type;

        /**
        * 要货渠道id
        */
        @NotBlank(message = "要货渠道id不能为空")
        @Size(max = 19,message = "要货渠道id最大长度不能超过19位")
        private String channelId;

        /**
        * 要货渠道中文名
        */
        @NotBlank(message = "要货渠道中文名不能为空")
        @Size(max = 500,message = "要货渠道中文名最大长度不能超过500位")
        private String channelName;

        /**
        * 要货仓库id
        */
        @NotBlank(message = "要货仓库id不能为空")
        @Size(max = 19,message = "要货仓库id最大长度不能超过19位")
        private String requisitionWarehouseId;

        /**
        * 要货仓库中文名
        */
        @NotBlank(message = "要货仓库中文名不能为空")
        @Size(max = 255,message = "要货仓库中文名最大长度不能超过255位")
        private String requisitionWarehouseName;

        /**
        * 调入仓库id
        */
        @NotBlank(message = "调入仓库id不能为空")
        @Size(max = 19,message = "调入仓库id最大长度不能超过19位")
        private String toWarehouseId;

        /**
        * 调入仓库中文名
        */
        @NotBlank(message = "调入仓库中文名不能为空")
        @Size(max = 255,message = "调入仓库中文名最大长度不能超过255位")
        private String toWarehouseName;

        /**
        * 调出仓库id
        */
        @NotBlank(message = "调出仓库id不能为空")
        @Size(max = 19,message = "调出仓库id最大长度不能超过19位")
        private String fromWarehouseId;

        /**
        * 调出仓库中文名
        */
        @NotBlank(message = "调出仓库中文名不能为空")
        @Size(max = 255,message = "调出仓库中文名最大长度不能超过255位")
        private String fromWarehouseName;

        /**
        * 处理人id
        */
        @NotBlank(message = "处理人id不能为空")
        @Size(max = 19,message = "处理人id最大长度不能超过19位")
        private String handleUserId;

        /**
        * 处理人中文名
        */
        @NotBlank(message = "处理人中文名不能为空")
        @Size(max = 255,message = "处理人中文名最大长度不能超过255位")
        private String handleUserName;

        /**
        * 处理时间
        */
        private LocalDateTime handleTime;


    }


}