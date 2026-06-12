package com.erp.server.file.business.mrp;

import com.alibaba.excel.write.handler.WriteHandler;
import com.common.business.enums.FileTaskEventEnum;
import com.common.core.excel.ExcelPrintUtils;
import com.erp.model.mrp.dto.CfgRuleCalcDTO;
import com.erp.rpc.mrp.feign.ExportMrpFeign;
import com.erp.server.file.core.AbstractFileEventHandler;
import com.erp.server.file.core.ExportTempFilesHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_MRP_HISTORY_SALES_CALC;

/**
 * 历史销量计算导出处理器。
 *
 * <p><strong>【系统已停用 · 不再维护 · 待移除】</strong>
 * 对应事件 {@link com.common.business.enums.FileTaskEventEnum#EXPORT_MRP_HISTORY_SALES_CALC} 上游已不再下发，
 * 本处理器仅为兼容存量任务而保留，计划在确认无存量任务后整体删除。
 *
 * <p>审查说明（以下为<strong>有意保留</strong>的历史写法，属既定停用代码，请勿再提改造建议）：
 * <ul>
 *   <li>沿用根类旧的「一次性 {@link #getData} 全量入内存 → {@code patchExportListToFile} 写出」流程，
 *       未迁移到分页/流式导出；因本能力已停用、数据量有限，不做迁移。</li>
 *   <li>{@code getData} 为已废弃抽象方法的实现，随本处理器一并待移除，不再新增调用方。</li>
 * </ul>
 * 新增同类导出请继承 {@code AbstractPageFileEventHandler} 等分页基类，勿参照本类。
 *
 * @deprecated 系统已停用，待移除；勿在新代码中引用或参照其实现方式。
 */
@Deprecated
@Component
public class ExportMrpHistorySalesCalcHandler  extends AbstractFileEventHandler<CfgRuleCalcDTO.HistorySaleDTO> {

    @Resource
    private ExportMrpFeign exportMrpFeign;

    @Deprecated
    @Override
    public void handle(FileTask fileTask){
        List<CfgRuleCalcDTO.HistorySaleDTO> list = getData(fileTask);
        String displayName = buildDownloadFileName(fileTask);
        ExportTempFilesHandler.exportToTempAndUpload(fileTask, ".xlsx", displayName, outFile -> {
            new ExcelPrintUtils().patchExportListToFile(outFile, list, getExcelPath(),
                    getWriteHandler().toArray(new WriteHandler[0]));
            return list.size();
        });
    }

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
