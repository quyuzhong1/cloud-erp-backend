package com.erp.server.file.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.file.dto.FileDTO;
import com.erp.model.file.enums.FeishuFileTypeEnum;
import com.erp.model.sys.dto.SysCommonDTO;
import com.erp.server.file.handler.FeiShuFileHandler;
import com.erp.server.file.handler.FileRegistry;
import com.erp.server.file.service.FeiShuFileService;
import com.google.gson.JsonParser;
import com.lark.oapi.Client;
import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.service.drive.v1.model.*;
import com.lark.oapi.service.wiki.v2.model.GetNodeSpaceReq;
import com.lark.oapi.service.wiki.v2.model.GetNodeSpaceResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zdy
 * @ClassName FeiShuFileServiceImpl
 * @description: TODO
 * @date 2026年03月20日
 * @version: 1.0
 */
@Slf4j
@Service
public class FeiShuFileServiceImpl implements FeiShuFileService {

    private static final String URL_REGEX = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$";
    private static final Pattern pattern = Pattern.compile(URL_REGEX);
    @Resource
    private FileRegistry fileRegistry;
    @Value("${third.fs.downloadClientId}")
    private String FEISHU_APP_ID;
    @Value("${third.fs.downloadClientSecret}")
    private String FEISHU_APP_SECRET;
//    final static String FEISHU_APP_ID = "cli_a3675230d0fa9013";
//    final static String FEISHU_APP_SECRET = "fjN4dbPZGF6arsJSYOZpLgAcH7Z0XSvL";

    // 策略模式：文件处理器映射
    private final Map<String, FeiShuFileHandler> FeiShuFileHandlers = new HashMap<>();

    @PostConstruct
    public void init() {
        FeiShuFileHandlers.put(FeishuFileTypeEnum.FILE.getCode(), this::handleFile);
        FeiShuFileHandlers.put(FeishuFileTypeEnum.WIKI.getCode(), this::handleWiki);
        FeiShuFileHandlers.put(FeishuFileTypeEnum.DOCX.getCode(), this::handleDocument);
        FeiShuFileHandlers.put(FeishuFileTypeEnum.XLSX.getCode(), this::handleSpreadsheet);
        FeiShuFileHandlers.put(FeishuFileTypeEnum.BASE.getCode(), this::handleBase);
    }

    @Override
    public SysCommonDTO.AttachmentDTO getFeiShuFile(FileDTO.UploadDTO uploadDTO) throws Exception {
        String fileUrl = uploadDTO.getFileUrl();
        //校验url是否正确
        if (!isValidURL(fileUrl)) {
            throw new ServiceException(ApiError.FILE_URL_INVALID);
        }
        // 解析URL
        String domain = fileUrl.split("/")[2];
        String fileType = fileUrl.split("/")[3];
        String fileToken = fileUrl.split("/")[4];

        // 获取查询参数
        String sheet = null;
        String view = null;
        String[] split1 = fileUrl.split("\\?");
        if (split1.length > 1) {
            String[] params = split1[1].split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    if ("sheet".equals(keyValue[0])) {
                        sheet = keyValue[1];
                    } else if ("view".equals(keyValue[0])) {
                        view = keyValue[1];
                    }
                }
            }
        }

        // 构建client
        Client client = Client.newBuilder(FEISHU_APP_ID, FEISHU_APP_SECRET).build();

        // 使用策略模式处理不同类型的文件
        FeiShuFileHandler handler = FeiShuFileHandlers.get(fileType);
        if (handler != null) {
            return handler.handle(fileToken, sheet, view, client);
        }

        throw new ServiceException(ApiError.FILE_UNSUPPORTED_TYPE, fileType);
    }

    // 处理普通文件
    private SysCommonDTO.AttachmentDTO handleFile(String fileToken, String sheet, String view, Client client) throws Exception {
        DownloadFileResp resp = downloadFile(fileToken, client);
        String url = FastDFSClientUtil.uploadFile(resp.getData().toByteArray(), resp.getFileName(), null);
        log.warn("url:{}", url);
        return SysCommonDTO.AttachmentDTO.builder()
                .attachName(resp.getFileName())
                .attachUrl(url)
                .attachSize(new BigDecimal(resp.getData().size()).divide(new BigDecimal(1024 * 1024), 4, RoundingMode.HALF_UP))
                .build();
    }

    // 处理Wiki文件
    private SysCommonDTO.AttachmentDTO handleWiki(String fileToken, String sheet, String view, Client client) throws Exception {
        GetNodeSpaceResp getNodeSpaceResp = getNode(fileToken, client);
        String objToken = getNodeSpaceResp.getData().getNode().getObjToken();
        String objType = getNodeSpaceResp.getData().getNode().getObjType();

        return processExportTask(objToken, objType, client, sheet);
    }

    // 处理文档文件（DOCX）
    private SysCommonDTO.AttachmentDTO handleDocument(String fileToken, String sheet, String view, Client client) throws Exception {
        return processExportTask(fileToken, FeishuFileTypeEnum.DOCX.getCode(), client, sheet);
    }

    // 处理电子表格文件（XLSX）
    private SysCommonDTO.AttachmentDTO handleSpreadsheet(String fileToken, String sheet, String view, Client client) throws Exception {
        fileToken = fileToken.split("\\?")[0];
        return processExportTask(fileToken, FeishuFileTypeEnum.XLSX.getCode(), client, sheet);
    }

    // 处理Base文件
    private SysCommonDTO.AttachmentDTO handleBase(String fileToken, String sheet, String view, Client client) throws Exception {
        fileToken = fileToken.split("\\?")[0];
        if (sheet != null) {
            sheet = sheet.split("&")[0];
        }
        return processExportTask(fileToken, FeishuFileTypeEnum.BASE.getCode(), client, sheet);
    }

    // 通用的导出任务处理逻辑
    private SysCommonDTO.AttachmentDTO processExportTask(String fileToken, String fileType, Client client, String subId) throws Exception {
        CreateExportTaskResp createExportTaskResp = exportTask(fileToken, fileType, client, subId);
        String ticket = createExportTaskResp.getData().getTicket();

        GetExportTaskResp getExportTaskResp = retryQueryTask(ticket, fileToken, client);
        ExportTask result = getExportTaskResp.getData().getResult();

        DownloadExportTaskResp downloadExportTaskResp = downloadTask(result.getFileToken(), client);

        String url = FastDFSClientUtil.uploadFile(downloadExportTaskResp.getData().toByteArray(), downloadExportTaskResp.getFileName(), null);

        return SysCommonDTO.AttachmentDTO.builder()
                .attachName(downloadExportTaskResp.getFileName())
                .attachUrl(url)
                .attachSize(new BigDecimal(result.getFileSize()).divide(new BigDecimal(1024 * 1024), 4, RoundingMode.HALF_UP))
                .build();
    }

    /**
     * 下载文件
     * @param fileToken
     * @param client
     * @return
     * @throws Exception
     */
    private DownloadFileResp downloadFile(String fileToken, Client client) throws Exception {
        DownloadFileReq downloadFileReq = new DownloadFileReq();
        downloadFileReq.setFileToken(fileToken);
        DownloadFileResp resp = client.drive().v1().file().download(downloadFileReq);
        log.warn("resp:{}", JSONUtil.toJsonStr(resp));

        if (!resp.success()) {
            String format = String.format("code:%s,msg:%s,reqId:%s, resp:%s",
                    resp.getCode(), resp.getMsg(), resp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(resp.getRawResponse().getBody(), StandardCharsets.UTF_8))));
            throw new ServiceException(format);
        }
        return resp;
    }

    /**
     * 下载导出任务
     * @param fileToken
     * @param client
     * @return
     * @throws Exception
     */
    private DownloadExportTaskResp downloadTask(String fileToken, Client client) throws Exception {
        // 创建请求对象
        DownloadExportTaskReq req = DownloadExportTaskReq.newBuilder()
                .fileToken(fileToken)
                .build();
        log.warn("下载导出任务请求：{}", JSONUtil.toJsonStr(req));
        // 发起请求
        DownloadExportTaskResp resp = null;
        try {
            resp = client.drive().v1().exportTask().download(req);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 处理服务端错误
        if (!resp.success()) {
            String format = String.format("code:%s,msg:%s,reqId:%s, resp:%s",
                    resp.getCode(), resp.getMsg(), resp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(resp.getRawResponse().getBody(), StandardCharsets.UTF_8))));
            throw new ServiceException(format);
        }
        log.warn("下载导出任务成功：{}", JSONUtil.toJsonStr(resp));
        return resp;
    }

    /**
     * 重试查询导出任务结果  最多重试3次  每次间隔间隔1秒
     * @param ticket
     * @param objToken
     * @param client
     * @return
     * @throws Exception
     */
    private static GetExportTaskResp retryQueryTask(String ticket, String objToken, Client client) throws Exception {
        int retryCount = 0;
        for (int i = 0; i < 3; i++) {
            try {
                return queryTask(ticket, objToken, client);
            } catch (Exception e) {
                retryCount++;
                log.warn("查询导出任务结果失败，重试{}次，间隔1秒", retryCount);
                Thread.sleep(1000);
            }
        }
        throw new ServiceException("查询导出任务结果失败，重试3次后仍失败");
    }
    /**
     * 查询导出任务结果
     * @param ticket
     * @param objToken
     * @param client
     * @return
     * @throws Exception
     */
    private static GetExportTaskResp queryTask(String ticket, String objToken, Client client) throws Exception {
        // 创建请求对象
        GetExportTaskReq req = GetExportTaskReq.newBuilder()
                .ticket(ticket)
                .token(objToken)
                .build();
        log.warn("查询导出任务结果请求：{}", JSONUtil.toJsonStr(req));
        // 发起请求
        GetExportTaskResp getExportTaskResp = client.drive().v1().exportTask().get(req);

        // 处理服务端错误
        if (!getExportTaskResp.success()) {
            String format = String.format("code:%s,msg:%s,reqId:%s, resp:%s",
                    getExportTaskResp.getCode(), getExportTaskResp.getMsg(), getExportTaskResp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(getExportTaskResp.getRawResponse().getBody(), StandardCharsets.UTF_8))));
            throw new ServiceException(format);
        }
        log.warn("查询导出任务结果成功：{}", JSONUtil.toJsonStr(getExportTaskResp));
        if (getExportTaskResp.getData().getResult() == null || CharSequenceUtil.isBlank(getExportTaskResp.getData().getResult().getFileToken())) {
            throw new ServiceException("查询导出任务结果失败，导出任务结果为空");
        }
        return getExportTaskResp;
    }

    /**
     * 创建导出任务
     * @param objToken
     * @param objType
     * @param client
     * @param subId
     * @return
     * @throws Exception
     */
    private static CreateExportTaskResp exportTask(String objToken, String objType, Client client, String subId) throws Exception {
        String fileExtension = objType;
        if ("sheet".equals(objType) || "base".equals(objType) || "sheets".equals(objType)) {
            fileExtension = "xlsx";
        }else if ("doc".equals(objType)) {
            fileExtension = "docx";
        }
        if ("sheets".equals(objType)){
            objType = "sheet";
        }else if ("base".equals(objType)){
            objType = "bitable";
        }
        // 创建请求对象
        CreateExportTaskReq req = CreateExportTaskReq.newBuilder()
                .exportTask(ExportTask.newBuilder()
                        .fileExtension(fileExtension)
                        .token(objToken)
                        .type(objType)
                        .subId(subId)
                        .build())
                .build();
        log.warn("创建导出任务请求：{}", JSONUtil.toJsonStr(req));
        // 发起请求
        CreateExportTaskResp resp = client.drive().v1().exportTask().create(req);

        // 处理服务端错误
        if (!resp.success()) {
            String format = String.format("code:%s,msg:%s,reqId:%s, resp:%s",
                    resp.getCode(), resp.getMsg(), resp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(resp.getRawResponse().getBody(), StandardCharsets.UTF_8))));
            throw new ServiceException(format);
        }
        log.warn("创建导出任务成功：{}", JSONUtil.toJsonStr(resp));
        return resp;
    }

    /**
     * 获取知识空间节点信息
     * @param fileToken
     * @param client
     * @return
     * @throws Exception
     */
    private static GetNodeSpaceResp getNode(String fileToken, Client client) throws Exception {
        // 创建请求对象
        GetNodeSpaceReq req = GetNodeSpaceReq.newBuilder()
                .token(fileToken)
//                .objType("docx")
                .build();
        log.warn("获取知识空间节点请求：{}", JSONUtil.toJsonStr(req));
        // 发起请求
        GetNodeSpaceResp resp = client.wiki().v2().space().getNode(req);
        // 处理服务端错误
        if (!resp.success()) {
            String format = String.format("获取知识空间节点信息异常：code:%s,msg:%s,reqId:%s, resp:%s",
                    resp.getCode(), resp.getMsg(), resp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(resp.getRawResponse().getBody(), StandardCharsets.UTF_8))));
            throw new ServiceException(format);
        }
        log.warn("获取知识空间节点信息成功：{}", JSONUtil.toJsonStr(resp));
        return resp;
    }

    public static boolean isValidURL(String url) {
        if (url == null) return false;
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }
}
