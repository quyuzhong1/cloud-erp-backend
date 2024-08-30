package com.erp.server.file.business.wms;

import cn.hutool.core.lang.Pair;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.model.wms.dto.VirtualInventoryDiffDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
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
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_VIRTUAL_INVENTORY_DIFF;


@Component
@Slf4j
public class ExportWmsVirtualInventoryDiffHandler extends AbstractPageFileEventHandler<Pair<Integer, List<?>>, VirtualInventoryDiffDTO.SearchParamDTO> {
    @Resource
    private ExportWmsFeign exportWmsFeign;

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
        VirtualInventoryDiffDTO.SearchParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<VirtualInventoryDiffDTO.SearchParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    @SuppressWarnings("all")
    public List<Pair<Integer, List<?>>> listSeqData(VirtualInventoryDiffDTO.SearchParamDTO searchParamDTO) {
        List<VirtualInventoryDiffDTO.ListDiffExportDataDTO> list = new ArrayList<>();
        PagingDTO<VirtualInventoryDiffDTO.SearchParamDTO> dto = new PagingDTO<>();
        dto.setPageSize(getPageSize());
        dto.setCurrPage(1);
        boolean hasNext = true;
        while (hasNext) {
            dto.setParams(searchParamDTO);
            PagingVO<VirtualInventoryDiffDTO.ListDiffExportDataDTO> data = getListDiffExportData(dto);
            if (!CollectionUtils.isEmpty(data.getList())) {
                list.addAll((Collection<? extends VirtualInventoryDiffDTO.ListDiffExportDataDTO>) data.getList());
            }
            int totalCount = data.getTotalCount();
            if (totalCount <= dto.getCurrPage() * getPageSize()) {
                hasNext = false;
            }
            dto.setCurrPage(dto.getCurrPage() + 1);
        }
        List<VirtualInventoryDTO.WarehouseStatisticsExcelDTO> warehouseStatisticsList = new ArrayList<>();
        PagingDTO<VirtualInventoryDiffDTO.SearchParamDTO> detailDto = new PagingDTO<>();
        detailDto.setPageSize(getPageSize());
        detailDto.setCurrPage(1);
        boolean detailHasNext = true;
        while (detailHasNext) {
            detailDto.setParams(searchParamDTO);
            PagingVO<VirtualInventoryDTO.WarehouseStatisticsExcelDTO> data = getWarehouseStatisticsData(detailDto);
            if (!CollectionUtils.isEmpty(data.getList())) {
                warehouseStatisticsList.addAll((Collection<? extends VirtualInventoryDTO.WarehouseStatisticsExcelDTO>) data.getList());
            }
            int totalCount = data.getTotalCount();
            if (totalCount <= detailDto.getCurrPage() * getPageSize()) {
                detailHasNext = false;
            }
            detailDto.setCurrPage(detailDto.getCurrPage() + 1);
        }
        List<Pair<Integer, List<?>>> pairList = new ArrayList<>();
        //主表数据
        pairList.add(new Pair<>(MathUtil.ZERO, list));
        //明细数据
        pairList.add(new Pair<>(MathUtil.ONE, warehouseStatisticsList));
        return pairList;
    }

    private PagingVO<VirtualInventoryDTO.WarehouseStatisticsExcelDTO> getWarehouseStatisticsData(PagingDTO<VirtualInventoryDiffDTO.SearchParamDTO> dto) {
        return exportWmsFeign.exportWarehouseStatisticsData(dto);
    }

    private PagingVO<VirtualInventoryDiffDTO.ListDiffExportDataDTO> getListDiffExportData(PagingDTO<VirtualInventoryDiffDTO.SearchParamDTO> dto) {
        return exportWmsFeign.exportListDiffExportData(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_VIRTUAL_INVENTORY_DIFF;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/virtualInventoryDiff.xlsx";
    }

    @Override
    protected PagingVO<Pair<Integer, List<?>>> getPageData(PagingDTO<VirtualInventoryDiffDTO.SearchParamDTO> dto) {
        return null;
    }
}
