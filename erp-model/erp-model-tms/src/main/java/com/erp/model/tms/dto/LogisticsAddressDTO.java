package com.erp.model.tms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.core.anno.RegularValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import com.erp.model.tms.enums.LogisticsAddressTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * <p>
 * 物流地址表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Data
@NoArgsConstructor
public class LogisticsAddressDTO implements Serializable {


    /**
     * 分页
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {

        /**
         * id
         */
        private String id;

        /**
         * 名称
         */
        private String name;

        /**
         * 类型
         */
        private LogisticsAddressTypeEnum type;

        /**
         * 类型名称
         */
        private String typeName;

        /**
         * 公司名
         */
        private String companyName;

        /**
         * 联系人
         */
        private String contact;

        /**
         * 邮箱
         */
        private String email;

        /**
         * 电话
         */
        private String telNumber;

        /**
         * 国家二子码
         */
        private String country;

        /**
         * 国家名称
         */
        private String countryName;

        /**
         * 省
         */
        private String provinceName;


        /**
         * 城市
         */
        private String cityName;


        /**
         * 区
         */
        private String districtName;


        /**
         * 详细地址1
         */
        private String addressFirst;

        /**
         * 详细地址2
         */
        private String addressSecond;

        /**
         * 邮编
         */
        private String zipCode;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
    }

    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        private List<String> typeList;


        private String address;

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
    public static class ExportDTO extends PagingParamDTO {
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
        private String id;

        /**
         * 名称
         */
        private String name;

        /**
         * 类型
         */
        private LogisticsAddressTypeEnum type;

        /**
         * 类型
         */
        private String typeName;

        /**
         * 公司名
         */
        private String companyName;

        /**
         * 联系人
         */
        private String contact;

        /**
         * 邮箱
         */
        private String email;

        /**
         * 电话
         */
        private String telNumber;

        /**
         * 国家二字码
         */
        private String country;

        /**
         * 国家名称
         */
        private String countryName;

        /**
         * 省
         */
        private String provinceName;


        /**
         * 城市
         */
        private String cityName;

        /**
         * 区
         */
        private String districtName;


        /**
         * 详细地址1
         */
        private String addressFirst;

        /**
         * 详细地址2
         */
        private String addressSecond;

        /**
         * 邮编
         */
        private String zipCode;


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
         * 名称
         */
        @NotBlank(message = "名称不能为空")
        @Size(max = 50, message = "名称最大长度不能超过50位")
        private String name;

        /**
         * 类型
         * http://172.16.100.11:3002/project/128/interface/api/25522
         */
        @NotNull(message = "类型不能为空")
        private LogisticsAddressTypeEnum type;

        /**
         * 公司名
         */
        @Size(max = 50, message = "公司名最大长度不能超过50位")
        private String companyName;

        /**
         * 联系人
         */
        @Size(max = 50, message = "联系人最大长度不能超过50位")
        private String contact;

        /**
         * 邮箱
         */
        @Size(max = 50, message = "邮箱最大长度不能超过50位")
        @RegularValid(formatPattern = FieldFormatPatternTypeEnum.MAILBOX, message = "邮箱格式有误")
        private String email;

        /**
         * 电话
         */
        @Size( max = 20, message = "电话 最小长度 不能小于6位字符,最大不能超过20位字符")
        private String telNumber;

        /**
         * 国家id
         */
        @NotBlank(message = "国家不能为空")
        private String country;

        /**
         * 省
         */
        @NotBlank(message = "省不能为空")
        private String provinceName;

        /**
         * 城市
         */
        private String cityName;

        /**
         * 区
         */

        private String districtName;

        /**
         * 详细地址1
         */
        @NotBlank(message = "详细地址1不能为空")
        @Size(max = 255, message = "详细地址1最大长度不能超过250位")
        private String addressFirst;

        /**
         * 详细地址2
         */
        @Size(max = 255, message = "详细地址2最大长度不能超过250位")
        private String addressSecond;

        /**
         * 邮编
         */
        @Size(max = 20, message = "邮编最大长度不能超过20位")
        private String zipCode;


    }


    @Data
    @NoArgsConstructor
    public static class ListDTO{

        private String addressId;

        private String id;

        private String name;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressByTypeDTO{

        /**
         * 主键id
         */
        @NotBlank(message = "地址类型不能为空")
        private String type;

        @NotNull(message = "店铺id不能为空")
        private List<String> shopIds;
    }

    @Data
    @NoArgsConstructor
    public static class SelectDTO {
        /**
         * 类型
         */
        private String type;
        /**
         * 模糊搜索
         */
        private String searchKeyword;
    }
}