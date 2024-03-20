package com.erp.model.sys.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author Lambda
 * @Classname DictCountryDTO

 * @Date 2023-05-11 16:11
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class DictCountryDTO implements Serializable {



    @Data
    @NoArgsConstructor
    public static class PagingViewDTO{

        /**
         * ID 也是国家二字码
         */
        private String id;




        /**
         * 国家名称
         */
        private String nameCn;

        /**
         * 金蝶code
         */
        private String kingdeeCode;

        /**
         * 上级区域
         */
        private String parentRegionName;


        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
        /**
         * 更新人
         */
        private String updateUserName;
    }

    @Data
    @NoArgsConstructor
    public static class ViewDTO{

        /**
         * id 也是国家二字码
         */
        private String id;

        private String nameCn;

        private String parentRegionId;

    }


    @Data
    @NoArgsConstructor
    public static class AddDTO{

        /**
         * 国家名称
         */
        @NotBlank(message = "国家名称不能为空")
        private String name;

        /**
         * 国家名称
         */
        @NotBlank(message = "国家二字码不能为空")
        private String code;

        /**
         * 上级区域 来源  http://172.16.100.11:3002/project/36/interface/api/31119
         */
        @NotBlank(message = "上级区域不能为空")
        private String parentRegionId;

    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO{

        /**
         * 国家名称
         */
        @NotBlank(message = "国家名称不能为空")
        private String name;

        /**
         * 国家名称
         */
        @NotBlank(message = "国家二字码不能为空")
        private String code;

        /**
         * 上级区域 来源  http://172.16.100.11:3002/project/36/interface/api/31119
         */
        @NotBlank(message = "上级区域不能为空")
        private String parentRegionId;

    }

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


    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * id
         */
        private String id;

        /**
         * 中文名
         */
        private String nameCn;

        /**
         * 英文名
         */
        private String nameEn;

        /**
         * 中文简称
         */
        private String shortNameCn;

        /**
         * 英文简称
         */
        private String shortNameEn;

        /**
         * 大区code
         */
        private String regionCode;

        /**
         * 大区名称
         */
        private String areaName;

        /**
         * 是否禁用
         * false 没有
         */
        private Boolean disabled;

        /**
         * 子区域code
         */
        private String subregionCode;

        /**
         * 子区域名称
         */
        private String subregionName;

    }

    @Data
    @NoArgsConstructor
    public static class CascadeDTO{

        private String dictAreaCode;

        private List<ChildrenDTO> children;

    }

    @Data
    @NoArgsConstructor
    public static class ChildrenDTO{

        private String dictCountryCode;

        private String dictCountryName;

    }

    @Data
    @NoArgsConstructor
    public static class ListParamDTO{

        /**
         * 搜索关键字
         */
        private String searchKeyword;

        /**
         * 区域编码
         */
        private String regionCode;

        /**
         * 国家中文名称
         */
        private List<String> nameCnList;
    }

    @Data
    @NoArgsConstructor
    public static class ListRegionDTO{

        /**
         * 区域编码
         */
        private String regionCode;

        /**
         * 区域名称
         */
        private String regionName;

        /**
         * 国家
         */
        private List<ListDTO> list;


        public ListRegionDTO (String regionCode,String regionName) {
            this.regionCode = regionCode;
            this.regionName = regionName;
        }
    }
}
