package com.erp.server.file.service.impl;

import cn.hutool.json.JSONUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.file.dto.FileDTO;
import com.erp.model.sys.dto.SysCommonDTO;
import com.erp.server.file.handler.FileRegistry;
import com.erp.server.file.service.FileTaskService;
import com.google.gson.JsonParser;
import com.lark.oapi.Client;
import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.service.drive.v1.model.DownloadFileReq;
import com.lark.oapi.service.drive.v1.model.DownloadFileResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
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
        //根据url进行文件类型解析  https://ulanzichina.feishu.cn/docx/RwgbdMGogowhPLxCUVDcoy6vnrh
        //https://ulanzichina.feishu.cn/wiki/GevlwEB1VirtyFkpgH0cjhWjnld
        //https://ulanzichina.feishu.cn/docx/MURjdH3gvoRMNqxYr5mc6WjxnNc
        //https://ulanzichina.feishu.cn/file/JnSebf1hQoGOJexFw0JcuZi3nee
        //https://ulanzichina.feishu.cn/sheets/KTFqsRTLthfFR9ttOG2cMCrYnGf?sheet=wvKWL0

        //域名 https://ulanzichina.feishu.cn
        //文件类型 docx
        //文件token RwgbdMGogowhPLxCUVDcoy6vnrh
        String domain = fileUrl.split("/")[2];
        String fileType = fileUrl.split("/")[3];
        String fileToken = fileUrl.split("/")[4];

        // 构建client
        Client client = Client.newBuilder(FEISHU_APP_ID, FEISHU_APP_SECRET).build();

        DownloadFileReq downloadFileReq = new DownloadFileReq();
        downloadFileReq.setFileToken(fileToken);
        DownloadFileResp resp = client.drive().v1().file().download(downloadFileReq);
        log.warn("resp:{}", JSONUtil.toJsonStr(resp));
        // 处理服务端错误
        if (!resp.success()) {
            System.out.println(String.format("code:%s,msg:%s,reqId:%s, resp:%s",
                    resp.getCode(), resp.getMsg(), resp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(resp.getRawResponse().getBody(), StandardCharsets.UTF_8)))));
        }
        ByteArrayOutputStream data = resp.getData();
        String url = FastDFSClientUtil.uploadFile(data.toByteArray(), resp.getFileName(), null);
        log.warn("url:{}", url);
        return null;
    }
}
