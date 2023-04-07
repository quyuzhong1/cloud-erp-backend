package com.erp.model.plm.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Classname BomVO
 * @Description TODO
 * @Date 2023-01-14 11:05
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BomVO implements Serializable {




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


    private LocalDateTime updateTime;


    private LocalDateTime createTime;


    private String createUserId;

    private String createUserName = "";
}
