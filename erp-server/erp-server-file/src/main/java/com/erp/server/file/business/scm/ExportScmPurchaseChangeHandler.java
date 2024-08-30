package com.erp.server.file.business.scm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.PurchaseChangeDTO;
import com.erp.model.scm.dto.excel.PurchaseChangeExportExcelDTO;
import com.erp.rpc.wms.feign.ExportScmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_PURCHASE_CHANGE;

@Component
@Slf4j
public class ExportScmPurchaseChangeHandler extends AbstractPageFileEventHandler<PurchaseChangeExportExcelDTO, PurchaseChangeDTO.SearchParamDTO> {
    @Resource
    private ExportScmFeign exportScmFeign;
    @Override
    protected List<PurchaseChangeExportExcelDTO> getData(FileTask fileTask) {
        PurchaseChangeDTO.SearchParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<PurchaseChangeDTO.SearchParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<PurchaseChangeExportExcelDTO> getPageData(PagingDTO<PurchaseChangeDTO.SearchParamDTO> dto) {
        return exportScmFeign.exportPurchaseChange(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SCM_PURCHASE_CHANGE;
    }

    @Override
    public String getExcelPath() {
        return "excel/scm/purchaseChange.xlsx";
    }
}
