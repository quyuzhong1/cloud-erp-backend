package com.erp.model.oms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 发票海关编码请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-04-07
*/
@Data
@NoArgsConstructor
public class DictInvoiceHsDTO implements Serializable {

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
        private String id;
        /**
         * 海关编码
         */
        private String hsCode;
        /**
         * 描述
         */
        private String desc;
        /**
         * 国家
         */
        private String country;
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
        * 海关编码
        */
        private String hsCode;

        /**
        * 描述
        */
        private String desc;

        /**
        * 国家
        */
        private String country;


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
        * 海关编码
        */
        @NotBlank(message = "海关编码不能为空")
        @Size(max = 64,message = "海关编码最大长度不能超过64位")
        private String hsCode;

        /**
        * 描述
        */
        @NotBlank(message = "描述不能为空")
        private String desc;

        /**
        * 国家
        */
        @NotBlank(message = "国家不能为空")
        @Size(max = 32,message = "国家最大长度不能超过32位")
        private String country;


    }


}