package com.erp.server.file.business.scm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.PurchaseApplicationDTO;
import com.erp.rpc.scm.feign.ExportScmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_PURCHASE_APPLICATION;

@Component
@Slf4j
public class ExportScmPurchaseApplicationHandler extends AbstractPageFileEventHandler<PurchaseApplicationDTO.ListDTO, PurchaseApplicationDTO.SearchParamDTO> {
    @Resource
    private ExportScmFeign exportScmFeign;
    @Override
    protected List<PurchaseApplicationDTO.ListDTO> getData(FileTask fileTask) {
        PurchaseApplicationDTO.SearchParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<PurchaseApplicationDTO.SearchParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<PurchaseApplicationDTO.ListDTO> getPageData(PagingDTO<PurchaseApplicationDTO.SearchParamDTO> dto) {
        return exportScmFeign.exportPurchaseApplication(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SCM_PURCHASE_APPLICATION;
    }

    @Override
    public String getExcelPath() {
        return "excel/scm/purchaseApplication.xlsx";
    }
}
