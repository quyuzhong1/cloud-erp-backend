package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
     * 列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         *
         */
         private String  id;
         /**
          * 拣货车编号
          */
        private String code;
        /**
         * 拣货车类型Id
         */
        private String  typeId;
        /**
         * 拣货车类型名称
         */
        private String typeName;
        /**
         * 状态
         */
        private Boolean disabled;
        /**
         * 修改时间
         */
        private LocalDateTime updateTime;
        /**
         * 修改名称
         */
        private String updateUserName;

    }


    /**
     * 列表参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO  extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
    }


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

    @Data
    @NoArgsConstructor
    public static class UpdateStatusDTO {
        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 是否禁用，true是,false否
         */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;

    }
}