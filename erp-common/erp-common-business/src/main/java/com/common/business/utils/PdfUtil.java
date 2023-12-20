package com.common.business.utils;

import com.lowagie.text.Document;
import lombok.Cleanup;
import org.apache.commons.collections.CollectionUtils;

import java.io.*;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import org.apache.commons.lang.StringUtils;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
public class PdfUtil {
    public static void main(String[] args) throws WriterException {
        String barcodeText = "123456789";  // 要编码的文本
        int width = 300;  // 条形码的宽度
        int height = 100;  // 条形码的高度
        Path file = FileSystems.getDefault().getPath("barcode.png");  // 输出的文件路径
        Code128Writer code128Writer = new Code128Writer();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");  // 设置字符集，如果你需要的话
        BitMatrix bitMatrix = null;
        bitMatrix = code128Writer.encode(barcodeText, BarcodeFormat.CODE_128, width, height, hints);
        try {
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", file);  // 输出为PNG格式，你也可以选择其他格式，如JPEG等
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * pdf 合并操作2
     * 直接将各个pdf对应的字节码，进行合并成新的pdf文件数据
     * @param base64Pdfs  多个pdf的base64编码文件
     * @return
     */
    public static String getNewMergePdfBase64_2(List<String> base64Pdfs) throws Exception {
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
            return base64ForPdf(fileInputStream);
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
}
