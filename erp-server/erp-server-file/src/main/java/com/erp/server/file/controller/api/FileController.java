package com.erp.server.file.controller.api;


import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.SysCommonDTO;
import com.erp.server.file.handler.FileRegistry;
import com.erp.server.file.service.FileService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件中心
 */
@RestController
@RequestMapping("/file")
@LogSystemModule("文件中心")
public class FileController extends BaseController {
    @Resource
    private FileRegistry fileRegistry;

    /**
     * 上传文件
     *
     * @param multipartFile
     * @return
     */
    @PostMapping(value = "/uploadFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResult<SysCommonDTO.AttachmentDTO> uploadFile(@RequestPart("multipartFile")MultipartFile multipartFile){
        FileService fileService = fileRegistry.getHandler();
        String url = fileService.uploadFile(multipartFile);
        String fileName = multipartFile.getOriginalFilename();
        return success(new SysCommonDTO.AttachmentDTO(fileName, url));
    }

    /**
     * 删除文件
     * @param url
     * @return
     */
    @PostMapping("/deleteFile")
    @LogAction(value = LogActionEnum.DELETE, desc = "删除文件 路径={url}")
    public ApiResult<Integer> deleteFile(@RequestParam("url") String url){
        FileService fileService = fileRegistry.getHandler();
        return success(fileService.deleteFile(url));
    }

    /**
     * 批量删除
     * @param urlList
     */
    @PostMapping("/deleteBatchFile")
    @LogAction(value = LogActionEnum.DELETE, desc = "批量删除文件")
    public ApiResult deleteBatchFile(@RequestParam("urlList") List<String> urlList){
        FileService fileService = fileRegistry.getHandler();
        fileService.deleteBatchFile(urlList);
        return success();
    }

    /**
     * 批量上传附件
     * @param multipartFile 文件数组
     * @return
     * @date: 2024-09-05
     * @author: tanmujin
     */
    @LogAction(value = LogActionEnum.UPLOAD, desc = "上传图片:文件名={name}")
    @PostMapping(value = "/uploadBatch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResult<List<SysCommonDTO.AttachmentDTO>> uploadBatch(@RequestPart("multipartFile") MultipartFile[] multipartFile, HttpServletRequest request) {
        FileService fileService = fileRegistry.getHandler();
        List<SysCommonDTO.AttachmentDTO> list = new ArrayList<>();
        for (MultipartFile file : multipartFile) {
            String filePath = fileService.uploadFile(file);
            String fileName = file.getOriginalFilename();
            list.add(new SysCommonDTO.AttachmentDTO(fileName, filePath));
        }
        return success(list);
    }
    @GetMapping("/downloadByParams")
    public ResponseEntity<byte[]> download(
            @RequestParam String fileUrl,
            @RequestParam String fileName,
            @RequestParam(required = false) String contentType) {
        FileService fileService = fileRegistry.getHandler();
        contentType = contentType == null ? "application/octet-stream; charset=UTF-8" : contentType;
        try {
            return fileService.downloadByte(fileUrl, fileName, contentType,false);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
