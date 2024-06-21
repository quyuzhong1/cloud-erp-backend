package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 拣货车管理请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-06-20
*/
@Data
@NoArgsConstructor
public class PickingCartDTO implements Serializable {




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
        * 编号
        */
        private String code;

        /**
        * 拣货车类型id
        */
        private String typeId;

        /**
        * 状态,true是，false否
        */
        private Boolean disabled;


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
        * 拣货车类型id
        */
        @NotBlank(message = "拣货车类型id不能为空")
        @Size(max = 19,message = "拣货车类型id最大长度不能超过19位")
        private String typeId;

    }


}