package com.erp.model.oms.dto;

import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lambda
 * @Classname SoChangeDTO
 * @Description TODO
 * @Date 2023-05-11 9:58
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SoChangeDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class TabListDTO {
        /**
         * 类型
         */
        private String searchType;

        /**
         * 数量
         */
        private Integer count;
    }


    /**
     * 分页信息
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {

        /**
         * id
         */
        private String id;

        /**
         * code
         */
        private String code;

        /**
         * 销售订单code
         */
        private String soCode;


        /**
         * 审核状态code
         */
        private ApproveStatusEnum approveStatus;


        /**
         * 审核状态名
         */
        private String approveStatusName;
        /**
         * 类型
         */
        private String type;

        /**
         * 类型名称
         */
        private String typeName;

        /**
         * 作废状态
         */
        private Boolean invalidStatus;

        /**
         * 作废状态名
         */
        private Boolean invalidStatusName;


        /**
         * 客户id
         */
        private String customerId;

        /**
         * 客户
         */
        private String customerName;


        /**
         * sku id
         */
        private String skuId;


        /**
         * sku no
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 销售数量
         */
        private Integer qty;

        /**
         * 原销售数量
         */
        private Integer oldQty;

        /**
         * 销售金额
         */
        private BigDecimal amount;

        /**
         * 原销售金额
         */
        private BigDecimal oldAmount;

        /**
         * 最新审核人
         */
        private String approveUserName;


        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;


    }

    /**
     * 分页参数信息
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        /**
         * sku no 集合
         */
        private List<String> skuNoList;

        /**
         * code
         */
        private String code;

        /**
         * 销售订单code
         */
        private String soCode;

        /**
         * 类型
         */
        private String type;

        /**
         * 审核列表集合
         */
        private List<String> approveStatusList;

        /**
         * 作废状态
         * true 已作废
         * false 未作废
         */
        private Boolean invalidStatus;

        /**
         * 客户 集合
         */
        private List<String> customerIdList;

        /**
         * 创建人 id 集合
         */
        private List<String> createUserIdList;

        /**
         * 创建时间
         */
        private List<LocalDateTime> createTimeList;

    }


    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {


        /**
         * 销售订单id
         */
        private String soId;


        /**
         * 变更日期
         */
        @NotNull(message = "变更日期不能为空")
        private LocalDate billDate;


        /**
         * 变更部门id
         */
        private String changeDeptId;

        /**
         * 变更人
         */
        @NotBlank(message = "变更人不能为空")
        private String useId;



        /**
         * 变更原因
         */
        @NotBlank(message = "变更原因不能为空")
        @Size(max = 200, message = "变更原因最大50字符")
        private String remark;


        /**
         * 产品信息
         */
        private List<SoChangeDetailDTO.AddDTO> detailList;


    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * id
         */
        private String id;

        /**
         * 销售订单id
         */
        private String soId;

        /**
         * 销售订单code
         */
        private String soCode;


        /**
         * 变更日期
         */
        private LocalDate billDate;


        /**
         * 变更部门id
         */
        private String changeDeptId;

        /**
         * 变更员id
         */
        private String changeUserId;


        /**
         * 客户id
         */
        private String customerId;


        /**
         * 收货人
         */
        private String receiverName;

        /**
         * 电话
         */
        private String telNumber;

        /**
         * 收货人地址
         */
        private String receiverAddress;

        /**
         * 交货方式
         */
        private String deliveryMode;


        /**
         * 币种
         */
        private String currency;

        /**
         * 是否含税
         * true 是
         */
        private Boolean isTax;

        /**
         * 地址类型
         */
        private String addressType;


        /**
         * 备注
         */
        private String remark;


        /**
         * 产品信息
         */
        private List<SoChangeDetailDTO.ViewDTO> detailList;


    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        /**
         * id
         */
        private String id;

        /**
         * 销售订单id
         */
        private String soId;

        /**
         * 销售订单code
         */
        private String soCode;


        /**
         * 变更日期
         */
        private LocalDate billDate;


        /**
         * 变更部门id
         */
        private String changeDeptId;

        /**
         * 变更员id
         */
        private String changeUserId;


        /**
         * 客户id
         */
        private String customerId;


        /**
         * 收货人
         */
        private String receiverName;

        /**
         * 电话
         */
        private String telNumber;

        /**
         * 收货人地址
         */
        private String receiverAddress;

        /**
         * 交货方式
         */
        private String deliveryMode;


        /**
         * 币种
         */
        private String currency;

        /**
         * 是否含税
         * true 是
         */
        private Boolean isTax;

        /**
         * 地址类型
         */
        private String addressType;


        /**
         * 备注
         */
        private String remark;


        /**
         * 产品信息
         */
        private List<SoChangeDetailDTO.UpdateDTO> detailList;
    }


    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {

        private List<String> ids;
    }
}
