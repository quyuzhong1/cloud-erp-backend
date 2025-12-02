package com.erp.model.oms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author Lambda
 * @Classname DictBasicDTO
 * @Date 2023-07-14 9:38
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class DictBasicDTO implements Serializable {



    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        private String id;

        private String remark;

        private String value;

        private String type;
        private String subType;

        private String name;

        private Integer sort;


    }


    @Data
    @NoArgsConstructor
    public static class AddOrUpdateDTO {

        private String id;

        private String remark;

        private String value;

        private String type;

        private String name;

        private String sort;

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
        private Map<String, String> sqlMap;
    }
}
