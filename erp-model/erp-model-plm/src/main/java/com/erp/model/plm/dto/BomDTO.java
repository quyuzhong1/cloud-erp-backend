package com.erp.model.plm.dto;

import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @Classname BomDTO
 * @Description TODO
 * @Date 2023-01-11 16:27
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BomDTO implements Serializable {

    /**
     * 表Id
     */
    private String id;

    /**
     * 版本
     */
    @NotNull(message = "版本不能为空")
    private Integer version;


    /**
     * 编号
     */
    private String serialNumber;


    /**
     * combination 组合
     * single 单品
     */
    @StateEnumValue(strValues = {"combination", "single"}, message = "类型有误")
    private String type;


    @Valid
    private List<BomSkuDTO> skuList;


    private LocalDateTime createTime;


    private String createUserId;

    private String createUserName;
}
