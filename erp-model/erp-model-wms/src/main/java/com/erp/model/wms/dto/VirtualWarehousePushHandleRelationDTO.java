package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 分货单拆单关联关系表请求响应实体
 * </p>
 *
 * @author hyj
 * @since 2024-06-07
*/
@Data
@NoArgsConstructor
public class VirtualWarehousePushHandleRelationDTO implements Serializable {




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
        * 分货单id
        */
        private String allocationId;

        /**
        * 分货单明细id
        */
        private String allocationDetailId;

        /**
        * 分货单拆单表id
        */
        private String handleId;

        /**
        * 分货单拆单明细表id
        */
        private String handleDetailId;


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
        * 分货单id
        */
        @NotBlank(message = "分货单id不能为空")
        @Size(max = 19,message = "分货单id最大长度不能超过19位")
        private String allocationId;

        /**
        * 分货单明细id
        */
        @NotBlank(message = "分货单明细id不能为空")
        @Size(max = 19,message = "分货单明细id最大长度不能超过19位")
        private String allocationDetailId;

        /**
        * 分货单拆单表id
        */
        @NotBlank(message = "分货单拆单表id不能为空")
        @Size(max = 19,message = "分货单拆单表id最大长度不能超过19位")
        private String handleId;

        /**
        * 分货单拆单明细表id
        */
        @NotBlank(message = "分货单拆单明细表id不能为空")
        @Size(max = 19,message = "分货单拆单明细表id最大长度不能超过19位")
        private String handleDetailId;


    }


}