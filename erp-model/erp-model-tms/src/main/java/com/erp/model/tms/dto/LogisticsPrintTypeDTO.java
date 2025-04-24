package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * <p>
 * 面板打印设置表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
*/
@Data
@NoArgsConstructor
public class LogisticsPrintTypeDTO implements Serializable {




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
        * 物流渠道id
        */
        private String logisticsChannelId;

        /**
        * 打印类型
        */
        private String printType;

        /**
        * 打印类型名称
        */
        private String printTypeName;

        /**
        * 标签类型
        */
        private String labelType;

        /**
        * 标签类型名称
        */
        private String labelTypeName;


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
        * 打印类型 来源  http://172.16.100.11:3002/project/128/interface/api/25522 key=printType
         * addressBill 地址单
         * allocateCargoBill  配货单
         * customsBill  报关单
        */
        private String printType;

        /**
        * 标签类型  来源  http://172.16.100.11:3002/project/128/interface/api/25522 key=labelType
         * authority 官方
         * custom 自定义
        */
        @NotBlank(message = "标签类型不能为空")
        private String labelType;


    }


}