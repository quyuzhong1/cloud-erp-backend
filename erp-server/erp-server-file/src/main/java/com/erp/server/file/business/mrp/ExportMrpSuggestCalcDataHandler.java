package com.erp.server.file.business.mrp;

import cn.hutool.core.lang.Pair;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.rpc.mrp.feign.ExportMrpFeign;
import com.erp.server.file.core.AbstractFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.erp.server.file.exception.BusinessException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_MRP_SUGGESTION_CALC_DATA;

@Slf4j
@Component
public class ExportMrpSuggestCalcDataHandler extends AbstractFileEventHandler<Pair<Integer, List<?>>> {

    @Resource
    private ExportMrpFeign exportMrpFeign;


    @Override
    public void handle(FileTask fileTask) {
        List<Pair<Integer, List<?>>> list = getData(fileTask);
        fileTask.setCount(list.get(0).getValue().size());
        StringBuilder sb = new StringBuilder();
        String excelPath = getExcelPath();
        String name = fileTask.getFileName();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        sb.append(excelPath.substring(excelPath.lastIndexOf(".")));
        try {
            byte[] bytes = new ExcelPrintUtils().sheetPatchExport(list, sb.toString(),excelPath);
            String s = FastDFSClientUtil.uploadFile(bytes, sb.toString(), null);
            fileTask.setFileUrl(s);
        } catch (IOException e) {
            log.error("上传文件失败{}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    protected List<Pair<Integer, List<?>>> getData(FileTask fileTask) {
        BaseIdDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<BaseIdDTO>() {
        });
        ReplenishmentSuggestionDTO.ExportResultDTO resultDTO = exportMrpFeign.exportSuggestCalcData(dto);

        List<Pair<Integer, List<?>>> pairList = new ArrayList<>();
        //历史销量去噪销量导出
        pairList.add(new Pair<>(MathUtil.ZERO, resultDTO.getSalesInfoDenoisingExportList()));
        //预估日销量导出
        pairList.add(new Pair<>(MathUtil.ONE, resultDTO.getSalesEstimateExportList()));
        //库存预测
        pairList.add(new Pair<>(MathUtil.TWO, resultDTO.getInventoryEstimateExportList()));
        //默认日销量导出
        pairList.add(new Pair<>(MathUtil.THREE, resultDTO.getDefaultSalesQtyExportList()));
        //动态日销量导出
        pairList.add(new Pair<>(MathUtil.FOUR, resultDTO.getDynamicSalesQtyExportList()));
        //固定日销量导出
        pairList.add(new Pair<>(MathUtil.FIVE, resultDTO.getFixedSalesQtyExportList()));
        //销量去噪导出
        pairList.add(new Pair<>(MathUtil.SIX, resultDTO.getSalesDenoisingExportList()));
        return pairList;
    }

    @Override
    protected String getExcelPath() {
        return "excel/mrp/suggestCalcData.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_MRP_SUGGESTION_CALC_DATA;
    }
}
