package com.erp.model.plm.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class MouldRefundVoucherDTO {

    /**
     * 模具id
     */
    private String mouldDetailId;

    /**
     * 返还金额
     */
    private BigDecimal refundAmount;

    /**
     * 实际返还金额
     */
    private BigDecimal realRefundAmount;

    /**
     * 文件地址
     */
    private List<String> fileUrlList;

    /**
     * 文件名字
     */
    private List<String> fileNameList;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    private String createUserName;
}
