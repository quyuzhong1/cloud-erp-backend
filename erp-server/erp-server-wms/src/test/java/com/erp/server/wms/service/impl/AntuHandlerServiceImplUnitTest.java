package com.erp.server.wms.service.impl;

import com.common.business.enums.FileTypeEnum;
import com.common.business.utils.PdfUtil;
import com.erp.model.wms.dto.third.ThirdWarehouseUploadFileReq;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;
import org.junit.Assert;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class AntuHandlerServiceImplUnitTest {

    @Test
    public void convertPdfAttachmentToPngUseOtherDocumentsInvoice() throws Exception {
        ThirdWarehouseUploadFileReq req = new ThirdWarehouseUploadFileReq();
        req.setFileType(FileTypeEnum.PDF.getCode());
        req.setModule("order_attach");
        req.setFileData("data:application/pdf;base64," + buildPdfBase64(PageSize.A4.getWidth(), PageSize.A4.getHeight()));

        AntuHandlerServiceImpl.convertPdfAttachmentToPng(req);

        Assert.assertEquals(FileTypeEnum.PNG.getCode(), req.getFileType());
        Assert.assertEquals("other_documents_invoice", req.getModule());
        byte[] pngBytes = Base64.getDecoder().decode(req.getFileData());
        Assert.assertNotNull(ImageIO.read(new ByteArrayInputStream(pngBytes)));
    }

    @Test
    public void convertPdfAttachmentToPngSkipOrderLabel() {
        String fileData = "data:application/pdf;base64,test";
        ThirdWarehouseUploadFileReq req = new ThirdWarehouseUploadFileReq();
        req.setModule("order_label");
        req.setFileType(FileTypeEnum.PDF.getCode());
        req.setFileData(fileData);

        AntuHandlerServiceImpl.convertPdfAttachmentToPng(req);

        Assert.assertEquals(FileTypeEnum.PDF.getCode(), req.getFileType());
        Assert.assertEquals("order_label", req.getModule());
        Assert.assertEquals(fileData, req.getFileData());
    }

    @Test
    public void convertPdfAttachmentToPngSkipDefaultLabelUpload() {
        String fileData = "data:application/pdf;base64,test";
        ThirdWarehouseUploadFileReq req = new ThirdWarehouseUploadFileReq();
        req.setFileType(FileTypeEnum.PDF.getCode());
        req.setFileData(fileData);

        AntuHandlerServiceImpl.convertPdfAttachmentToPng(req);

        Assert.assertEquals(FileTypeEnum.PDF.getCode(), req.getFileType());
        Assert.assertNull(req.getModule());
        Assert.assertEquals(fileData, req.getFileData());
    }

    @Test
    public void convertPdfAttachmentToPngSkipPngAttachment() {
        String fileData = Base64.getEncoder().encodeToString("png".getBytes());
        ThirdWarehouseUploadFileReq req = new ThirdWarehouseUploadFileReq();
        req.setModule("other_documents_invoice");
        req.setFileType(FileTypeEnum.PNG.getCode());
        req.setFileData(fileData);

        AntuHandlerServiceImpl.convertPdfAttachmentToPng(req);

        Assert.assertEquals(FileTypeEnum.PNG.getCode(), req.getFileType());
        Assert.assertEquals("other_documents_invoice", req.getModule());
        Assert.assertEquals(fileData, req.getFileData());
    }

    @Test
    public void getPdfFirstPageSizeMmReturnsApprox150By100() {
        float widthMm = 150F;
        float heightMm = 100F;
        String pdfBase64 = buildPdfBase64(mmToPoints(widthMm), mmToPoints(heightMm));

        float[] size = PdfUtil.getPdfFirstPageSizeMm(pdfBase64);

        Assert.assertEquals(widthMm, size[0], 0.5F);
        Assert.assertEquals(heightMm, size[1], 0.5F);
    }

    private static String buildPdfBase64(float widthPoints, float heightPoints) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(new com.lowagie.text.Rectangle(widthPoints, heightPoints));
            PdfWriter.getInstance(document, outputStream);
            document.open();
            document.add(new com.lowagie.text.Paragraph("test"));
            document.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static float mmToPoints(float mm) {
        return mm * 72F / 25.4F;
    }
}
