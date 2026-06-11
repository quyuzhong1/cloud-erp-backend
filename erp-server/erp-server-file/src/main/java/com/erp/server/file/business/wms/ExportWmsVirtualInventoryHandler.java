package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.multisheet.AbstractMasterDerivedSheetHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_VIRTUAL_INVENTORY;

@Component
public class ExportWmsVirtualInventoryHandler extends AbstractMasterDerivedSheetHandler<VirtualInventoryDTO.SearchParamDTO, VirtualInventoryDTO.ListDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected String getExcelPath(VirtualInventoryDTO.SearchParamDTO params) {
        return "excel/wms/virtualInventory.xlsx";
    }

    @Override
    protected PagingVO<VirtualInventoryDTO.ListDTO> fetchMasterPage(PagingDTO<VirtualInventoryDTO.SearchParamDTO> dto) {
        PagingVO<VirtualInventoryDTO.ListDTO> paging = exportWmsFeign.getVirtualInventory(dto);
        // 显式判空，与其他 Handler 在分页查询处的错误语义保持一致（不依赖基类兜底）
        if (paging == null) {
            throw new ServiceException("虚拟库存导出分页查询失败");
        }
        List<VirtualInventoryDTO.ListDTO> list = (List<VirtualInventoryDTO.ListDTO>) paging.getList();
        if (!CollectionUtils.isEmpty(list)) {
            dto.setLastId(list.get(list.size() - 1).getIndexId());
        }
        return paging;
    }

    @Override
    protected List<java.util.function.Function<List<VirtualInventoryDTO.ListDTO>, List<?>>> buildSheetExtractors() {
        List<java.util.function.Function<List<VirtualInventoryDTO.ListDTO>, List<?>>> extractors = new ArrayList<>(2);
        // 主表透传：返回行数须与主分页切片一致（见 MultiSheetTemplateWriter#fillMasterDerivedBatch），不可改为返回空列表
        extractors.add(mainSlice -> new ArrayList<>(mainSlice));
        extractors.add(mainRows -> {
            List<VirtualInventoryDTO.ListDetailDTO> detailRows = new ArrayList<>();
            for (VirtualInventoryDTO.ListDTO row : mainRows) {
                if (row != null && !CollectionUtils.isEmpty(row.getDetailList())) {
                    detailRows.addAll(row.getDetailList());
                }
            }
            return detailRows;
        });
        return extractors;
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_VIRTUAL_INVENTORY;
    }
}
