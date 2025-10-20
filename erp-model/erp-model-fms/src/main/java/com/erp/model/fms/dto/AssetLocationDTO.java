package com.erp.model.fms.dto;

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
 * 资产位置表请求响应实体
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
*/
@Data
@NoArgsConstructor
public class AssetLocationDTO implements Serializable {


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
          * 名称
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
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * 审批状态
        */
        private String approveStatus;

        /**
        * 审批人ID
        */
        private String approveUserId;

        /**
        * 审批人姓名
        */
        private String approveUserName;

        /**
        * 审批时间
        */
        private LocalDateTime approveTime;

        /**
        * 是否作废
        */
        private Boolean invalidStatus;

        /**
        * 作废备注
        */
        private String invalidRemark;

        /**
        * 位置编码
        */
        private String code;

        /**
        * 位置描述
        */
        private String description;

        /**
        * 地址
        */
            private String address;

        /**
        * 详细地址
        */
        private String detailedAddress;

        /**
        * 备注
        */
        private String remark;


        /**
        * 审核状态名称
        */
        private String approveStatusName;

        /**
        * 作废状态名称
        */
        private String invalidStatusName;

        /**
        * 禁用状态名称
        */
        private String disabledStatusName;

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
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * 审批状态
        */
        private String approveStatus;

        /**
        * 审批人ID
        */
        private String approveUserId;

        /**
        * 审批人姓名
        */
        private String approveUserName;

        /**
        * 审批时间
        */
        private LocalDateTime approveTime;

        /**
        * 是否作废
        */
        private Boolean invalidStatus;

        /**
        * 作废备注
        */
        private String invalidRemark;

        /**
        * 位置编码
        */
        private String code;

        /**
        * 位置描述
        */
        private String description;

        /**
        * 地址
        */
        private String address;

        /**
        * 详细地址
        */
        private String detailedAddress;

        /**
        * 备注
        */
        private String remark;


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
        * 位置描述
        */
        @Size(max = 50,message = "位置描述最大长度不能超过50位")
        private String description;

        /**
        * 地址
        */
        @NotBlank(message = "地址不能为空")
        @Size(max = 200,message = "地址最大长度不能超过200位")
        private String address;

        /**
        * 详细地址
        */
        @Size(max = 200,message = "详细地址最大长度不能超过200位")
        private String detailedAddress;

        /**
        * 备注
        */
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;


    }


}