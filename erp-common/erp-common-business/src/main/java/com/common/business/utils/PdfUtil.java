package com.common.business.utils;

import cn.hutool.core.net.URLDecoder;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
public class PdfUtil {

    private PdfUtil() {
    }

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
                if (file.isFile() && file.exists()) {
                    if(!file.delete()){
                        log.info("PDFUtil:删除失败 --------------");
                    }
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
                    boolean deleteResult = file.delete();
                    if (!deleteResult){
                        log.warn("file.delete 删除失败");
                    }
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

    public static void main(String[] args) {
        String pdfUrl = "https://p16-printer-pdf-sign-sg.fanczs.com/tos-alisg-i-js2nuampgw-sg/3a0c23f5c0a7493780e942573d4e21f1?rk3s=8c7bcdf4\\u0026x-expires=1744537712\\u0026x-signature=H6nSq74XJ%2FcNo5BSr91P4UOaMQ4%3D";
        try {
            String base64String = convertPdfUrlToBase64(pdfUrl,true);
            System.out.println("Base64 encoded PDF:\n" + base64String);

            // 桌面路径（根据操作系统自动获取）
            String desktopPath = System.getProperty("user.home") + "/Desktop/output.pdf";

            try {
                // 将Base64字符串解码为PDF文件并保存到桌面
                saveBase64ToPdf(base64String, desktopPath);
                System.out.println("PDF文件已保存到桌面: " + desktopPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String convertPdfUrlToBase64(String pdfUrl,boolean needEscape) throws IOException {
        if(needEscape){
            String decoded = StringEscapeUtils.unescapeJava(pdfUrl)
                    .replaceAll("%(?![0-9a-fA-F]{2})", "%25");
            pdfUrl = URLDecoder.decode(decoded, StandardCharsets.UTF_8);
        }
        URL url = new URL(pdfUrl);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (InputStream inputStream = url.openStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        byte[] pdfBytes = outputStream.toByteArray();
        return Base64.getEncoder().encodeToString(pdfBytes);
    }

    /**
     * 将Base64字符串解码为PDF文件并保存到指定路径
     *
     * @param base64String Base64编码的PDF字符串
     * @param outputPath   输出文件路径
     * @throws IOException 如果文件写入失败
     */
    public static void saveBase64ToPdf(String base64String, String outputPath) throws IOException {
        // 解码Base64字符串为字节数组
        byte[] pdfBytes = Base64.getDecoder().decode(base64String);

        // 将字节数组写入文件
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            fos.write(pdfBytes);
        }
    }

    /**
     * 根据base64图片生成pdf base64
     * @param imageBase64 图片base64
     * @return pdf base64
     * @throws IOException
     */
    public static String ImageToPdfBase64(String imageBase64)throws IOException {
        // 清理Base64前缀
        String pureBase64 = imageBase64.replaceFirst("^data:image/\\w+;base64,", "");
        byte[] imageBytes = Base64.getDecoder().decode(pureBase64);
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // 创建与图片同尺寸的PDF页面
            PDImageXObject image = PDImageXObject.createFromByteArray(
                    document, imageBytes, "converted");
            PDPage page = new PDPage(new org.apache.pdfbox.pdmodel.common.PDRectangle(
                    image.getWidth(), image.getHeight()));
            document.addPage(page);
            // 绘制图片到PDF
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.drawImage(image, 0, 0);
            }
            // 直接输出到内存流
            document.save(baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }
}
