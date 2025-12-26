package com.erp.model.oms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.common.business.dto.base.SortDTO;
import java.util.List;

import com.erp.model.oms.entity.KolAddressInfoEntity;
import com.erp.model.oms.entity.KolCooperationPlatformEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 企业达人库请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-12-02
*/
@Data
@NoArgsConstructor
public class KolPartnerInfoDTO implements Serializable {


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

         private String tabFlagName;

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

         /**
          * 勾选的id集合
          */
         private List<String> ids;

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
        * 是否启用
        */
        private Boolean disabled;
        private String disabledName;

        /**
        * 备注
        */
        private String remark;

        /**
        * 编号
        */
        private String code;

        /**
        * 达人昵称
        */
        private String nickname;

        /**
        * 达人类型
        */
        private String type;
        private String typeName;

        /**
        * 合作类型
        */
        private String cooperationType;
        private String cooperationTypeName;

        /**
        * 合作日期
        */
        private LocalDate cooperationDate;

        /**
        * 国家ID
        */
        private String countryId;

        /**
        * 国家名称
        */
        private String countryName;

        /**
        * 语言
        */
        private String language;
        private String languageName;

        /**
        * 邮箱
        */
        private String email;

        /**
        * 联系电话
        */
        private String phone;

        /**
        * 负责人ID
        */
        private String chargeId;

        /**
        * 负责人姓名
        */
        private String chargeName;

        /**
        * 部门ID
        */
        private String deptId;

        /**
        * 部门名称
        */
        private String deptName;

        /**
        * 创建时间
        */
        private LocalDateTime createTime;

        /**
        * 创建人名称
        */
        private String createUserId;
        private String createUserName;

        /**
         * detailId
         */
        private String detailId;
        /**
         * 合作平台名称
         */
        private String platformName;

        /**
         * 平台ID
         */
        private String platformAccountId;

        /**
         * 账号名称
         */
        private String platformAccountName;

        /**
         * 粉丝数量
         */
        private Integer followerCount;

        /**
         * 主页链接
         */
        private String homepageUrl;

        /**
         * 平台备注
         */
        private String platformRemark;
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
        * 是否启用
        */
        private Boolean disabled;

        /**
        * 备注
        */
        private String remark;

        /**
        * 编号
        */
        private String code;

        /**
        * 达人昵称
        */
        private String nickname;

        /**
        * 达人类型
        */
        private String type;
        private String typeName;

        /**
        * 合作类型
        */
        private String cooperationType;
        private String cooperationTypeName;

        /**
        * 合作日期
        */
        private LocalDate cooperationDate;

        /**
        * 国家ID
        */
        private String countryId;

        /**
        * 国家名称
        */
        private String countryName;

        /**
        * 语言
        */
        private String language;

        private String languageName;

        /**
        * 邮箱
        */
        private String email;

        /**
        * 联系电话
        */
        private String phone;

        /**
        * 负责人ID
        */
        private String chargeId;

        /**
        * 负责人姓名
        */
        private String chargeName;

        /**
        * 部门ID
        */
        private String deptId;

        /**
        * 部门名称
        */
        private String deptName;

        private List<KolAddressInfoEntity> kolAddressInfoDTOList;

        private List<KolCooperationPlatformEntity> kolCooperationPlatformDTOList;

        /**
         * 附件集合
         */
        private List<String> attachNameList;
        private List<String> attachUrlList;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


        private List<KolAddressInfoDTO.@Valid AddDTO> kolAddressInfoDTOList;

        private List<KolCooperationPlatformDTO. @Valid AddDTO> kolCooperationPlatformDTOList;

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

        private List<KolAddressInfoDTO.@Valid UpdateDTO> kolAddressInfoDTOList;

        private List<KolCooperationPlatformDTO. @Valid UpdateDTO> kolCooperationPlatformDTOList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 是否启用
        */
        private Boolean disabled;

        /**
        * 备注
        */
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;

        /**
        * 达人昵称
        */
        @NotBlank(message = "达人昵称不能为空")
        @Size(max = 200,message = "达人昵称最大长度不能超过200位")
        private String nickname;

        /**
        * 达人类型
        */
        private String type;
        private List<String> typeList;

        /**
        * 合作类型
        */
        private String cooperationType;
        private List<String> cooperationTypeList;

        /**
        * 合作日期
        */
        private LocalDate cooperationDate;

        /**
        * 国家ID
        */
        @NotBlank(message = "国家ID不能为空")
        @Size(max = 19,message = "国家ID最大长度不能超过19位")
        private String countryId;

        /**
        * 国家名称
        */
        private String countryName;

        /**
        * 语言
        */
        private String language;

        /**
        * 邮箱
        */
        @Size(max = 100,message = "邮箱最大长度不能超过100位")
        private String email;

        /**
        * 联系电话
        */
        @Size(max = 20,message = "联系电话最大长度不能超过20位")
        private String phone;

        /**
        * 负责人ID
        */
        @NotBlank(message = "负责人ID不能为空")
        private String chargeId;

        /**
        * 负责人姓名
        */
        private String chargeName;

        /**
        * 部门ID
        */
        private String deptId;

        /**
        * 部门名称
        */
        private String deptName;


        /**
         * 附件集合
         */
        private List<String> attachNameList;

        private List<String> attachUrlList;
    }

    @Data
    @NoArgsConstructor
    public static class PartnerAddressDTO {
        /**
         * 地址id
         */
        private String  addressId;

        /**
         * 是否启用
         */
        private Boolean disabled;

        /**
         * 国家id
         */
        private String countryId;

        /**
         * 国家
         */
        private String countryName;

        /**
         * 省/州id
         */
        private String provinceId;
        private String province;

        /**
         * 城市
         */
        private String cityId;
        private String city;


        /**
         * 区域
         */
        private String districtId;
        private String district;

        /**
         * 详细地址
         */
        private String detailAddress;

        /**
         * 联系人
         */
        private String contactPerson;

        /**
         * 邮箱
         */
        private String email;

        /**
         * 联系电话
         */
        private String phone;

        /**
         * 邮编
         */
        private String zipCode;

        /**
         * 是否默认地址
         */
        private Boolean isDefault;
        private String isDefaultName;

        /**
         * 地址备注
         */
        private String remark;

    }


    @Data
    @NoArgsConstructor
    public static class DropDownDTO {

        /**
         * 主键id
         */
        private String  id;

        /**
         * 地址是否启用
         */
        private Boolean disabled;

        /**
         * 编号
         */
        private String code;

        /**
         * 达人昵称
         */
        private String nickname;

        /**
         * 达人类型
         */
        private String type;
        private String typeName;

        /**
         * 合作类型
         */
        private String cooperationType;
        private String cooperationTypeName;

        /**
         * 国家ID
         */
        private String countryId;

        /**
         * 国家名称
         */
        private String countryName;
        /**
         * 地址id
         */
        private String  addressId;

        /**
         * 地址信息--国家id
         */
        private String addressCountryId;

        /**
         * 地址信息--国家
         */
        private String addressCountryName;

        /**
         * 省/州id
         */
        private String provinceId;
        private String province;

        /**
         * 城市
         */
        private String cityId;
        private String city;


        /**
         * 区域
         */
        private String districtId;
        private String district;

        /**
         * 详细地址
         */
        private String detailAddress;

        /**
         * 联系人
         */
        private String contactPerson;

        /**
         * 邮箱
         */
        private String email;

        /**
         * 联系电话
         */
        private String phone;

        /**
         * 邮编
         */
        private String zipCode;

        /**
         * 是否默认地址
         */
        private Boolean isDefault;
    }

    @Data
    @NoArgsConstructor
    public static class SelectDTO {
        /**
         * 关键词
         */
        private String searchKeyword;
    }

    @Data
    @NoArgsConstructor
    public static class AddressSelectDTO  extends  SelectDTO{
        /**
         * 达人id
         */
        @NotBlank(message = "达人id不能为空")
        private String id;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IdsDTO {

        /**
         * id
         */
        @NotEmpty(message = "ids不能为空")
        private List<String> ids;
        /**
         * true 禁用 false 启用
         */
        @NotNull(message = "操作类型不能为空")
        private Boolean disabled;

    }


}