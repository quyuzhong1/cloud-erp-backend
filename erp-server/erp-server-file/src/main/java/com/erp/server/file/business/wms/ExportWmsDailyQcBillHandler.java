package com.erp.server.file.business.wms;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
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
        List<String> picFormats = Arrays.stream(PicFormatEnum.values())
                .map(PicFormatEnum::getCode)
                .collect(Collectors.toList());

        for (QcInfoDTO.QcDailyReportDTO data : qcDailyReportDTOS) {
            data.setProductImg(getFirstItem(data.getProductImgUrl()));
            data.setBoxMarkImg(getFirstItem(data.getBoxMarkImgUrl()));
            data.setBadAttachment(getBadAttachment(data.getBadAttachments(), picFormats));

            // 设置尺寸格式
            data.setProductSize(formatSize(data.getProductLength(), data.getProductWidth(), data.getProductHeight()));
            data.setBoxSize(formatSize(data.getBoxLength(), data.getBoxWidth(), data.getBoxHeight()));

            // 获取第一个带链接的报告
            data.setReportListLink(getReportListLink(data.getReportList()));
        }
        return qcDailyReportDTOS;
    }

    /**
     * 获取图片列表中第一个 URL
     *
     * @param urls url
     */
    private String getFirstItem(List<String> urls) {
        return CollUtil.isNotEmpty(urls) ? urls.get(0) : null;
    }

    /**
     * 图片格式过滤
     *
     * @param attachments 参数
     * @param picFormats  图片格式
     */
    private String getBadAttachment(List<WmsAttachmentDTO.UpdateDTO> attachments, List<String> picFormats) {
        if (CollUtil.isEmpty(attachments)) return null;
        // 查找图片格式附件
        WmsAttachmentDTO.UpdateDTO imageAttachment = attachments.stream()
                .filter(att -> att.getAttachUrl().contains(".") &&
                        picFormats.contains(att.getAttachUrl().substring(att.getAttachUrl().lastIndexOf(".") + 1)))
                .findFirst()
                .orElse(null);
        // 如果有图片，返回图片链接；否则返回第一个附件的名称和链接
        return imageAttachment != null ? imageAttachment.getAttachUrl()
                : attachments.get(0).getAttachName() + "," + attachments.get(0).getAttachUrl();
    }

    /**
     * 尺寸格式化
     *
     * @param length 长
     * @param width  宽
     * @param height 高
     */
    private String formatSize(Object length, Object width, Object height) {
        return CharSequenceUtil.format("{}*{}*{}",
                Objects.toString(length, "0"),
                Objects.toString(width, "0"),
                Objects.toString(height, "0"));
    }

    private String getReportListLink(List<QcReportDetailDTO.ViewDTO> reportList) {
        if (CollUtil.isEmpty(reportList)) return null;
        return reportList.stream()
                .filter(report -> CollUtil.isNotEmpty(report.getReportUrlList()))
                .findFirst()
                .map(report -> report.getReportNameList().get(0) + "," + report.getReportUrlList().get(0))
                .orElse(null);
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
