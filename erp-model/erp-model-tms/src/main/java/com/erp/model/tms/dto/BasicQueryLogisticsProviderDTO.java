package com.erp.model.tms.dto;

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
 * 查询物流商信息表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2026-03-31
*/
@Data
@NoArgsConstructor
public class BasicQueryLogisticsProviderDTO implements Serializable {



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
        * 备注
        */
        private String remark;

        /**
        * 物流商名称（中文）
        */
        private String logisticsNameCn;

        /**
        * 物流商名称（英文）
        */
        private String logisticsNameEn;

        /**
        * 公司编码
        */
        private String companyCode;

        /**
        * 查询平台：TRACK123=Track123,KUAIDI100=快递100
        */
        private String trackPlatformType;

        /**
        * 是否注册手机号
        */
        private Boolean isRegisterPhone;


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
     * 下拉列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class ListAllParamDTO {

        /**
         * 物流商名称（中文）
         */
        private String logisticsNameCn;

    }

    /**
     * 下拉列表数据
     */
    @Data
    @NoArgsConstructor
    public static class ListAllVO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 物流商名称（中文）
         */
        private String logisticsNameCn;

        /**
         * 物流商名称（英文）
         */
        private String logisticsNameEn;

        /**
         * 公司编码
         */
        private String companyCode;

        /**
         * 查询平台：TRACK123=Track123,KUAIDI100=快递100
         */
        private String trackPlatformType;

        /**
         * 是否注册手机号
         */
        private Boolean isRegisterPhone;

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
        * 备注
        */
        private String remark;

        /**
        * 物流商名称（中文）
        */
        private String logisticsNameCn;

        /**
        * 物流商名称（英文）
        */
        private String logisticsNameEn;

        /**
        * 公司编码
        */
        private String companyCode;

        /**
        * 查询平台：TRACK123=Track123,KUAIDI100=快递100
        */
        private String trackPlatformType;

        /**
        * 是否注册手机号
        */
        private Boolean isRegisterPhone;


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
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;

        /**
        * 物流商名称（中文）
        */
        @NotBlank(message = "物流商名称（中文）不能为空")
        @Size(max = 100,message = "物流商名称（中文）最大长度不能超过100位")
        private String logisticsNameCn;

        /**
        * 物流商名称（英文）
        */
        @NotBlank(message = "物流商名称（英文）不能为空")
        @Size(max = 100,message = "物流商名称（英文）最大长度不能超过100位")
        private String logisticsNameEn;

        /**
        * 公司编码
        */
        @NotBlank(message = "公司编码不能为空")
        @Size(max = 50,message = "公司编码最大长度不能超过50位")
        private String companyCode;

        /**
        * 查询平台：TRACK123=Track123,KUAIDI100=快递100
        */
        @NotBlank(message = "查询平台：TRACK123=Track123,KUAIDI100=快递100不能为空")
        @Size(max = 100,message = "查询平台：TRACK123=Track123,KUAIDI100=快递100最大长度不能超过100位")
        private String trackPlatformType;

        /**
        * 是否注册手机号
        */
        @NotNull(message = "是否注册手机号不能为空")
        private Boolean isRegisterPhone;


    }


}