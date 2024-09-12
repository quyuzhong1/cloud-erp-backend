package com.erp.server.file.business.oms;

import cn.hutool.core.collection.CollUtil;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.business.oms.utils.SoUtils;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.erp.server.file.exception.BusinessException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_SO;

@Component
@Slf4j
public class ExportOmsSoHandler extends AbstractPageFileEventHandler<SoInfoDTO.PagingViewDTO, SoInfoDTO.ExportDTO> {

    @Resource
    private ExportOmsFeign exportOmsFeign;
    private static final ThreadLocal<SoInfoDTO.ExportDTO> threadLocal = new ThreadLocal<>();

    @Override
    protected List<SoInfoDTO.PagingViewDTO> getData(FileTask fileTask) {
        SoInfoDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SoInfoDTO.ExportDTO>() {
        });
        threadLocal.set(dto);
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<SoInfoDTO.PagingViewDTO> getPageData(PagingDTO<SoInfoDTO.ExportDTO> dto) {
        return exportOmsFeign.exportSo(dto);
    }

    @Override
    public void handle(FileTask fileTask) {
        List<SoInfoDTO.PagingViewDTO> list = getData(fileTask);
        fileTask.setCount(list.size());
        StringBuilder sb = new StringBuilder();
        String name = fileTask.getFileName();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            SoInfoDTO.ExportDTO dto = threadLocal.get();
            List<String> nopermitFields = dto.getNopermitFields();
            Map<String, String> headMap = SoUtils.getExportHeadList();
            List<LinkedHashMap<String, Object>> dataList = new ArrayList<>();
            if (CollUtil.isNotEmpty(list)) {
                for (SoInfoDTO.PagingViewDTO item : list) {
                    LinkedHashMap<String, Object> data = SoUtils.fillToMap(item, headMap, nopermitFields);
                    dataList.add(data);
                }
            }
            // 去除掉无权限字段
            if (CollUtil.isNotEmpty(nopermitFields)) {
                headMap.keySet().removeIf(nopermitFields::contains);
            }
            // 隐藏主单列
            SoUtils.hideForExport(dataList);
            // 去除id字段
            headMap.keySet().removeIf(r -> Objects.equals("id", r));
            dataList.forEach(data -> data.keySet().removeIf(r -> Objects.equals("id", r)));
            byte[] bytes = ExcelUtil.easyUtilStr(new ArrayList<>(headMap.values()), "销售订单", dataList, sb.toString());
            String s = FastDFSClientUtil.uploadFile(bytes, sb.toString() + ".xlsx", null);
            fileTask.setFileUrl(s);
        } catch (Exception e) {
            log.error("上传文件失败{}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }finally {
            threadLocal.remove();
        }

    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_OMS_SO;
    }

    @Override
    public String getExcelPath() {
        return null;
    }
}
