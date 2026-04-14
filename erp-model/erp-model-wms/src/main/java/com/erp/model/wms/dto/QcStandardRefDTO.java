package com.erp.model.wms.dto;

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
 * 请求响应实体
 * </p>
 *
 * @author wtr
 * @since 2026-03-23
*/
@Data
@NoArgsConstructor
public class QcStandardRefDTO implements Serializable {



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
        * 质检单id
        */
        private String mainId;

        /**
        * 抽样方案id
        */
        private String samplingPlanId;

        /**
        * 建议抽样数量
        */
        private Integer suggestSamplingQty;

        /**
        * 质检项目
        */
        private String inspectItem;

        /**
        * 质检要求
        */
        private String inspectRequirement;

        /**
        * 附件url
        */
        private String attachUrl;

        /**
        * 附件名称
        */
        private String attachName;


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
        * 质检单id
        */
        private String mainId;

        /**
        * 抽样方案id
        */
        private String samplingPlanId;

        /**
        * 建议抽样数量
        */
        private Integer suggestSamplingQty;

        /**
        * 质检项目
        */
        private String inspectItem;

        /**
        * 质检要求
        */
        private String inspectRequirement;

        /**
        * 附件url
        */
        private String attachUrl;

        /**
        * 附件名称
        */
        private String attachName;


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
        * 质检单id
        */
        @NotBlank(message = "质检单id不能为空")
        @Size(max = 19,message = "质检单id最大长度不能超过19位")
        private String mainId;

        /**
        * 抽样方案id
        */
        @NotBlank(message = "抽样方案id不能为空")
        @Size(max = 255,message = "抽样方案id最大长度不能超过255位")
        private String samplingPlanId;

        /**
        * 建议抽样数量
        */
        @NotNull(message = "建议抽样数量不能为空")
        private Integer suggestSamplingQty;

        /**
        * 质检项目
        */
        @NotBlank(message = "质检项目不能为空")
        @Size(max = 255,message = "质检项目最大长度不能超过255位")
        private String inspectItem;

        /**
        * 质检要求
        */
        @NotBlank(message = "质检要求不能为空")
        @Size(max = 255,message = "质检要求最大长度不能超过255位")
        private String inspectRequirement;

        /**
        * 附件url
        */
        @NotBlank(message = "附件url不能为空")
        @Size(max = 255,message = "附件url最大长度不能超过255位")
        private String attachUrl;

        /**
        * 附件名称
        */
        @NotBlank(message = "附件名称不能为空")
        @Size(max = 255,message = "附件名称最大长度不能超过255位")
        private String attachName;


    }


}