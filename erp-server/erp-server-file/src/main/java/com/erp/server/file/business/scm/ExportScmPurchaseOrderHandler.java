package com.erp.server.file.business.scm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.rpc.wms.feign.ExportScmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_PURCHASE_ORDER;

@Component
@Slf4j
public class ExportScmPurchaseOrderHandler extends AbstractPageFileEventHandler<PurchaseOrderDTO.ListDTO, PurchaseOrderDTO.SearchParamDTO> {
    @Resource
    private ExportScmFeign exportScmFeign;
    @Override
    protected List<PurchaseOrderDTO.ListDTO> getData(FileTask fileTask) {
        PurchaseOrderDTO.SearchParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<PurchaseOrderDTO.SearchParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<PurchaseOrderDTO.ListDTO> getPageData(PagingDTO<PurchaseOrderDTO.SearchParamDTO> dto) {
        return exportScmFeign.exportPurchaseOrder(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SCM_PURCHASE_ORDER;
    }

    @Override
    public String getExcelPath() {
        return "excel/plm/bom.xlsx";
    }
}
