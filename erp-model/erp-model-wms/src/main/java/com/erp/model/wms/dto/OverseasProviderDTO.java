package com.erp.model.wms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.common.business.dto.base.SortDTO;
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
 * 海外物流商请求响应实体
 * </p>
 *
 * @author Luo_wg
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

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 服务商名称
        */
        @NotBlank(message = "服务商名称不能为空")
        @Size(max = 255,message = "服务商名称最大长度不能超过255位")
        private String name;

        /**
        * 授权状态 already 已授权 not未授权 cancel 取消授权
        */
        @NotBlank(message = "授权状态 already 已授权 not未授权 cancel 取消授权不能为空")
        @Size(max = 255,message = "授权状态 already 已授权 not未授权 cancel 取消授权最大长度不能超过255位")
        private String authStatus;

        /**
        * 授权时间
        */
        private LocalDateTime authTime;

        /**
        * 授权的信息json格式 例如：{'app_key':'test','token':'test'}
        */
        @NotBlank(message = "授权的信息json格式 例如：{'app_key':'test','token':'test'}不能为空")
        private String authJson;


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
        private String updateUserIdList;

        /**
         * 更新时间
         */
        private List<LocalDate> updateTimeList;
    }


    /**
     * 列表查询入参
     */
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
}