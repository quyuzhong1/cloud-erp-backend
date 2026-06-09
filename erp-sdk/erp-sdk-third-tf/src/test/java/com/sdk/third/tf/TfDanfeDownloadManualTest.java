package com.sdk.third.tf;

import cn.hutool.core.text.CharSequenceUtil;
import com.sdk.third.tf.client.InvoiceApiClient;
import com.sdk.third.tf.client.TfApiClient;
import com.sdk.third.tf.dto.GetDanfeDTO;
import com.sdk.third.tf.dto.GetDanfeResponseDTO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 手动调用TF Fiscal获取DANFE PDF并下载到本地。
 * 运行前通过环境变量或-D参数传入敏感信息，避免把token/appKey写进仓库。
 */
public class TfDanfeDownloadManualTest {

    private static final int TIMEOUT_MILLIS = 30000;
    private static final float DEFAULT_RENDER_DPI = 300F;

    public static void main(String[] args) throws Exception {
        String outputDir = defaultIfBlank(optional("TF_DANFE_OUTPUT_DIR"), "target/tf-danfe");
        String localPdf = optional("TF_LOCAL_PDF");
        float renderDpi = Float.parseFloat(defaultIfBlank(optional("TF_DANFE_RENDER_DPI"), String.valueOf(DEFAULT_RENDER_DPI)));
        Path outputPath = Paths.get(outputDir);
        Files.createDirectories(outputPath);

        if (CharSequenceUtil.isNotBlank(localPdf)) {
            convertPdfToPng(Paths.get(localPdf), outputPath, "danfe_simples", renderDpi);
            System.out.println("本地PDF转PNG完成，目录：" + outputPath.toAbsolutePath());
            return;
        }

        String token = required("TF_TOKEN");
        String appKey = defaultIfBlank(optional("TF_APP_KEY"), token);
        String uuid = required("TF_INVOICE_UUID");

        GetDanfeDTO dto = new GetDanfeDTO();
        dto.setUuid(uuid);
        dto.setAltura(150);
        dto.setLargura(100);

        InvoiceApiClient invoiceApiClient = new InvoiceApiClient(new TfApiClient());
        GetDanfeResponseDTO.GetDanfeDataDTO danfe = invoiceApiClient.getDanfe(dto, token, appKey);

        downloadIfPresent(danfe.getDanfe(), outputPath.resolve("danfe.pdf"));
        Path danfeSimplesPdf = outputPath.resolve("danfe_simples.pdf");
        downloadIfPresent(danfe.getDanfeSimples(), danfeSimplesPdf);
        convertPdfToPng(danfeSimplesPdf, outputPath, "danfe_simples", renderDpi);

        System.out.println("DANFE下载完成，目录：" + outputPath.toAbsolutePath());
    }

    private static void downloadIfPresent(String fileUrl, Path target) throws Exception {
        if (CharSequenceUtil.isBlank(fileUrl)) {
            System.out.println("跳过空URL：" + target.getFileName());
            return;
        }

        URLConnection connection = URI.create(fileUrl).toURL().openConnection();
        connection.setConnectTimeout(TIMEOUT_MILLIS);
        connection.setReadTimeout(TIMEOUT_MILLIS);
        try (InputStream inputStream = connection.getInputStream()) {
            Files.copy(inputStream, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        System.out.println("已下载：" + target.toAbsolutePath());
    }

    private static void convertPdfToPng(Path pdfPath, Path outputPath, String fileNamePrefix, float renderDpi) throws Exception {
        if (!Files.exists(pdfPath)) {
            System.out.println("跳过不存在的PDF：" + pdfPath.toAbsolutePath());
            return;
        }

        System.out.println("PDF转PNG渲染DPI：" + renderDpi);
        try (PDDocument document = PDDocument.load(pdfPath.toFile())) {
            PDFRenderer renderer = new PDFRenderer(document);
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, renderDpi, ImageType.RGB);
                Path pngPath = outputPath.resolve(buildPngFileName(fileNamePrefix, document.getNumberOfPages(), i));
                ImageIO.write(image, "png", pngPath.toFile());
                System.out.println("已转换PNG：" + pngPath.toAbsolutePath());
            }
        }
    }

    private static String buildPngFileName(String fileNamePrefix, int pageCount, int pageIndex) {
        if (pageCount == 1) {
            return fileNamePrefix + ".png";
        }
        return fileNamePrefix + "_page_" + (pageIndex + 1) + ".png";
    }

    private static String required(String key) {
        String value = optional(key);
        if (CharSequenceUtil.isBlank(value)) {
            throw new IllegalArgumentException("请通过环境变量或-D参数传入：" + key);
        }
        return value;
    }

    private static String optional(String key) {
        String value = System.getProperty(key);
        return CharSequenceUtil.isNotBlank(value) ? value : System.getenv(key);
    }

    private static String defaultIfBlank(String value, String defaultValue) {
        return CharSequenceUtil.isBlank(value) ? defaultValue : value;
    }
}
