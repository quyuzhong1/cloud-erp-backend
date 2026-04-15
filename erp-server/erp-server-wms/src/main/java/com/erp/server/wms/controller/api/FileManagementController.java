package com.erp.server.wms.controller.api;


import cn.hutool.core.collection.CollUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.FileManagementDTO;
import com.erp.model.wms.dto.QcStandardDTO;
import com.erp.model.wms.entity.FileManagementEntity;
import com.erp.model.wms.entity.WmsAttachmentEntity;
import com.erp.model.wms.enums.WmsFileTypeEnum;
import com.erp.server.wms.service.FileManagementService;
import com.erp.server.wms.service.WmsAttachmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件管理
 *
 * @author zdy
 * @since 2026-03-20
 */
@Slf4j
@RestController
@LogSystemModule("文件管理")
@RequestMapping("/fileManagement")
public class FileManagementController extends BaseController {

    @Resource
    private FileManagementService fileManagementService;
    @Resource
    private WmsAttachmentService wmsAttachmentService;
    /**
     * 新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author zdy
     * @date: 2026-03-20
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "文件管理新增")
    public ApiResult<List<BatchResultDTO>> add(@RequestBody @Validated FileManagementDTO.AddDTO dto) {
        List<BatchResultDTO> resultDTOS = fileManagementService.add(dto);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);

    }

    /**
     * 修改
     *
     * @param dto
     * @return ApiResult
     * @author zdy
     * @date: 2026-03-20
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "文件管理修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:fileManagement:update",
            serviceClass = FileManagementService.class,
            keyIdName = "id")
    public ApiResult<List<BatchResultDTO>> update(@RequestBody @Validated FileManagementDTO.UpdateDTO dto) {
        List<BatchResultDTO> resultDTOS = fileManagementService.update(dto);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 列表查询
     *
     * @param dto
     * @return ApiResult<PagingVO < FileManagementDTO.ListDTO>>
     * @author zdy
     * @date: 2026-03-20
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:fileManagement:paging",
            tableAlias = "fm"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<FileManagementDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<FileManagementDTO.PagingParamDTO> dto) {
        return success(fileManagementService.paging(dto));
    }


    /**
     * 详情
     *
     * @param id
     * @return ApiResult<FileManagementDTO.ViewDTO>>
     * @author zdy
     * @date: 2026-03-20
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:fileManagement:view",
            serviceClass = FileManagementService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<FileManagementDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(fileManagementService.view(id));
    }

    /**
     * 版本记录
     *
     * @param id
     * @return
     */
    @GetMapping("/history")
    public ApiResult<List<FileManagementDTO.VersionDTO>> history(@RequestParam("id") String id) {
        return success(fileManagementService.history(id));
    }
    /**
     * 批量生成质检标准
     * ids 取值 id
     */
    @PostMapping("/genQcStandard")
    public ApiResult<List<BatchResultDTO>> genQcStandard(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        List<FileManagementEntity> fileManagementEntityList = CollUtil.isEmpty(idsDTO.getIds()) ? null : fileManagementService.listByIds(idsDTO.getIds());
        if (CollUtil.isEmpty(fileManagementEntityList)){
            throw new ServiceException("未找到文件管理数据");
        }
        List<String> fileIds = fileManagementEntityList.stream().map(FileManagementEntity::getFileId).distinct().collect(Collectors.toList());
        List<WmsAttachmentEntity> attachmentEntityList = CollUtil.isEmpty(fileIds) ? null : wmsAttachmentService.listByIds(fileIds);
        if (CollUtil.isEmpty(attachmentEntityList)){
            throw new ServiceException("未找到文件附件数据");
        }
        List<BatchResultDTO> batchResultDTOList = new ArrayList<>();
        for (FileManagementEntity entity : fileManagementEntityList) {
            //只有评审报告类型的文件允许生成质检标准
            if (!WmsFileTypeEnum.REVIEW_REPORT.getCode().equals(entity.getFileType())){
                batchResultDTOList.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), "文件类型不是评审报告"));
                continue;
            }
            WmsAttachmentEntity attachmentEntity = attachmentEntityList.stream().filter(e -> e.getId().equals(entity.getFileId())).findFirst().orElse(null);
            if (attachmentEntity == null){
                batchResultDTOList.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), "未找到文件附件数据"));
                continue;
            }
            List<BatchResultDTO> batchResultDTOS = fileManagementService.genQcStandard(Collections.singletonList(entity.getSkuNo()), attachmentEntity.getAttachUrl());
            batchResultDTOList.addAll(batchResultDTOS);
        }
        return batchResultDTOList.stream().allMatch(BatchResultDTO::getSuccess) ? success(batchResultDTOList) : failure(batchResultDTOList);
    }
    /**
     * 生成单个质检标准
     *
     * @param id 取值 id
     * @return
     */
    @GetMapping("/genSingleQcStandard")
    public ApiResult<QcStandardDTO.AddDTO> genSingleQcStandard(@RequestParam("id") String id) {
        return success(fileManagementService.genSingleQcStandard(id));
    }
}
