package com.erp.model.fms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 资产验收人员关联表请求响应实体
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
*/
@Data
@NoArgsConstructor
public class AssetAcceptPersonDTO implements Serializable {




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
        * 资产验收ID
        */
        private String assetAcceptId;

        /**
        * 人员类型：purchaseDev-采购开发人员,qualityEngineer-质量工程师,structureEngineer-结构工程师,productManager-产品经理,projectManager-项目经理
        */
        private String personType;

        /**
        * 人员ID
        */
        private String userId;

        /**
        * 人员姓名
        */
        private String userName;


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
        * 资产验收ID
        */
        @NotBlank(message = "资产验收ID不能为空")
        @Size(max = 19,message = "资产验收ID最大长度不能超过19位")
        private String assetAcceptId;

        /**
        * 人员类型：purchaseDev-采购开发人员,qualityEngineer-质量工程师,structureEngineer-结构工程师,productManager-产品经理,projectManager-项目经理
        */
        @NotBlank(message = "人员类型：purchaseDev不能为空")
        @Size(max = 20,message = "人员类型：purchaseDev最大长度不能超过20位")
        private String personType;

        /**
        * 人员ID
        */
        @NotBlank(message = "人员ID不能为空")
        @Size(max = 19,message = "人员ID最大长度不能超过19位")
        private String userId;

        /**
        * 人员姓名
        */
        @NotBlank(message = "人员姓名不能为空")
        @Size(max = 50,message = "人员姓名最大长度不能超过50位")
        private String userName;


    }


}