package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 虚拟仓请求响应实体
 * </p>
 *
 * @author hyj
 * @since 2024-06-02
 */
@Data
@NoArgsConstructor
public class VirtualWarehouseDTO implements Serializable {


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
         * 是否失效 true 失效 false 未失效
         */
        private Boolean disabled;

        /**
         * code
         */
        private String code;

        /**
         * 名称
         */
        private String name;

        private List<VirtualWarehouseChannelDTO.ChannelAddDTO> channelList;
        private List<ThirdMappingDTO.ViewDTO> thirdMappingList;
        private List<String> warehouseIdList;
    }
    @Data
    @NoArgsConstructor
    public static class SearchDTO{

        /**
         * 关联id（如店铺id）,无关联id时传空字符
         */
        private String relationId;

        /**
         * 平台
         */
        private String dictPlatform;

        /**
         * 实体仓Id
         */
        @NotBlank(message = "实体仓不能为空")
        private String warehouseId;
    }
    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class VwDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 是否失效 true 失效 false 未失效
         */
        private Boolean disabled;

        /**
         * code
         */
        private String code;

        /**
         * 名称
         */
        private String name;
    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        @Valid
        private List<VirtualWarehouseChannelDTO.ChannelAddDTO> channelList;
        //        @Valid
        private List<ThirdMappingDTO.AddDTO> thirdMappingList;
        private List<String> warehouseIdList;
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

        private List<VirtualWarehouseChannelDTO.ChannelAddDTO> channelList;
        private List<ThirdMappingDTO.AddDTO> thirdMappingList;
        private List<String> warehouseIdList;

    }

    @Data
    @NoArgsConstructor
    public static class UpdateStateDTO {

        @NotBlank(message = "id不能为空")
        private String id;

        /**
         * true 禁用
         * false 启用
         */
        @NotNull(message = "状态不能为空")
        private Boolean disabled;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 是否失效 true 失效 false 未失效
         */
        private Boolean disabled;

        /**
         * 名称
         */
        @NotBlank(message = "名称不能为空")
        @Size(max = 200, message = "名称最大长度不能超过200位")
        private String name;


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
        private Map<String, String> sqlMap;

    }

    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键
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

        /**
         * 是否失效 true 失效 false 未失效
         */
        private Boolean disabled;
        /**
         * 是否失效 true 失效 false 未失效
         */
        private String disabledName;
        /**
         * code
         */
        private String code;
        /**
         * 名称
         */
        private String name;

        public String getDisabledName() {
            return Objects.equals(disabled, Boolean.TRUE) ? "停用" : "启用";
        }
    }


    @Data
    @NoArgsConstructor
    public static class ShopSelectDTO {

        /**
         * 关键词
         */
        private String searchKeyword;
        /**
         * 平台
         */
        private String dictPlatform;
        /**
         * 是否已授权
         */
        private Boolean showByAuth = false;
        /**
         * 平台
         */
        private List<String> shopIdList;
        /**
         * 虚拟仓id
         */
        private String id;
    }
    @Data
    @NoArgsConstructor
    public static class WarehouseSelectDTO {

        /**
         * 关键词
         */
        private String searchKeyword;
    }

    @Data
    @NoArgsConstructor
    public static class Tree {
        /**
         * 编码
         */
        private String code;
        /**
         * 值
         */
        private String value;
        private Boolean disabled;
        private List<VirtualWarehouseDTO.ChildTree> childTreeList;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChildTree {
        /**
         * 编码
         */
        private String code;
        /**
         * 值
         */
        private String value;
        private Boolean disabled;
        private Boolean platformDisabled;
        private Boolean shopDisabled;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BindChannelDto {
        /**
         * 平台
         */
        private String dictPlatform;
        /**
         * 类型
         */
        private String type;
        /**
         * 虚拟仓id
         */
        private String virtualWarehouseId;
        /**
         * 虚拟仓id
         */
        private String relationId;

    }
    @Data
    @NoArgsConstructor
    public static class SelectDTO {
        private String id;
        private String name;
        private String code;
        private Boolean disabled;
    }


    @Data
    @NoArgsConstructor
    public static class CfgRuleVirtualWarehouseDTO {
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 虚拟仓id
         */
        private String virtualWarehouseId;
        /**
         * 类型
         */
        private String type;
        /**
         * 平台
         */
        private String dictPlatform;
        /**
         * 关联id集合
         */
        private List<String> relationIdList;

    }
}