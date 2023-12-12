package com.erp.model.wms.dto;

import com.erp.model.wms.entity.OverseasWarehouseInboundAddressEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 海外仓入库单揽收地址请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
 */
@Data
@NoArgsConstructor
public class OverseasWarehouseInboundAddressDTO implements Serializable {
    

    /**
     * 列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * ID
         */
        private String id;

        /**
         * 省名称
         */
        private String dictProvinceId;

        /**
         * 省ID
         */
        private String dictProvinceName;

        /**
         * 城市ID
         */
        private String dictCityId;

        /**
         * 城市名称
         */
        private String dictCityName;

        /**
         * 地区ID
         */
        private String dictDistrictId;

        /**
         * 地区名称
         */
        private String dictDistrictName;

        /**
         * 性
         */
        private String firstName;

        /**
         * 名
         */
        private String lastName;

        /**
         * 手机
         */
        private String mobile;

        /**
         * 详情
         */
        private String street;

        /**
         * 地址邮编
         */
        private String zipcode;

        public ListDTO(OverseasWarehouseInboundAddressEntity entity, Map<String, String> dictCountryEntityMap) {
            this.id = entity.getId();
            this.dictProvinceId = entity.getDictProvinceId();
            this.dictProvinceName = dictCountryEntityMap.getOrDefault(this.dictProvinceId, "");
            this.dictCityId = entity.getDictCityId();
            this.dictCityName = dictCountryEntityMap.getOrDefault(this.dictCityId, "");;
            this.dictDistrictId = entity.getDictDistrictId();
            this.dictDistrictName = dictCountryEntityMap.getOrDefault(this.dictDistrictId, "");;
            this.firstName = entity.getFirstName();
            this.lastName = entity.getLastName();
            this.mobile = entity.getMobile();
            this.street = entity.getStreet();
            this.zipcode = entity.getZipcode();
        }
    }


    /**
     * 通用
     */
    @Data
    @NoArgsConstructor
    public static class CommonDTO {
        /**
         * 省ID
         */
        @NotBlank(message = "省ID不能为空")
        private String dictProvinceId;

        /**
         * 城市ID
         */
        @NotBlank(message = "城市ID不能为空")
        private String dictCityId;

        /**
         * 地区ID
         */
        @NotBlank(message = "地区ID不能为空")
        private String dictDistrictId;

        /**
         * 性
         */
        @NotBlank(message = "性不能为空")
        private String firstName;

        /**
         * 名
         */
        @NotBlank(message = "名不能为空")
        private String lastName;

        /**
         * 手机
         */
        @NotBlank(message = "手机不能为空")
        private String mobile;

        /**
         * 详情
         */
        @NotBlank(message = "详情不能为空")
        private String street;

        /**
         * 地址邮编
         */
        @NotBlank(message = "【zipcode】地址邮编不能为空")
        private String zipcode;

        /**
         * 所有ID
         */
        public List<String> getAllDictCityId() {
            return Arrays.asList(this.dictProvinceId, this.dictCityId, this.dictDistrictId);
        }
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
     * 编辑
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {
        /**
         * ID
         */
        @NotBlank(message = "ID不能为空")
        private String id;
    }


}