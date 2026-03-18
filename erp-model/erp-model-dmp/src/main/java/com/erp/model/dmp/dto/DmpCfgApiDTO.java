package com.erp.model.dmp.dto;

import com.common.business.dto.base.SortDTO;
import java.util.List;

import com.erp.model.dmp.enums.DmpCfgOutputTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import com.common.business.dto.base.SuperDTO;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 输入输出api信息请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2026-03-17
*/
@Data
@NoArgsConstructor
public class DmpCfgApiDTO implements Serializable {



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
        * 系统id
        */
        private String systemId;

        /**
        * 输入输出类型：input=输入，output=输出
        */
        private String type;

        /**
        * api类型
        */
        private String apiType;

        /**
        * api名称
        */
        private String name;

        /**
        * api实现类
        */
        private String apiClass;

        /**
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * 单据业务类型:子任务为空
        */
        private String billType;


        /**
        * 审核状态名称
        */
        private String approveStatusName;


        /**
        * 创建时间
        */
        private LocalDateTime createTime;

        /**
        * 创建人名称
        */
        private String createUserName;

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
        * 系统id
        */
        private String systemId;

        /**
        * 输入输出类型：input=输入，output=输出
        */
        private String type;

        /**
        * api类型
        */
        private String apiType;

        /**
        * api名称
        */
        private String name;

        /**
        * api实现类
        */
        private String apiClass;

        /**
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * 单据业务类型:子任务为空
        */
        private String billType;

        /**
         * 输入/输出类
         */
        private String cfgType = "api";

        /**
         * 输入/输出类名称
         */
        private String cfgTypeName = DmpCfgOutputTypeEnum.getName("api");
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
    public static class CommonDTO extends SuperDTO {

        /**
        * 系统id
        */
        @NotBlank(message = "系统id不能为空")
        @Size(max = 50,message = "系统id最大长度不能超过50位")
        private String systemId;

        /**
        * 输入输出类型：input=输入，output=输出
        */
        @NotBlank(message = "输入输出类型：input=输入，output=输出不能为空")
        @Size(max = 50,message = "输入输出类型：input=输入，output=输出最大长度不能超过50位")
        private String type;

        /**
        * api类型
        */
        @NotBlank(message = "api类型不能为空")
        @Size(max = 255,message = "api类型最大长度不能超过255位")
        private String apiType;

        /**
        * api名称
        */
        @NotBlank(message = "api名称不能为空")
        @Size(max = 50,message = "api名称最大长度不能超过50位")
        private String name;

        /**
        * api实现类
        */
        @NotBlank(message = "api实现类不能为空")
        @Size(max = 50,message = "api实现类最大长度不能超过50位")
        private String apiClass;

        /**
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;

        /**
        * 单据业务类型:子任务为空
        */
        @NotBlank(message = "单据业务类型:子任务为空不能为空")
        @Size(max = 64,message = "单据业务类型:子任务为空最大长度不能超过64位")
        private String billType;


    }


}