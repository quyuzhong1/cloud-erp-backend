package com.erp.model.plm.dto;

import lombok.Getter;
import lombok.Setter;

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
     * 文件地址
     */
    private List<String> fileUrl;

    /**
     * 文件名字
     */
    private List<String> fileName;

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
