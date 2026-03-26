package com.erp.server.file.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.file.dto.FileDTO;
import com.erp.model.file.enums.FeishuFileTypeEnum;
import com.erp.model.sys.dto.SysCommonDTO;
import com.erp.server.file.handler.FileRegistry;
import com.erp.server.file.service.FileTaskService;
import com.google.gson.JsonParser;
import com.lark.oapi.Client;
import com.lark.oapi.core.request.RequestOptions;
import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.service.drive.v1.model.*;
import com.lark.oapi.service.wiki.v2.model.GetNodeSpaceReq;
import com.lark.oapi.service.wiki.v2.model.GetNodeSpaceResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

/**
 * @author zdy
 * @ClassName FileTaskServiceImpl
 * @description: TODO
 * @date 2026年03月20日
 * @version: 1.0
 */
@Slf4j
@Service
public class FileTaskServiceImpl implements FileTaskService {

    @Resource
    private FileRegistry fileRegistry;

    final String FEISHU_FILE_URL = "https://open.feishu.cn/open-apis/drive/v1/files/:file_token/download";
    final String FEISHU_USER_ACCESS_TOKEN = "u-e.VZVpvUZ7QHm3EyjKmYuS0lhN3glhWNW0GyYMI203mc";
    final static String FEISHU_APP_ID = "cli_a3675230d0fa9013";
    final static String FEISHU_APP_SECRET = "fjN4dbPZGF6arsJSYOZpLgAcH7Z0XSvL";

    @Override
    public SysCommonDTO.AttachmentDTO getFeiShuFile(FileDTO.UploadDTO uploadDTO) throws Exception {
        String fileUrl = uploadDTO.getFileUrl();
        //根据url进行文件类型解析
//         https://ulanzichina.feishu.cn/docx/RwgbdMGogowhPLxCUVDcoy6vnrh
//         https://ulanzichina.feishu.cn/wiki/GevlwEB1VirtyFkpgH0cjhWjnld
//         https://ulanzichina.feishu.cn/docx/MURjdH3gvoRMNqxYr5mc6WjxnNc
//         https://ulanzichina.feishu.cn/file/JnSebf1hQoGOJexFw0JcuZi3nee
//         https://ulanzichina.feishu.cn/sheets/KTFqsRTLthfFR9ttOG2cMCrYnGf?sheet=wvKWL0
//        https://ulanzichina.feishu.cn/base/Bi0mbFqAWaBR36swcOkcV7XAnQe?table=tblz2QEAG3BrGKqz&view=vew101gNh4

        //域名 https://ulanzichina.feishu.cn
        //文件类型 docx
        //文件token RwgbdMGogowhPLxCUVDcoy6vnrh
        String domain = fileUrl.split("/")[2];
        String fileType = fileUrl.split("/")[3];
        String fileToken = fileUrl.split("/")[4];
        //获取sheet=wvKWL0
        String sheet = null;
        String view = null;
        String[] split1 = fileUrl.split("\\?");
        if (split1.length > 1) {
            String[] split2 = split1[1].split("=");
            if (split2.length > 1) {
                sheet = split2[1];
            }else if (split2.length == 3){
                view = split2[2];
            }
        }
        // 构建client
        Client client = Client.newBuilder(FEISHU_APP_ID, FEISHU_APP_SECRET).build();
        if (FeishuFileTypeEnum.FILE.getCode().equals(fileType)) {
            DownloadFileReq downloadFileReq = new DownloadFileReq();
            downloadFileReq.setFileToken(fileToken);
            DownloadFileResp resp = client.drive().v1().file().download(downloadFileReq);
            log.warn("resp:{}", JSONUtil.toJsonStr(resp));
            // 处理服务端错误
            if (!resp.success()) {
                String format = String.format("code:%s,msg:%s,reqId:%s, resp:%s",
                        resp.getCode(), resp.getMsg(), resp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(resp.getRawResponse().getBody(), StandardCharsets.UTF_8))));
                throw new ServiceException(format);
            }
            ByteArrayOutputStream data = resp.getData();
            String url = FastDFSClientUtil.uploadFile(data.toByteArray(), resp.getFileName(), null);
            log.warn("url:{}", url);
            return SysCommonDTO.AttachmentDTO.builder().attachName(resp.getFileName()).attachUrl(url).attachSize(BigDecimal.valueOf(data.size() / 1024 / 1024)).build();
        } else if (FeishuFileTypeEnum.WIKI.getCode().equals(fileType)) {
            // 处理WIKI文件
            //获取知识空间节点信息
            GetNodeSpaceResp getNodeSpaceResp = getNode(fileToken, client);
            //得到节点对应的obj_token（文档 Token）和obj_type（文档类型）
            String objToken = getNodeSpaceResp.getData().getNode().getObjToken();
            String objType = getNodeSpaceResp.getData().getNode().getObjType();
            //创建导出任务
            CreateExportTaskResp createExportTaskResp = exportTask(objToken, objType, client, sheet);
            String ticket = createExportTaskResp.getData().getTicket();
            //查询导出任务结果
            GetExportTaskResp getExportTaskResp = retryQueryTask(ticket, objToken, client);
            ExportTask result = getExportTaskResp.getData().getResult();
            //下载文件
            DownloadExportTaskResp downloadExportTaskResp = downloadTask(result.getFileToken(), client);
            //上传fastdfs
            String url = FastDFSClientUtil.uploadFile(downloadExportTaskResp.getData().toByteArray(), downloadExportTaskResp.getFileName(), null);
            return SysCommonDTO.AttachmentDTO.builder().attachName(downloadExportTaskResp.getFileName()).attachUrl(url).attachSize(BigDecimal.valueOf(result.getFileSize() / 1024 / 1024)).build();
        } else if (FeishuFileTypeEnum.DOCX.getCode().equals(fileType)) {
            // 处理DOCX文件
            //创建导出任务
            CreateExportTaskResp createExportTaskResp = exportTask(fileToken, fileType, client, sheet);
            String ticket = createExportTaskResp.getData().getTicket();
            //查询导出任务结果  7621369825390906312  RwgbdMGogowhPLxCUVDcoy6vnrh
            GetExportTaskResp getExportTaskResp = retryQueryTask(ticket, fileToken, client);
            ExportTask result = getExportTaskResp.getData().getResult();
            //下载文件
            DownloadExportTaskResp downloadExportTaskResp = downloadTask(result.getFileToken(), client);
            //上传fastdfs
            String url = FastDFSClientUtil.uploadFile(downloadExportTaskResp.getData().toByteArray(), downloadExportTaskResp.getFileName(), null);
            return SysCommonDTO.AttachmentDTO.builder().attachName(downloadExportTaskResp.getFileName()).attachUrl(url).attachSize(BigDecimal.valueOf(result.getFileSize() / 1024 / 1024)).build();

        } else if (FeishuFileTypeEnum.XLSX.getCode().equals(fileType)) {
            // 处理XLSX文件 截去fileToken后缀?sheet=wvKWL0部分
            fileToken = fileToken.split("\\?")[0];
            //创建导出任务
            CreateExportTaskResp createExportTaskResp = exportTask(fileToken, fileType, client, sheet);
            String ticket = createExportTaskResp.getData().getTicket();
            //查询导出任务结果
            GetExportTaskResp getExportTaskResp = retryQueryTask(ticket, fileToken, client);
            ExportTask result = getExportTaskResp.getData().getResult();
            //下载文件
            DownloadExportTaskResp downloadExportTaskResp = downloadTask(result.getFileToken(), client);
            //上传fastdfs
            String url = FastDFSClientUtil.uploadFile(downloadExportTaskResp.getData().toByteArray(), downloadExportTaskResp.getFileName(), null);
            return SysCommonDTO.AttachmentDTO.builder().attachName(downloadExportTaskResp.getFileName()).attachUrl(url).attachSize(BigDecimal.valueOf(result.getFileSize() / 1024 / 1024)).build();
        }else if (FeishuFileTypeEnum.BASE.getCode().equals(fileType)){
            fileToken = fileToken.split("\\?")[0];
            sheet =sheet.split("\\&")[0];
            //创建导出任务
            CreateExportTaskResp createExportTaskResp = exportTask(fileToken, fileType, client, sheet);
            String ticket = createExportTaskResp.getData().getTicket();
            //查询导出任务结果
            GetExportTaskResp getExportTaskResp = retryQueryTask(ticket, fileToken, client);
            ExportTask result = getExportTaskResp.getData().getResult();
            //下载文件
            DownloadExportTaskResp downloadExportTaskResp = downloadTask(result.getFileToken(), client);
            //上传fastdfs
            String url = FastDFSClientUtil.uploadFile(downloadExportTaskResp.getData().toByteArray(), downloadExportTaskResp.getFileName(), null);
            return SysCommonDTO.AttachmentDTO.builder().attachName(downloadExportTaskResp.getFileName()).attachUrl(url).attachSize(BigDecimal.valueOf(result.getFileSize() / 1024 / 1024)).build();

        }

        return null;
    }

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
}
