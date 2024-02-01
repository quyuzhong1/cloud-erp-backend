package com.erp.model.wms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

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

        /**
         * 数量
         */
        private Integer count;
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
         *单位
         */
        private String weightUnit;

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


        private String soId;

        private String soCode;

        private String soType;



    }

    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO{


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
        * 打印状态
        */
        private String printStatus;

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