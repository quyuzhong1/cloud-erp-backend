package com.erp.model.plm.dto;

import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Classname BomDTO

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
    @NotBlank(message = "版本不能为空")
    private String version;


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

    @Data
    @NoArgsConstructor
    public static class BomSku{


        private String type;

        private String parentSkuId;

        private String parentSkuNo;

        private String skuId;

        private String skuNo;

        private String serialNumber;

        private Integer qty;



    }
}
