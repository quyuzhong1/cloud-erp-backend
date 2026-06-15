package com.erp.model.tms.dto;

import java.time.LocalDateTime;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import com.common.business.dto.base.SuperDTO;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 异步任务记录明细请求响应实体
 * </p>
 *
 * @author jack
 * @since 2026-01-28
*/
@Data
@NoArgsConstructor
public class TmsAsyncTaskDetailDTO implements Serializable {



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
        * 主表id
        */
        private String mainId;

        /**
        * 单据id
        */
        private String businessId;

        /**
        * 单据编码
        */
        private String businessCode;

        /**
        * 单据类型
        */
        private String businessType;

        /**
        * 开始时间
        */
        private LocalDateTime startTime;

        /**
        * 结束时间
        */
        private LocalDateTime endTime;

        /**
        * 状态：success=成功,  failed=失败
        */
        private String status;

        /**
        * json
        */
        private String errorData;


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
        * 主表id
        */
        private String mainId;

        /**
        * 单据id
        */
        private String businessId;

        /**
        * 单据编码
        */
        private String businessCode;

        /**
        * 单据类型
        */
        private String businessType;

        /**
        * 开始时间
        */
        private LocalDateTime startTime;

        /**
        * 结束时间
        */
        private LocalDateTime endTime;

        /**
        * 状态：success=成功,  failed=失败
        */
        private String status;

        /**
        * json
        */
        private String errorData;


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
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 单据id
        */
        @NotBlank(message = "单据id不能为空")
        @Size(max = 19,message = "单据id最大长度不能超过19位")
        private String businessId;

        /**
        * 单据编码
        */
        @NotBlank(message = "单据编码不能为空")
        @Size(max = 50,message = "单据编码最大长度不能超过50位")
        private String businessCode;

        /**
        * 单据类型
        */
        @NotBlank(message = "单据类型不能为空")
        @Size(max = 50,message = "单据类型最大长度不能超过50位")
        private String businessType;

        /**
        * 开始时间
        */
        @NotNull(message = "开始时间不能为空")
        private LocalDateTime startTime;

        /**
        * 结束时间
        */
        @NotNull(message = "结束时间不能为空")
        private LocalDateTime endTime;

        /**
        * 状态：success=成功,  failed=失败
        */
        @NotBlank(message = "状态：success=成功,  failed=失败不能为空")
        @Size(max = 50,message = "状态：success=成功,  failed=失败最大长度不能超过50位")
        private String status;

        /**
        * json
        */
        private String errorData;


    }


}