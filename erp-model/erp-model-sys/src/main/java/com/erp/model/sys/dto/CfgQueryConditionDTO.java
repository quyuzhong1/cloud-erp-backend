package com.erp.model.sys.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 查询条件配置表请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-01-03
*/
@Data
@NoArgsConstructor
public class CfgQueryConditionDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class MenuDTO  {

        /**
         * 系统菜单
         */
        private String system;

        /**
         * menuCode
         */
        private String menuCode;

        /**
         * 菜单名
         */
        private String name;
    }

    @Data
    @NoArgsConstructor
    public static class ListDTO  {
        /**
         * id
         */
        private String id;

        /**
         * 所属系统
         */
        private String system;

        /**
         * 名称
         */
        private String name;

        /**
         * 编码
         */
        private String code;

        /**
         * 字段值
         */
        private String value;

        /**
         * 字段显示值
         */
        private String label;

        /**
         * 选项配置名称
         */
        private String optionName;

        /**
         * option配置id
         */
        private String queryOptionId;

        /**
         * 控件类型
         */
        private String controls;

        /**
         * 数据类型
         */
        private String dataType;

        /**
         * 日期类型
         */
        private String dateType;

        /**
         * 属性绑定的props
         */
        @TableField(typeHandler = JacksonTypeHandler.class)
        private Map<String,Object> props;

        /**
         * 排序序号
         */
        private Integer index;

        /**
         * 是否扩展字段
         */
        private Boolean isExtend;

        /**
         * 分组名称,用于多个sql跨库查询
         */
        private String groupName;

        /**
         * 显示类型
         */
        private String displayType;

        /**
         * 创建人
         */
        private String createUserName;

        /**
         * 更新人
         */
        private String updateUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;

        /**
         * 自定义option (数组json格式)
         */
        private List<Map<String,Object>> optionList;
    }

    @Data
    @NoArgsConstructor
    public static class SearchParamDTO {

        /**
         * 页面code
         */
        private String code;

        /**
         * 系统目录名
         */
        private String system;

        /**
         * 菜单名
         */
        private String name;

        /**
         * 条件名称
         */
        private String label;

        /**
         * 条件字段
         */
        private String value;
    }

    @Data
    @NoArgsConstructor
    public static class MenuSearchParamDTO {

        /**
         * 菜单code
         */
        private String menuCode;
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
        * 字段值,直接在数据库的查询别名
        */
        private String value;

        /**
        * 页面展示值
        */
        private String label;

        /**
        * 输入类型
        */
        private String type;

        /**
        * 数据类型
        */
        private String dataType;

        /**
         * item
         */
        private Item item;

        private List<Compare> compareList;

        /**
         * 分组名
         */
        private String groupName;

        /**
         * 显示类型
         */
        private String displayType;

        /**
         * 是否扩展字段
         */
        private Boolean isExtend;

    }

    @Data
    @NoArgsConstructor
    public static class Compare {

        /**
         * 比较符号
         */
        private String logic;

        /**
         * 比较符名称
         */
        private String logicName;

    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class Item {

        /**
         * apiUrl
         */
        private String apiUrl;

        /**
         * 请求类型
         */
        private String requestType;

        /**
         * 请求参数
         */
        private Map<String, Object> params;

        /**
         * 下拉框显示值
         */
        private String selectLabel;

        /**
         * 下拉框绑定值
         */
        private String selectValue;

        /**
         * 下拉框禁用情况
         */
        private String selectDisabled;

        /**
         * 远程搜索绑定搜索字段值
         */
        private String searchKeyField;

        /**
         * 日期类型 eg:month,week
         */
        private String dateType;

        private List<Option> optionList;

        /**
         * 前端组件props值
         */
        private Map<String,Object> props;

        /**
         * 属性绑定的props
         */
        private Map<String,Object> fieldProps;
    }

    @Data
    @NoArgsConstructor
    public static class Option {

        //[{"label":"","value":""}]
        private Object label;

        private String value;
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
         * 页面code
         */
        @NotBlank(message = "页面code不能为空")
        private String code;

        /**
         * 字段值
         */
        @NotBlank(message = "字段值不能为空")
        private String value;

        /**
         * 字段显示值
         */
        @NotBlank(message = "字段显示值不能为空")
        private String label;

        /**
         * optionCfgId
         */
        private String queryOptionId;

        /**
         * 前端控件类型
         */
        private String controls;

        /**
         * 数据类型 url:{{sys_url}}common/enumDropDown?type=QueryDataType
         */
        @NotBlank(message = "数据类型不能为空")
        private String dataType;

        /**
         * 日期格式 url:{{sys_url}}common/enumDropDown?type=QueryDateType
         */
        private String dateType;

        /**
         * 前端控件Props属性
         */
        private Map<String,Object> props;

        /**
         * 字段排序Index
         */
        private Integer index;

        /**
         * 是否扩展字段
         */
        private Boolean isExtend;

        /**
         * 分组名称,用于多个sql跨库查询
         */
        private String groupName;

        /**
         * 显示类型{{sys_url}}common/enumDropDown?type=QueryDisplayType
         */
        private String displayType;

        /**
         * 自定义option (数组json格式)
         */
        private List<Map<String,Object>> optionList;
    }
}