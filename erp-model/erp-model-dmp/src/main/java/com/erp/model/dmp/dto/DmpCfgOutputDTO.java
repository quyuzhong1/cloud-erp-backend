package com.erp.model.dmp.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 推送数据配置请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Data
@NoArgsConstructor
public class DmpCfgOutputDTO implements Serializable {


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
         * 推送系统id
         */
        private String systemId;

        /**
         * 推送系统id
         */
        private String systemName;

        /**
         * 外部系统接口转换内部数据id
         */
        private String inputConvertId;

        /**
         * 外部系统接口转换类
         */
        private String inputConvertClass;

        /**
         * (转内数据)外部系统接口转换类型名称
         */
        private String inputConvertType;

        /**
         * 输入类型：api=接口拉取,mq=MQ订阅,db=DB直连
         */
        private String type;

        /**
         * 输入类型id，api取dmp_cfg_api表，mq取dmp_cfg_mq表
         */
        private String typeId;

        /**
         * 输出类型名称
         */
        private String typeName;

        /**
         * 是否禁用
         */
        private Boolean disabled;

        /**
         * 扩展json
         */
        private String extendJson;

        /**
         * 推送速率，一秒推送个数，默认1秒推送3个，小于0不限速
         */
        private Integer pushRate;

        /**
         * 输出类型类
         */
        private String outputClass;

        /**
         * restcloud应用id
         */
        private String appId;
        /**
         * restcloud流程编码
         */
        private String flowCode;
        /**
         * restcloud流程名称
         */
        private String flowName;
        /**
         * 执行系统：DmpCfgInputExecSystemEnum 枚举
         */
        private String execSystem;

        /**
         * restCloud执行流程api路径
         */
        private String execUrl;

        /**
         * restCloud执行流程api路径
         */
        private String fullName;

        /**
         * 创建人【可排序】
         */
        private String createUserName;

        /**
         * 创建时间【可排序】
         */
        private LocalDateTime createTime;

        /**
         * 更新人【可排序】
         */
        private String updateUserName;

        /**
         * 更新人【可排序】
         */
        private LocalDateTime updateTime;
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
         * 推送系统id
         * /dmpBasicSystem/listDmpBasicSystem的code取值
         */
        @NotBlank(message = "推送系统id不能为空")
        @Size(max = 50, message = "推送系统id最大长度不能超过50位")
        private String systemId;

        /**
         * 输入类型：api=接口拉取,mq=MQ订阅,db=DB直连
         */
        @NotBlank(message = "输入类型：api=接口拉取,mq=MQ订阅,db=DB直连不能为空")
        @Size(max = 50, message = "输入类型：api=接口拉取,mq=MQ订阅,db=DB直连最大长度不能超过50位")
        private String type;

        /**
         * 输入类型id，api取dmp_cfg_api表，mq取dmp_cfg_mq表
         */
        @Size(max = 50, message = "输入类型id，api取dmp_cfg_api表，mq取dmp_cfg_mq表最大长度不能超过50位")
        private String typeId;

        /**
         * 是否禁用
         */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;

        /**
         * 扩展json
         */
        private String extendJson;

        /**
         * 推送速率，一秒推送个数，默认1秒推送3个，小于0不限速
         */
        private Integer pushRate;

        /**
         * 输出处理类
         */
        private String outputClass;

        /**
         * 应用id
         */
        private String appId;

        /**
         * 流程编号
         */
        private String flowCode;

        /**
         * 流程名称
         */
        private String flowName;

        /**
         * 执行系统：DmpCfgInputExecSystemEnum 枚举
         */
        private String execSystem;

        /**
         * restCloud执行流程api路径
         */
        private String execUrl;

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
         * 类型名称
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
        private Map<String, String> sqlMap;

    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 推送系统id
         */
        private String systemId;

        /**
         * 推送系统id
         */
        private String systemName;

        /**
         * 外部系统接口转换内部数据id
         */
        private String inputConvertId;

        /**
         * 外部系统接口转换类
         */
        private String inputConvertClass;

        /**
         * (转内数据)外部系统接口转换类型名称
         */
        private String inputConvertType;

        /**
         * 输入类型：api=接口拉取,mq=MQ订阅,db=DB直连
         */
        private String type;

        /**
         * 输入类型id，api取dmp_cfg_api表，mq取dmp_cfg_mq表
         */
        private String typeId;

        /**
         * 输出类型名称
         */
        private String typeName;

        /**
         * 是否禁用
         */
        private Boolean disabled;

        /**
         * 扩展json
         */
        private String extendJson;

        /**
         * 推送速率，一秒推送个数，默认1秒推送3个，小于0不限速
         */
        private Integer pushRate;

        /**
         * 输出类型类
         */
        private String outputClass;

        /**
         * restcloud应用id
         */
        private String appId;
        /**
         * restcloud流程编码
         */
        private String flowCode;
        /**
         * restcloud流程名称
         */
        private String flowName;
        /**
         * 执行系统：DmpCfgInputExecSystemEnum 枚举
         */
        private String execSystem;

        /**
         * restCloud执行流程api路径
         */
        private String execUrl;

        /**
         * 创建人【可排序】
         */
        private String createUserName;

        /**
         * 创建时间【可排序】
         */
        private LocalDateTime createTime;

        /**
         * 更新人【可排序】
         */
        private String updateUserName;

        /**
         * 更新人【可排序】
         */
        private LocalDateTime updateTime;
    }

    /**
     * 导出DTO
     */
    @Data
    @NoArgsConstructor
    public static class ExportDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 推送系统id
         */
        private String systemId;

        /**
         * 外部系统接口转换内部数据id
         */
        private String inputConvertId;

        /**
         * 输入类型：api=接口拉取,mq=MQ订阅,db=DB直连
         */
        private String type;

        /**
         * 输入类型id，api取dmp_cfg_api表，mq取dmp_cfg_mq表
         */
        private String typeId;

        /**
         * 是否禁用
         */
        private Boolean disabled;

        /**
         * 扩展json
         */
        private String extendJson;


    }

    @Data
    @NoArgsConstructor
    public static class ListDmpCfgOutputDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 单据名称
         */
        private String name;
        /**
         * 是否禁用
         */
        private Boolean disabled;
    }

    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class SimplePagingParamDTO extends SortDTO {

        /**
         * 关键字
         */
        private String searchKey;


    }
}