package com.erp.server.file.business.mrp;

import cn.hutool.core.lang.Pair;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.mrp.dto.VirtualInventoryHistoryDTO;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_MRP_VIRTUAL_INVENTORY;

@Component
@Slf4j
public class ExportMrpVirtualInventoryHandler extends AbstractPageFileEventHandler<VirtualInventoryHistoryDTO.ListDTO, VirtualInventoryHistoryDTO.SearchParamDTO> {
    @Resource
    private ExportMrpFeign exportMrpFeign;

    @Override
    public void handle(FileTask fileTask) {
        List<VirtualInventoryHistoryDTO.ListDTO> list = getData(fileTask);
        fileTask.setCount(list.size());
        //明细数据
        List<VirtualInventoryHistoryDTO.ListDetailDTO> detailList = list.stream().flatMap(obj -> Stream.of(obj.getDetailList().toArray(new VirtualInventoryHistoryDTO.ListDetailDTO[0]))).collect(Collectors.toList());
        List<Pair<Integer, List<?>>> pairList = new ArrayList<>();
        pairList.add(new Pair<>(MathUtil.ZERO, list));
        pairList.add(new Pair<>(MathUtil.ONE, detailList));
        StringBuilder sb = new StringBuilder();
        String excelPath = getExcelPath();
        String name = fileTask.getFileName();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        sb.append(excelPath.substring(excelPath.lastIndexOf(".")));
        try {
            byte[] bytes = new ExcelPrintUtils().sheetPatchExport(pairList, sb.toString(),excelPath);
            String s = FastDFSClientUtil.uploadFile(bytes, sb.toString() + ".xlsx", null);
            fileTask.setFileUrl(s);
        } catch (IOException e) {
            log.error("上传文件失败{}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    protected List<VirtualInventoryHistoryDTO.ListDTO> getData(FileTask fileTask) {
        VirtualInventoryHistoryDTO.SearchParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<VirtualInventoryHistoryDTO.SearchParamDTO>() {
        });
        return listSeqData(dto);
    }


    @Override
    protected PagingVO<VirtualInventoryHistoryDTO.ListDTO>  getPageData(PagingDTO<VirtualInventoryHistoryDTO.SearchParamDTO> dto) {
        PagingVO<VirtualInventoryHistoryDTO.ListDTO> virtualInventory = exportMrpFeign.getVirtualInventory(dto);
        List<VirtualInventoryHistoryDTO.ListDTO> dataList = (List<VirtualInventoryHistoryDTO.ListDTO>) virtualInventory.getList();
        if (!CollectionUtils.isEmpty(dataList)) {
            dto.setLastId(dataList.get(dataList.size() - 1).getIndexId());
        }
        return virtualInventory;
    }


    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_MRP_VIRTUAL_INVENTORY;
    }

    @Override
    public String getExcelPath() {
        return "excel/mrp/virtualHisInventory.xlsx";
    }
}
