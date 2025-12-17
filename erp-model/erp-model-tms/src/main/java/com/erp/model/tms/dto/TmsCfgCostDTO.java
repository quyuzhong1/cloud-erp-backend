package com.erp.model.tms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 费用管理配置表请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-03-15
*/
@Data
@NoArgsConstructor
public class TmsCfgCostDTO implements Serializable {


    /**
     * 分页查询参数
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
     * 分页查询列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 主键id【可排序】
         */
        private String  id;

        /**
         * 费用归属【可排序】
         */
        private String  dictCostAttribution;

        /**
         * 费用归属名称
         */
        private String  dictCostAttributionName;

        /**
         * 费用分类【可排序】
         */
        private String  dictCostCategory;

        /**
         * 费用分类名称
         */
        private String  dictCostCategoryName;

        /**
         * 费用项
         */
        private String  costName;

        /**
         * 是否默认
         */
        private Boolean isDefault;

        /**
         * 是否分摊（t是，f否）
         */
        private Boolean isAllocate;

        /**
         * 创建人名称【可排序】
         */
        private String  createUserName;

        /**
         * 创建时间【可排序】
         */
        private LocalDateTime createTime;

        /**
         * 更新人名称【可排序】
         */
        private String  updateUserName;

        /**
         * 更新时间【可排序】
         */
        private LocalDateTime  updateTime;
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
        * 费用归属（字典dictCostAttribution）
        */
        private String dictCostAttribution;

        /**
        * 费用分类（字典dictCostCategory）
        */
        private String dictCostCategory;

        /**
        * 费用名称
        */
        private String costName;

        /**
         * 是否默认，true是，false否
         */
        private Boolean isDefault;
        /**
         * 是否分摊（t是，f否）
         */
        private Boolean isAllocate;
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
        * 费用归属（字典dictCostAttribution），/tms/drop/down/dict/list?key=dictCostAttribution
        */
        @NotBlank(message = "费用归属（字典dictCostAttribution）不能为空")
        @Size(max = 32,message = "费用归属（字典dictCostAttribution）最大长度不能超过32位")
        private String dictCostAttribution;

        /**
        * 费用分类（字典dictCostCategory），/tms/drop/down/dict/list?key=dictCostCategory
        */
        @NotBlank(message = "费用分类（字典dictCostCategory）不能为空")
        @Size(max = 32,message = "费用分类（字典dictCostCategory）最大长度不能超过32位")
        private String dictCostCategory;

        /**
        * 费用名称
        */
        @NotBlank(message = "费用名称不能为空")
        @Size(max = 64,message = "费用名称最大长度不能超过64位")
        private String costName;

        /**
         * 是否默认，true是，false否
         */
        @NotNull(message = "默认值不能为空")
        private Boolean isDefault;

        /**
         * 是否分摊（t是，f否）
         */
        private Boolean isAllocate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DropDownParamDTO {

        /**
         * 费用归属,firstMile头程、selfDeliver自发货、declare报关,/tms/drop/down/dict/list?key=dictCostAttribution
         */
        private String dictCostAttribution;
    }

    @Data
    @NoArgsConstructor
    public static class DropDownDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 名称
         */
        private String costName;

        /**
         * 是否默认
         */
        private Boolean isDefault;
        
        /**
         * 费用分类
         */
        private String dictCostCategory;
    }
}