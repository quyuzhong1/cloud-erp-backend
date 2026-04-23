package com.erp.model.tms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.dto.base.SuperDTO;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 物流下单表请求响应实体
 * </p>
 *
 * @author lei.nie
 * @since 2026-04-20
 */
@Data
@NoArgsConstructor
public class LogisticsOrderDTO implements Serializable {

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

        /**
         * 数量
         */
        private Integer count;

    }

    /**
     * 分页列表查询参数
     */
    @EqualsAndHashCode(callSuper = true)
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
        private Map<String, String> sqlMap;

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
        private String id;

        /**
         * 售后申请id,after_sale.id
         */
        private String afterSaleId;

        /**
         * 单据编号
         */
        private String code;

        /**
         * 单据状态
         */
        private String status;

        /**
         * 单据状态名称
         */
        private String statusName;

        /**
         * 面单状态
         */
        private String labelStatus;

        /**
         * 面单状态名称
         */
        private String labelStatusName;

        /**
         * 异常类型
         */
        private String exceptionType;

        /**
         * 异常原因
         */
        private String exceptionReason;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 来源单据类型
         */
        private String sourceType;

        /**
         * 来源单据类型名称
         */
        private String sourceTypeName;

        /**
         * 物流平台
         */
        private String logisticsPlatform;

        /**
         * 物流平台名称
         */
        private String logisticsPlatformName;

        /**
         * 物流渠道id
         */
        private String logisticsChannelId;

        /**
         * 物流渠道名称
         */
        private String logisticsChannelName;

        /**
         * 物流跟踪号
         */
        private String trackNo;

        /**
         * 运单号
         */
        private String transportNo;

        /**
         * 备注
         */
        private String remark;

        /**
         * 收件人
         */
        private String receiver;

        /**
         * 电话
         */
        private String contactNumber;

        /**
         * 国家,dict_country.id
         */
        private String country;

        /**
         * 省/州
         */
        private String province;

        /**
         * 城市
         */
        private String city;

        /**
         * 详细地址
         */
        private String detailedAddress;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 创建人名称
         */
        private String createUserName;

    }

    /**
     * 导出Excel
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
         * 勾选的id集合
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
        private String id;

        /**
         * 售后申请id,after_sale.id
         */
        private String afterSaleId;

        /**
         * 单据编号
         */
        private String code;

        /**
         * 单据状态
         */
        private String status;

        /**
         * 单据状态名称
         */
        private String statusName;

        /**
         * 面单状态
         */
        private String labelStatus;

        /**
         * 面单状态名称
         */
        private String labelStatusName;

        /**
         * 异常类型
         */
        private String exceptionType;

        /**
         * 异常原因
         */
        private String exceptionReason;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 来源单据类型
         */
        private String sourceType;

        /**
         * 来源单据类型名称
         */
        private String sourceTypeName;

        /**
         * 物流平台
         */
        private String logisticsPlatform;

        /**
         * 物流平台名称
         */
        private String logisticsPlatformName;

        /**
         * 物流渠道id
         */
        private String logisticsChannelId;

        /**
         * 物流渠道名称
         */
        private String logisticsChannelName;

        /**
         * 物流跟踪号
         */
        private String trackNo;

        /**
         * 运单号
         */
        private String transportNo;

        /**
         * 备注
         */
        private String remark;

        /**
         * 收件人
         */
        private String receiver;

        /**
         * 电话
         */
        private String contactNumber;

        /**
         * 国家,dict_country.id
         */
        private String country;

        /**
         * 省/州
         */
        private String province;

        /**
         * 城市
         */
        private String city;

        /**
         * 详细地址
         */
        private String detailedAddress;

    }

    /**
     * 新增
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

    }

    /**
     * 修改
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class CommonDTO extends SuperDTO {

        /**
         * 售后申请id,after_sale.id
         */
        @Size(max = 19, message = "售后申请id,after_sale.id最大长度不能超过19位")
        private String afterSaleId;

        /**
         * 单据状态
         */
        @Size(max = 32, message = "单据状态最大长度不能超过32位")
        private String status;

        /**
         * 面单状态
         */
        @Size(max = 32, message = "面单状态最大长度不能超过32位")
        private String labelStatus;

        /**
         * 异常类型
         */
        @Size(max = 19, message = "异常类型最大长度不能超过19位")
        private String exceptionType;

        /**
         * 异常原因
         */
        @Size(max = 200, message = "异常原因最大长度不能超过200位")
        private String exceptionReason;

        /**
         * 来源单号
         */
        @Size(max = 32, message = "来源单号最大长度不能超过32位")
        private String sourceCode;

        /**
         * 来源单据类型
         */
        @Size(max = 32, message = "来源单据类型最大长度不能超过32位")
        private String sourceType;

        /**
         * 物流平台
         */
        @Size(max = 30, message = "物流平台最大长度不能超过30位")
        private String logisticsPlatform;

        /**
         * 物流渠道id
         */
        @Size(max = 19, message = "物流渠道id最大长度不能超过19位")
        private String logisticsChannelId;

        /**
         * 物流跟踪号
         */
        @Size(max = 30, message = "物流跟踪号最大长度不能超过30位")
        private String trackNo;

        /**
         * 运单号
         */
        @Size(max = 30, message = "运单号最大长度不能超过30位")
        private String transportNo;

        /**
         * 备注
         */
        @Size(max = 500, message = "备注最大长度不能超过500位")
        private String remark;

        /**
         * 收件人
         */
        @Size(max = 50, message = "收件人最大长度不能超过50位")
        private String receiver;

        /**
         * 电话
         */
        @Size(max = 32, message = "电话最大长度不能超过32位")
        private String contactNumber;

        /**
         * 国家,dict_country.id
         */
        @Size(max = 100, message = "国家,dict_country.id最大长度不能超过100位")
        private String country;

        /**
         * 省/州
         */
        @NotBlank(message = "省/州不能为空")
        @Size(max = 100, message = "省/州最大长度不能超过100位")
        private String province;

        /**
         * 城市
         */
        @NotBlank(message = "城市不能为空")
        @Size(max = 100, message = "城市最大长度不能超过100位")
        private String city;

        /**
         * 详细地址
         */
        @NotBlank(message = "详细地址不能为空")
        @Size(max = 200, message = "详细地址最大长度不能超过200位")
        private String detailedAddress;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UploadFileDTO {

        @NotBlank(message = "id不能为空")
        private String id;

        private MultipartFile file;
    }

    @Data
    @NoArgsConstructor
    public static class LogisticsLabelPreviewDTO {

        /**
         * 有运单号数量
         */
        private Integer trackNoCount;

        /**
         * 无运单号数量
         */
        private Integer notTrackNoCount;

        /**
         * 不可打印数量
         */
        private Integer notPrintCount;

        /**
         * 物流面单预览列表
         */
        private List<LogisticsLabelPreviewListDTO> labelPreviewListDTOS;

    }

    @Data
    @NoArgsConstructor
    public static class LogisticsLabelPreviewListDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 售后申请id,after_sale.id
         */
        private String afterSaleId;

        /**
         * 单据编号
         */
        private String code;

        /**
         * 单据状态
         */
        private String status;

        /**
         * 面单状态
         */
        private String labelStatus;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 物流平台
         */
        private String logisticsPlatform;

        /**
         * 物流平台名称
         */
        private String logisticsPlatformName;

        /**
         * 物流渠道id
         */
        private String logisticsChannelId;

        /**
         * 物流渠道名称
         */
        private String logisticsChannelName;

        /**
         * 物流跟踪号
         */
        private String trackNo;

        /**
         * 运单号
         */
        private String transportNo;

        /**
         * 附件名称
         */
        private String attachName;

        /**
         * url
         */
        private String attachUrl;

    }

    @Data
    @NoArgsConstructor
    public static class LogisticsLabelDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 单据编号
         */
        private String code;

        /**
         * 物流平台
         */
        private String logisticsPlatform;

        /**
         * 物流渠道id
         */
        @NotBlank(message = "渠道不能为空")
        private String logisticsChannelId;

        /**
         * 物流跟踪号
         */
        @NotBlank(message = "物流跟踪号不能为空")
        private String trackNo;

        /**
         * 运单号
         */
        private String transportNo;

        /**
         * 是否来自mq消费
         */
        private Boolean isFromMq = false;

    }

}