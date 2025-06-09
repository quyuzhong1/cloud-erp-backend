package com.erp.server.file.business.mrp;

import com.common.business.enums.FileTaskEventEnum;
import com.erp.model.mrp.dto.CfgRuleCalcDTO;
import com.erp.rpc.mrp.feign.ExportMrpFeign;
import com.erp.server.file.core.AbstractFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_MRP_HISTORY_SALES_CALC;

@Component
public class ExportMrpHistorySalesCalcHandler  extends AbstractFileEventHandler<CfgRuleCalcDTO.HistorySaleDTO> {

    @Resource
    private ExportMrpFeign exportMrpFeign;

    @Override
    protected List<CfgRuleCalcDTO.HistorySaleDTO> getData(FileTask fileTask) {
        CfgRuleCalcDTO.DownloadDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<CfgRuleCalcDTO.DownloadDTO>() {
        });
        List<CfgRuleCalcDTO.HistorySaleDTO> dtos = exportMrpFeign.exportCalcHistorySale(dto);
        return new ArrayList<>(dtos.stream()
                .collect(Collectors.toMap(
                        v -> new CfgRuleCalcDTO.GroupDTO(v.getSkuId(), v.getShopId(), v.getBillDate()),
                        v -> v,
                        (v1, v2) -> {
                            v1.setQty(v1.getQty() + v2.getQty());
                            return v1;
                        }
                ))
                .values());
    }

    @Override
    protected String getExcelPath() {
        return "excel/mrp/historySaleQty.xlsx";
    }


    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_MRP_HISTORY_SALES_CALC;
    }

}
