package com.erp.model.dmp.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 第三方系统仓库表请求响应实体
 * </p>
 *
 * @author hyj
 * @since 2024-05-17
 */
@Data
@NoArgsConstructor
public class ThirdWarehouseDTO implements Serializable {


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
         * 是否禁用/停用 true 是 false 不是
         */
        private Boolean disabled;

        /**
         * 系统类型：lingxing领星，wangdian旺店通
         */
        private String sysType;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 类型:1:普通（内部）,2:自流转,3:平台,4:京东沧海.6:抖音云仓,125:代发仓,126:分销委外
         */
        private String type;

        /**
         * 子类型：0:默认,1:旺店通,2:菜鸟,3:百世WMS,4:巨沃,5:心怡WMS,6:科捷,7:吉客云,8:中通WMS,9:通天晓,10:酷仓宝WMS,11:景天WMS,12:网店管家笛佛WMS,13:九曳WMS,14:万里牛,15:麓客WMS,16:青图WMS(ERP),17:安鲜达WMS,18:顺丰WMS,19苏宁WMS,20:雅澳E,21:EMS,22:递四方,23:中邮WMS,24:云腾WMS,25:天图WMS;26:但丁WMS,27:e仓宝,28:仓卫士,29:山橙WMS,30橙蚁WMS,31:中山邮政WMS,32:赢路WMS,33:无忧WMS,34:筋斗云WMS,35:韵达WMS,36:GEEK
         */
        private String subType;

        /**
         * 编号
         */
        private String code;

        /**
         * 名称
         */
        private String name;

        /**
         * 地址
         */
        private String address;

        /**
         * 联系人
         */
        private String contacts;

        /**
         * 联系人电话
         */
        private String telNumber;

        /**
         * 固话
         */
        private String telno;

        /**
         * 邮箱
         */
        private String email;

        /**
         * 邮编
         */
        private String zip;

        /**
         * 网址
         */
        private String website;

        /**
         * 省份
         */
        private String province;

        /**
         * 城市
         */
        private String city;

        /**
         * 区县
         */
        private String district;

        /**
         * 第三方创建时间
         */
        private String created;

        /**
         * 第三方修改时间
         */
        private String modified;

        /**
         * 备注
         */
        private String remark;


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
         * 是否禁用/停用 true 是 false 不是
         */
        private Boolean disabled;

        /**
         * 系统类型：lingxing领星，wangdian旺店通, TeMu
         */
        private String sysType;

        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库编码
         */
        private String code;

        /**
         * 类型:1:普通（内部）,2:自流转,3:平台,4:京东沧海.6:抖音云仓,125:代发仓,126:分销委外
         */
        private String type;

        /**
         * 子类型：0:默认,1:旺店通,2:菜鸟,3:百世WMS,4:巨沃,5:心怡WMS,6:科捷,7:吉客云,8:中通WMS,9:通天晓,10:酷仓宝WMS,11:景天WMS,12:网店管家笛佛WMS,13:九曳WMS,14:万里牛,15:麓客WMS,16:青图WMS(ERP),17:安鲜达WMS,18:顺丰WMS,19苏宁WMS,20:雅澳E,21:EMS,22:递四方,23:中邮WMS,24:云腾WMS,25:天图WMS;26:但丁WMS,27:e仓宝,28:仓卫士,29:山橙WMS,30橙蚁WMS,31:中山邮政WMS,32:赢路WMS,33:无忧WMS,34:筋斗云WMS,35:韵达WMS,36:GEEK
         */
        private String subType;

        /**
         * 名称
         */
        private String name;

        /**
         * 地址
         */
        private String address;

        /**
         * 联系人
         */
        private String contacts;

        /**
         * 联系人电话
         */
        private String telNumber;

        /**
         * 固话
         */
        private String telno;

        /**
         * 邮箱
         */
        private String email;

        /**
         * 邮编
         */
        private String zip;

        /**
         * 网址
         */
        private String website;

        /**
         * 省份
         */
        private String province;

        /**
         * 城市
         */
        private String city;

        /**
         * 区县
         */
        private String district;

        /**
         * 第三方创建时间
         */
        private String created;

        /**
         * 第三方修改时间
         */
        private String modified;

        /**
         * 备注
         */
        private String remark;
        /**
         * 分类
         */
//        @NotBlank(message = "分类不能为空")
//        @Size(max = 255, message = "分类最大长度不能超过255位")
        private String category;
        private String warehouseList;

        private String thirdShortName;
    }

    @Data
    @NoArgsConstructor
    public static class PageDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 系统类型：lingxing领星，wangdian旺店通
         */
        private String sysType;
        /**
         * 名称
         */
        private String name;
        /**
         * 编号
         */
        private String code;
    }

    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        /**
         * 系统类型：lingxing领星，wangdian旺店通
         */
        @NotBlank(message = "系统类型不能为空")
        @Size(max = 19, message = "系统类型：lingxing领星，wangdian旺店通 最大长度不能超过19位")
        private String sysType;
        /**
         * 名称
         */
        @Size(max = 200, message = "名称最大长度不能超过200位")
        private String name;
    }

    @Data
    @NoArgsConstructor
    public static class PageSelectDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 信息id
         */
        private String infoId;
        /**
         * 系统类型：lingxing领星，wangdian旺店通
         */
        private String sysType;
        /**
         * 名称
         */
        private String name;
        /**
         * 编号
         */
        private String code;
        /**
         * 是否可选
         */
        private Boolean canCheck = true;
    }

    /**
     * 远程搜索
     */
    @Data
    @NoArgsConstructor
    public static class SelectDTO {
        /**
         * 系统类型
         */
        @NotBlank(message = "系统类型不能为空")
        private String sysType;

        /**
         * 仓库简称
         */
        private String shortName;

        /**
         * 关键词
         */
        private String searchKeyword;
        /**
         * 分类
         */
        private String category;
    }
}