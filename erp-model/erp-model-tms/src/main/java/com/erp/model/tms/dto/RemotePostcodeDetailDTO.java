package com.erp.model.tms.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.core.anno.FieldValid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 偏远邮编明细表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2024-11-29
*/
@Data
@NoArgsConstructor
public class RemotePostcodeDetailDTO implements Serializable {


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
        * 主表id 
        */
        private String mainId;


        /**
        * 国家
        */
        private String country;

        /**
        * 城市
        */
        private String city;

        /**
        * 匹配类型dict_basic表matchType: preciseMatch=精准匹配, prefixMatch=匹配前缀, suffixMatch=匹配后缀, fuzzyMatch=模糊匹配
        */
        private String matchType;

        /**
        * 邮编
        */
        private String postCode;

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
        * 主表id 
        */
        private String mainId;

        /**
        * 国家
        */
        private String country;

        /**
        * 城市
        */
        private String city;

        /**
         * 城市
         */
        private String cityName;

        /**
        * 匹配类型dict_basic表matchType: preciseMatch=精准匹配, prefixMatch=匹配前缀, suffixMatch=匹配后缀, fuzzyMatch=模糊匹配
        */
        private String matchType;

        private String matchTypeName;
        /**
        * 邮编
        */
        private String postCode;

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

    /**
     * 导入
     */
    @Data
    @NoArgsConstructor
    public static class ImportResultDTO {
        /**
         * 成功返回数据
         */
        private List<ImportDTO> successList;

        /**
         * 错误的url
         */
        private String errorUrl;
    }

    /**
    * 导入
    */
    @Data
    @NoArgsConstructor
    public static class ImportDTO {
        /**
         * 国家
         */
        @ExcelProperty(value = "*国家二字码")
        @FieldValid(fieldName = "*国家二字码",isNotBlank = true)
        @Size(max = 32, message = "国家二字码最大长度不能超过32位")
        private String country;

        /**
         * 城市
         */
        private String city;

        @ExcelProperty(value = "城市")
        private String cityName;

        /**
         * 匹配类型dict_basic表matchType: preciseMatch=精准匹配, prefixMatch=匹配前缀, suffixMatch=匹配后缀, fuzzyMatch=模糊匹配
         */
        private String matchType = "preciseMatch";

        private String matchTypeName = "精准匹配";

        /**
         * 邮编
         */
        @ExcelProperty(value = "*邮编")
        @FieldValid(fieldName = "*邮编",isNotBlank = true)
        @Size(max = 32, message = "邮编二字码最大长度不能超过32位")
        private String postCode;
        /**
         * 错误信息
         */
        private String errorMsg;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 主表id
        */
        private String mainId;

        /**
        * 国家
        */
        @NotBlank(message = "国家不能为空")
        private String country;

        /**
        * 城市
        */
        private String city;

        private String cityName;

        /**
        * 匹配类型dict_basic表matchType: preciseMatch=精准匹配, prefixMatch=匹配前缀, suffixMatch=匹配后缀, fuzzyMatch=模糊匹配
        */
        @NotBlank(message = "匹配类型dict_basic表matchType: preciseMatch=精准匹配, prefixMatch=匹配前缀, suffixMatch=匹配后缀, fuzzyMatch=模糊匹配不能为空")
        private String matchType;

        /**
        * 邮编
        */
        @NotBlank(message = "邮编不能为空")
        @Size(max = 32,message = "邮编最大长度不能超过32位")
        private String postCode;


    }


}