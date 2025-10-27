package com.erp.server.file.business.plm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.AssetPurchaseOrderDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_ASSET_PURCHASE_ORDER;

/**
 * @Author: wtr
 * @Date: 2025/10/26 14:14
 * @Param:
 * @Return:
 * @Description:
 **/
@Component
@Slf4j
public class ExportPlmAssetPurchaseOrderHandler extends AbstractPageFileEventHandler<AssetPurchaseOrderDTO.ListDTO, AssetPurchaseOrderDTO.PagingParamDTO> {

    @Resource
    private ExportPlmFeign exportPlmFeign;

    @Override
    protected PagingVO<AssetPurchaseOrderDTO.ListDTO> getPageData(PagingDTO<AssetPurchaseOrderDTO.PagingParamDTO> dto) {
        return exportPlmFeign.exportAssetPurchaseOrder(dto);
    }

    @Override
    protected List<AssetPurchaseOrderDTO.ListDTO> getData(FileTask fileTask) {
        AssetPurchaseOrderDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<AssetPurchaseOrderDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/plm/assetPurchaseOrder.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_ASSET_PURCHASE_ORDER;
    }
}

