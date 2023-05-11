package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname DictGlobalAreaDTO
 * @Description TODO
 * @Date 2023-05-11 14:52
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class DictGlobalAreaDTO  implements Serializable {


    @Data
    @NoArgsConstructor
    public static class AddOrUpdateDTO {

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


    }


}
