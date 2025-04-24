package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 虚拟库存交易规则表请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-06-03
*/
@Data
@NoArgsConstructor
public class CfgVirtualTransRulesDTO implements Serializable {




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
        * 业务类型
        */
        private String dictBizType;

        /**
        * 仓库选项
        */
        private String warehouseOption;

        /**
        * 库存状态
        */
        private String inventoryStatus;

        /**
        * 交易类型 1表示+，-1表示-
        */
        private Integer transactionMode;

        /**
        * 交易类型描述信息
        */
        private String remark;


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
        * 业务类型
        */
        @NotBlank(message = "业务类型不能为空")
        @Size(max = 32,message = "业务类型最大长度不能超过32位")
        private String dictBizType;

        /**
        * 仓库选项
        */
        @NotBlank(message = "仓库选项不能为空")
        @Size(max = 10,message = "仓库选项最大长度不能超过10位")
        private String warehouseOption;

        /**
        * 库存状态
        */
        @NotBlank(message = "库存状态不能为空")
        @Size(max = 64,message = "库存状态最大长度不能超过64位")
        private String inventoryStatus;

        /**
        * 交易类型 1表示+，-1表示-
        */
        @NotNull(message = "交易类型 1表示+，不能为空")
        private Integer transactionMode;

        /**
        * 交易类型描述信息
        */
        @NotBlank(message = "交易类型描述信息不能为空")
        @Size(max = 64,message = "交易类型描述信息最大长度不能超过64位")
        private String remark;


    }


}