package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.TransferInfoDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_TRANSFER_INFO;

@Component
@Slf4j
public class ExportWmsTransferInfoHandler extends AbstractPageFileEventHandler<TransferInfoDTO.ListDTO, TransferInfoDTO.SearchParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;
    @Override
    public String getExcelPath() {
        return "excel/wms/transferInfo.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_TRANSFER_INFO;
    }



    @Override
    protected PagingVO<TransferInfoDTO.ListDTO> getPageData(PagingDTO<TransferInfoDTO.SearchParamDTO> dto) {
        return exportWmsFeign.exportTransferInfo(dto);
    }
}
