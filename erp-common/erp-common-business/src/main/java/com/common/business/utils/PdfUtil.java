package com.common.business.utils;

import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import lombok.Cleanup;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
public class PdfUtil {


    /**
     * pdf 合并操作2
     * 直接将各个pdf对应的字节码，进行合并成新的pdf文件数据
     * @param base64Pdfs  多个pdf的base64编码文件
     * @return
     */
    public static String getNewMergePdfBase64(List<String> base64Pdfs) throws Exception {
        String newPdfName = null;
        String prefix = "data:application/pdf;base64,";
        try{
            List<byte[]> byteLists = base64ToByte(base64Pdfs);
            // 将pdf的byte[] 数据生成新的pdf文件
            if(CollectionUtils.isEmpty(byteLists)){
                return null;
            }
            // 生成新的pdf文件
            newPdfName = "report_"+UUID.randomUUID().toString()+".pdf";
            mergePdfFiles2(byteLists,newPdfName);
            @Cleanup FileInputStream fileInputStream = new FileInputStream(newPdfName);
            return prefix.concat(base64ForPdf(fileInputStream));
        }finally {
            // 如果临时文件存在，则删除临时文件
            if(StringUtils.isNotBlank(newPdfName)){
                File file = new File(newPdfName);
                if (file != null && file.isFile() && file.exists()) {
                    file.delete();
                }
            }
        }
    }


    /**
     * pdf 合并操作2
     * 直接将各个pdf对应的字节码，进行合并成新的pdf文件数据
     * @param base64Pdfs  多个pdf的base64编码文件
     * @return
     */
    public static FileInputStream getNewMergePdfStream(List<String> base64Pdfs) throws Exception {
        String newPdfName = null;
        try{
            List<byte[]> byteLists = base64ToByte(base64Pdfs);
            // 将pdf的byte[] 数据生成新的pdf文件
            if(CollectionUtils.isEmpty(byteLists)){
                return null;
            }
            // 生成新的pdf文件
            newPdfName = "report_"+UUID.randomUUID().toString()+".pdf";
            mergePdfFiles2(byteLists,newPdfName);
            @Cleanup FileInputStream fileInputStream = new FileInputStream(newPdfName);
            return fileInputStream;
        }finally {
            // 如果临时文件存在，则删除临时文件
            if(StringUtils.isNotBlank(newPdfName)){
                File file = new File(newPdfName);
                if (file != null && file.isFile() && file.exists()) {
                    file.delete();
                }
            }
        }
    }

    public static void mergePdfFiles2(List<byte[]> bytes, String newFile) {
        try {
            // 以第一个pdf作为基础，后面的每页信息逐渐累加
            Document document = new Document(new PdfReader(bytes.get(0)).getPageSize(1));
            PdfCopy copy = new PdfCopy(document, new FileOutputStream(newFile));
            document.open();
            for (byte[] aByte : bytes) {
                PdfReader reader = new PdfReader(aByte);
                int n = reader.getNumberOfPages();
                for (int j = 1; j <= n; j++) {
                    document.newPage();
                    PdfImportedPage page = copy.getImportedPage(reader, j);
                    copy.addPage(page);
                }
            }
            document.close();
        } catch (IOException | DocumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * base62 文件，转byte[]
     * @param base64Lists  多个pdf的base64 编码
     * @return 多个pdf文件的字节码数组集合
     */
    private static List<byte[]> base64ToByte(List<String> base64Lists) throws Exception {
        if(CollectionUtils.isEmpty(base64Lists)){
            return null;
        }
        List<byte[]> returnStrLists = new ArrayList<>();
        BASE64Decoder decoder = new BASE64Decoder();
        for (String base64Str : base64Lists) {
            byte[] fileBytes = decoder.decodeBuffer(base64Str);
            returnStrLists.add(fileBytes);
        }
        return returnStrLists;
    }

    /**
     *将文件输入流，转换为 base64 返回给请求端
     **/
    public static String base64ForPdf(InputStream fin) throws Exception {
        BASE64Encoder encoder = new BASE64Encoder();

        BufferedInputStream bin = new BufferedInputStream(fin);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedOutputStream bout = new BufferedOutputStream(baos);
        byte[] buffer = new byte[1024];

        for (int len = bin.read(buffer); len != -1; len = bin.read(buffer)) {
            bout.write(buffer, 0, len);
        }

        bout.flush();
        byte[] bytes = baos.toByteArray();
        String var11 = encoder.encodeBuffer(bytes).trim();
        return var11;
    }

    /**
     * pdf提取文本，根据行分割成数组
     * @param inputStream
     * @return
     * @throws Exception
     */
    public static String[] extractTextFromPDF(InputStream inputStream) throws Exception{
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            return text.split("\n");
        }
    }

    /**
     * 导出base64
     * @author will
     * @date 2024/11/4 16:25
     * @param response
     * @param base64List
     */
    public static void exportBase64ForPdf(HttpServletResponse response,List<String>base64List ){
        try {
            String newMergePdfBase64 = PdfUtil.getNewMergePdfBase64(base64List);

            // 设置响应头，告诉浏览器返回的是一个 PDF 文件
            response.setContentType("application/pdf");
            // 设置 PDF 的显示方式和文件名
            response.setHeader("Content-Disposition", "inline; filename=\"filename.pdf\"");
            BASE64Decoder decoder = new BASE64Decoder();
            try (OutputStream out = response.getOutputStream()) {
                // 将 Base64 编码的字符串解码为字节数组
                byte[] pdfBytes = decoder.decodeBuffer(newMergePdfBase64);
                // 将字节数组写入到响应输出流中
                out.write(pdfBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(ApiError.ERROR_PDF_MERGE);
        }
    }

}
