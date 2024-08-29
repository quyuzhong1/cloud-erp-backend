package com.erp.server.file.business.scm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.vo.BomExportExcelVO;
import com.erp.rpc.wms.feign.ExportScmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_PURCHASE_ORDER_CONTRACT;

@Component
@Slf4j
public class ExportScmPurchaseOrderContractHandler extends AbstractPageFileEventHandler<BomExportExcelVO, String> {
    @Resource
    private ExportScmFeign exportScmFeign;
    @Override
    protected List<BomExportExcelVO> getData(FileTask fileTask) {
        return listSeqData(fileTask.getMetaInfo());
    }

    @Override
    protected PagingVO<BomExportExcelVO> getPageData(PagingDTO<String> dto) {
        return exportScmFeign.exportPurchaseOrderContract(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SCM_PURCHASE_ORDER_CONTRACT;
    }

    @Override
    public String getExcelPath() {
        return "excel/scm/purchaseContractExport.xlsx";
    }
}
