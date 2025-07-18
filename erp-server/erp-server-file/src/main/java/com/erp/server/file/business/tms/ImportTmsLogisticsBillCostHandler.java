package com.erp.server.file.business.tms;

import com.common.business.dto.base.BaseDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.server.file.core.AbstractImportEventHandler;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.IMPORT_TMS_LOGISTICS_BILL_COST;

@Component
@Slf4j
public class ImportTmsLogisticsBillCostHandler extends AbstractImportEventHandler<BaseDTO.ImportDTO> {
    @Resource
    private ExportTmsFeign exportTmsFeign;

    @Override
    public String getExcelPath() {
        return "excel/tms/logisticsBillCost.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return IMPORT_TMS_LOGISTICS_BILL_COST;
    }

    @Override
    protected BaseDTO.ImportResultDTO getData(FileTask fileTask) {
        BaseDTO.ImportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<BaseDTO.ImportDTO>() {
        });
        return exportTmsFeign.importLogisticsBillCost(dto);
    }
}
