package com.erp.server.file.business.plm;

import com.alibaba.excel.write.handler.WriteHandler;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.MouldInfoDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.business.plm.hanlder.MouldInfoWriteHandler;
import com.erp.server.file.core.AbstractSingleSheetGroupPageFileEventHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_MOULD_INFO;

@Component
public class ExportPlmMouldInfoHandler extends AbstractSingleSheetGroupPageFileEventHandler<MouldInfoDTO.MouldInfoExportDTO, MouldInfoDTO.PagingParamDTO> {

    @Resource
    private ExportPlmFeign exportPlmFeign;

    @Override
    protected String getExcelPath() {
        return "excel/plm/mouldInfo.xlsx";
    }

    @Override
    protected PagingVO<MouldInfoDTO.MouldInfoExportDTO> getPageData(PagingDTO<MouldInfoDTO.PagingParamDTO> dto) {
        return exportPlmFeign.exportMouldInfo(dto);
    }

    /**
     * 模具台账导出以「模具明细 detailId」为单据维度。
     * <p>
     * 上游 {@code exportMouldInfo} 须按 detailId 连续返回，使同一明细的多行相邻；否则
     * {@link #failOnNonContinuousSheetGroup()} 会快速失败，避免置空错位产出错误展示。
     * detailId 为空的行不参与分组（返回 {@code null}），其字段始终完整保留。
     */
    @Override
    protected Object sheetGroupKey(MouldInfoDTO.MouldInfoExportDTO row) {
        if (row == null || ObjectUtils.isEmpty(row.getDetailId())) {
            return null;
        }
        return row.getDetailId();
    }

    /**
     * 组内会置空重复展示字段，分组非连续会导致展示错位，必须快速失败。
     */
    @Override
    protected boolean failOnNonContinuousSheetGroup() {
        return true;
    }

    /**
     * 基类已按 {@link #sheetGroupKey(MouldInfoDTO.MouldInfoExportDTO)} 合并跨页尾组并强制单 sheet；
     * 此处只处理单个完整明细分组内的重复行置空（仅首行保留）。
     */
    @Override
    protected void beforeWriteGroupRows(Object groupKey, List<MouldInfoDTO.MouldInfoExportDTO> groupRows) {
        if (groupKey == null || ObjectUtils.isEmpty(groupRows)) {
            return;
        }
        for (int i = 1; i < groupRows.size(); i++) {
            MouldInfoDTO.MouldInfoExportDTO exportDTO = groupRows.get(i);
            if (exportDTO == null) {
                continue;
            }
            exportDTO.setProjectNo("");
            exportDTO.setName("");
            exportDTO.setMouldNo("");
            exportDTO.setThirdMouldNo("");
            exportDTO.setStatusName("");
            exportDTO.setLifeCycle(null);
            exportDTO.setDevelopCycle(null);
            exportDTO.setEnableDate(null);
            exportDTO.setSupplierName("");
            exportDTO.setWarehouseName("");
            exportDTO.setWarehouseLocationName("");
            exportDTO.setAddress("");
            exportDTO.setRemark("");
            exportDTO.setCreateUserName("");
            exportDTO.setCreateTime(null);
            exportDTO.setUpdateUserName("");
            exportDTO.setUpdateTime(null);
        }
    }

    @Override
    public List<WriteHandler> getWriteHandler() {
        return Collections.singletonList(new MouldInfoWriteHandler());
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_MOULD_INFO;
    }
}
