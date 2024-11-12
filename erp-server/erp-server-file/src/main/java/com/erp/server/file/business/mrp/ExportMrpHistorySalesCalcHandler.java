package com.erp.server.file.business.mrp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.CfgRuleCalcDTO;
import com.erp.model.mrp.dto.DeliverySuggestDTO;
import com.erp.model.mrp.dto.FbaHistoryInventoryDTO;
import com.erp.rpc.mrp.feign.ExportMrpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_MRP_HISTORY_SALES_CALC;

@Component
public class ExportMrpHistorySalesCalcHandler  extends AbstractPageFileEventHandler<CfgRuleCalcDTO.HistorySaleDTO , CfgRuleCalcDTO.DownloadDTO> {

    @Resource
    private ExportMrpFeign exportMrpFeign;

    @Override
    protected List<CfgRuleCalcDTO.HistorySaleDTO> getData(FileTask fileTask) {
        CfgRuleCalcDTO.DownloadDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<CfgRuleCalcDTO.DownloadDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return null;
    }

    @Override
    protected PagingVO<CfgRuleCalcDTO.HistorySaleDTO> getPageData(PagingDTO<CfgRuleCalcDTO.DownloadDTO> dto) {
        return exportMrpFeign.exportCalcHistorySale(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_MRP_HISTORY_SALES_CALC;
    }

    @Override
    protected int getPageSize() {
        return 10000;
    }
}
