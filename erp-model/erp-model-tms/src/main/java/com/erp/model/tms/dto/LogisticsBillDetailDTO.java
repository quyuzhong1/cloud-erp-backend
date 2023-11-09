package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 物流单明细表请求响应实体
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
*/
@Data
@NoArgsConstructor
public class LogisticsBillDetailDTO implements Serializable {




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
        * 物流单id
        */
        private String mainId;

        /**
        * 运输状态
        */
        private String trackStatus;

        /**
        * 运单号
        */
        private String trackNo;


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
        * 物流单id
        */
        @NotBlank(message = "物流单id不能为空")
        @Size(max = 19,message = "物流单id最大长度不能超过19位")
        private String mainId;

        /**
        * 运输状态
        */
        @NotBlank(message = "运输状态不能为空")
        @Size(max = 30,message = "运输状态最大长度不能超过30位")
        private String trackStatus;

        /**
        * 运单号
        */
        @NotBlank(message = "运单号不能为空")
        @Size(max = 30,message = "运单号最大长度不能超过30位")
        private String trackNo;


    }


}