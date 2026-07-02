package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.constant.ThirdWarehouseConstants;
import com.common.business.enums.FileTypeEnum;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.utils.PdfUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
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

    void convertPdfAttachmentToPng(ThirdWarehouseUploadFileReq uploadFileReq) {
        if (!needConvertPdfAttachment(uploadFileReq)) {
            return;
        }
        String fileData = uploadFileReq.getFileData();
        if (CharSequenceUtil.isBlank(fileData)) {
            throw new ServiceException("安兔上传文件内容不能为空");
        }
        if (fileData.length() > ThirdWarehouseConstants.MAX_INVOICE_PDF_BASE64_LENGTH) {
            throw new ServiceException("发票PDF文件过大，无法为安兔生成PNG");
        }
        try {
            uploadFileReq.setFileData(PdfUtil.pdfBase64FirstPageToPngBase64(fileData));
        } catch (Exception e) {
            log.error("安兔发票PDF转PNG失败，使用原始PDF格式上传, authId:{}, module:{}",
                    uploadFileReq.getAuthId(), uploadFileReq.getModule(), e);
            return;
        }
        uploadFileReq.setFileType(FileTypeEnum.PNG.getCode());
        uploadFileReq.setModule(ThirdWarehouseConstants.MODULE_OTHER_DOCUMENTS_INVOICE);
    }

    boolean needConvertPdfAttachment(ThirdWarehouseUploadFileReq uploadFileReq) {
        if (uploadFileReq == null || CharSequenceUtil.isBlank(uploadFileReq.getModule()) || ThirdWarehouseConstants.MODULE_ORDER_LABEL.equalsIgnoreCase(uploadFileReq.getModule())) {
            return false;
        }
        return isPdfFile(uploadFileReq);
    }

    private static boolean isPdfFile(ThirdWarehouseUploadFileReq uploadFileReq) {
        return FileTypeEnum.PDF.getCode().equalsIgnoreCase(uploadFileReq.getFileType())
                || CharSequenceUtil.startWithIgnoreCase(uploadFileReq.getFileData(), "data:application/pdf;base64,");
    }
}
