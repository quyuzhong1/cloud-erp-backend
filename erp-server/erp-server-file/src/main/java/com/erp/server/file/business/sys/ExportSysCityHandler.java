package com.erp.server.file.business.sys;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.DictCityDTO;
import com.erp.rpc.sys.feign.ExportSysFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SYS_CITY;

@Component
public class ExportSysCityHandler  extends AbstractPageFileEventHandler<DictCityDTO.PagingViewDTO, DictCityDTO.ProvincePagingParamDTO> {
    @Resource
    private ExportSysFeign exportSysFeign;
    @Override
    protected List<DictCityDTO.PagingViewDTO> getData(FileTask fileTask) {
        DictCityDTO.ProvincePagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<DictCityDTO.ProvincePagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/sys/city.xlsx";
    }

    @Override
    protected PagingVO<DictCityDTO.PagingViewDTO> getPageData(PagingDTO<DictCityDTO.ProvincePagingParamDTO> dto) {
        return exportSysFeign.exportCity(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SYS_CITY;
    }
}
