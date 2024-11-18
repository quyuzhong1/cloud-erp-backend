package com.erp.model.wms.dto;

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
 * 发货单箱子信息明细表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
*/
@Data
@NoArgsConstructor
public class CartonDTO implements Serializable {


    /**
     * 列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO implements Serializable{
        private static final long serialVersionUID = 1905122041950251207L;
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;
    }
    /**
     * 即时库存分页列表
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {

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
        * first_mile_carton表id
        */
        private String cartonId;

        /**
        * first_mile_carton_detail表id
        */
        private String cartonDetailId;

        /**
        * 箱号
        */
        private String boxNo;

        /**
        * 描述（sku*qty+sku*qty+...）
        */
        private String boxDesc;


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
        * 箱子
        */
        @NotBlank(message = "箱规id不能为空")
        @Size(max = 19,message = "箱规id最大长度不能超过19位")
        private String specId;

        /**
         * 来源id
         */
        @NotBlank(message = "sourceId不能为空")
        @Size(max = 19,message = "sourceId最大长度不能超过19位")
        private String sourceId;

        /**
        * 箱号
        */
        @NotBlank(message = "箱号不能为空")
        @Size(max = 19,message = "箱号最大长度不能超过19位")
        private String boxNo;

        /**
        * 描述（sku*qty+sku*qty+...）
        */
        @NotBlank(message = "描述（sku*qty+sku*qty+...）不能为空")
        @Size(max = 255,message = "描述（sku*qty+sku*qty+...）最大长度不能超过255位")
        private String boxDesc;


    }


}