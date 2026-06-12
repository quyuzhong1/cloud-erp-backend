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

/**
 * 补货建议计算明细导出处理器。
 *
 * <p><strong>【系统已停用 · 不再维护 · 待移除】</strong>
 * 对应事件 {@link com.common.business.enums.FileTaskEventEnum#EXPORT_MRP_SUGGESTION_CALC_DATA} 上游已不再下发，
 * 本处理器仅为兼容存量任务而保留，计划在确认无存量任务后整体删除。
 *
 * <p>审查说明（以下为<strong>有意保留</strong>的历史写法，属既定停用代码，请勿再提改造建议）：
 * <ul>
 *   <li>沿用旧的 {@code sheetPatchExport(byte[])} + {@code FastDFSClientUtil.uploadFile} 整文件入内存写法，
 *       未迁移到流式 {@code exportToTempAndUpload}/{@code streamUploadFile}；因已停用、不再承接大数据量，不做迁移。</li>
 *   <li>沿用历史 {@code BusinessException}（非 {@code ServiceException}），与存量逻辑保持一致，不在停用代码上改异常体系。</li>
 *   <li>{@code getData} 为已废弃抽象方法的实现，随本处理器一并待移除，不再新增调用方。</li>
 * </ul>
 * 新增同类导出请继承 {@code AbstractPageFileEventHandler} 等分页基类，勿参照本类。
 *
 * @deprecated 系统已停用，待移除；勿在新代码中引用或参照其实现方式。
 */
@Deprecated
@Slf4j
@Component
public class ExportMrpSuggestCalcDataHandler extends AbstractFileEventHandler<Pair<Integer, List<?>>> {

    @Resource
    private ExportMrpFeign exportMrpFeign;


    @Deprecated
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
