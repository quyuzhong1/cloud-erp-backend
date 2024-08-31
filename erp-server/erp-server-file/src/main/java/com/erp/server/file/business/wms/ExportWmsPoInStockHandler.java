package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.PoInstockDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_PO_IN_STOCK;

@Component
@Slf4j
public class ExportWmsPoInStockHandler extends AbstractPageFileEventHandler<PoInstockDTO.ListDTO, PoInstockDTO.ExportParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;
    private static final ThreadLocal<PoInstockDTO.ExportParamDTO> threadLocal = new ThreadLocal<>();

    @Override
    public String getExcelPath() {
        PoInstockDTO.ExportParamDTO dto = threadLocal.get();
        Boolean isHaveFieldPower = dto.getIsHaveFieldPower();
        String excelPath;
        if (isHaveFieldPower != null && isHaveFieldPower) {
            excelPath = "excel/wms/poInStock.xlsx";
        } else {
            excelPath = "excel/wms/poInStockNotField.xlsx";
        }
        threadLocal.remove();
        return excelPath;
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_PO_IN_STOCK;
    }

    @Override
    protected List<PoInstockDTO.ListDTO> getData(FileTask fileTask) {
        PoInstockDTO.ExportParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<PoInstockDTO.ExportParamDTO>() {
        });
        threadLocal.set(dto);
        return listSeqData(dto);
    }


    @Override
    protected PagingVO<PoInstockDTO.ListDTO> getPageData(PagingDTO<PoInstockDTO.ExportParamDTO> dto) {
        return exportWmsFeign.exportPoInStock(dto);
    }
}
