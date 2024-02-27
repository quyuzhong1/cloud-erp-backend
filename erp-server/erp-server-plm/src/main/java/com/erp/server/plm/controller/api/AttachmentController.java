package com.erp.server.plm.controller.api;

import com.common.business.dto.base.BaseIdDTO;
import com.common.core.anno.LogAction;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.entity.PlmAttachmentEntity;
import com.erp.server.plm.service.PlmAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * 附件表
 *
 * @author Lambda
 * @since 2023-06-09
 */
@RestController
@RequestMapping("/attachment")
public class AttachmentController extends BaseController {

    @Autowired
    private PlmAttachmentService plmAttachmentService;


    /**
     * 上传
     * @author Will
     * @date: 2024/2/23 10:56
     * @param multipartFile
     * @param type
     * @param request
     * @return ApiResult<String>
     */
    @LogAction(value = LogActionEnum.UPLOAD, desc = "上传文件:文件名={name}")
    @PostMapping("/upload")
    public ApiResult<PlmAttachmentEntity> upload(@RequestParam("multipartFile") MultipartFile multipartFile, @RequestParam("type") String type, HttpServletRequest request) {
        PlmAttachmentEntity entity = plmAttachmentService.upload(multipartFile, type);
        return this.success(entity);
    }


    /**
     * 批量上传
     * @author Will
     * @date: 2024/2/27 15:02
     * @param multipartFileList
     * @param type
     * @param request
     * @return ApiResult<List<PlmAttachmentEntity>>
     */
    @LogAction(value = LogActionEnum.UPLOAD, desc = "上传文件:文件名={name}")
    @PostMapping("/batchUpload")
    public ApiResult<List<PlmAttachmentEntity>> batchUpload(@RequestParam("multipartFileList") List<MultipartFile> multipartFileList, @RequestParam("type") String type, HttpServletRequest request) {
        List<PlmAttachmentEntity> list = plmAttachmentService.batchUpload(multipartFileList, type);
        return this.success(list);
    }

    /**
     * 删除附件信息
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除附件信息")
    @PostMapping("/delete")
    public ApiResult removeAttachment(@RequestBody BaseIdDTO dto) {
        plmAttachmentService.removeAttachment(dto);
        return success();
    }

}
