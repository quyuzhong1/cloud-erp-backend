package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.KolPartnerInfoDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_KOL_PARTNER_INFO;

/**
 * 企业达人库导出
 * @date 2025-12-03
 * @author jack
 */
@Component
@Slf4j
public class ExportOmsKolPartnerInfoHandler extends AbstractPageFileEventHandler<KolPartnerInfoDTO.ListDTO, KolPartnerInfoDTO.PagingParamDTO> {

    @Resource
    private ExportOmsFeign exportOmsFeign;

    @Override
    protected PagingVO<KolPartnerInfoDTO.ListDTO> getPageData(PagingDTO<KolPartnerInfoDTO.PagingParamDTO> dto) {
        return exportOmsFeign.exportKolPartnerInfo(dto);
    }

    @Override
    protected List<KolPartnerInfoDTO.ListDTO> getData(FileTask fileTask) {
        KolPartnerInfoDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<KolPartnerInfoDTO.PagingParamDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/oms/kolPartnerInfoExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_OMS_KOL_PARTNER_INFO;
    }
}
