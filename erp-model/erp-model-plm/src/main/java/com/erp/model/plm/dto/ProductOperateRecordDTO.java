package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 产品操作记录表
 */
@Data
@NoArgsConstructor
public class ProductOperateRecordDTO implements Serializable {
    /**
     * 主键id
     */
    private String id;

    /**
     * 产品表id
     */
    private String productId;

    /**
     * 记录描述
     */
    private String remark;
}