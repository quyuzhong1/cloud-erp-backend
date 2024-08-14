package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.PackageForecastDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_PACKAGE_FORECAST;


@Component
public class ExportWmsPackageForecastHandler  extends AbstractPageFileEventHandler<PackageForecastDTO.ExportViewDTO, PackageForecastDTO.ExportDTO> {
    @Resource
    private ExportWmsFeign exportWmsFeign;
    @Override
    protected List<PackageForecastDTO.ExportViewDTO> getData(FileTask fileTask) {
        PackageForecastDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<PackageForecastDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<PackageForecastDTO.ExportViewDTO> getPageData(PagingDTO<PackageForecastDTO.ExportDTO> dto) {
        return exportWmsFeign.exportPackageForecast(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_PACKAGE_FORECAST;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/packageForecast.xlsx";
    }
}
