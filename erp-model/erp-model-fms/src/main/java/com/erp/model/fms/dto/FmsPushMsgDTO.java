package com.erp.model.fms.dto;

import com.common.business.dto.base.SortDTO;
import java.util.List;
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
 * 本地推送消息表请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-12-30
*/
@Data
@NoArgsConstructor
public class FmsPushMsgDTO implements Serializable {



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
        * 目标系统
        */
        private String targetPlatform;

        /**
        * 来源系统
        */
        private String sourcePlatform;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 来源编号
        */
        private String sourceCode;

        /**
        * 操作类型
        */
        private String syncOperate;

        /**
        * 推送数据
        */
        private String pushData;

        /**
        * 备注
        */
        private String remark;

        /**
        * 父id
        */
        private String parentId;


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
        * 目标系统
        */
        private String targetPlatform;

        /**
        * 来源系统
        */
        private String sourcePlatform;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 来源编号
        */
        private String sourceCode;

        /**
        * 操作类型
        */
        private String syncOperate;

        /**
        * 推送数据
        */
        private String pushData;

        /**
        * 备注
        */
        private String remark;

        /**
        * 父id
        */
        private String parentId;


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
        * 目标系统
        */
        @NotBlank(message = "目标系统不能为空")
        @Size(max = 31,message = "目标系统最大长度不能超过31位")
        private String targetPlatform;

        /**
        * 来源系统
        */
        @NotBlank(message = "来源系统不能为空")
        @Size(max = 31,message = "来源系统最大长度不能超过31位")
        private String sourcePlatform;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 31,message = "来源类型最大长度不能超过31位")
        private String sourceType;

        /**
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 19,message = "来源id最大长度不能超过19位")
        private String sourceId;

        /**
        * 来源编号
        */
        @NotBlank(message = "来源编号不能为空")
        @Size(max = 200,message = "来源编号最大长度不能超过200位")
        private String sourceCode;

        /**
        * 操作类型
        */
        @NotBlank(message = "操作类型不能为空")
        @Size(max = 63,message = "操作类型最大长度不能超过63位")
        private String syncOperate;

        /**
        * 推送数据
        */
        private String pushData;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 500,message = "备注最大长度不能超过500位")
        private String remark;

        /**
        * 父id
        */
        @NotBlank(message = "父id不能为空")
        @Size(max = 19,message = "父id最大长度不能超过19位")
        private String parentId;


    }


}