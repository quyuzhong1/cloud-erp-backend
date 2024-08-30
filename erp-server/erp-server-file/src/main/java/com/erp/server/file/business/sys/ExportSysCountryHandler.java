package com.erp.server.file.business.sys;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.rpc.sys.feign.ExportSysFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SYS_COUNTRY;

@Component
public class ExportSysCountryHandler extends AbstractPageFileEventHandler<DictCountryDTO.PagingViewDTO, DictCountryDTO.PagingParamDTO> {
    @Resource
    private ExportSysFeign exportSysFeign;
    @Override
    protected List<DictCountryDTO.PagingViewDTO> getData(FileTask fileTask) {
        DictCountryDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<DictCountryDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/sys/country.xlsx";
    }

    @Override
    protected PagingVO<DictCountryDTO.PagingViewDTO> getPageData(PagingDTO<DictCountryDTO.PagingParamDTO> dto) {
        return exportSysFeign.exportCountry(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SYS_COUNTRY;
    }
}
