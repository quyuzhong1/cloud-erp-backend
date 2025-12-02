package com.erp.model.oms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 企业达人库请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-12-02
*/
@Data
@NoArgsConstructor
public class KolPartnerInfoDTO implements Serializable {


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
        * 是否启用
        */
        private Boolean disabled;

        /**
        * 备注
        */
        private String remark;

        /**
        * 编号
        */
        private String code;

        /**
        * 达人昵称
        */
        private String nickname;

        /**
        * 达人类型
        */
        private String type;

        /**
        * 合作类型
        */
        private String cooperationType;

        /**
        * 合作日期
        */
        private LocalDate cooperationDate;

        /**
        * 国家ID
        */
        private String countryId;

        /**
        * 国家名称
        */
        private String countryName;

        /**
        * 语言
        */
        private String language;

        /**
        * 邮箱
        */
        private String email;

        /**
        * 联系电话
        */
        private String phone;

        /**
        * 负责人ID
        */
        private String chargeId;

        /**
        * 负责人姓名
        */
        private String chargeName;

        /**
        * 部门ID
        */
        private String deptId;

        /**
        * 部门名称
        */
        private String deptName;

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
        * 是否启用
        */
        private Boolean disabled;

        /**
        * 备注
        */
        private String remark;

        /**
        * 编号
        */
        private String code;

        /**
        * 达人昵称
        */
        private String nickname;

        /**
        * 达人类型
        */
        private String type;

        /**
        * 合作类型
        */
        private String cooperationType;

        /**
        * 合作日期
        */
        private LocalDate cooperationDate;

        /**
        * 国家ID
        */
        private String countryId;

        /**
        * 国家名称
        */
        private String countryName;

        /**
        * 语言
        */
        private String language;

        /**
        * 邮箱
        */
        private String email;

        /**
        * 联系电话
        */
        private String phone;

        /**
        * 负责人ID
        */
        private String chargeId;

        /**
        * 负责人姓名
        */
        private String chargeName;

        /**
        * 部门ID
        */
        private String deptId;

        /**
        * 部门名称
        */
        private String deptName;


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
        * 是否启用
        */
        @NotNull(message = "是否启用不能为空")
        private Boolean disabled;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;

        /**
        * 达人昵称
        */
        @NotBlank(message = "达人昵称不能为空")
        @Size(max = 200,message = "达人昵称最大长度不能超过200位")
        private String nickname;

        /**
        * 达人类型
        */
        @NotBlank(message = "达人类型不能为空")
        @Size(max = 100,message = "达人类型最大长度不能超过100位")
        private String type;

        /**
        * 合作类型
        */
        @NotBlank(message = "合作类型不能为空")
        @Size(max = 100,message = "合作类型最大长度不能超过100位")
        private String cooperationType;

        /**
        * 合作日期
        */
        private LocalDate cooperationDate;

        /**
        * 国家ID
        */
        @NotBlank(message = "国家ID不能为空")
        @Size(max = 19,message = "国家ID最大长度不能超过19位")
        private String countryId;

        /**
        * 国家名称
        */
        @NotBlank(message = "国家名称不能为空")
        @Size(max = 50,message = "国家名称最大长度不能超过50位")
        private String countryName;

        /**
        * 语言
        */
        @NotBlank(message = "语言不能为空")
        @Size(max = 30,message = "语言最大长度不能超过30位")
        private String language;

        /**
        * 邮箱
        */
        @NotBlank(message = "邮箱不能为空")
        @Size(max = 100,message = "邮箱最大长度不能超过100位")
        private String email;

        /**
        * 联系电话
        */
        @NotBlank(message = "联系电话不能为空")
        @Size(max = 20,message = "联系电话最大长度不能超过20位")
        private String phone;

        /**
        * 负责人ID
        */
        @NotBlank(message = "负责人ID不能为空")
        @Size(max = 19,message = "负责人ID最大长度不能超过19位")
        private String chargeId;

        /**
        * 负责人姓名
        */
        @NotBlank(message = "负责人姓名不能为空")
        @Size(max = 50,message = "负责人姓名最大长度不能超过50位")
        private String chargeName;

        /**
        * 部门ID
        */
        @NotBlank(message = "部门ID不能为空")
        @Size(max = 19,message = "部门ID最大长度不能超过19位")
        private String deptId;

        /**
        * 部门名称
        */
        @NotBlank(message = "部门名称不能为空")
        @Size(max = 100,message = "部门名称最大长度不能超过100位")
        private String deptName;


    }

    @Data
    @NoArgsConstructor
    public static class DropDownDTO {

        /**
         * 主键id
         */
        private String  id;

        /**
         * 是否启用
         */
        private Boolean disabled;

        /**
         * 编号
         */
        private String code;

        /**
         * 达人昵称
         */
        private String nickname;

        /**
         * 达人类型
         */
        private String type;
        private String typeName;

        /**
         * 合作类型
         */
        private String cooperationType;
        private String cooperationTypeName;

        /**
         * 国家ID
         */
        private String countryId;

        /**
         * 国家名称
         */
        private String countryName;

    }

    @Data
    @NoArgsConstructor
    public static class SelectDTO {
        /**
         * 关键词
         */
        private String searchKeyword;
    }


}