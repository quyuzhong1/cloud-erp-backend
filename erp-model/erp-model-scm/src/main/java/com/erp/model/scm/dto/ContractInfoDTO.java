package com.erp.model.scm.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
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

         /**
          * 主键id
          */
         private List<String> ids;

     }
    /**
    * 分页列表
    */
    @Data
    @NoArgsConstructor
    public static class ListDTO extends BaseDTO{
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
        private String serviceProviderName;

        /**
        * 审核时间
        */
        private LocalDate approveTime;

        /**
        * 审核人
        */
        private String approveUserId;
        private String approveUserName;

        /**
         * 生效状态名称
         */
        private String status;
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
        private String disableName;

        /**
        * 审核状态名称
        */
        private String approveStatusName;

        private List<String> attachmentUrlList;
        private List<String> attachmentNameList;
    }


    @Data
    @NoArgsConstructor
    public static class BaseDTO {
        /**
         * id
         */
        private String id;

        /**
         * 创建人id
         */
        private String createUserId;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 修改人id
         */
        private String updateUserId;

        /**
         * 修改人名称
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
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
        * 单据编号
        */
        private String code;

        /**
        * 服务商id
        */
        private String serviceProviderId;
        private String serviceProviderName;

        /**
        * 合同类型,contractType字典
        */
        private String type;
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
         * 生效状态名称
         */
        private String status;
        private String statusName;

        private List<String> attachmentUrlList;
        private List<String> attachmentNameList;
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
        @NotBlank(message = "服务商不能为空")
        @Size(max = 19,message = "服务商最大长度不能超过19位")
        private String serviceProviderId;

        /**
        * 合同类型,contractType字典
        */
        @NotBlank(message = "合同类型不能为空")
        @Size(max = 32,message = "合同类型最大长度不能超过32位")
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
        @NotEmpty(message = "附件不能为空")
        @Size(max = 1, message = "支持1个附件上传")
        private List<String> attachmentUrlList;
        @NotEmpty(message = "附件不能为空")
        @Size(max = 1, message = "支持1个附件上传")
        private List<String> attachmentNameList;
    }


    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class EnableStatusDTO {

        @NotEmpty(message = "至少选择1个")
        private List<String> ids;

        @NotNull(message = "状态不能为空")
        private Boolean disabled;

    }

    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class ListAttachDTO {

        private String id;

        private String type;

        private String serviceProviderName;

        private String typeName;

        private String attachUrl;

        private String attachName;

    }


}