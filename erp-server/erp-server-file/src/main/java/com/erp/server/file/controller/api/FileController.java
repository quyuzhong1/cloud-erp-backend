package com.erp.server.file.controller.api;


import com.common.core.anno.LogAction;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.SysCommonDTO;
import com.erp.server.file.service.FileService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/file")
public class FileController extends BaseController {
    @Resource
    private FileService fileService;

    /**
     * 上传文件
     *
     * @param file
     * @return
     */
    @PostMapping("/uploadFile")
    public ApiResult<String> uploadFile(@RequestParam("multipartFile")MultipartFile file){
        return success(fileService.uploadFile(file));
    }

    /**
     * 删除文件
     * @param url
     * @return
     */
    @PostMapping("/deleteFile")
    public ApiResult<Integer> deleteFile(@RequestParam("url") String url){
        return success(fileService.deleteFile(url));
    }

    /**
     * 批量删除
     * @param urlList
     */
    @PostMapping("/deleteBatchFile")
    public ApiResult deleteBatchFile(@RequestParam("urlList") List<String> urlList){
        fileService.deleteBatchFile(urlList);
        return success();
    }

    /**
     * 上传文件
     * @param file
     * @param fileName
     * @return
     */
    @PostMapping("/uploadFileAndName")
    public ApiResult<String> uploadFileAndName(@RequestParam("file") File file, @RequestParam("fileName") String fileName){
        return success(fileService.uploadFile(file, fileName));
    }

    /**
     * 批量上传附件
     * @param multipartFile 文件数组
     * @return
     * @date: 2024-09-05
     * @author: tanmujin
     */
    @LogAction(value = LogActionEnum.UPLOAD, desc = "上传图片:文件名={name}")
    @PostMapping("/uploadBatch")
    public ApiResult<List<SysCommonDTO.AttachmentDTO>> uploadBatch(@RequestParam("multipartFile") MultipartFile[] multipartFile, HttpServletRequest request) {
        List<SysCommonDTO.AttachmentDTO> list = new ArrayList<>();
        for (MultipartFile file : multipartFile) {
            String filePath = fileService.uploadFile(file);
            String fileName = file.getOriginalFilename();
            list.add(new SysCommonDTO.AttachmentDTO(fileName, filePath));
        }
        return success(list);
    }
}
