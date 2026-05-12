package com.erp.model.plm.validation;

/**
 * Bean Validation 分组：嵌套 {@link com.erp.model.plm.dto.ProductInfoDTO} 时要求填写「产品质保期」。
 * <p>多规格「自动生成」接口 {@link com.erp.model.plm.dto.VariantAutoAddDTO} 不使用该分组，质保期可为空。</p>
 */
public interface PlmProductInfoWarrantyRequired {
}
