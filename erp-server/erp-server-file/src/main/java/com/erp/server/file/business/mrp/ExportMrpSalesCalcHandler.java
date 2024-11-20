package com.erp.server.file.business.mrp;

import cn.hutool.core.lang.Pair;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.mrp.dto.CalcSalesInfoDimDTO;
import com.erp.model.mrp.dto.CfgRuleSalesDenoisingCalcDTO;
import com.erp.model.mrp.dto.CfgRuleSalesFormulaCalcDTO;
import com.erp.rpc.mrp.feign.ExportMrpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.erp.server.file.exception.BusinessException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_MRP_SALES_CALC;

@Component
@Slf4j
public class ExportMrpSalesCalcHandler extends AbstractPageFileEventHandler<Pair<Integer, List<?>>, CalcSalesInfoDimDTO.ExportSalesInfoDTO> {

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
        CalcSalesInfoDimDTO.ExportSalesInfoDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<CalcSalesInfoDimDTO.ExportSalesInfoDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/mrp/salesCalc.xlsx";
    }

    @Override
    public List<Pair<Integer, List<?>>> listSeqData(CalcSalesInfoDimDTO.ExportSalesInfoDTO exportSalesInfoDTO) {
        List<CalcSalesInfoDimDTO.SalesInfoDenoisingDTO> salesInfoDenoising = new ArrayList<>();
        List<CalcSalesInfoDimDTO.SalesInfoEstimateDTO> salesInfoEstimate = new ArrayList<>();
        List<CfgRuleSalesFormulaCalcDTO.ExportDTO> defaultSalesQtyExportList = new ArrayList<>();
        List<CfgRuleSalesFormulaCalcDTO.ExportDTO> dynamicSalesQtyExportList = new ArrayList<>();
        List<CfgRuleSalesFormulaCalcDTO.ExportDTO> fixedSalesQtyExportList = new ArrayList<>();
        List<CfgRuleSalesDenoisingCalcDTO.ExportDTO> cfgRuleSalesDenoising = new ArrayList<>();
        PagingDTO<CalcSalesInfoDimDTO.ExportSalesInfoDTO> dto = new PagingDTO<>();
        dto.setPageSize(getPageSize());
        dto.setCurrPage(1);
        boolean hasNext = true;
        while (hasNext) {
            dto.setParams(exportSalesInfoDTO);
            PagingVO<CalcSalesInfoDimDTO.ExportResultDTO> data = getListExportData(dto);
            if (!CollectionUtils.isEmpty(data.getList())) {
                List<CalcSalesInfoDimDTO.ExportResultDTO> list = (List<CalcSalesInfoDimDTO.ExportResultDTO>) data.getList();
                list.forEach(obj -> {
                    salesInfoDenoising.addAll(obj.getSalesInfoDenoising());
                    salesInfoEstimate.addAll(obj.getSalesInfoEstimate());
                    defaultSalesQtyExportList.addAll(obj.getDefaultSalesQtyExportList());
                    dynamicSalesQtyExportList.addAll(obj.getDynamicSalesQtyExportList());
                    fixedSalesQtyExportList.addAll(obj.getFixedSalesQtyExportList());
                    cfgRuleSalesDenoising.addAll(obj.getCfgRuleSalesDenoising());
                });
            }
            int totalCount = data.getTotalCount();
            if (totalCount <= dto.getCurrPage() * getPageSize()) {
                hasNext = false;
            }
            dto.setCurrPage(dto.getCurrPage() + 1);
        }
        List<Pair<Integer, List<?>>> pairList = new ArrayList<>();
        //历史销量去噪销量导出
        pairList.add(new Pair<>(MathUtil.ZERO, salesInfoDenoising));
        //预估日销量导出
        pairList.add(new Pair<>(MathUtil.ONE, salesInfoEstimate));
        //默认日销量导出
        pairList.add(new Pair<>(MathUtil.TWO, defaultSalesQtyExportList));
        //动态日销量导出
        pairList.add(new Pair<>(MathUtil.THREE, dynamicSalesQtyExportList));
        //固定日销量导出
        pairList.add(new Pair<>(MathUtil.FOUR, fixedSalesQtyExportList));
        //销量去噪导出
        pairList.add(new Pair<>(MathUtil.FIVE, cfgRuleSalesDenoising));
        return pairList;
    }

    private PagingVO<CalcSalesInfoDimDTO.ExportResultDTO> getListExportData(PagingDTO<CalcSalesInfoDimDTO.ExportSalesInfoDTO> dto) {
        return exportMrpFeign.getListExportData(dto);
    }

    @Override
    protected PagingVO<Pair<Integer, List<?>>> getPageData(PagingDTO<CalcSalesInfoDimDTO.ExportSalesInfoDTO> dto) {
        return null;
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_MRP_SALES_CALC;
    }
}
