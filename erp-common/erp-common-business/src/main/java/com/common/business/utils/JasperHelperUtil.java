package com.common.business.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.common.business.dto.ReportCommonDTO.ReportDTO;
import com.common.business.dto.ReportDataSourceDTO;
import com.common.business.enums.FileTypeEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.MathUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.base.JRBaseReport;
import net.sf.jasperreports.engine.export.*;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import net.sf.jasperreports.j2ee.servlets.BaseHttpServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class JasperHelperUtil {


    /**
     * @description: 导出
     * @date: 2023/12/22 9:39
     * @param jasperReport
     * @param docType
     */
    public static void prepareReport(JasperReport jasperReport, String docType) {
        /*
         * 如果导出的是excel，则需要去掉周围的margin
         */
        if (FileTypeEnum.XLS.name().equals(docType) || FileTypeEnum.XLSX.name().equals(docType)) {
            try {
                Field margin = JRBaseReport.class
                        .getDeclaredField("leftMargin");
                margin.setAccessible(true);
                margin.setInt(jasperReport, 0);
                margin = JRBaseReport.class.getDeclaredField("topMargin");
                margin.setAccessible(true);
                margin.setInt(jasperReport, 0);
                margin = JRBaseReport.class.getDeclaredField("bottomMargin");
                margin.setAccessible(true);
                margin.setInt(jasperReport, 0);

                Field pageHeight = JRBaseReport.class
                        .getDeclaredField("pageHeight");
                pageHeight.setAccessible(true);
                pageHeight.setInt(jasperReport, 2147483647);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    /**
     * @description: 判断格式类型
     * @author Will
     * @date: 2023/12/22 9:40
     * @param docType
     * @return JRAbstractExporter
     */
    private static JRAbstractExporter getJRExporter(FileTypeEnum docType) {
        JRAbstractExporter exporter = null;
        switch (docType) {
            case PDF:
                exporter = new JRPdfExporter();
                break;
            case DOC:
            case DOCX:
                exporter = new JRDocxExporter();
                break;
            case XLS:
            case XLSX:
                exporter = new JRXlsxExporter();
                break;
            case XML:
                exporter = new JRXmlExporter();
                break;
            case RTF:
                exporter = new JRRtfExporter();
                break;
            default:
                exporter = new JRPdfExporter();
        }
        return exporter;
    }

    /**
     * 按照类型导出不同格式文件
     *
     * @param type       导出类型
     * @param fileName   报表文件名称
     * @param is         jasper文件输入流
     * @param parameters 参数
     */
    public static void export(String type, String fileName, InputStream is, Map<String, Object> parameters, List<?> dataList) {
        export(type, fileName, is, parameters, null, MathUtil.ONE, dataList);
    }

    /**
     * 按照类型导出不同格式文件
     *
     * @param type       导出类型
     * @param fileName   报表文件名称
     * @param is         jasper文件输入流
     * @param parameters 参数
     * @param conn       数据源连接
     */
    public static void export(String type, String fileName, InputStream is, Map<String, Object> parameters, Connection conn) {
        export(type, fileName, is, parameters, conn, MathUtil.ZERO, null);
    }

    /**
     * 按照类型导出不同格式文件
     *
     * @param type       导出类型
     * @param fileName   报表文件名称
     * @param is         jasper文件输入流
     * @param parameters 参数
     * @param conn       数据源连接
     */
    public static void export(String type, String fileName, InputStream is, Map<String, Object> parameters, Connection conn, Integer isCustomData, List<?> dataList) {
        try {
            FileTypeEnum docType = FileTypeEnum.fromTypeName(type);
            if (docType == null) {
                return;
            } else if (docType == FileTypeEnum.HTML) {
                docType = FileTypeEnum.PDF;
            }
            HttpServletRequest request = ErpWebUtils.getRequest();
            HttpServletResponse response = ErpWebUtils.getResponse();

            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(is);
            prepareReport(jasperReport, type);
            JasperPrint jasperPrint;
            if (MathUtil.compareTo(isCustomData, MathUtil.ONE) == 0) {
                jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new ReportDataSourceDTO(dataList));
            } else {
                jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);
            }
            request.getSession().setAttribute(BaseHttpServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE, jasperPrint);

            response.setContentType(docType.getTypeContent());
            if (FileTypeEnum.HTML.getCode().equals(type)) {
                response.setHeader("Content-Disposition", "inline;");
            } else {
                String reportFileName = fileName;
                String reportFileNameStr = "reportFileName";
                if (!ObjectUtil.isEmpty(parameters.get(reportFileNameStr))) {
                    reportFileName = parameters.get(reportFileNameStr).toString();
                }
                response.setHeader("Content-Disposition", "attachment; filename=\""
                        + URLEncoder.encode(reportFileName, "UTF-8") + docType.getTypeSuffix() + "\"");
            }

            final OutputStream outputStream = response.getOutputStream();
            JRAbstractExporter exporter = getJRExporter(docType);
            if ((FileTypeEnum.XLS.getCode().equals(type) || FileTypeEnum.XLSX.getCode().equals(type))) {
                SimpleXlsxReportConfiguration conf = new SimpleXlsxReportConfiguration();
                conf.setDetectCellType(true);
                exporter.setConfiguration(conf);
            }

            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            exporter.exportReport();
            outputStream.flush();
            outputStream.close();
            is.close();
            if (conn != null) {
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 加载报表模板：jrxml 用当前运行时 Jasper 版本编译；jasper 直接反序列化。
     * <p>避免 Studio 高版本编译的 .jasper 在低版本运行时出现
     * {@code NoClassDefFoundError: Xxx (wrong name: Xxx_hash)}。</p>
     *
     * @param templateBytes 模板文件字节（.jrxml 或 .jasper）
     * @return 可填充的 JasperReport
     */
    public static JasperReport loadReport(byte[] templateBytes) {
        if (templateBytes == null || templateBytes.length == 0) {
            throw new ServiceException(ApiError.FILE_EXPORT_FAILED);
        }
        try {
            if (isJrxmlBytes(templateBytes)) {
                return JasperCompileManager.compileReport(new ByteArrayInputStream(templateBytes));
            }
            return (JasperReport) JRLoader.loadObject(new ByteArrayInputStream(templateBytes));
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("加载/编译 Jasper 模板失败: {}", e.getMessage(), e);
            throw new ServiceException(ApiError.FILE_EXPORT_FAILED);
        }
    }

    /**
     * 判断是否为 jrxml（XML）模板字节
     */
    private static boolean isJrxmlBytes(byte[] bytes) {
        int i = 0;
        // UTF-8 BOM
        if (bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xEF
                && (bytes[1] & 0xFF) == 0xBB
                && (bytes[2] & 0xFF) == 0xBF) {
            i = 3;
        }
        while (i < bytes.length) {
            byte b = bytes[i];
            if (b == ' ' || b == '\n' || b == '\r' || b == '\t') {
                i++;
                continue;
            }
            return b == '<';
        }
        return false;
    }

    /**
     * 使用已加载的 JasperReport 导出 PDF
     *
     * @param jasperReport 报表对象
     * @param parameters   参数
     * @param dataList     数据源列表
     * @return PDF 字节
     */
    public static byte[] exportToPdfStream(JasperReport jasperReport, Map<String, Object> parameters, List<?> dataList) {
        try {
            prepareReport(jasperReport, FileTypeEnum.PDF.getCode());
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport, parameters, new ReportDataSourceDTO(dataList));
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (Exception e) {
            log.error("exportToPdfStream(JasperReport) 失败: {}", e.getMessage(), e);
            log.warn("按照类型导出不同格式文件错误>>>>>>>>入参: parameters=={}", parameters);
            throw new ServiceException(ApiError.FILE_EXPORT_FAILED);
        }
    }

    /**
     * 按照类型导出不同格式文件
     *
     * @param is         jasper/jrxml 文件输入流
     * @param parameters 参数
     */
    public static byte[] exportToPdfStream(InputStream is, Map<String, Object> parameters, List<?> dataList) {
        return exportToPdfStream(is, parameters, null, MathUtil.ONE, dataList);
    }
    /**
     * 按照类型导出不同格式文件
     *
     * @param is         jasper文件输入流
     * @param parameters 参数
     */
    public static String exportToPdfUrl(InputStream is, Map<String, Object> parameters, List<?> dataList) {
        byte[] bytes = exportToPdfStream(is, parameters, null, MathUtil.ONE, dataList);
        return FastDFSClientUtil.uploadFile(bytes, RandomUtil.randomNumbers(5) + ".pdf", null);
    }
    /**
     * 按照类型导出不同格式文件
     *
     * @param is         jasper文件输入流
     * @param parameters 参数
     * @param conn       数据源连接
     */
    public static byte[] exportToPdfStream(InputStream is, Map<String, Object> parameters, Connection conn) {
       return exportToPdfStream(is, parameters, conn, MathUtil.ZERO, null);
    }

    /**
     * 按照类型导出不同格式文件
     * @param is         jasper文件输入流
     * @param parameters 参数
     * @param conn       数据源连接
     */
    public static byte[] exportToPdfStream(InputStream is, Map<String, Object> parameters, Connection conn, Integer isCustomData, List<?> dataList) {
        JasperPrint jasperPrint = null;
        try {
            byte[] templateBytes = toByteArray(is);
            JasperReport jasperReport = loadReport(templateBytes);
            prepareReport(jasperReport, FileTypeEnum.PDF.getCode());

            if (MathUtil.compareTo(isCustomData, MathUtil.ONE) == 0) {
                jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new ReportDataSourceDTO(dataList));
            } else {
                jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);
            }
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("（按照类型导出不同格式文件）方法：exportToPdfStream " + e.getMessage());
            log.warn("按照类型导出不同格式文件错误>>>>>>>>入参: parameters=={}", parameters);
            throw new ServiceException(ApiError.FILE_EXPORT_FAILED);
        }
    }

    private static byte[] toByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int n;
        while ((n = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, n);
        }
        return buffer.toByteArray();
    }

    /**
     * 按照类型批量导出报表
     *
     * @param reportDTOList
     * @param type 类型
     * @param conn
     */
    public static String batchSendReport(List<ReportDTO> reportDTOList, String type, Connection conn, String subreportDir, String reportJasperPath, String filePath) {
        //用于生成压缩包的文件list
        List<File> sysFileList = new ArrayList<>();
        String returnPath = "";
        try {
            if (CollectionUtil.isEmpty(reportDTOList)) {
                return "";
            }
            FileTypeEnum docType = FileTypeEnum.fromTypeName(type);
            if (docType == null) {
                return "";
            } else if (docType == FileTypeEnum.HTML) {
                docType = FileTypeEnum.PDF;
            }
            HttpServletResponse response = ErpWebUtils.getResponse();
            Map<String, List<ReportDTO>> sysReportMap = reportDTOList.stream().collect(Collectors.groupingBy(e -> e.getReportName().substring(e.getReportName().lastIndexOf("_") + 1)));
            for (int i = 0; i < reportDTOList.size(); i++) {
                ReportDTO reportDTO = reportDTOList.get(i);
                InputStream jasperStream = new FileInputStream(reportJasperPath + reportDTO.getReportName() + ".jasper");
                JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);
                prepareReport(jasperReport, type);
                JasperPrint jasperPrint;
                //组装传入报表参数
                Map<String, Object> parameters = new HashMap<>(4);
                parameters.put("prtid", reportDTO.getPrtid());
                parameters.put("id", reportDTO.getId());
                parameters.put("SUBREPORT_DIR", subreportDir);
                parameters.put("operator", reportDTO.getOperator());
                jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);
                JRAbstractExporter exporter = getJRExporter(docType);
                //处理报表名称
                String reportName = reportDTO.getReportName().substring(reportDTO.getReportName().lastIndexOf("_") + 1);
                List<ReportDTO> reportDTOS = sysReportMap.get(reportName);
                if (reportDTOS != null && reportDTOS.size() > 0) {
                    reportName = reportName + (i + 1);
                }
                //生成报表名称
                String sFileName = reportDTO.getBillNo() + "_" + reportName;
                //定义临时生成报表文件夹
                String sendReport = "sendFolder";
                String sFilePath = reportJasperPath + sendReport + "/";
                log.info(sFilePath + "生成文件夹路径-------------------------------");
                File targetFile = new File(sFilePath);
                if (!targetFile.exists()) {
                    targetFile.mkdirs();
                }
                sFilePath += sFileName + "." + type;
                log.info(sFilePath + "生成报表完整路径-------------------------------");
                File file1 = new File(sFilePath);
                sysFileList.add(file1);
                exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, sFilePath);
                exporter.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);
                exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
                exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.FALSE);
                exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                exporter.setParameter(JRPdfExporterParameter.IS_128_BIT_KEY, Boolean.TRUE);
                exporter.exportReport();
                jasperStream.close();
            }
            if (conn != null) {
                conn.close();
            }
            //生成zip路径
            String zipPath = filePath + LocalDate.now() + "/";
            String sFileName = String.valueOf(UUID.randomUUID());
            //如果没有文件夹则创建
            File targetFile = new File(zipPath);
            if (!targetFile.exists()) {
                targetFile.mkdirs();
            }
            File sendFile = new File(zipPath + sFileName + ".zip");
            //用于返回给前端的zip路径
            returnPath = zipPath + sFileName + ".zip";
            log.info(sendFile + "生成zip压缩包路径 --------------");
            //执行成压缩包
            ZipUtils.zipFiles(sysFileList, sendFile);
            response.setContentLength((int) sendFile.length());
            InputStream in = new BufferedInputStream(new FileInputStream(sendFile), 4096);
            OutputStream os = new BufferedOutputStream(response.getOutputStream());
            byte[] bytes = new byte[4096];
            int i = 0;
            while ((i = in.read(bytes)) > 0) {
                os.write(bytes, 0, i);
            }
            os.flush();
            os.close();
            response.flushBuffer();// 不可少
            //删除文件
            for (File file1 : sysFileList) {
                checkAndDelete(file1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnPath;
    }

    private static void checkAndDelete(File file1) {
        if (!file1.exists()) {
            log.info("file1 not exists() --------------");
            return;
        }
        if(!file1.delete()){
            log.info("file1.delete() delete --------------");
        }
    }


    /**
     * 按照类型导出不同格式文件
     *
     * @param is         jasper文件输入流
     * @param parameters 参数
     */
    public static byte[] exportToPdfStream(InputStream is, Map<String, Object> parameters) {
        JasperPrint jasperPrint = null;
        try {
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(is);
            prepareReport(jasperReport, FileTypeEnum.PDF.getCode());
            jasperPrint = JasperFillManager.fillReport(jasperReport, parameters);
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (Exception e) {
            log.error("（按照类型导出不同格式文件）方法：exportToPdfStream " + e.getMessage());
            log.info("按照类型导出不同格式文件错误>>>>>>>>入参: parameters=={}", parameters);
            throw new ServiceException(ApiError.FILE_EXPORT_FAILED);
        }
    }
}
