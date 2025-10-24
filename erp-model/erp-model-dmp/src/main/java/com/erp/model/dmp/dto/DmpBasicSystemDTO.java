package com.erp.model.dmp.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 外部系统请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
*/
@Data
@NoArgsConstructor
public class DmpBasicSystemDTO implements Serializable {




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
        * 系统代码：amazon=亚马逊，kingdee=金蝶
        */
        private String code;

        /**
        * 系统名称
        */
        private String name;

        /**
        * 系统类型：wms=仓储,tms=物流,finance=财务
        */
        private String type;

        /**
        * 是否禁用
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

        @NotBlank(message = "系统代号：不能为空")
        @Size(max = 50,message = "系统代号：最大长度不能超过50位")
        private String code;

        /**
        * 系统名称
        */
        @NotBlank(message = "系统名称不能为空")
        @Size(max = 255,message = "系统名称最大长度不能超过255位")
        private String name;

        /**
        * 系统类型：wms=仓储,tms=物流,finance=财务
        */
        @NotBlank(message = "系统类型：不能为空")
        @Size(max = 50,message = "系统类型：最大长度不能超过50位")
        private String type;

        /**
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;


    }

    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

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
    public static class ListDTO {

        /**
         * 主键id
         */
        private String  id;

        /**
         * 系统代码：amazon=亚马逊，kingdee=金蝶 【可排序】
         */
        private String code;

        /**
         * 系统名称 【可排序】
         */
        private String name;

        /**
         * 系统类型：wms=仓储,tms=物流,finance=财务
         */
        private String type;

        /**
         * 系统类型名称
         */
        private String typeName;

        /**
         * 是否禁用【可排序】
         */
        private Boolean disabled;

        /**
         * 创建人【可排序】
         */
        private String createUserName;

        /**
         * 创建时间【可排序】
         */
        private LocalDateTime createTime;

        /**
         * 更新人【可排序】
         */
        private String updateUserName;

        /**
         * 更新人【可排序】
         */
        private LocalDateTime updateTime;
    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ExportDTO {

        /**
         * 主键id
         */
        private String  id;

        /**
         * 系统代码：amazon=亚马逊，kingdee=金蝶
         */
        private String code;

        /**
         * 系统名称
         */
        private String name;

        /**
         * 系统类型：wms=仓储,tms=物流,finance=财务
         */
        private String type;

        /**
         * 是否禁用
         */
        private Boolean disabled;


    }

    /**
     * 状态统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {

        /**
         * 类型
         */
        private String tabFlag;

        /**
         * 类型名称
         */
        private String tabFlagName;

        /**
         * 数量
         */
        private Integer count;
    }
}