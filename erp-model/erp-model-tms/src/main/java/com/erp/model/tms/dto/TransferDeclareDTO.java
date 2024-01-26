package com.erp.model.tms.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 中转报关表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
*/
@Data
@NoArgsConstructor
public class TransferDeclareDTO implements Serializable {




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
        * 预计中转日期
        */
        private LocalDate planTransferDate;

        /**
        * 上传状态
        */
        private String uploadStatus;

        /**
        * 发货物流商id
        */
        private String deliveryLogisticsSupplierId;

        /**
        * 发货物流商中文
        */
        private String deliveryLogisticsSupplierName;

        /**
        * 中转物流商id
        */
        private String transferLogisticsSupplierId;

        /**
        * 中转物流服务商
        */
        private String transferLogisticsSupplierName;

        /**
        * 中转渠道id
        */
        private String transferChannelId;

        /**
        * 中转渠道中文
        */
        private String transferChannelName;

        /**
        * 包裹总数量
        */
        private Integer packageTotalQty;

        /**
        * 包裹总重量
        */
        private BigDecimal packageTotalWeight;

        /**
        * 包裹重量单位
        */
        private String weightUnit;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 详情
         */
        private List<TransferDeclareDetailDTO.AddDTO> detailList;
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
         * 详情
         */
        private List<TransferDeclareDetailDTO.UpdateDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 发货物流商id
        */
        @NotBlank(message = "发货物流商id不能为空")
        @Size(max = 19,message = "发货物流商id最大长度不能超过19位")
        private String deliveryLogisticsSupplierId;

        /**
        * 中转物流商id
        */
        @NotBlank(message = "中转物流商id不能为空")
        @Size(max = 19,message = "中转物流商id最大长度不能超过19位")
        private String transferLogisticsSupplierId;

        /**
        * 中转渠道id
        */
        @NotBlank(message = "中转渠道id不能为空")
        @Size(max = 19,message = "中转渠道id最大长度不能超过19位")
        private String transferChannelId;
    }

    /**
     * 列表查询入参
     */
    @Data
    public static class PagingParamDTO extends SortDTO {
        /**
         * 详情表id（用于导出excel）
         */
        private List<String> detailIdList;

        /**
         * tabFlag
         * 来源：/tms/drop/down/dict/list/key=TransferDeclareTabFlag
         */
        private String tabFlag;

        /**
         * 单据编号
         */
        private String code;

        /**
         * 订单编号
         */
        private String soCode;

        /**
         * 物流渠道id
         * 地址：http://172.16.100.11:3002/project/128/interface/api/25999
         */
        private List<String> logisticsChannelIdList;

        /**
         * 发货物流商id
         * 地址：http://172.16.100.11:3002/project/128/interface/api/25999
         */
        private List<String> deliveryLogisticsSupplierIdList;

        /**
         * 中转物流商id
         * 地址：http://172.16.100.11:3002/project/128/interface/api/28031
         */
        private List<String> transferLogisticsSupplierIdList;

        /**
         * 上传状态
         * 来源：/tms/drop/down/dict/list/key=transferUploadStatus
         */
        private List<String> uploadStatusList;

        /**
         * 出库状态
         * 来源：/tms/drop/down/dict/list/key=transferOutstockStatus
         */
        private List<String> outstockStatusList;

        /**
         * 中转状态
         */
        private List<String> transferStatusList;

        /**
         * 创建人
         */
        private List<String> createUserIdList;

        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;


        public PagingParamDTO() {
            this.detailIdList = new ArrayList<>();
            this.logisticsChannelIdList = new ArrayList<>();
            this.deliveryLogisticsSupplierIdList = new ArrayList<>();
            this.transferLogisticsSupplierIdList = new ArrayList<>();
            this.uploadStatusList = new ArrayList<>();
            this.outstockStatusList = new ArrayList<>();
            this.transferStatusList = new ArrayList<>();
            this.createUserIdList = new ArrayList<>();
            this.createTimeList = new ArrayList<>();
        }
    }

    /**
     * 分页列表信息
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 明细id
         */
        private String detailId;
        /**
         * 中转报关单号
         */
        private String code;
        /**
         * 预计中转时间
         */
        private String planTransferDate;
        /**
         * 上传状态(批次)
         */
        private String uploadBatchStatus;
        /**
         * 上传状态（批次）中文
         */
        private String uploadBatchStatusName;
        /**
         * 发货物流商中文
         */
        private String deliveryLogisticsSupplierName;
        /**
         * 中转物流商中文
         */
        private String transferLogisticsSupplierName;
        /**
         * 中转渠道中文
         */
        private String transferChannelName;
        /**
         * 包裹总数量
         */
        private String packageTotalQty;
        /**
         * 包裹总重量
         */
        private String packageTotalWeight;
        /**
         * 重量单位
         */
        private String weightUnit;
        /**
         * 销售单号
         */
        private String soCode;
        /**
         * 物流渠道中文
         */
        private String logisticsChannelName;
        /**
         * 物流跟踪号
         */
        private String trackNo;
        /**
         * 上传状态(订单)
         */
        private String uploadOrderStatus;
        /**
         * 上传状态(订单)中文
         */
        private String uploadOrderStatusName;
        /**
         * 出库状态
         */
        private String outstockStatus;
        /**
         * 出库状态中文
         */
        private String outstockStatusName;
        /**
         * 中转状态
         */
        private String transferStatus;
        /**
         * 中转状态中文
         */
        private String transferStatusName;
        /**
         * 失败原因
         */
        private String failureReason;
        /**
         * 创建人
         */
        private String createUserName;
        /**
         * 创建时间
         */
        private LocalDateTime createTime;
    }

    /**
     * tab页
     */
    @Data
    @NoArgsConstructor
    public static class TabListDTO {
        /**
         * tab
         */
        private String tabFlag;
        /**
         * 数量
         */
        private Integer count;

    }

    /**
     * 详情列表查询条件
     */
    @Data
    @NoArgsConstructor
    public static class ViewDetailParamDTO {
        /**
         * 主表id
         */
        @NotBlank(message = "mainId不能为空")
        private String mainId;

        /**
         * 物流跟踪号
         */
        private String trackNo;

        /**
         * 物流渠道id
         * 地址：http://172.16.100.11:3002/project/128/interface/api/25999
         */
        private List<String> logisticsChannelIdList;
    }

    /**
     * 预报设置入参
     */
    @Data
    @NoArgsConstructor
    public static class ForcastSettingParamDTO {
        /**
         * 主键id（报关设置id）
         */
        private String id;

        /**
         * 发货物流商id
         */
        @NotEmpty(message = "发货物流商id不能为空")
        private List<String> deliveryLogisticsSupplierIdList;

        /**
         * 中转物流商/中转渠道Id
         */
        @NotBlank(message = "中转渠道Id不能为空")
        private String transferChannelId;

    }
}