package com.erp.model.tms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 物流-第三方渠道关系表请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2025-05-29
*/
@Data
@NoArgsConstructor
public class LogisticsThirdChannelRefDTO implements Serializable {




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
        * 备注
        */
        private String remark;

        /**
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * 是否推送电话
        */
        private Boolean isPushMobile;

        /**
        * 物流渠道id
        */
        private String logisticsChannelId;

        /**
        * 渠道名称
        */
        private String logisticsChannelName;

        /**
        * 渠道代码
        */
        private String logisticsChannelCode;

        /**
        * 第三方物流商编码
        */
        private String thirdSupplierCode;

        /**
        * 第三方物流商名称
        */
        private String thirdSupplierName;

        /**
        * 平台类型(TRACK123)
         * TrackPlatformTypeEnum
        */
        private String platformType;
        private String platformTypeName;

        /**
        * 物流商id
        */
        private String logisticsSupplierId;

        /**
        * 物流商名称
        */
        private String logisticsSupplierName;

        /**
        * 第三方渠道编码
        */
        private String thirdChannelCode;

        /**
        * 第三方渠道名称
        */
        private String thirdChannelName;

        /**
        * 推送类型:sender=发件人,receiver=收件人,orderReceiver=订单收件人,shopSender=发件人-店铺,platformSender=发件人-平台
        */
        private String pushType;
        private String pushTypeName;

        /**
         * 明细
         */
        private List<LogisticsThirdChannelRefDetailDTO.ViewDTO> detailList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        /**
         * 明细
         */
        private List<LogisticsThirdChannelRefDetailDTO.AddDTO> detailList;
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
        /**
         * 明细
         */
        private List<LogisticsThirdChannelRefDetailDTO.UpdateDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 备注
        */
//        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 是否禁用
        */
//        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;

        /**
        * 是否推送电话
        */
        @NotNull(message = "是否推送电话不能为空")
        private Boolean isPushMobile;

        /**
        * 物流渠道id
        */
        @NotBlank(message = "物流渠道id不能为空")
        @Size(max = 19,message = "物流渠道id最大长度不能超过19位")
        private String logisticsChannelId;

        /**
        * 渠道名称
        */
//        @NotBlank(message = "渠道名称不能为空")
        @Size(max = 100,message = "渠道名称最大长度不能超过100位")
        private String logisticsChannelName;

        /**
        * 渠道代码
        */
//        @NotBlank(message = "渠道代码不能为空")
        @Size(max = 50,message = "渠道代码最大长度不能超过50位")
        private String logisticsChannelCode;

        /**
        * 第三方物流商编码
        */
//        @NotBlank(message = "第三方物流商编码不能为空")
        @Size(max = 50,message = "第三方物流商编码最大长度不能超过50位")
        private String thirdSupplierCode;

        /**
        * 第三方物流商名称
        */
//        @NotBlank(message = "第三方物流商名称不能为空")
        @Size(max = 100,message = "第三方物流商名称最大长度不能超过100位")
        private String thirdSupplierName;

        /**
        * 平台类型(TRACK123)
        */
        @NotBlank(message = "平台类型(TRACK123)不能为空")
        @Size(max = 50,message = "平台类型(TRACK123)最大长度不能超过50位")
        private String platformType;

        /**
        * 物流商id
        */
        @NotBlank(message = "物流商id不能为空")
        @Size(max = 19,message = "物流商id最大长度不能超过19位")
        private String logisticsSupplierId;

        /**
        * 物流商名称
        */
//        @NotBlank(message = "物流商名称不能为空")
        @Size(max = 100,message = "物流商名称最大长度不能超过100位")
        private String logisticsSupplierName;

        /**
        * 第三方渠道编码
        */
//        @NotBlank(message = "第三方渠道编码不能为空")
        @Size(max = 50,message = "第三方渠道编码最大长度不能超过50位")
        private String thirdChannelCode;

        /**
        * 第三方渠道名称
        */
//        @NotBlank(message = "第三方渠道名称不能为空")
        @Size(max = 100,message = "第三方渠道名称最大长度不能超过100位")
        private String thirdChannelName;

        /**
        * 推送类型:sender=发件人,receiver=收件人,orderReceiver=订单收件人,shopSender=发件人-店铺,platformSender=发件人-平台
         * LogisticsThirdChannelRefPushTypeEnum
        */
        @NotBlank(message = "推送类型:sender=发件人,receiver=收件人,orderReceiver=订单收件人,shopSender=发件人不能为空")
        @Size(max = 50,message = "推送类型:sender=发件人,receiver=收件人,orderReceiver=订单收件人,shopSender=发件人最大长度不能超过50位")
        private String pushType;


    }


    @Data
    @NoArgsConstructor
    public static class PagingVO {
        /**
         * 主表id
         */
        private String id;
        /**
         * 查询服务商[可排序]
         */
        private String platformType;
        /**
         * 查询服务商名称
         */
        private String platformTypeName;
        /**
         * 我司 物流商id[可排序]
         */
        private String logisticsSupplierId;
        /**
         * 我司 物流商名称[可排序]
         */
        private String logisticsSupplierName;
        /**
         * 我司 物流渠道id[可排序]
         */
        private String logisticsChannelId;
        /**
         * 我司 物流渠道名称[可排序]
         */
        private String logisticsChannelName;
        /**
         * 我司 物流渠道代码[可排序]
         */
        private String logisticsChannelCode;
        /**
         * 第三方 物流商编码[可排序]
         */
        private String thirdSupplierCode;
        /**
         * 第三方 物流商名称[可排序]
         */
        private String thirdSupplierName;
        /**
         * 第三方 物流渠道编码[可排序]
         */
        private String thirdChannelCode;
        /**
         * 第三方 物流渠道名称[可排序]
         */
        private String thirdChannelName;
        /**
         * 是否推送电话[可排序]
         */
        private Boolean isPushMobile;
        /**
         * 推送电话名称
         */
        private String pushMobileName;
        /**
         * 推送类型:sender=发件人,receiver=收件人,orderReceiver=订单收件人,shopSender=发件人-店铺,platformSender=发件人-平台[可排序]
         * LogisticsThirdChannelRefPushTypeEnum
         */
        private String pushType;
        /**
         * 推送类型名称
         */
        private String pushTypeName;
        /**
         * 创建人名称[可排序]
         */
        private String createUserName;
        /**
         * 是否禁用[可排序]
         */
        private Boolean disabled;
        private String disabledName;
        /**
         * 详情id
         */
        private String detailId;
        /**
         * 店铺id[可排序]
         */
        private String shopId;
        /**
         * 店铺名称[可排序]
         */
        private String shopName;
        /**
         * 平台类型[可排序]
         */
        private String dictPlatform;
        /**
         * 平台类型名称
         */
        private String dictPlatformName;
        /**
         * 默认手机号
         */
        private String mobile;
        /**
         * 手机号码
         */
        private String detailMobile;
        /**
         * 平台店铺名称
         */
        private String platformShopName;
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
    public static class ImportDTO {
    }

    @Data
    @NoArgsConstructor
    public static class DisabledParamDTO {
        /**
         * 主键id列表
         */
        private List<String> ids;
        /**
         * 是否禁用
         */
        private Boolean disabled;
    }
}