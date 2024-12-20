package com.erp.model.tms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 偏远邮编组请求响应实体
 * </p>
 *
 * @author jack
 * @since 2024-11-29
*/
@Data
@NoArgsConstructor
public class RemotePostcodeDTO implements Serializable {


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
         * 数量
         */
         private Integer count;

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
    * 分页列表
    */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 名称
        */
        private String name;

        /**
        * 备注
        */
        private String remark;

        /**
        * 邮编组状态:true 禁用 false 启用
        */
        private Boolean disabled;

        /**
        * 邮编组状态:true 禁用 false 启用
        */
        private String disabledName;

        /**
        * 更新时间
        */
        private LocalDateTime updateTime;

        /**
        * 更新人id
        */
        private String updateUserId;
        /**
        * 更新人名称
        */
        private String updateUserName;
    }

    /**
    * 导出Excel
    */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
        * 勾选的id集合
        */
        private List<String> ids;
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
        * 名称
        */
        private String name;

        /**
        * 备注
        */
        private String remark;

        /**
        * 邮编组状态:true 禁用 false 启用
        */
        private Boolean disabled;

        /**
         * 邮编组状态:true 禁用 false 启用
         */
        private String disabledName;

        /**
         * 明细表
         */
        private List<RemotePostcodeDetailDTO.ViewDTO> details;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        @NotEmpty
        @Valid
        private List<RemotePostcodeDetailDTO.AddDTO> details;
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

        @NotEmpty
        @Valid
        private List<RemotePostcodeDetailDTO.UpdateDTO> details;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 名称
        */
        @NotBlank(message = "名称不能为空")
        @Size(max = 64,message = "名称最大长度不能超过64位")
        private String name;

        /**
        * 备注
        */
        private String remark;

        /**
        * 邮编组状态:true 禁用 false 启用
        */
        @NotNull(message = "邮编组状态:true 禁用 false 启用不能为空")
        private Boolean disabled;

    }

    /**
     * 导出
     */
    @Data
    @NoArgsConstructor
    public static class ExportListDTO {

        /**
         * 主键id
         */
        private String  id;

        /**
         * 名称
         */
        private String name;

        /**
         * 备注
         */
        private String remark;

        /**
         * 邮编组状态:true 禁用 false 启用
         */
        private Boolean disabled;

        /**
         * 邮编组状态:true 禁用 false 启用
         */
        private String disabledName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
        private String updateTimeStr;

        /**
         * 更新人id
         */
        private String updateUserId;
        /**
         * 更新人名称
         */
        private String updateUserName;

        /**
         *
         */
        private String country;
        /**
         *
         */
        private String city;
        /**
         *
         */
        private String cityName;
        /**
         *
         */
        private String matchType;
        /**
         *
         */
        private String matchTypeName;
        /**
         *
         */
        private String postCode;
    }

    @Data
    @NoArgsConstructor
    public static class SelectDTO {
        /**
         * 关键词
         */
        private String searchKeyword;
        /**
         * 是否启用
         */
        private Boolean disabled;

    }
}