package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 数据对比对比加工临时表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-03-20
*/
@Data
@NoArgsConstructor
public class WmsDataCompareTempDTO implements Serializable {




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
        * 任务id
        */
        private String taskId;

        /**
        * 主数据类型：system=系统数据，import=导入数据
        */
        private String mainDataType;

        /**
        * 对比状态：wait=待对比，finish=对比完成
        */
        private String compareStatus;

        /**
        * 差异字段，多个用逗号隔开
        */
        private String diffFields;

        /**
        * 主键字段值
        */
        private String pkFieldValue;

        /**
        * 系统数据id
        */
        private String systemDataId;

        /**
        * 对比结果：same=完全一致，exceed=系统多单，miss=系统漏单，diff=差异
        */
        private String compareResult;

        /**
        * 导入数据json
        */
        private String importDataJson;

        /**
        * 系统数据json
        */
        private String systemDataJson;


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
        * 任务id
        */
        @NotBlank(message = "任务id不能为空")
        @Size(max = 19,message = "任务id最大长度不能超过19位")
        private String taskId;

        /**
        * 主数据类型：system=系统数据，import=导入数据
        */
        @NotBlank(message = "主数据类型：system=系统数据，import=导入数据不能为空")
        @Size(max = 50,message = "主数据类型：system=系统数据，import=导入数据最大长度不能超过50位")
        private String mainDataType;

        /**
        * 对比状态：wait=待对比，finish=对比完成
        */
        @NotBlank(message = "对比状态：wait=待对比，finish=对比完成不能为空")
        @Size(max = 50,message = "对比状态：wait=待对比，finish=对比完成最大长度不能超过50位")
        private String compareStatus;

        /**
        * 差异字段，多个用逗号隔开
        */
        @NotBlank(message = "差异字段，多个用逗号隔开不能为空")
        private String diffFields;

        /**
        * 主键字段值
        */
        @NotBlank(message = "主键字段值不能为空")
        private String pkFieldValue;

        /**
        * 系统数据id
        */
        @NotBlank(message = "系统数据id不能为空")
        @Size(max = 19,message = "系统数据id最大长度不能超过19位")
        private String systemDataId;

        /**
        * 对比结果：same=完全一致，exceed=系统多单，miss=系统漏单，diff=差异
        */
        @NotBlank(message = "对比结果：same=完全一致，exceed=系统多单，miss=系统漏单，diff=差异不能为空")
        @Size(max = 50,message = "对比结果：same=完全一致，exceed=系统多单，miss=系统漏单，diff=差异最大长度不能超过50位")
        private String compareResult;

        /**
        * 导入数据json
        */
        @NotBlank(message = "导入数据json不能为空")
        private String importDataJson;

        /**
        * 系统数据json
        */
        @NotBlank(message = "系统数据json不能为空")
        private String systemDataJson;


    }


}