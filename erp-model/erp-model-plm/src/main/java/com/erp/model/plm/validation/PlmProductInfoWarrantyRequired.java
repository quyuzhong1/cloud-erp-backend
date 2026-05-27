package com.erp.model.plm.validation;

/**
 * Bean Validation 分组：嵌套 {@link com.erp.model.plm.dto.ProductInfoDTO} 时要求填写「产品质保期」「研发团队」。
 * <p>多规格「自动生成」{@link com.erp.model.plm.dto.VariantAutoAddDTO}、多规格「新增/修改」
 * {@code saveOrUpdateManySpec}（含任务生成 SKU 后保存）不使用该分组，上述字段可为空。</p>
 */
public interface PlmProductInfoWarrantyRequired {
}
