package com.erp.model.tms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-03-19
*/
@Data
@NoArgsConstructor
public class TmsWarehouseMappingDTO implements Serializable {

    /**
     * 分页列表参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO {

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
         * 主键id【可排序】
         */
        private String  id;

        /**
         * 仓库代码（物流商）【可排序】
         */
        private String logisticsWarehouseCode;

        /**
         * 仓库id（数大臣）【可排序】
         */
        private String erpWarehouseId;

        /**
         * 仓库名称（数大臣）【可排序】
         */
        private String erpWarehouseName;

        /**
         * 创建人名称【可排序】
         */
        private String createUserName;

        /**
         * 创建时间【可排序】
         */
        private LocalDateTime createTime;

        /**
         * 更新时间【可排序】
         */
        private LocalDateTime updateTime;
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
        * 仓库代码（物流商）
        */
        private String logisticsWarehouseCode;

        /**
        * 仓库id（数大臣）
        */
        private String erpWarehouseId;

        /**
        * 仓库名称（数大臣）
        */
        private String erpWarehouseName;


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
        * 仓库代码（物流商）
        */
        @NotBlank(message = "仓库代码（物流商）不能为空")
        @Size(max = 64,message = "仓库代码（物流商）最大长度不能超过64位")
        private String logisticsWarehouseCode;

        /**
        * 仓库id（数大臣）
        */
        @NotBlank(message = "仓库id（数大臣）不能为空")
        @Size(max = 19,message = "仓库id（数大臣）最大长度不能超过19位")
        private String erpWarehouseId;


    }


}