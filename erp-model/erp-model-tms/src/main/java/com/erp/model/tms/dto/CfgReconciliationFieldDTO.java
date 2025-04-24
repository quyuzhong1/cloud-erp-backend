package com.erp.model.tms.dto;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 对账字段配置表请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2024-03-25
 */
@Data
@NoArgsConstructor
public class CfgReconciliationFieldDTO implements Serializable {


    public static final String FORMAT = "{}_{}";

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
         * 核对类型
         * 来源:/common/enumDropDown?type=CfgReconciliationType
         */
        private String reconciliationType;

        /**
         * 核对类型名称
         */
        private String reconciliationTypeName;

        /**
         * 物流商
         * 来源接口: <a href="http://172.16.100.11:3002/project/128/interface/api/28051">物流商列接口</a>
         */
        private String thirdName;

        /**
         * 物流商代号
         * 来源接口: <a href="http://172.16.100.11:3002/project/128/interface/api/28051">物流商列接口</a>
         */
        private String thirdCode;

        /**
         * 第三方字段名称
         */
        private String thirdFieldName;

        /**
         * ERP字段名称
         * 来源接口: <a href="http://172.16.100.11:3002/project/128/interface/api/31515">数大臣字段列表</a>
         */
        private String erpFieldName;

        /**
         * ERP字段来源类型
         * 来源接口: <a href="http://172.16.100.11:3002/project/128/interface/api/31515">数大臣字段列表</a>
         */
        private String sourceType;

        /**
         * ERP字段来源ID
         * 来源接口: <a href="http://172.16.100.11:3002/project/128/interface/api/31515">数大臣字段列表</a>
         */
        private String sourceId;

        /**
         * 启用状态
         */
        private Boolean status;

        /**
         * 备注
         */
        private String remark;


    }

    /**
     * 新增
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
     * 修改
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
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
         * 核对类型
         * 来源:/common/enumDropDown?type=CfgReconciliationType
         */
        @NotBlank(message = "核对类型不能为空")
        @Size(max = 64, message = "核对类型最大长度不能超过64位")
        private String reconciliationType;

        /**
         * 物流商
         * 来源接口: <a href="http://172.16.100.11:3002/project/83/interface/api/31895">物流商列接口</a>
         */
        @NotBlank(message = "物流商不能为空")
        @Size(max = 64, message = "物流商最大长度不能超过64位")
        private String thirdName;

        /**
         * 物流商代号
         * 来源接口: <a href="http://172.16.100.11:3002/project/83/interface/api/31895">物流商列接口</a>
         */
        @NotBlank(message = "物流商代号不能为空")
        @Size(max = 19, message = "物流商代号最大长度不能超过19位")
        private String thirdCode;

        /**
         * 物流商字段名称
         */
        @NotBlank(message = "物流商字段名称不能为空")
        @Size(max = 64, message = "物流商字段名称最大长度不能超过64位")
        private String thirdFieldName;

        /**
         * ERP字段名称
         * 来源接口: <a href="http://172.16.100.11:3002/project/128/interface/api/31515">数大臣字段列表</a>
         */
        @NotBlank(message = "ERP字段名称不能为空")
        @Size(max = 64, message = "ERP字段名称最大长度不能超过64位")
        private String erpFieldName;

        /**
         * ERP字段来源类型
         * 来源接口: <a href="http://172.16.100.11:3002/project/128/interface/api/31515">数大臣字段列表</a>
         */
        @NotBlank(message = "ERP字段来源类型不能为空")
        @Size(max = 64, message = "ERP字段来源类型最大长度不能超过64位")
        private String sourceType;

        /**
         * ERP字段来源ID
         * 来源接口: <a href="http://172.16.100.11:3002/project/128/interface/api/31515">数大臣字段列表</a>
         */
        @NotBlank(message = "ERP字段来源ID不能为空")
        @Size(max = 64, message = "ERP字段来源ID最大长度不能超过64位")
        private String sourceId;

        /**
         * 启用状态
         */
        private Boolean status;

        /**
         * 备注
         */
        private String remark;


    }

    /**
     * 列表查询参数
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
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

    @Data
    @NoArgsConstructor
    public static class PagingVO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 核对类型
         * 来源:/common/enumDropDown?type=CfgReconciliationType
         */
        private String reconciliationType;

        /**
         * 核对类型名称
         * 核对类型
         * 来源:/common/enumDropDown?type=CfgReconciliationType
         */
        private String reconciliationTypeName;

        /**
         * 物流商
         */
        private String thirdName;

        /**
         * 物流商代号
         */
        private String thirdCode;

        /**
         * 物流商字段
         */
        private String thirdFieldName;

        /**
         * ERP字段名称
         */
        private String erpFieldName;

        /**
         * ERP字段来源类型
         * 来源接口: <a href="http://172.16.100.11:3002/project/128/interface/api/31887">数大臣字段列表</a>
         */
        private String sourceType;

        /**
         * ERP字段来源ID
         * 来源接口: <a href="http://172.16.100.11:3002/project/128/interface/api/31887">数大臣字段列表</a>
         */
        private String sourceId;

        /**
         * 启用状态
         */
        private Boolean status;

        /**
         * 创建人
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 备注
         */
        private String remark;


    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErpFieldDropDownDTO {
        /**
         * 核对类型
         */
        private String reconciliationType;
        /**
         * ERP字段名称
         */
        private String erpFieldName;
        /**
         * ERP字段来源类型
         */
        private String sourceType;
        /**
         * ERP字段来源ID
         */
        private String sourceId;
        /**
         * ERP字段来源代号或分类
         */
        private String sourceCodeValue;

        public String combineUniqueCode(){
            return CharSequenceUtil.format(FORMAT, this.sourceType, this.sourceId);
        }

        public static String convertUniqueCode(String sourceType, String sourceId){
            return CharSequenceUtil.format(FORMAT, sourceType, sourceId);
        }

    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErpFieldViewDTO {
        /**
         * 核对类型
         */
        private String reconciliationType;
        /**
         * ERP字段名称
         */
        private String erpFieldName;
        /**
         * ERP字段名称(字典的字段编码)
         */
        private String erpFieldCode;
        /**
         * ERP字段来源类型
         */
        private String sourceType;
        /**
         * ERP字段来源ID
         */
        private String sourceId;

        /**
         * 第三方名称
         */
        private String thirdName;
        /**
         * 第三方名称代号
         */
        private String thirdCode;
        /**
         * 第三方字段名称
         */
        private String thirdFieldName;

        /**
         * 启用状态
         */
        private Boolean status;
        /**
         * 备注
         */
        private String remark;

        /**
         * 费用分类
         */
        private String dictCostCategory;

        public String combineUniqueCode(){
            return CharSequenceUtil.format(FORMAT, this.sourceType, this.sourceId);
        }

        public static String convertUniqueCode(String sourceType, String sourceId){
            return CharSequenceUtil.format(FORMAT, sourceType, sourceId);
        }

    }

}