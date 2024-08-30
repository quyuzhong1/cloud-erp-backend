package com.erp.server.wms.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseApproveParamDTO;
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
import com.erp.model.wms.dto.inventory.InitStockDTO;
import com.erp.model.wms.dto.inventory.InitStockDetailDTO;
import com.erp.model.wms.entity.InitStockEntity;
import com.erp.model.wms.entity.MachineInfoEntity;
import com.erp.server.wms.service.InitStockService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 期初库存管理
 * @author zhangchunlin
 * @since 2023-05-11
 */
@Slf4j
@AllArgsConstructor
@RestController
@LogSystemModule("期初库存")
@RequestMapping(value = "/initStock")
public class InitStockController extends BaseController {

    private final InitStockService initStockService;

    /**
     * 分页列表
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:initStock:paging",
            tableAlias = "ism"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<InitStockDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<InitStockDTO.SearchParamDTO> dto) {
        return success(initStockService.paging(dto));
    }

    /**
     * 新增
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增期初库存")
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:initStock:add",
            serviceClass = InitStockService.class,
            keyIdName = "id")
    public ApiResult<Void> add(@RequestBody @Validated InitStockDTO.AddDTO dto) {
        initStockService.add(dto);
        return  success();
    }

    /**
     * 修改
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改期初库存")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:initStock:update",
            serviceClass = InitStockService.class,
            keyIdName = "id")
    public ApiResult<Void> update(@RequestBody @Validated InitStockDTO.UpdateDTO dto) {
        initStockService.update(dto);
        return  success();
    }

    /**
     * 新增并提交
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交期初库存")
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:initStock:add",
            serviceClass = InitStockService.class,
            keyIdName = "id")
    public ApiResult<Void> addAndSubmit(@RequestBody @Validated InitStockDTO.AddDTO dto) {
        initStockService.addAndSubmit(dto);
        return  success();
    }

    /**
     * 修改并提交
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交期初库存")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:initStock:update",
            serviceClass = InitStockService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated InitStockDTO.UpdateDTO dto) {
        initStockService.updateAndSubmit(dto);
        return  success();
    }


    /**
     * 详情
     * @param id
     * @return
     */
    @LogViewService
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:initStock:view",
            serviceClass = InitStockService.class,
            keyIdName = "id")
    public ApiResult<InitStockDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(initStockService.view(id));
    }

    /**
     * 提交审核
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交初期库存")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:initStock:submit",
            serviceClass = InitStockService.class,
            keyIdName = "ids")
    public ApiResult<Void> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        initStockService.submit(dto.getIds());
        return  success();
    }

    /**
     * 审核
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核初期库存")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:initStock:approve",
            serviceClass = InitStockService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<InitStockEntity> entityList = initStockService.listByIds(ids);
        for (String id : ids) {
            InitStockEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"初期库存记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(initStockService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess()));
            }catch (Exception e){
                log.error("初期库存审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 反审核
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核初期库存")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:initStock:disApprove",
            serviceClass = InitStockService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<InitStockEntity> entityList = initStockService.listByIds(dto.getIds());
        for (String id : ids) {
            InitStockEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"初期库存记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(initStockService.disApprove(entity));
            }catch (Exception e){
                log.error("初期库存反审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 撤销
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销初期库存")
    @PostMapping("/cancel")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:initStock:cancel",
            serviceClass = InitStockService.class,
            keyIdName = "ids")
    public ApiResult cancel(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        initStockService.cancel(dto.getIds());
        return  success();
    }

    /**
     * 作废
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INVALID, desc = "作废初期库存")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:initStock:invalid",
            serviceClass = InitStockService.class,
            keyIdName = "ids")
    public ApiResult<Void> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        initStockService.invalid(dto.getIds(), dto.getRemark());
        return success();
    }

    /**
     * 删除
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除初期库存")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:initStock:delete",
            serviceClass = InitStockService.class,
            keyIdName = "ids")
    public ApiResult delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        initStockService.delete(dto.getIds());
        return success();
    }

    /**
     * 导出
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出初期库存")
    @PostMapping(value = "/exportExcel")
    public ApiResult<Boolean> exportExcel(@RequestBody InitStockDTO.ExportSearchParamDTO dto) {
        initStockService.exportExcel(dto);
        return success(true);
    }

    /**
     * 下载模板
     * @param response
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载模板初期库存")
    @GetMapping("/exportExcelTemplate")
    public ApiResult exportTemplate(HttpServletResponse response) {
        initStockService.downloadTemplate(response);
        return success();
    }

    /**
     * 导入
     * @param file
     * @param response
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导入初期库存")
    @PostMapping("/importFile")
    public ApiResult<InitStockDetailDTO.ImportDTO> importFile(@RequestParam("excelFile") MultipartFile file, HttpServletResponse response) {
        return success(initStockService.importFile(file, response));
    }

}