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
 * @Classname DictGlobalAreaDTO

 * @Date 2023-05-11 14:52
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class DictGlobalAreaDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class PagingViewDTO{

        /**
         * ID
         */
        private String id;

        /**
         * 区域名称
         */
        private String regionName;

        /**
         * 金蝶code
         */
        private String kingdeeCode;

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
    public static class ViewDTO{

        /**
         * id
         */
        private String id;


        /**
         * 区域名
         */
        private String regionName;

        /**
         * 区域code
         */
        private String regionCode;

        /**
         * 金蝶code
         */
        private String kingdeeCode;

    }
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        @NotBlank(message = "区域不能为空")
        private String id;

        @NotBlank(message = "区域名不能为空")
        private String  regionName;

    }

    @Data
    @NoArgsConstructor
    public static class AddDTO {


        /**
         * 区域名
         */
        @NotBlank(message = "区域名不能为空")
        private String regionName;


        /**
         * 金蝶code
         */
        @NotBlank(message = "金蝶code不能为空")
        private String kingdeeCode;




    }

    @Data
    @NoArgsConstructor
    public static class InfoDTO {

        private String id;

        /**
         * 国家所属子区域例如“北欧”、“中东”等
         */
        private String subregionName;

        /**
         * 国家所属的大洲或地理区域code
         */
        private String regionCode;

        /**
         * 国家所属的大洲或地理区域例如“欧洲”、“亚洲”、“南美洲”等
         */
        private String regionName;

        private Integer index;

        /**
         * 国家id
         */
        private String countryId;
    }
}
