package com.erp.model.tms.dto;

import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import com.common.business.dto.base.SuperDTO;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 费用项配置请求响应实体
 * </p>
 *
 * @author jack
 * @since 2026-01-20
*/
@Data
@NoArgsConstructor
public class CfgLogisticsCostImportDTO implements Serializable {



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
        private Map<String,String> sqlMap;

     }


    /**
    * 分页列表
    */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 单据编码
        */
        private String code;

        /**
        * 配置单据
        */
        private String bussinessType;

        /**
        * 配置类型：logistics_supplier=物流商,platform=平台
        */
        private String cfgType;

        /**
        * 配置平台
        */
        private String dictPlatform;

        /**
        * 识别名称
        */
        private String name;

        /**
        * sheet名称
        */
        private String sheetName;

        /**
        * 行开始
        */
        private Integer headerRow;

        /**
        * 费用来源：api=API,excel=线下表格
        */
        private String costType;

        /**
        * 导入处理：import_update=导入更新,import_add_old=导入新增(按原单),import_add_new=导入新增(按新单)
        */
        private String importType;

        /**
        * 启用状态
        */
        private Boolean disabled;

        /**
        * 备注
        */
        private String remark;


        /**
        * 审核状态名称
        */
        private String approveStatusName;


        /**
        * 创建时间
        */
        private LocalDateTime createTime;

        /**
        * 创建人名称
        */
        private String createUserName;

    }


    /**
    * 导出Excel
    */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
        * 勾选的id集合
        */
        private List<String> ids;
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
        * 单据编码
        */
        private String code;

        /**
        * 配置单据
        */
        private String bussinessType;

        /**
        * 配置类型：logistics_supplier=物流商,platform=平台
        */
        private String cfgType;

        /**
        * 配置平台
        */
        private String dictPlatform;

        /**
        * 识别名称
        */
        private String name;

        /**
        * sheet名称
        */
        private String sheetName;

        /**
        * 行开始
        */
        private Integer headerRow;

        /**
        * 费用来源：api=API,excel=线下表格
        */
        private String costType;

        /**
        * 导入处理：import_update=导入更新,import_add_old=导入新增(按原单),import_add_new=导入新增(按新单)
        */
        private String importType;

        /**
        * 启用状态
        */
        private Boolean disabled;

        /**
        * 备注
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
    public static class CommonDTO extends SuperDTO {

        /**
        * 配置单据
        */
        @NotBlank(message = "配置单据不能为空")
        @Size(max = 50,message = "配置单据最大长度不能超过50位")
        private String bussinessType;

        /**
        * 配置类型：logistics_supplier=物流商,platform=平台
        */
        @NotBlank(message = "配置类型：logistics_supplier=物流商,platform=平台不能为空")
        @Size(max = 50,message = "配置类型：logistics_supplier=物流商,platform=平台最大长度不能超过50位")
        private String cfgType;

        /**
        * 配置平台
        */
        @NotBlank(message = "配置平台不能为空")
        @Size(max = 50,message = "配置平台最大长度不能超过50位")
        private String dictPlatform;

        /**
        * 识别名称
        */
        @NotBlank(message = "识别名称不能为空")
        @Size(max = 50,message = "识别名称最大长度不能超过50位")
        private String name;

        /**
        * sheet名称
        */
        @NotBlank(message = "sheet名称不能为空")
        @Size(max = 50,message = "sheet名称最大长度不能超过50位")
        private String sheetName;

        /**
        * 行开始
        */
        @NotNull(message = "行开始不能为空")
        private Integer headerRow;

        /**
        * 费用来源：api=API,excel=线下表格
        */
        @NotBlank(message = "费用来源：api=API,excel=线下表格不能为空")
        @Size(max = 50,message = "费用来源：api=API,excel=线下表格最大长度不能超过50位")
        private String costType;

        /**
        * 导入处理：import_update=导入更新,import_add_old=导入新增(按原单),import_add_new=导入新增(按新单)
        */
        @NotBlank(message = "导入处理：import_update=导入更新,import_add_old=导入新增(按原单),import_add_new=导入新增(按新单)不能为空")
        @Size(max = 100,message = "导入处理：import_update=导入更新,import_add_old=导入新增(按原单),import_add_new=导入新增(按新单)最大长度不能超过100位")
        private String importType;

        /**
        * 启用状态
        */
        @NotNull(message = "启用状态不能为空")
        private Boolean disabled;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;


    }


}