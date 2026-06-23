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
import com.erp.server.file.core.dynamic.AbstractSingleSheetDynamicHeadersFileEventHandler;
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
 * 销售订单导出：列由运行时表头算出（无固定模板），故走动态表头流式分页路径，按页写盘避免全量内存建表的 OOM 风险。
 * 主单金额列去重（{@link SoUtils#hideRepeatedMainRow}）按 sheet 判重（见基类 {@code decorateSheetRow} 钩子说明），
 * 故继承单 sheet 变体 {@link AbstractSingleSheetDynamicHeadersFileEventHandler} 强制单数据 sheet：避免数据换 sheet
 * 导致同组判重在 sheet 边界重复展示、以及产生不可预估的 sheet 数量。
 */
@Component
@Slf4j
public class ExportOmsSoHandler extends AbstractSingleSheetDynamicHeadersFileEventHandler<SoInfoDTO.ExportDTO> {

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
        PagingVO<SoInfoDTO.PagingViewDTO> page = exportOmsFeign.exportSo(dto);
        // 与基类分页出口守卫对齐：Feign 返回 null 视为上游查询失败/熔断，显式失败而非包装成空结果，
        // 避免被基类后续误报为「导出数据不能为空」掩盖真实根因。
        if (page == null) {
            throw new ServiceException("导出分页查询失败，查询为空：页码=" + dto.getCurrPage());
        }
        PagingVO<DynamicExcelDTO> result = new PagingVO<>();
        result.setTotalCount(page.getTotalCount());

        SoInfoDTO.ExportDTO params = dto.getParams();
        List<String> noPermitFields = params == null ? null : params.getNopermitFields();
        Map<String, String> headMap = SoUtils.getExportHeadList();

        // 数据行：保留 id 作为按 sheet 去重的辅助列（id 不在导出表头中，不会写出到 Excel）
        List<LinkedHashMap<String, Object>> dataList = new ArrayList<>();
        if (CollUtil.isNotEmpty(page.getList())) {
            for (SoInfoDTO.PagingViewDTO item : page.getList()) {
                dataList.add(SoUtils.fillToMap(item, headMap, noPermitFields));
            }
        }

        // 导出表头：去除无权限列，并去掉 id 辅助列（与旧实现一致）
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

    /**
     * 还原旧版销售订单导出样式：表头灰底、加粗、13 号、居中，正文居中。直接复用公共
     * {@link ExcelUtil#getStyleStrategy()}，与其它模板导出保持同一套样式，避免重复定义。
     */
    @Override
    protected HorizontalCellStyleStrategy dynamicHeaderCellStyleStrategy() {
        return ExcelUtil.getStyleStrategy();
    }

    @Override
    protected Object newSheetState() {
        return new HashSet<String>();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void decorateSheetRow(LinkedHashMap<String, Object> rowMap, Object sheetState, int sheetNo) {
        SoUtils.hideRepeatedMainRow(rowMap, (Set<String>) sheetState);
    }
}
