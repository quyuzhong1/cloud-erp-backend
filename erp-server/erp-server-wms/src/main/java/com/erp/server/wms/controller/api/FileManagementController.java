package com.erp.server.wms.controller.api;


import cn.hutool.core.collection.CollUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
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
import com.erp.model.wms.entity.QcStandardSkuRefEntity;
import com.erp.model.wms.entity.WmsAttachmentEntity;
import com.erp.model.wms.enums.WmsFileTypeEnum;
import com.erp.server.wms.service.FileManagementService;
import com.erp.server.wms.service.QcStandardSkuRefService;
import com.erp.server.wms.service.WmsAttachmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
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
    private QcStandardSkuRefService qcStandardSkuRefService;
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
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated FileManagementDTO.AddDTO dto) {
        return success(fileManagementService.add(dto));
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
    public ApiResult<Boolean> update(@RequestBody @Validated FileManagementDTO.UpdateDTO dto) {
        return success(fileManagementService.update(dto));
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
     * ids 取值 skuRefId
     */
    @PostMapping("/genQcStandard")
    public ApiResult<List<BatchResultDTO>> genQcStandard(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        List<QcStandardSkuRefEntity> skuRefEntityList = CollUtil.isEmpty(idsDTO.getIds()) ? null : qcStandardSkuRefService.listByIds(idsDTO.getIds());
        if (CollUtil.isEmpty(skuRefEntityList)){
            throw new ServiceException("未找到质检标准关联SKU记录数据");
        }
        List<String> mainIds = skuRefEntityList.stream().map(QcStandardSkuRefEntity::getMainId).distinct().collect(Collectors.toList());
        List<FileManagementEntity> fileManagementEntityList = CollUtil.isEmpty(mainIds) ? null : fileManagementService.listByIds(mainIds);
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
            List<String> skuNoList = skuRefEntityList.stream().filter(e -> e.getMainId().equals(entity.getId())).map(QcStandardSkuRefEntity::getSkuNo).collect(Collectors.toList());
            if (CollUtil.isEmpty(skuNoList)){
                batchResultDTOList.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), "未找到质检标准关联SKU记录数据"));
                continue;
            }
            WmsAttachmentEntity attachmentEntity = attachmentEntityList.stream().filter(e -> e.getId().equals(entity.getFileId())).findFirst().orElse(null);
            if (attachmentEntity == null){
                batchResultDTOList.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), "未找到文件附件数据"));
                continue;
            }
            List<BatchResultDTO> batchResultDTOS = fileManagementService.genQcStandard(skuNoList, attachmentEntity.getAttachUrl());
            batchResultDTOList.addAll(batchResultDTOS);
        }
        return batchResultDTOList.stream().allMatch(BatchResultDTO::getSuccess) ? success(batchResultDTOList) : failure(batchResultDTOList);
    }
    /**
     * 生成单个质检标准
     *
     * @param id 取值 skuRefId
     * @return
     */
    @GetMapping("/genSingleQcStandard")
    public ApiResult<QcStandardDTO.AddDTO> genSingleQcStandard(@RequestParam("id") String id) {
        return success(fileManagementService.genSingleQcStandard(id));
    }
}
