package com.erp.model.dmp.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 中台退货入库单主表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2025-04-09
*/
@Data
@NoArgsConstructor
public class DmpReturnInstockDTO implements Serializable {




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
        * 订单来源平台（编码）：Amazon，AliExpress，shopify，...
        */
        private String sourcePlatform;

        /**
        * 来源平台：gyy，kingdee，mabang
        */
        private String sourceSystem;

        /**
        * 第三方退货入库id
        */
        private String thirdReturnInstockId;

        /**
        * 第三方退货入库单据编号
        */
        private String thirdReturnInstockCode;

        /**
        * 第三方创建时间
        */
        private LocalDateTime thirdCreateTime;

        /**
        * 第三方更新时间
        */
        private LocalDateTime thirdUpdateTime;

        /**
        * 退货入库状态
        */
        private String returnInstockStatus;

        /**
        * 退货入库时间
        */
        private LocalDateTime returnInstockTime;

        /**
        * 物流商代码
        */
        private String logisticCompanyCode;

        /**
        * 物流商名称
        */
        private String logisticCompanyName;

        /**
        * 退货入库物流单号
        */
        private String returnLogisticCode;

        /**
        * 销售组织编码
        */
        private String salesCompanyCode;

        /**
        * 收款组织编码
        */
        private String receivingCompanyCode;

        /**
        * 组织名称
        */
        private String organizationName;

        /**
        * 组织编码
        */
        private String organizationCode;

        /**
        * 平台名称
        */
        private String platformName;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 店铺编码
        */
        private String shopNo;

        /**
        * 平台退货入库单号
        */
        private String platformReturnInstockCode;
        
        /**
         * 平台退货订单号
         */
         private String platformOrderCode;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;

        /**
        * 输入任务id
        */
        private String inputTaskId;

        /**
        * 转换id
        */
        private String convertId;

        /**
        * 下一层级id
        */
        private String nextLevelId;

        /**
         * 平台原始入库单号
         */
        private String thirdCode;

        private List<DmpReturnInstockDetailDTO.ViewDTO> detailList;
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
        * 订单来源平台（编码）：Amazon，AliExpress，shopify，...
        */
        @NotBlank(message = "订单来源平台（编码）：Amazon，AliExpress，shopify，...不能为空")
        @Size(max = 50,message = "订单来源平台（编码）：Amazon，AliExpress，shopify，...最大长度不能超过50位")
        private String sourcePlatform;

        /**
        * 来源平台：gyy，kingdee，mabang
        */
        @NotBlank(message = "来源平台：gyy，kingdee，mabang不能为空")
        @Size(max = 25,message = "来源平台：gyy，kingdee，mabang最大长度不能超过25位")
        private String sourceSystem;

        /**
        * 第三方退货入库id
        */
        @NotBlank(message = "第三方退货入库id不能为空")
        @Size(max = 64,message = "第三方退货入库id最大长度不能超过64位")
        private String thirdReturnInstockId;

        /**
        * 第三方退货入库单据编号
        */
        @NotBlank(message = "第三方退货入库单据编号不能为空")
        @Size(max = 64,message = "第三方退货入库单据编号最大长度不能超过64位")
        private String thirdReturnInstockCode;

        /**
        * 第三方创建时间
        */
        private LocalDateTime thirdCreateTime;

        /**
        * 第三方更新时间
        */
        private LocalDateTime thirdUpdateTime;

        /**
        * 退货入库状态
        */
        @NotBlank(message = "退货入库状态不能为空")
        @Size(max = 25,message = "退货入库状态最大长度不能超过25位")
        private String returnInstockStatus;

        /**
        * 退货入库时间
        */
        private LocalDateTime returnInstockTime;

        /**
        * 物流商代码
        */
        @NotBlank(message = "物流商代码不能为空")
        @Size(max = 64,message = "物流商代码最大长度不能超过64位")
        private String logisticCompanyCode;

        /**
        * 物流商名称
        */
        @NotBlank(message = "物流商名称不能为空")
        @Size(max = 255,message = "物流商名称最大长度不能超过255位")
        private String logisticCompanyName;

        /**
        * 退货入库物流单号
        */
        @NotBlank(message = "退货入库物流单号不能为空")
        @Size(max = 255,message = "退货入库物流单号最大长度不能超过255位")
        private String returnLogisticCode;

        /**
        * 销售组织编码
        */
        @NotBlank(message = "销售组织编码不能为空")
        @Size(max = 255,message = "销售组织编码最大长度不能超过255位")
        private String salesCompanyCode;

        /**
        * 收款组织编码
        */
        @NotBlank(message = "收款组织编码不能为空")
        @Size(max = 255,message = "收款组织编码最大长度不能超过255位")
        private String receivingCompanyCode;

        /**
        * 组织名称
        */
        @NotBlank(message = "组织名称不能为空")
        @Size(max = 255,message = "组织名称最大长度不能超过255位")
        private String organizationName;

        /**
        * 组织编码
        */
        @NotBlank(message = "组织编码不能为空")
        @Size(max = 255,message = "组织编码最大长度不能超过255位")
        private String organizationCode;

        /**
        * 平台名称
        */
        @NotBlank(message = "平台名称不能为空")
        @Size(max = 255,message = "平台名称最大长度不能超过255位")
        private String platformName;

        /**
        * 店铺名称
        */
        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 255,message = "店铺名称最大长度不能超过255位")
        private String shopName;

        /**
        * 店铺编码
        */
        @NotBlank(message = "店铺编码不能为空")
        @Size(max = 255,message = "店铺编码最大长度不能超过255位")
        private String shopNo;

        /**
        * 平台退货入库单号
        */
        @NotBlank(message = "平台退货入库单号不能为空")
        @Size(max = 255,message = "平台退货入库单号最大长度不能超过255位")
        private String platformReturnInstockCode;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;

        /**
        * 输入任务id
        */
        @NotBlank(message = "输入任务id不能为空")
        @Size(max = 19,message = "输入任务id最大长度不能超过19位")
        private String inputTaskId;

        /**
        * 转换id
        */
        @NotBlank(message = "转换id不能为空")
        @Size(max = 19,message = "转换id最大长度不能超过19位")
        private String convertId;

        /**
        * 下一层级id
        */
        @NotBlank(message = "下一层级id不能为空")
        @Size(max = 19,message = "下一层级id最大长度不能超过19位")
        private String nextLevelId;


    }


}