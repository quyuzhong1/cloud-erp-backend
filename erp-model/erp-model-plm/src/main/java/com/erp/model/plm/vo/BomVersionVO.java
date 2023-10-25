package com.erp.model.plm.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Classname BomVersionVO

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
    @NotBlank(message = "版本不能为空")
    private String version;


    private String serialNumber;


    /**
     * 类型
     */
    private String type;


    private LocalDateTime createTime;


    private String createUserId;

    private String createUserName = "";


    private String refSku;
}
