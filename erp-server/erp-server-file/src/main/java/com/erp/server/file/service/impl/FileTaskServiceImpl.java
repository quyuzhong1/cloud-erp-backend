package com.erp.server.file.service.impl;

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
    final String FEISHU_APP_ID = "cli_a3675230d0fa9013";
    final String FEISHU_APP_SECRET = "fjN4dbPZGF6arsJSYOZpLgAcH7Z0XSvL";

    @Override
    public SysCommonDTO.AttachmentDTO getFeiShuFile(FileDTO.UploadDTO uploadDTO) throws Exception {
        String fileUrl = uploadDTO.getFileUrl();
        //根据url进行文件类型解析
        // https://ulanzichina.feishu.cn/docx/RwgbdMGogowhPLxCUVDcoy6vnrh
        // https://ulanzichina.feishu.cn/wiki/GevlwEB1VirtyFkpgH0cjhWjnld
        // https://ulanzichina.feishu.cn/docx/MURjdH3gvoRMNqxYr5mc6WjxnNc
        // https://ulanzichina.feishu.cn/file/JnSebf1hQoGOJexFw0JcuZi3nee
        // https://ulanzichina.feishu.cn/sheets/KTFqsRTLthfFR9ttOG2cMCrYnGf?sheet=wvKWL0

        //域名 https://ulanzichina.feishu.cn
        //文件类型 docx
        //文件token RwgbdMGogowhPLxCUVDcoy6vnrh
        String domain = fileUrl.split("/")[2];
        String fileType = fileUrl.split("/")[3];
        String fileToken = fileUrl.split("/")[4];
        //获取sheet=wvKWL0
        String sheet = null;
        String[] split1 = fileUrl.split("\\?");
        if (split1.length > 1) {
            String[] split2 = split1[1].split("=");
            if (split2.length > 1) {
                sheet = split2[1];
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
            GetExportTaskResp getExportTaskResp = queryTask(ticket, objToken, client);
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
            //查询导出任务结果
            GetExportTaskResp getExportTaskResp = queryTask(ticket, fileToken, client);
            ExportTask result = getExportTaskResp.getData().getResult();
            //下载文件
            DownloadExportTaskResp downloadExportTaskResp = downloadTask(result.getFileToken(), client);
            //上传fastdfs
            String url = FastDFSClientUtil.uploadFile(downloadExportTaskResp.getData().toByteArray(), downloadExportTaskResp.getFileName(), null);
            return SysCommonDTO.AttachmentDTO.builder().attachName(downloadExportTaskResp.getFileName()).attachUrl(url).attachSize(BigDecimal.valueOf(result.getFileSize() / 1024 / 1024)).build();

        } else if (FeishuFileTypeEnum.XLSX.getCode().equals(fileType)) {
            // 处理XLSX文件
            //创建导出任务
            CreateExportTaskResp createExportTaskResp = exportTask(fileToken, fileType, client, sheet);
            String ticket = createExportTaskResp.getData().getTicket();
            //查询导出任务结果
            GetExportTaskResp getExportTaskResp = queryTask(ticket, fileToken, client);
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
        log.warn("下载导出任务成功：{}", resp);
        return resp;
    }

    private GetExportTaskResp queryTask(String ticket, String objToken, Client client) throws Exception {
        // 创建请求对象
        GetExportTaskReq req = GetExportTaskReq.newBuilder()
                .ticket(ticket)
                .token(objToken)
                .build();
        // 发起请求
        GetExportTaskResp getExportTaskResp = client.drive().v1().exportTask().get(req);

        // 处理服务端错误
        if (!getExportTaskResp.success()) {
            String format = String.format("code:%s,msg:%s,reqId:%s, resp:%s",
                    getExportTaskResp.getCode(), getExportTaskResp.getMsg(), getExportTaskResp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(getExportTaskResp.getRawResponse().getBody(), StandardCharsets.UTF_8))));
            throw new ServiceException(format);
        }
        log.warn("查询导出任务结果成功：{}", getExportTaskResp);
        return getExportTaskResp;
    }

    private static CreateExportTaskResp exportTask(String objToken, String objType, Client client, String subId) throws Exception {

        // 创建请求对象
        CreateExportTaskReq req = CreateExportTaskReq.newBuilder()
                .exportTask(ExportTask.newBuilder()
                        .fileExtension("csv")
                        .token(objToken)
                        .type(objType)
                        .subId(subId)
                        .build())
                .build();

        // 发起请求
        CreateExportTaskResp resp = client.drive().v1().exportTask().create(req);

        // 处理服务端错误
        if (!resp.success()) {
            String format = String.format("code:%s,msg:%s,reqId:%s, resp:%s",
                    resp.getCode(), resp.getMsg(), resp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(resp.getRawResponse().getBody(), StandardCharsets.UTF_8))));
            throw new ServiceException(format);
        }
        log.warn("创建导出任务成功：{}", resp);
        return resp;
    }

    private static GetNodeSpaceResp getNode(String fileToken, Client client) throws Exception {
        // 创建请求对象
        GetNodeSpaceReq req = GetNodeSpaceReq.newBuilder()
                .token(fileToken)
//                .objType("docx")
                .build();

        // 发起请求
        GetNodeSpaceResp resp = client.wiki().v2().space().getNode(req);
        // 处理服务端错误
        if (!resp.success()) {
            String format = String.format("获取知识空间节点信息异常：code:%s,msg:%s,reqId:%s, resp:%s",
                    resp.getCode(), resp.getMsg(), resp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(resp.getRawResponse().getBody(), StandardCharsets.UTF_8))));
            throw new ServiceException(format);
        }
        log.warn("获取知识空间节点信息成功：{}", resp);
        return resp;
    }
}
