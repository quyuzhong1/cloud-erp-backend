package com.erp.model.wms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.common.business.dto.base.SortDTO;
import com.erp.tms.aliexpress.model.handover.UserInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.*;

/**
 * <p>
 * 组包预报表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2024-01-26
*/
@Data
@NoArgsConstructor
public class PackageForecastDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class TabListDTO{


        private String tabFlag;

        private String tabName;

        /**
         * 数量
         */
        private Integer count;
    }

    @Data
    @NoArgsConstructor
    public static class AlExpressHandoverBaseDTO{

        /**
         * 速卖通授权信息
         */
        private Map<String, String> authMap;

        /**
         * 速卖通卖家id
         */
        private UserInfo userInfo;

        /**
         * ISV名称，ISV：ISV-ISV英文或拼音名称、商家ERP：SELLER-商家英文或拼音名称
         */
        private String  client;

        /**
         * 多语言
         */
        private String  locale;

    }

    @Data
    @NoArgsConstructor
    public static class TransferDeclareDTO{
        /**
         * 组包预报单
         */
        @Size(min = 1 ,message = "组包预报单不能为空")
        private List<String> ids;

        /**
         * 报关物流商id  来源 http://172.16.100.11:3002/project/128/interface/api/28035  id
         */
        @NotBlank(message = "报关商不能为空")
        private String transferLogisticsSupplierId;


        /**
         * 报关物流商渠道id http://172.16.100.11:3002/project/128/interface/api/28035  children.id
         */
        @NotBlank(message = "报关商渠道不能为空")
        private String transferLogisticsChannelId;
    }

    /**
     * 上传
     */
    @Data
    @NoArgsConstructor
    public static class UploadDTO{
        /**
         * 组包预报单
         */
        @Size(min = 1 ,message = "组包预报单不能为空")
        private List<String> ids;

        /**
         * 揽收方式  来源 http://172.16.100.11:3002/project/92/interface/api/13147   type=collectMode
         */
        @NotBlank(message = "揽收方式不能为空")
        private String collectMode;


        /**
         * 揽收地址id 来源 http://172.16.100.11:3002/project/128/interface/api/25783  type=collect
         */
        @NotBlank(message = "揽收地址不能为空")
        private String collectAddressId;
    }


    /**
     * 分页
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO{

        /**
         * 主表id
         */
        private String id;

        /**
         * 详情id
         */
        private String detailId;

        /**
         * 组包日期
         */
        private LocalDate billDate;

        /**
         * 编号
         */
        private String code;

        /**
         *第三方交接单号
         */
        private String handoverNo;

        /**
         *第三方组包号
         */
        private String platformPackageNo;

        /**
         * 导出用到 handoverNo/platformPackageNo
         */
        private String platformNo;

        /**
         *物流商id
         */
        private String logisticsSupplierId;

        /**
         * 物流商
         */
        private String logisticsSupplierName;

        /**
         *大包运输单号
         */
        private String transportNo;

        /**
         * 包裹总数量
         */
        private Integer totalPackageQty;

        /**
         *包裹总重量
         */
        private BigDecimal totalPackageWeight;

        /**
         * 导出用到 包裹总重量
         */
        private String totalPackageWeightStr;

        /**
         *单位
         */
        private String totalPackageWeightUnit;

        /**
         * 上传状态
         */
        private String uploadStatus;

        /**
         * 上传状态名
         */
        private String uploadStatusName;

        /**
         * 打印状态
         */
        private String printStatus;

        /**
         * 打印状态名
         */
        private String printStatusName;

        /**
         * 交接状态
         */
        private String handoverStatus;

        /**
         * 交接状态名
         */
        private String handoverStatusName;

        /**
         * 最新失败原因
         */
        private String remark;

        /**
         * 销售订单id
         */
        private String soId;

        /**
         * 销售订单code
         */
        private String soCode;


        /**
         * 物流渠道id
         */
        private String logisticsChannelId;


        /**
         * 物流渠道名
         */
        private String logisticsChannelName;



        /**
         * 跟踪单号
         */
        private String trackNo;


        /**
         * 小包运输单号
         */
        private String minPackageTransportNo;

        /**
         * 重量
         */
        private BigDecimal weight;

        /**
         * 重量
         */
        private String weightStr;

        /**
         * 重量单位
         */
        private String weightUnit;


        /**
         * 小包交接状态
         */
        private String minPackageHandoverStatus;


        /**
         * 小包交接状态名
         *
         */
        private String minPackageHandoverStatusName;

        /**
         * 创建人
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;
    }


    @Data
    @NoArgsConstructor
    public static class DetailQueryParamDTO{


        /**
         * 组包预报不能为空
         */
        @NotBlank(message = "组包预报不能为空")
        private String id;

        /**
         * 销售订单code
         */
        private List<String> soCodeList;

        /**
         * 交接状态
         */
        private String handoverStatus;

        /**
         * 物流跟踪号
         */
        private String trackNo;

    }

    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 组包单号
         */
        private String code;

        /**
         * tabflag
         */
        private String tabFlag;

        /**
         *  物流商id集合 来源 http://172.16.100.11:3002/project/128/interface/api/26440
         */
        private List<String> logisticsSupplierIdList;

        /**
         * 大包运单号
         */
        private String transportNo;

        /**
         * 交接单号/组包号
         */
        private String platformNo;

        /**
         * 大包交接状态
         */
        private List<String> handoverStatusList;


        /**
         * 小包交接状态
         */
        private List<String> minHandoverStatusList;


        /**
         * 上传状态 集合  来源 http://172.16.100.11:3002/project/92/interface/api/13147   type=packageForecastUploadStatus
         */
        private List<String> uploadStatusList;

        /**
         * 创建人id 集合
         */
        private List<String> createUserIdList;

        /**
         * 创建时间 集合
         */
        private List<LocalDateTime> createTimeList;


    }

    /**
     * 导出参数
     */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO{

        /**
         * 注意该ids 为 detailId 的集合
         */
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
        private String  id;

        /**
        * 单据编号
        */
        private String code;

        /**
        * 第三方交接单号
        */
        private String handoverNo;

        /**
        * 第三方组包号
        */
        private String platformPackageNo;

        /**
        * 物流商id
        */
        private String logisticsSupplierId;

        /**
        * 物流商名
        */
        private String logisticsSupplierName;

        /**
        * 包裹总数量
        */
        private Integer totalPackageQty;

        /**
        * 包裹总重量
        */
        private BigDecimal totalPackageWeight;

        /**
        * 重量单位
        */
        private String weightUnit;

        /**
        * 运输单号
        */
        private String transportNo;

        /**
        * 上传状态
        */
        private String uploadStatus;

        /**
         * 上传状态名
         */
        private String uploadStatusName;

        /**
        * 打印状态
        */
        private String printStatus;

        /**
         * 打印状态名
         */
        private String printStatusName;

        /**
        * 组包日期
        */
        private LocalDate billDate;

        /**
        * 揽收方式
        */
        private String collectMode;

        /**
        * 揽收地址
        */
        private String collectAddress;

        /**
        * 揽收地址id logistics_address
        */
        private String collectAddressId;

        /**
        * 第三方交接状态
        */
        private String handoverStatus;

        /**
        * 备注
        */
        private String remark;

        /**
         * 详情
         */
        private List<PackageForecastDetailDTO.ViewDTO> detailList;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        List<PackageForecastDetailDTO.AddDTO> detailList;


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO  {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;


        /**
         * 组包日期
         */
        @NotNull(message = "组包日期不能为空")
        private LocalDate billDate;


        /**
         * 详情id 不能为空
         */
        @NotNull(message = "详情id不能为空")
        @Size(min = 1, message = "详情不能为空")
        private List<String> detailIdList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {


        /**
        * 物流商id
        */
        @NotBlank(message = "物流商id不能为空")
        private String logisticsSupplierId;

        private String logisticsSupplierName;


        /**
        * 包裹总数量
        */
        @NotNull(message = "包裹总数量不能为空")
        private Integer totalPackageQty;

        /**
        * 包裹总重量
        */
        @NotNull(message = "包裹总重量不能为空")
        @Digits(integer = 12, fraction = 4, message = "包裹总重量整数位不能超过12位，小数位不能超过4位")
        private BigDecimal totalPackageWeight;

        /**
        * 重量单位
        */
        @NotBlank(message = "重量单位不能为空")
        @Size(max = 10,message = "重量单位最大长度不能超过10位")
        private String weightUnit;

        /**
        * 运输单号
        */
        private String transportNo;


        /**
        * 组包日期
        */
        private LocalDate billDate;


    }


}