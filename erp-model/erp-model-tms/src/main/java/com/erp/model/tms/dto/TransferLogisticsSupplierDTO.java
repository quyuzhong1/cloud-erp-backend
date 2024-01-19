package com.erp.model.tms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.common.business.dto.base.SortDTO;
import com.erp.model.tms.enums.LogisticsSupplierTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 物理商表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
*/
@Data
@NoArgsConstructor
public class TransferLogisticsSupplierDTO implements Serializable {

    @Data
    @NoArgsConstructor
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




    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 物流商
         */
        private String name;
        /**
         * 授权状态集合
         */
        private List<String> authStatusList;

        /**
         * 启用状态集合
         */
        private List<Boolean> disabledList;

        /**
         * 创建人
         */
        private List<String> createUserIdList;

        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;

        /**
         * 更新时间
         */
        private List<LocalDate> updateTimeList;


    }

    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        private List<String> ids;
    }

    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {
        /**
         * id
         */
        private String id;

        /**
         * 报关物流商名
         */
        private String name;

        /**
         * 禁用状态 false 未禁用
         */
        private Boolean disabled;

        /**
         * 启用状态名
         */
        private String disabledName;

        /**
         * 授权的平台
         */
        private String logisticsPlatform;

        /**
         * 授权状态
         */
        private String authStatus;

        /**
         * 授权状态名
         */
        private String authStatusName;

        /**
         * 创建人
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
    }


    @Data
    @NoArgsConstructor
    public static class AuthDTO {
        /**
         * 主键id
         */
        private String id;


        /**
         * 物流商id
         */
        private String mainId;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 名称
         */
        private String supplierName;

        /**
         * 是否禁用 true 禁用
         */
        private Boolean disabled;

        /**
         * 授权状态
         */
        private String authStatus;

        /**
         * 授权的平台
         */
        private String logisticsPlatform;


        /**
         * 授权的id
         */
        private String authId;
    }

    @Data
    @NoArgsConstructor
    public static class ChannelViewDTO {
        /**
         * 渠道列表
         */
        private List<TransferLogisticsChannelDTO.ViewDTO> channelList;

    }


    /**
     * 基础信息
     */
    @Data
    @NoArgsConstructor
    public static class BaseDTO {
        /**
         * 渠道id
         */
        private String id;


        private String mainId;

        private String sourceId;

        /**
         * 渠道名
         */
        private String name;

        /**
         * 物流商名
         */
        private String logisticsSupplierName;

        private String logisticsSupplierId;

        /**
         * 渠道代码
         */
        private String code;

        /**
         * 渠道分拣码
         */
        private String sortingCode;

        /**
         * 渠道时效
         */
        private String effectiveTimeStr;


        /**
         * 运费模板名
         */
        private String shippingTemplateName;

        private Boolean disabled;

        /**
         * 平台编码
         */
        private String logisticsPlatform;

        /**
         * 平台是否允许打印
         */
        private Boolean isPrintPlatform;
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
        private String id;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 名称
         */
        private String supplierName;

        /**
         * 是否禁用 true 禁用
         */
        private Boolean disabled;

        /**
         * 授权状态
         */
        private String authStatus;

        /**
         * 授权时间
         */
        private LocalDateTime authTime;


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


    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDisabledDTO  {

        private String supplierId;

        private Boolean disabled;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 供应商id
         * 来源 http://172.16.100.11:3002/project/83/interface/api/14038
         */
        @NotBlank(message = "供应商id不能为空")
        private String supplierId;


    }


}