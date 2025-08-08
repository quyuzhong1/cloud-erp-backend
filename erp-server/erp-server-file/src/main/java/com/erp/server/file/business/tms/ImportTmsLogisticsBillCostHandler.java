package com.erp.server.file.business.tms;

import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.erp.rpc.tms.feign.ImportTmsFeign;
import com.erp.server.file.core.AbstractImportEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.IMPORT_TMS_LOGISTICS_BILL_COST;

//@Component
@Slf4j
public class ImportTmsLogisticsBillCostHandler extends AbstractImportEventHandler<BaseDTO.ImportDTO> {
    @Resource
    private ImportTmsFeign importTmsFeign;

    @Override
    public FileTaskEventEnum getEvent() {
        return IMPORT_TMS_LOGISTICS_BILL_COST;
    }

    @Override
    protected void getData(FileTask fileTask) {
        BaseDTO.ImportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<BaseDTO.ImportDTO>() {
        });
        dto.setTaskId(fileTask.getId());
        importTmsFeign.importLogisticsBillCost(dto);
    }
}
