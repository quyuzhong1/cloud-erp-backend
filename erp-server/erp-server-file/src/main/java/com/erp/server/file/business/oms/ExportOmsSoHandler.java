package com.erp.server.file.business.oms;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.common.business.dto.DynamicExcelDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.file.entity.FileTask;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.business.oms.utils.SoUtils;
import com.erp.server.file.core.dynamic.AbstractMultiSheetGroupDynamicHeadersFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_SO;

/**
 * 销售订单导出：列由运行时表头算出（无固定模板），按主单 {@code id} 分组展示金额列，
 * 数据量超过单 sheet 时自动多 tab 分页（方案 C）。
 */
@Component
@Slf4j
public class ExportOmsSoHandler extends AbstractMultiSheetGroupDynamicHeadersFileEventHandler<SoInfoDTO.ExportDTO> {

    private static final String SHEET_NAME = "销售订单";
    /** 仅作主单去重的辅助列，不参与导出表头。 */
    private static final String ID_KEY = "id";

    @Resource
    private ExportOmsFeign exportOmsFeign;

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_OMS_SO;
    }

    /**
     * 首行单据名，还原旧版导出（首行合并居中「销售订单」）。
     */
    @Override
    protected String firstRowName() {
        return SHEET_NAME;
    }

    /**
     * sheet 标签名还原旧版「日期+文件名」（如 20260622销售订单20260622102525）；多 sheet 溢出时由基类追加序号。
     */
    @Override
    protected String resolveSheetBaseName(FileTask fileTask) {
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        return date + fileTask.getFileName();
    }

    @Override
    protected PagingVO<DynamicExcelDTO> getPageData(PagingDTO<SoInfoDTO.ExportDTO> dto) {
        // 导出按主单 id 分组判重，必须保证同一 id 连续。listExport 的 ORDER BY 默认以 sortList 为主排序，
        // 若透传用户列排序会打散 id，触发 failOnNonContinuousSheetGroup；故导出固定清空 sortList，
        // 回落到 si.create_time desc, sod.id asc（同主单明细天然相邻）。
        if (dto.getParams() != null) {
            dto.getParams().setSortList(null);
        }
        PagingVO<SoInfoDTO.PagingViewDTO> page = exportOmsFeign.exportSo(dto);
        // Feign 返回 null 视为上游查询失败/熔断，显式失败而非包装成空结果，避免被基类误报为「导出数据不能为空」。
        if (page == null) {
            throw new ServiceException("导出分页查询失败，查询为空：页码=" + dto.getCurrPage());
        }
        PagingVO<DynamicExcelDTO> result = new PagingVO<>();
        result.setTotalCount(page.getTotalCount());

        SoInfoDTO.ExportDTO params = dto.getParams();
        List<String> noPermitFields = params == null ? null : params.getNopermitFields();
        Map<String, String> headMap = SoUtils.getExportHeadList();

        List<LinkedHashMap<String, Object>> dataList = new ArrayList<>();
        if (CollUtil.isNotEmpty(page.getList())) {
            for (SoInfoDTO.PagingViewDTO item : page.getList()) {
                dataList.add(SoUtils.fillToMap(item, headMap, noPermitFields));
            }
        }

        LinkedHashMap<String, String> exportHeaders = new LinkedHashMap<>(headMap);
        if (CollUtil.isNotEmpty(noPermitFields)) {
            exportHeaders.keySet().removeIf(noPermitFields::contains);
        }
        exportHeaders.remove(ID_KEY);

        DynamicExcelDTO excelDTO = new DynamicExcelDTO();
        excelDTO.setHeaders(exportHeaders);
        excelDTO.setData(dataList);
        result.setList(Collections.singletonList(excelDTO));
        return result;
    }

    @Override
    protected HorizontalCellStyleStrategy dynamicHeaderCellStyleStrategy() {
        return ExcelUtil.getStyleStrategy();
    }

    @Override
    protected Object sheetGroupKey(LinkedHashMap<String, Object> row) {
        if (row == null || !row.containsKey(ID_KEY)) {
            return null;
        }
        Object id = row.get(ID_KEY);
        return id == null ? null : String.valueOf(id);
    }

    @Override
    protected boolean failOnNonContinuousSheetGroup() {
        return true;
    }

    /**
     * 组内首行保留主单金额列，后续明细行置空（与旧版 hideRepeatedMainRow 语义一致）。
     */
    @Override
    protected void beforeWriteGroupRows(Object groupKey, List<LinkedHashMap<String, Object>> groupRows) {
        Set<String> seenMainIds = new HashSet<>();
        for (LinkedHashMap<String, Object> row : groupRows) {
            SoUtils.hideRepeatedMainRow(row, seenMainIds);
        }
    }
}
