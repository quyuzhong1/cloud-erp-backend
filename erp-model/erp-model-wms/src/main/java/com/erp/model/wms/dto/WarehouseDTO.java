package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author Lambda
 * @Classname WarehouseDTO
 * @Description TODO
 * @Date 2023-03-16 16:30
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class WarehouseDTO implements Serializable {


    /**
     * 添加仓库
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {

        @NotBlank(message = "金蝶仓库编号不能为空")
        private String kingdeeWarehouseCode;

        /**
         * 名称
         */
        @NotBlank(message = "仓库名称不能为空")
        @Size(max =200, message = "仓库名称最大200字符")
        private String name;

        /**
         * 仓库类型 对应dict 表id
         */
        @NotBlank(message = "仓库类型不能为空")
        private String typeId;

        /**
         * 负责人id
         */
        @NotBlank(message = "仓库负责人不能为空")
        private String chargeId;

        /**
         * 联系人
         */
        @NotBlank(message = "联系人不能为空")
        private String contacts;


        /**
         * 联系人电话
         */
        @NotBlank(message = "联系人电话不能为空")
        private String contactTelNumber;


        /**
         * 状态
         * true 启用
         * false 未启用
         */
        @NotNull(message = "仓库启用状态不能为空")
        private Boolean disabled;


        /**
         * 地址
         */
        private String address;

        /**
         * 组织id 对应 核算公司表id
         */
        @NotBlank(message = "组织id 不能为空")
        private String orgId;


        /**
         * 是否虚拟仓
         * true 是
         */
        @NotNull(message = "是否是虚拟仓不能为空")
        private String isVirtual;
    }



    /**
     * 添加仓库
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends AddDTO {

        @NotBlank(message = "id不能为空")
        private String id;
    }







}
