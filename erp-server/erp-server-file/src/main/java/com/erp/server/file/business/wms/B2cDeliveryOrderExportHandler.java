package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.erp.server.file.enums.FileTaskEventEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.erp.server.file.enums.FileTaskEventEnum.B2C_DELIVERY_ORDER_EXPORT;

@Component
@Slf4j
public class B2cDeliveryOrderExportHandler extends AbstractPageFileEventHandler<SoB2cDeliveryDTO.ListDTO, SoB2cDeliveryDTO.PagingParamDTO> {

    @Override
    public String getExcelPath() {
        return "excel/b2cDeliveryOrderExport.xlsx";
    }


    @Override
    public FileTaskEventEnum getEvent() {
        return B2C_DELIVERY_ORDER_EXPORT;
    }

    @Override
    protected List<SoB2cDeliveryDTO.ListDTO> getData(FileTask fileTask) {
        return null;
    }

    @Override
    protected PagingVO<SoB2cDeliveryDTO.ListDTO> getPageData(PagingDTO<SoB2cDeliveryDTO.PagingParamDTO> dto) {
        return null;
    }
}
