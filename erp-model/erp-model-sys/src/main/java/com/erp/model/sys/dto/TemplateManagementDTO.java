package com.erp.model.sys.dto;

import java.math.BigDecimal;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.*;

/**
 * <p>
 * 合同模板主表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-07-24
*/
@Data
@NoArgsConstructor
public class TemplateManagementDTO implements Serializable {




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
        * 禁用状态(false:启用,true:禁用)
        */
        private Boolean disabled;

        /**
        * 模板编号
        */
        private String code;

        /**
        * 模板名称
        */
        private String name;

        /**
        * 模板类型： contract=合同模板, shippingLabel=面单模板
        */
        private String type;
        private String typeName;

        /**
        * 业务类型：purchaseContract=采购框架合同,soContract=销售订单合同
        */
        private String bizType;
        private String bizTypeName;

        /**
        * 状态：finished=已发布,not=未发布,failed=发布失败
        */
        private String status;
        private String statusName;

        /**
        * 模板长度(mm)
        */
        private BigDecimal length;

        /**
        * 模板宽度(mm)
        */
        private BigDecimal width;

        /**
        * 前端渲染配置JSON
        */
        private String content;

        /**
        * 模板版本号
        */
        private Integer templateVersion;

        /**
        * 前端渲染配置JSON
        */
        private Boolean isDefault;

        /**
        * 序号
        */
        private Integer index;


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
        * 禁用状态(false:启用,true:禁用)
        */
        private Boolean disabled;

        /**
        * 模板名称
        */
        @NotBlank(message = "模板名称不能为空")
        @Size(max = 256,message = "模板名称最大长度不能超过256位")
        private String name;

        /**
        * 模板类型： contract=合同模板, shippingLabel=面单模板
        */
        @NotBlank(message = "模板类型不能为空")
        private String type;

        /**
        * 业务类型：purchaseContract=采购框架合同,soContract=销售订单合同
        */
        private String bizType;

        /**
        * 状态：finished=已发布,not=未发布,failed=发布失败
        */
        private String status;

        /**
        * 模板长度(mm)
        */
        @NotNull(message = "模板长度(mm)不能为空")
        @Digits(integer = 12, fraction = 4, message = "模板长度(mm)整数位不能超过12位，小数位不能超过4位")
        private BigDecimal length;

        /**
        * 模板宽度(mm)
        */
        @NotNull(message = "模板宽度(mm)不能为空")
        @Digits(integer = 12, fraction = 4, message = "模板宽度(mm)整数位不能超过12位，小数位不能超过4位")
        private BigDecimal width;

        /**
        * 前端渲染配置JSON
        */
        private String content;

        /**
        * 模板版本号
        */
        private Integer templateVersion;

        /**
        * 前端渲染配置JSON
        */
        private Boolean isDefault;

        /**
        * 序号
        */
        private Integer index;

    }


    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO extends BaseDTO{

        /**
         * 禁用状态(false:启用,true:禁用)
         */
        private Boolean disabled;

        /**
         * 模板编号
         */
        private String code;

        /**
         * 模板名称
         */
        private String name;

        /**
         * 模板类型： contract=合同模板, shippingLabel=面单模板
         */
        private String type;
        private String typeName;

        /**
         * 业务类型：purchaseContract=采购框架合同,soContract=销售订单合同
         */
        private String bizType;
        private String bizTypeName;

        /**
         * 状态：finished=已发布,not=未发布,failed=发布失败
         */
        private String status;
        private String statusName;

        /**
         * 模板长度(mm)
         */
        private BigDecimal length;

        /**
         * 模板宽度(mm)
         */
        private BigDecimal width;

        /**
         * 模板版本号
         */
        private Integer templateVersion;

        /**
         * 前端渲染配置JSON
         */
        private Boolean isDefault;

        /**
         * 序号
         */
        private Integer index;

    }

    @Data
    @NoArgsConstructor
    public static class BaseDTO {

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
     *
     */
    @Data
    @NoArgsConstructor
    public static class DisabledDTO {

        @NotEmpty(message = "ids不能为空")
        private List<String> ids;

        @NotNull(message = "状态不能为空")
        private Boolean disabledStatus;

    }



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
         * 类型
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


}