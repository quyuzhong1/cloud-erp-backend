package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.ListingPushRecordDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_LISTING_PUSH;

@Component
@Slf4j
public class ExportOmsListingPushHandler extends AbstractPageFileEventHandler<ListingPushRecordDTO.PagingViewDTO, ListingPushRecordDTO.PagingParamDTO> {

    @Resource
    private ExportOmsFeign exportOmsFeign;
    @Override
    protected List<ListingPushRecordDTO.PagingViewDTO> getData(FileTask fileTask) {
        ListingPushRecordDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<ListingPushRecordDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<ListingPushRecordDTO.PagingViewDTO> getPageData(PagingDTO<ListingPushRecordDTO.PagingParamDTO> dto) {
        return exportOmsFeign.exportListingPush(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_OMS_LISTING_PUSH;
    }

    @Override
    public String getExcelPath() {
        return "excel/oms/ListingPush.xlsx";
    }
}
