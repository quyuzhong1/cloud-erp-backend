package com.erp.server.wms.service;

import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.AfterSalePackDTO;
import com.erp.model.wms.dto.AfterSalesWarehouseLocationSuggestDto;

import java.util.List;
import java.util.Map;

/**
 * 售后 PDA：货品上架、整箱移仓（仓位移动已审核 + 库存流水）
 */
public interface PdaAfterSalesWarehouseMoveService {

    /**
     * 货品上架：单 SKU，空仓位/源仓位 → 目标仓位
     *
     * @return 仓位移动主单 id
     */
    String submitGoodsInfo(AfterSalesWarehouseLocationSuggestDto.PdaGoodsShelvingSubmitDto dto);

    /**
     * 整箱移仓提交：多行明细（箱+SKU+源仓位+数量）→ 同一仓库内目标仓位；
     * 提交前按箱唛接口校验 usageStatus，并按即时库存校验每行可用量。
     *
     * @return 仓位移动主单 id
     */
    String submitFullBoxInfo(AfterSalesWarehouseLocationSuggestDto.PdaFullBoxTransferSubmitDto dto);

    void saveMoveCartonDetails(String moveId, String targetCode, List<AfterSalePackDTO.ViewDTO> boxInfoList, Map<String, SkuVO> skuByNo);
}
