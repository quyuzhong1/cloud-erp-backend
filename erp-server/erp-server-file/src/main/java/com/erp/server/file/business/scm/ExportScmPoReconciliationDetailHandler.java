package com.erp.server.file.business.scm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.srm.dto.PoReconciliationDTO;
import com.erp.rpc.scm.feign.ExportScmFeign;
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
public class ExportScmPoReconciliationDetailHandler extends AbstractPageFileEventHandler<PurchaseOrderDTO.ListDTO, PoReconciliationDTO.PagingParamDTO> {
    @Resource
    private ExportScmFeign exportScmFeign;
    @Override
    protected List<PurchaseOrderDTO.ListDTO> getData(FileTask fileTask) {
        PoReconciliationDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<PoReconciliationDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<PurchaseOrderDTO.ListDTO> getPageData(PagingDTO<PoReconciliationDTO.PagingParamDTO> dto) {
        return  null;
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SCM_PURCHASE_ORDER;
    }

    @Override
    public String getExcelPath() {
        return "excel/scm/purchaseOrderExport.xlsx";
    }
}
