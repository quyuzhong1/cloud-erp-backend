package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.erp.model.wms.dto.AliexpressDeliveryDTO;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.erp.server.file.enums.FileTaskEventEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.erp.server.file.enums.FileTaskEventEnum.ALIEXPRESS_DELIVERY_EXPORT;

@Component
@Slf4j
public class AliexpressDeliveryExportHandler extends AbstractPageFileEventHandler<AliexpressDeliveryDTO.ListDTO, AliexpressDeliveryDTO.SearchParamDTO> {


    @Override
    public String getExcelPath() {
        return "excel/aliexpressDeliveryExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return ALIEXPRESS_DELIVERY_EXPORT;
    }

    @Override
    protected List<AliexpressDeliveryDTO.ListDTO> getData(FileTask fileTask) {
        return null;
    }


    @Override
    protected List<AliexpressDeliveryDTO.ListDTO> getPageData(PagingDTO<AliexpressDeliveryDTO.SearchParamDTO> dto) {
        return null;
    }

    @Override
    protected int count(AliexpressDeliveryDTO.SearchParamDTO searchParamDTO) {
        return 0;
    }
}
