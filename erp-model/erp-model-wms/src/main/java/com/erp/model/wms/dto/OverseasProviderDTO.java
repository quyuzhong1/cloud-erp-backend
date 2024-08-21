package com.erp.model.wms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;

/**
 * <p>
 * 海外物流商请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
*/
@Data
@NoArgsConstructor
public class OverseasProviderDTO implements Serializable {

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class AuthorizeViewDTO {
        /**
         * code
         */
        private String code;
        /**
         * 服务商名称
         */
        private String name;

        /**
         * 平台账号
         */
        private String platformAccount;

        /**
         * 仓库简称
         */
        private String shortName;

        /**
         * APPtoken
         */
        private String appToken;

        /**
         * AppKey
         */
        private String appKey;

        /**
         * 启用时间
         */
        private LocalDate enabledDate;
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
        * code
        */
        private String code;

        /**
        * 服务商名称
        */
        private String name;

        /**
        * 授权状态 already 已授权 not未授权 cancel 取消授权
        */
        private String authStatus;

        /**
        * 授权状态中文名
        */
        private String authStatusName;

        /**
        * 授权时间
        */
        private LocalDateTime authTime;

        /**
         * 详情
         */
        private List<OverseasProviderWarehouseDTO.ViewDTO> detailList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * 服务商编号  wms/common/enumDropDown?type=OmsPlatform
         */
        @NotBlank(message = "服务商编号不能为空")
        private String code;

        /**
         * 服务商名称
         */
        private String name;

        /**
         * 平台账号
         */
        @NotBlank(message = "平台账号不能为空")
        private String platformAccount;

        /**
         * 仓库简称
         */
        @NotBlank(message = "仓库简称不能为空")
        private String shortName;


    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {
        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * code
         */
        private String code;

        /**
         * 详情
         */
        private List<OverseasProviderWarehouseDTO.UpdateDTO> detailList;
    }
    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateThirdWarehouseDTO {

        @NotBlank(message = "id不能为空")
        private String id;

        /**
         * 平台账号
         */
        @NotBlank(message = "平台账号不能为空")
        private String platformAccount;

        /**
         * 仓库简称
         */
        @NotBlank(message = "仓库简称不能为空")
        private String shortName;

    }

    /**
     * 列表查询入参
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        /**
         * 服务商名称
         */
        private String name;

        /**
         * 授权状态
         */
        private String authStatus;

        /**
         * 更新人
         */
        private List<String> updateUserIdList;

        /**
         * 更新时间
         */
        private List<LocalDate> updateTimeList;
    }


    /**
     * 列表查询入参
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 服务商编号
         */
        private String code;
        /**
         * 服务商名称
         */
        private String name;

        /**
         * 平台账号
         */
        private String platformAccount;

        /**
         * 仓库简称
         */
        private String shortName;
        /**
         * 授权状态 already 已授权 not未授权 cancel 取消授权
         */
        private String authStatus;
        /**
         * 授权状态中文名
         */
        private String authStatusName;
        /**
         * 授权时间
         */
        private LocalDateTime authTime;
        /**
         * 修改时间
         */
        private LocalDateTime updateTime;
        /**
         * 修改人
         */
        private String updateUserId;
        /**
         * 修改人中文名
         */
        private String updateUserName;
    }

    /**
     * 授权参数
     */
    @Data
    @NoArgsConstructor
    public static class AuthorizeParamDTO {
        /**
         * id
         */
        @NotBlank(message = "id不能为空")
        private String id;

        /**
         * 授权的信息json格式 例如：{'app_key':'test','token':'test'}
         */
        @NotNull(message = "授权的信息不能为空")
        private Map<String, Object> authJson;
        /**
         * 启用时间
         */
        @NotNull(message = "启用时间不能为空")
        private LocalDate enabledDate;

    }

    /**
     * 仓库信息
     */
    @Data
    @NoArgsConstructor
    public static class WarehouseDTO {
        /**
         * ERP仓库id
         */
        private String warehouseId;
        /**
         * ERP仓库名称
         */
        private String warehouseName;
        /**
         * ERP仓库编码
         */
        private String warehouseCode;
        /**
         * 第三方平台仓库编码
         */
        private String platformWarehouseCode;
        /**
         * 第三方平台仓库名称
         */
        private String platformWarehouseName;
        /**
         * 所属国家二字码
         */
        private String country;
        /**
         * 国家中文
         */
        private String countryName;
    }

    /**
     * 列表查询入参
     */
    @Data
    @NoArgsConstructor
    public static class ListWithWarehouseDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 服务商编号
         */
        private String code;
        /**
         * 服务商名称
         */
        private String name;
        /**
         * 授权状态 already 已授权 not未授权 cancel 取消授权
         */
        private String authStatus;
        /**
         * 授权状态中文名
         */
        private String authStatusName;
        /**
         * 授权时间
         */
        private LocalDateTime authTime;
        /**
         * 详情ID
         */
        private String detailId;
        /**
         * ERP仓库id
         */
        private String warehouseId;
        /**
         * ERP仓库名称
         */
        private String warehouseName;
        /**
         * ERP仓库编码
         */
        private String warehouseCode;
        /**
         * 第三方平台仓库编码
         */
        private String platformWarehouseCode;
        /**
         * 第三方平台仓库名称
         */
        private String platformWarehouseName;
        /**
         * 所属国家二字码
         */
        private String country;
        /**
         * 国家中文
         */
        private String countryName;
    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class FeignDTO {

        /**
         * 第三方仓库id
         */
        private String overseasProviderWarehouseId;

        /**
         * code
         */
        private String code;

        /**
         * 第三方仓库编码
         */
        private String platformWarehouseCode;
        /**
         * 第三方仓库名称
         */
        private String platformWarehouseName;

        /**
         * ERP系统仓库id
         */
        private String warehouseId;
        /**
         * ERP系统仓库
         */
        private String warehouseCode;
        /**
         * ERP系统仓库
         */
        private String warehouseName;
        /**
         * 禁用状态
         */
        private Boolean disabled;
    }
}