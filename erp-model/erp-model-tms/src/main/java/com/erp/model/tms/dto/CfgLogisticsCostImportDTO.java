package com.erp.model.tms.dto;

import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import com.common.business.dto.base.SuperDTO;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

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
         * 类型
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
        private String bussinessTypeName;

        /**
        * 配置类型：logistics_supplier=物流商,platform=平台
        */
        private String cfgType;
        private String cfgTypeName;

        /**
        * 配置平台
        */
        private String dictPlatform;
        private String dictPlatformName;

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
        private String costTypeName;

        /**
        * 导入处理：import_update=导入更新,import_add_old=导入新增(按原单),import_add_new=导入新增(按新单)
        */
        private String importType;
        private String importTypeName;

        /**
        * 启用状态
        */
        private Boolean disabled;

        /**
        * 备注
        */
        private String remark;


        /**
        * 创建时间
        */
        private LocalDateTime createTime;

        /**
        * 更新时间
        */
        private LocalDateTime updateTime;

        /**
        * 创建人名称
        */
        private String createUserName;

        /**
        * 更新人名称
        */
        private String updateUserName;


        /**
         * 物流商抬头字段
         */
        private String sourceField;

        /**
         * 物流商明细字段
         */
        private String sourceDetailField;

        /**
         * 是否唯一
         */
        private Boolean isUniqueKey;

        /**
         * 是否绝对值
         */
        private Boolean isAbsoluteValue;

        /**
         * ERP字段id
         */
        private String targetFieldId;

        /**
         * ERP字段
         */
        private String targetField;

        /**
         * ERP字段名称
         */
        private String targetFieldName;

        /**
         * ERP字段类型
         */
        private String targetFieldType;


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
        private String bussinessTypeName;

        /**
        * 配置类型：logistics_supplier=物流商,platform=平台
        */
        private String cfgType;
        private String cfgTypeName;

        /**
        * 配置平台
        */
        private String dictPlatform;
        private String dictPlatformName;

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
        private String costTypeName;

        /**
        * 导入处理：import_update=导入更新,import_add_old=导入新增(按原单),import_add_new=导入新增(按新单)
        */
        private List<String> importTypeList;
        private String importTypeName;

        /**
        * 启用状态
        */
        private Boolean disabled;

        /**
        * 备注
        */
        private String remark;

        /**
         * 明细
         */
        private List<CfgLogisticsCostImportDetailDTO.UpdateDTO> detailList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        @NotEmpty(message = "明细不能为空")
        private List<CfgLogisticsCostImportDetailDTO.AddDTO> detailList;
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

        @NotEmpty(message = "明细不能为空")
        private List<CfgLogisticsCostImportDetailDTO.UpdateDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO extends SuperDTO {

        /**
        * 配置单据 api/tms/drop/down/dict/list?key=cfgCostType
        */
        @NotBlank(message = "配置单据不能为空")
        @Size(max = 50,message = "配置单据最大长度不能超过50位")
        private String bussinessType;

        /**
        * 配置类型：api/tms/common/enumDropDown?type =CfgLogisticsCostImportCfgTyp
        */
        @NotBlank(message = "配置类型平台不能为空")
        @Size(max = 50,message = "配置类型最大长度不能超过50位")
        private String cfgType;

        /**
        * 配置平台
         * 物流商下拉：/api/tms/logisticsSupplier/listAll
         * 销售平台下拉：/api/oms/drop/down/dict/list?key=salesPlatform
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
        @Min(value = 1,message = "行开始最小值为1")
        private Integer headerRow;

        /**
        * 费用来源：api/tms/common/enumDropDown?type =CfgLogisticsCostImportCostType
        */
        @NotBlank(message = "费用来源不能为空")
        @Size(max = 50,message = "费用来源最大长度不能超过50位")
        private String costType;

        /**
        * 导入处理：api/tms/common/enumDropDown?type =CfgLogisticsCostImportImportType
        */
        @NotEmpty(message = "导入处理不能为空")
        private List<String> importTypeList;

        private String importType;

        /**
        * 启用状态
        */
        @NotNull(message = "启用状态不能为空")
        private Boolean disabled;

        /**
        * 备注
        */
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;


    }

    @Data
    @NoArgsConstructor
    public class UpdateDisabledDTO {
        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 是否禁用 ： true 禁用  false 启用
         */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;
    }
}