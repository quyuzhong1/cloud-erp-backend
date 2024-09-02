package com.erp.server.file.business.wms;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.write.handler.WriteHandler;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.enums.PicFormatEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.QcInfoDTO;
import com.erp.model.wms.dto.QcReportDetailDTO;
import com.erp.model.wms.dto.WmsAttachmentDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.business.wms.hanldler.HyperlinkWriteHandler;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_DAILY_QC_BILL;

@Component
@Slf4j
public class ExportWmsDailyQcBillHandler extends AbstractPageFileEventHandler<QcInfoDTO.QcDailyReportDTO, QcInfoDTO.ExportDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected List<QcInfoDTO.QcDailyReportDTO> getData(FileTask fileTask) {
        QcInfoDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<QcInfoDTO.ExportDTO>() {
        });
        List<QcInfoDTO.QcDailyReportDTO> qcDailyReportDTOS = listSeqData(dto);
        for (QcInfoDTO.QcDailyReportDTO data : qcDailyReportDTOS) {
            if (CollUtil.isNotEmpty(data.getProductImgUrl())) {
                data.setProductImg(data.getProductImgUrl().get(0));
            }
            if (CollUtil.isNotEmpty(data.getBoxMarkImgUrl())) {
                data.setBoxMarkImg(data.getBoxMarkImgUrl().get(0));
            }
            if (CollUtil.isNotEmpty(data.getBadAttachments())) {
                // 判断是否有图片，有图片则显示图片，没有图片则添加超链接
                List<String> picFormats = Arrays.asList(PicFormatEnum.values()).stream().map(PicFormatEnum::getCode).collect(Collectors.toList());
                WmsAttachmentDTO.UpdateDTO badAttachment = data.getBadAttachments().stream().filter(r ->
                        r.getAttachUrl().contains(".") && picFormats.contains(r.getAttachUrl().substring(r.getAttachUrl().lastIndexOf(".") + 1))
                ).findFirst().orElse(null);
                if (Objects.nonNull(badAttachment)) {
                    data.setBadAttachment(badAttachment.getAttachUrl());
                }else {
                    badAttachment = data.getBadAttachments().get(0);
                    data.setBadAttachment(badAttachment.getAttachName() + "," + badAttachment.getAttachUrl());
                }
            }
            data.setProductSize(StrUtil.format("{}*{}*{}", Objects.isNull(data.getProductLength()) ? "0" : data.getProductLength(),
                    Objects.isNull(data.getProductWidth()) ? "0" : data.getProductWidth(),
                    Objects.isNull(data.getProductHeight()) ? "0" : data.getProductHeight()));
            data.setBoxSize(StrUtil.format("{}*{}*{}", Objects.isNull(data.getBoxLength()) ? "0" : data.getBoxLength(),
                    Objects.isNull(data.getBoxWidth()) ? "0" : data.getBoxWidth(),
                    Objects.isNull(data.getBoxHeight()) ? "0" : data.getBoxHeight()));
            if(CollUtil.isNotEmpty(data.getReportList())) {
                // 写入超链接（只能写一个）
                QcReportDetailDTO.ViewDTO reportAttachment = data.getReportList().stream().filter(r->CollUtil.isNotEmpty(r.getReportUrlList())).findFirst().orElse(null);
                if(Objects.nonNull(reportAttachment)) {
                    data.setReportListLink(reportAttachment.getReportNameList().get(0) + "," + reportAttachment.getReportUrlList().get(0));
                }
            }
        }
        return qcDailyReportDTOS;
    }

    @Override
    public List<WriteHandler> getWriteHandler() {
        return Collections.singletonList(new HyperlinkWriteHandler());
    }

    @Override
    protected PagingVO<QcInfoDTO.QcDailyReportDTO> getPageData(PagingDTO<QcInfoDTO.ExportDTO> dto) {
        return exportWmsFeign.exportDailyQcBill(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_DAILY_QC_BILL;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/qcBill.xlsx";
    }
}
