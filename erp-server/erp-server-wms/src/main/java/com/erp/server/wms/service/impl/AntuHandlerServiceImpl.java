package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.FileTypeEnum;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.utils.PdfUtil;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.third.ThirdWarehouseConstants;
import com.erp.model.wms.dto.third.ThirdWarehouseUploadFileReq;
import com.erp.model.wms.dto.third.ThirdWarehouseUploadFileResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:51
 *

 */
@Slf4j
@Service
@Validated
public class AntuHandlerServiceImpl extends EccangHandlerServiceImpl {

    @Override
    public OmsPlatformEnum getPlatForm() {
        return OmsPlatformEnum.OMS_ANTU;
    }

    @Override
    public ApiResult<ThirdWarehouseUploadFileResponse> uploadFile(@Valid ThirdWarehouseUploadFileReq uploadFileReq) {
        convertPdfAttachmentToPng(uploadFileReq);
        return super.uploadFile(uploadFileReq);
    }

    public static void convertPdfAttachmentToPng(ThirdWarehouseUploadFileReq uploadFileReq) {
        if (!needConvertPdfAttachment(uploadFileReq)) {
            return;
        }
        uploadFileReq.setFileData(PdfUtil.pdfBase64FirstPageToPngBase64(uploadFileReq.getFileData()));
        uploadFileReq.setFileType(FileTypeEnum.PNG.getCode());
        uploadFileReq.setModule(ThirdWarehouseConstants.MODULE_OTHER_DOCUMENTS_INVOICE);
    }

    public static boolean needConvertPdfAttachment(ThirdWarehouseUploadFileReq uploadFileReq) {
        if (uploadFileReq == null || CharSequenceUtil.isBlank(uploadFileReq.getModule()) || ThirdWarehouseConstants.MODULE_ORDER_LABEL.equalsIgnoreCase(uploadFileReq.getModule())) {
            return false;
        }
        return isPdfFile(uploadFileReq);
    }

    private static boolean isPdfFile(ThirdWarehouseUploadFileReq uploadFileReq) {
        return FileTypeEnum.PDF.getCode().equalsIgnoreCase(uploadFileReq.getFileType())
                || StrUtil.startWithIgnoreCase(uploadFileReq.getFileData(), "data:application/pdf;base64,");
    }
}
