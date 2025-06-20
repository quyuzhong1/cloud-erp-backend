package com.erp.model.scm.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 合同管理表请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-06-16
*/
@Data
@NoArgsConstructor
public class ContractInfoDTO implements Serializable {


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
        * 审核状态 
        */
        private String approveStatus;

        /**
        * 单据编号
        */
        private String code;

        /**
        * 服务商id
        */
        private String serviceProviderId;

        /**
        * 审核时间
        */
        private LocalDate approveTime;

        /**
        * 审核人
        */
        private String approveUserId;

        /**
         * 生效状态名称
         */
        private String statusName;

        /**
        * 合同类型,contractType字典
        */
        private String type;
        /**
         * 合同类型名称,contractType字典
         */
        private String typeName;

        /**
        * 生效时间
        */
        private LocalDate effectiveDate;

        /**
        * 失效时间
        */
        private LocalDate expireDate;

        /**
        * 是否禁用
        */
        private Boolean disable;

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
        * 审核状态 
        */
        private String approveStatus;

        /**
        * 单据编号
        */
        private String code;

        /**
        * 服务商id
        */
        private String serviceProviderId;

        /**
        * 审核时间
        */
        private LocalDate approveTime;

        /**
        * 审核人
        */
        private String approveUserId;

        /**
        * 合同类型,contractType字典
        */
        private String type;

        /**
        * 生效时间
        */
        private LocalDate effectiveDate;

        /**
        * 失效时间
        */
        private LocalDate expireDate;

        /**
        * 是否禁用
        */
        private Boolean disable;

        /**
         * 文件路径
         */
        private String attachUrl;
        /**
         * 文件路径
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
    public static class CommonDTO {

        /**
        * 服务商id
        */
        @NotBlank(message = "服务商id不能为空")
        @Size(max = 19,message = "服务商id最大长度不能超过19位")
        private String serviceProviderId;

        /**
        * 合同类型,contractType字典
        */
        @NotBlank(message = "合同类型,contractType字典不能为空")
        @Size(max = 32,message = "合同类型,contractType字典最大长度不能超过32位")
        private String type;

        /**
        * 生效时间
        */
        @NotNull(message = "生效时间不能为空")
        private LocalDate effectiveDate;

        /**
        * 失效时间
        */
        @NotNull(message = "失效时间不能为空")
        private LocalDate expireDate;

        /**
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disable;


        /**
         * 文件不能为空
         */
        @NotNull(message = "文件不能为空")
        private MultipartFile file;
    }


}