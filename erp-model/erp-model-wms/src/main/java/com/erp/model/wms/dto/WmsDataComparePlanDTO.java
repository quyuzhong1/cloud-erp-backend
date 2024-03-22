package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 数据对比映射方案请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-03-20
*/
@Data
@NoArgsConstructor
public class WmsDataComparePlanDTO implements Serializable {




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
        * 映射名称
        */
        private String name;

        /**
        * 单据类型：soOutstock=销售出库单，fbaShipment=FBA货件签收，overseasInbound=第三方仓货件签收
        */
        private String billType;

        /**
        * 导入数据字段映射json串
        */
        private String importDataMapping;


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
        * 映射名称
        */
        @NotBlank(message = "映射名称不能为空")
        @Size(max = 255,message = "映射名称最大长度不能超过255位")
        private String name;

        /**
        * 单据类型：soOutstock=销售出库单，fbaShipment=FBA货件签收，overseasInbound=第三方仓货件签收
        */
        @NotBlank(message = "单据类型：soOutstock=销售出库单，fbaShipment=FBA货件签收，overseasInbound=第三方仓货件签收不能为空")
        @Size(max = 50,message = "单据类型：soOutstock=销售出库单，fbaShipment=FBA货件签收，overseasInbound=第三方仓货件签收最大长度不能超过50位")
        private String billType;

        /**
        * 导入数据字段映射json串
        */
        @NotBlank(message = "导入数据字段映射json串不能为空")
        private String importDataMapping;


    }
    /**
     * 导入数据字段映射json串对应实体
     */
     @Data
     @NoArgsConstructor
     public static class ImportDataMappingDTO  {

    	 /**
    	 * 系统数据字段
    	 */
    	private String systemField;
    	 /**
    	 * 导入数据字段
    	 */
    	private String importField;
    	 /**
    	 * 唯一键标识，true或false
    	 */
    	private Boolean pkFlag;
     }

}