package com.erp.model.plm.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @Classname BomVersionVO
 * @Description TODO
 * @Date 2023-01-30 10:14
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BomVersionVO implements Serializable {
    /**
     * bom 历史表id
     */
    private String bomHistoryId;


    /**
     * 表Id
     */
    private String bomId;

    /**
     * 版本
     */
    @NotNull(message = "版本不能为空")
    private Integer version;


    private String serialNumber;


    /**
     * 类型
     */
    private String type;


    private Date createTime;


    private String createUserId;

    private String createUserName = "";


    private String refSku;
}
