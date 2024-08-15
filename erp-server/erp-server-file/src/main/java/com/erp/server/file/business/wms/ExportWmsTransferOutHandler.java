package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.TransferOutDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_TRANSFER_OUT;

@Component
@Slf4j
public class ExportWmsTransferOutHandler extends AbstractPageFileEventHandler<TransferOutDTO.PagingViewDTO, TransferOutDTO.ExportDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;
    @Override
    public String getExcelPath() {
        return "excel/wms/transferOut.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_TRANSFER_OUT;
    }

    @Override
    protected List<TransferOutDTO.PagingViewDTO> getData(FileTask fileTask) {
        TransferOutDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<TransferOutDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }


    @Override
    protected PagingVO<TransferOutDTO.PagingViewDTO> getPageData(PagingDTO<TransferOutDTO.ExportDTO> dto) {
        return exportWmsFeign.exportTransferOut(dto);
    }
}
