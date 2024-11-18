package com.erp.server.file.business.mrp;

import cn.hutool.core.lang.Pair;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.mrp.dto.*;
import com.erp.rpc.mrp.feign.ExportMrpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.erp.server.file.exception.BusinessException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class ExportMrpReplenishmentRuleHandler extends  AbstractPageFileEventHandler<Pair<Integer, List<?>>, ReplenishmentSuggestionDTO.PagingParamDTO>{

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
        ReplenishmentSuggestionDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<ReplenishmentSuggestionDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<Pair<Integer, List<?>>> getPageData(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto) {
        return null;
    }

    @Override
    @SuppressWarnings("all")
    public List<Pair<Integer, List<?>>> listSeqData(ReplenishmentSuggestionDTO.PagingParamDTO searchParamDTO) {
        List<CfgRuleStockUpDTO.StockUpExportDTO> stockUpExportList = new ArrayList<>();
        List<CfgRuleStockingRatioDTO.StockingRatioExportDTO> stockingRatioExportList = new ArrayList<>();
        List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> defaultSalesQtyExportList = new ArrayList<>();
        List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> dynamicSalesQtyExportList = new ArrayList<>();
        List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> fixedSalesQtyExportList = new ArrayList<>();
        List<CfgRuleSalesDenoisingDTO.SalesDenoisingExportDTO> salesDenoisingExportList = new ArrayList<>();
        PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto = new PagingDTO<>();
        dto.setPageSize(getPageSize());
        dto.setCurrPage(1);
        boolean hasNext = true;
        while (hasNext) {
            dto.setParams(searchParamDTO);
            PagingVO<ReplenishmentSuggestionDTO.ReplenishmentRuleExportDTO> data = getListExportData(dto);
            if (CollectionUtils.isNotEmpty(data.getList())) {
                List<ReplenishmentSuggestionDTO.ReplenishmentRuleExportDTO> list = (List<ReplenishmentSuggestionDTO.ReplenishmentRuleExportDTO>) data.getList();
                list.stream().forEach(obj -> {
                    stockUpExportList.addAll(obj.getStockUpExportList());
                    stockingRatioExportList.addAll(obj.getStockingRatioExportList());
                    defaultSalesQtyExportList.addAll(obj.getDefaultSalesQtyExportList());
                    dynamicSalesQtyExportList.addAll(obj.getDynamicSalesQtyExportList());
                    fixedSalesQtyExportList.addAll(obj.getFixedSalesQtyExportList());
                    salesDenoisingExportList.addAll(obj.getSalesDenoisingExportList());
                });
            }
            int totalCount = data.getTotalCount();
            if (totalCount <= dto.getCurrPage() * getPageSize()) {
                hasNext = false;
            }
            dto.setCurrPage(dto.getCurrPage() + 1);
        }
        List<Pair<Integer, List<?>>> pairList = new ArrayList<>();
        //备货导出
        pairList.add(new Pair<>(MathUtil.ZERO, stockUpExportList));
        //动态备货系数导出
        pairList.add(new Pair<>(MathUtil.ONE, stockingRatioExportList));
        //默认日销量导出
        pairList.add(new Pair<>(MathUtil.TWO, defaultSalesQtyExportList));
        //动态日销量导出
        pairList.add(new Pair<>(MathUtil.THREE, dynamicSalesQtyExportList));
        //固定日销量导出
        pairList.add(new Pair<>(MathUtil.FOUR, fixedSalesQtyExportList));
        //销量去噪导出
        pairList.add(new Pair<>(MathUtil.FIVE, salesDenoisingExportList));
        return pairList;
    }

    /**
     * 数据查询
     * @author will
     * @date 2024/9/9 15:20
     * @param dto
     * @return PagingVO<ReplenishmentRuleExportDTO>
     */
    private PagingVO<ReplenishmentSuggestionDTO.ReplenishmentRuleExportDTO> getListExportData(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto) {
        return exportMrpFeign.listReplenishmentRule(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return FileTaskEventEnum.EXPORT_MRP_REPLENISHMENT_RULE;
    }

    @Override
    public String getExcelPath() {
           return "excel/mrp/replenishmentRule.xlsx";
    }
}
