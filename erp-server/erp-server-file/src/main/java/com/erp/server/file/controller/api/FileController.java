package com.erp.server.file.controller.api;


import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.file.dto.FileDTO;
import com.erp.model.sys.dto.SysCommonDTO;
import com.erp.server.file.handler.FileRegistry;
import com.erp.server.file.service.FileService;
import com.erp.server.file.service.FeiShuFileService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Base64;
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
    @Resource
    private FeiShuFileService feiShuFileService;

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
        return success(new SysCommonDTO.AttachmentDTO(fileName, url, new BigDecimal(multipartFile.getSize()).divide(new BigDecimal(1024 * 1024), 4, RoundingMode.HALF_UP)));
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
    public ApiResult deleteBatchFile(@RequestBody List<String> urlList){
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
            list.add(new SysCommonDTO.AttachmentDTO(fileName, filePath, new BigDecimal(file.getSize()).divide(new BigDecimal(1024 * 1024), 4, RoundingMode.HALF_UP)));
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

    /**
     * 上传文件base64编码
     * @param uploadBase64
     * @return
     */
    @PostMapping(value = "/uploadFileByBase64")
    public SysCommonDTO.AttachmentDTO uploadFileByBase64(@RequestBody FileDTO.UploadBase64 uploadBase64){
        FileService fileService = fileRegistry.getHandler();
        String[] parts = uploadBase64.getBase64().split(",");
        byte[] bytes = Base64.getDecoder().decode(parts.length > 1 ? parts[1] : parts[0]);
        String fileName = uploadBase64.getFileName();
        String url = FastDFSClientUtil.publicUrl + fileService.uploadFile(bytes, fileName, null);
        return new SysCommonDTO.AttachmentDTO(fileName, url, new BigDecimal(bytes.length).divide(new BigDecimal(1024 * 1024), 4, RoundingMode.HALF_UP));
    }

    /**
     * 获取文件base64编码
     * @param fileId
     * @return
     */
    @PostMapping(value = "/getFileByBase64")
    public String getFileByBase64(@RequestBody String fileId){
        FileService fileService = fileRegistry.getHandler();
        byte[] bytes = fileService.downloadFile(fileId);
//        "data:application/pdf;base64," +
        return Base64.getEncoder().encodeToString(bytes);
    }
    /**
     * 合并多个文件为一个文件
     * @param fileIds 文件id列表
     * @return 合并后的文件url
     */
    @PostMapping(value = "/mergeFiles")
    public String mergeFiles(@RequestBody List<String> fileIds){
        FileService fileService = fileRegistry.getHandler();
        return fileService.mergeFiles(fileIds);
    }

    /**
     * 上传飞书文件链接
     *
     * @param uploadDTO
     * @return
     */
    @PostMapping(value = "/getFeiShuFile")
    public ApiResult<SysCommonDTO.AttachmentDTO> getFeiShuFile(@RequestBody FileDTO.UploadDTO uploadDTO) {
        SysCommonDTO.AttachmentDTO attachmentDTO = feiShuFileService.getFeiShuFile(uploadDTO);
        return success(attachmentDTO);
    }
}
