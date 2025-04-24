package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 海外仓签收记录请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
*/
@Data
@NoArgsConstructor
public class OverseasWarehouseInboundReceivedDTO implements Serializable {




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
        * 明细id
        */
        private String detailId;

        /**
        * 签收人
        */
        private String receiveUser;

        /**
        * 签收数量
        */
        private Integer receiveQty;

        /**
        * 签收时间
        */
        private LocalDateTime receiveTime;


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
        * 明细id
        */
        @NotBlank(message = "明细id不能为空")
        @Size(max = 19,message = "明细id最大长度不能超过19位")
        private String detailId;

        /**
        * 签收人
        */
        @NotBlank(message = "签收人不能为空")
        @Size(max = 255,message = "签收人最大长度不能超过255位")
        private String receiveUser;

        /**
        * 签收数量
        */
        @NotNull(message = "签收数量不能为空")
        private Integer receiveQty;

        /**
        * 签收时间
        */
        private LocalDateTime receiveTime;


    }


}