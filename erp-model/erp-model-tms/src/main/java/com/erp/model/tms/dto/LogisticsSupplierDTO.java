package com.erp.model.tms.dto;

import com.common.business.dto.base.SortDTO;
import com.erp.model.tms.enums.LogisticsSupplierTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 物理商表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Data
@NoArgsConstructor
public class LogisticsSupplierDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class TabListDTO {

        /**
         * 类型
         */
        private String tabFlag;

        /**
         * 类型名
         */
        private String tabName;

        /**
         * 数量
         */
        private Integer count;

    }

    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {


        /**
         * 物流商类型
         */
        @NotBlank(message = "物流类型不能为空")
        private String type;

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
         * 物流商名
         */
        private String name;

        /**
         * 类型
         */
        private LogisticsSupplierTypeEnum type;

        /**
         * 类型名称
         */
        private String typeName;

        /**
         * 禁用状态 false 未禁用
         */
        private Boolean disabled;

        /**
         * 启用状态名
         */
        private String disabledName;

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

        /**
         * 最新授权时间
         */
        private LocalDateTime authTime;


    }


    @Data
    @NoArgsConstructor
    public static class AuthDTO {
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
         * 类型
         */
        private String type;

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
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名
         */
        private String warehouseName;

        /**
         * 渠道列表
         */
        private List<LogisticsChannelDTO.BaseDTO> channelList;

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
         * 类型
         */
        private String type;

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


        /**
         * 类型
         * 来源 http://172.16.100.11:3002/project/128/interface/api/25522  key=logisticsSupplierType
         */
        @NotNull(message = "类型不能为空")
        private LogisticsSupplierTypeEnum type;


    }


}