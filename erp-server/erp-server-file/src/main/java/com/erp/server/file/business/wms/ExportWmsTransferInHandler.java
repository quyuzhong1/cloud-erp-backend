package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.TransferInDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_TRANSFER_IN;

@Component
@Slf4j
public class ExportWmsTransferInHandler extends AbstractPageFileEventHandler<TransferInDTO.PagingViewDTO, TransferInDTO.ExportDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;
    @Override
    public String getExcelPath() {
        return "excel/wms/transferIn.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_TRANSFER_IN;
    }

    @Override
    protected List<TransferInDTO.PagingViewDTO> getData(FileTask fileTask) {
        TransferInDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<TransferInDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }


    @Override
    protected PagingVO<TransferInDTO.PagingViewDTO> getPageData(PagingDTO<TransferInDTO.ExportDTO> dto) {
        return exportWmsFeign.exportTransferIn(dto);
    }
}
