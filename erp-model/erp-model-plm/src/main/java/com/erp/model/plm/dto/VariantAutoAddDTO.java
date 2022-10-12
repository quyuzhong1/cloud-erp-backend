package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description 商品管理-产品信息-多规格-自动生成 请求参数
 * @Author Luo_WG
 * @Date 2022/9/26 14:37
 **/
@Data
@NoArgsConstructor
public class VariantAutoAddDTO {

    /**
     * 产品spu基础信息
     */
    private ProductInfoDTO productSpuBaseInfoDTO;

    /**
     * 变体属性
     */
    private List<VarianRefPropertyDTO> varianRefPropertyList;
}
