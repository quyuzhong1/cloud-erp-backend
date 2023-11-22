package com.erp.model.wms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.json.JsonObject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

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

        /**
         * 详情
         */
        private List<OverseasProviderWarehouseDTO.UpdateDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 服务商名称
        */
        private String name;

        /**
        * 授权状态 already 已授权 not未授权 cancel 取消授权
        */
        private String authStatus;

        /**
        * 授权时间
        */
        private LocalDateTime authTime;

        /**
        * 授权的信息json格式 例如：{'app_key':'test','token':'test'}
        */
        @TableField(value = "auth_json", typeHandler = JacksonTypeHandler.class)
        private Map<String, Object> authJson;
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
    }
}