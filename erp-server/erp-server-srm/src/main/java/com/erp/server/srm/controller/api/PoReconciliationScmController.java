package com.erp.server.srm.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.srm.dto.PoReconciliationDTO;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.model.srm.entity.PoReconciliationEntity;
import com.erp.server.srm.query.PoReconciliationDetailScmQueryHandler;
import com.erp.server.srm.query.PoReconciliationScmQueryHandler;
import com.erp.server.srm.service.PoReconciliationScmService;
import com.erp.server.srm.service.PoReconciliationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 采购对账单【scm】
 *
 * @author will
 * @since 2024-01-19
 */
@Slf4j
@RestController
@LogSystemModule("采购对账单")
@RequestMapping("/poReconciliation/scm")
public class PoReconciliationScmController extends BaseController {

    @Resource
    private PoReconciliationScmService poReconciliationScmService;


    /**
     * 分页查询
     * @author Will
     * @date: 2024/1/20 12:11
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "srm:poReconciliation:scm:paging",
            tableAlias = "pr"
    )
    @WebAdvanceQuery(handler = PoReconciliationScmQueryHandler.class)
    public ApiResult<PagingVO<PoReconciliationDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<PoReconciliationDTO.PagingParamDTO> dto) {
        return success(poReconciliationScmService.paging(dto));
    }

    /**
     * 获取状态统计
     * @author Will
     * @date: 2024/1/23 15:59
     * @param dto
     * @return ApiResult<List<TabListDTO>>
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "srm:poReconciliation:scm:paging",
            tableAlias = "pr"
    )
    public ApiResult<List<PoReconciliationDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(poReconciliationScmService.tabList(dto));
    }

    /**
     * 修改
     * @author will
     * @date:  2024-01-19
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "采购对账单修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "srm:poReconciliation:scm:update",
            serviceClass = PoReconciliationService.class,
            keyIdName = "id")
    public ApiResult<Object> update(@RequestBody @Validated PoReconciliationDTO.ScmUpdateDTO dto) {
        poReconciliationScmService.update(dto);
        return success();
    }

    /**
     * 查看详情（对账单主表）
     * @author Will
     * @date: 2024/1/23 15:08
     * @param dto
     * @return ApiResult<ViewDTO>
     */
    @PostMapping("/viewMain")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "srm:poReconciliation:scm:viewMain",
            serviceClass = PoReconciliationService.class,
            keyIdName = "id")
    public ApiResult<PoReconciliationDTO.ViewDTO> viewMain(@RequestBody BaseIdDTO dto) {
        PoReconciliationDTO.ViewDTO viewDTO = poReconciliationScmService.viewMain(dto.getId());
        return success(viewDTO);
    }

    /**
     * 查看详情（对账单明细）传reconciliationDetail，值为对账单id
     * @author Will
     * @date: 2024/1/23 15:08
     * @param dto
     * @return ApiResult<ViewDTO>
     */
    @PostMapping("/viewDetail")
    @WebAdvanceQuery(handler = PoReconciliationDetailScmQueryHandler.class)
    public ApiResult<List<PoReconciliationDetailDTO.ViewDTO>> viewDetail(@RequestBody @Validated PoReconciliationDetailDTO.PagingParamDTO dto) {
        List<PoReconciliationDetailDTO.ViewDTO> list = poReconciliationScmService.viewDetail(dto);
        return success(list);
    }

    /**
     * 导出Excel
     * @author Will
     * @date: 2024/1/20 12:03
     * @param dto
     */
    @PostMapping("/export")
    @LogAction(value = LogActionEnum.EXPORT, desc = "采购对账单导出Excel数据")
    public ApiResult<Boolean> exportList(@RequestBody @Validated PoReconciliationDTO.PagingParamDTO dto) {
        poReconciliationScmService.exportList(dto);
        return success(true);
    }

    /**
     * 导出对账单
     * @author Will
     * @date: 2024/1/20 12:03
     * @param dto
     */
    @PostMapping("/exportPoReconciliation")
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出对账单数据")
    @WebAdvanceQuery(handler = PoReconciliationScmQueryHandler.class)
    public void exportPoReconciliation(@RequestBody @Validated PoReconciliationDTO.PagingParamDTO dto, HttpServletResponse response) {
        poReconciliationScmService.exportPoReconciliation(dto, response);
    }

    /**
     * 采方确认
     * @author Will
     * @date: 2024/1/23 11:48
     * @param dto
     * @return ApiResult<Object>
     */
    @PostMapping("/confirm")
    @LogAction(value = LogActionEnum.CONFIRM, desc = "采方确认")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "srm:poReconciliation:scm:confirm",
            serviceClass = PoReconciliationService.class,
            keyIdName = "ids")
    public ApiResult<Object> confirm(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = poReconciliationScmService.confirm(id);
            }catch (Exception e){
                log.error("对账单 采方确认失败",e);
                PoReconciliationEntity entity = poReconciliationScmService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "对账单不存在, 采方确认失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 取消确认
     * @author Will
     * @date: 2024/1/23 11:48
     * @param dto
     * @return ApiResult<Object>
     */
    @PostMapping("/cancelConfirm")
    @LogAction(value = LogActionEnum.CONFIRM, desc = "取消确认")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "srm:poReconciliation:scm:cancelConfirm",
            serviceClass = PoReconciliationService.class,
            keyIdName = "ids")
    public ApiResult<Object> cancelConfirm(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = poReconciliationScmService.cancelConfirm(id);
            }catch (Exception e){
                log.error("对账单 取消确认失败",e);
                PoReconciliationEntity entity = poReconciliationScmService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "对账单不存在, 取消确认失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 删除
     * @author Will
     * @date: 2024/1/23 14:16
     * @param dto
     * @return ApiResult<Object>
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.DELETE, desc = "删除")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "srm:poReconciliation:scm:delete",
            serviceClass = PoReconciliationService.class,
            keyIdName = "ids")
    public ApiResult<Object> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = poReconciliationScmService.delete(id);
            }catch (Exception e){
                log.error("对账单 删除失败",e);
                PoReconciliationEntity entity = poReconciliationScmService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "对账单不存在, 删除失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 单据签收
     * @author Will
     * @date: 2024/1/23 14:16
     * @param dto
     * @return ApiResult<Object>
     */
    @PostMapping("/receive")
    @LogAction(value = LogActionEnum.RECEIVE, desc = "单据签收")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "srm:poReconciliation:scm:receive",
            serviceClass = PoReconciliationService.class,
            keyIdName = "ids")
    public ApiResult<Object> receive(@RequestBody @Validated BaseIdsDTO.DateDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = poReconciliationScmService.receive(id,dto.getBillDate());
            }catch (Exception e){
                log.error("对账单 单据签收失败",e);
                PoReconciliationEntity entity = poReconciliationScmService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "对账单不存在, 单据签收失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 取消单据签收
     * @author Will
     * @date: 2024/1/23 14:16
     * @param dto
     * @return ApiResult<Object>
     */
    @PostMapping("/cancelReceive")
    @LogAction(value = LogActionEnum.RECEIVE, desc = "取消单据签收")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "srm:poReconciliation:scm:cancelReceive",
            serviceClass = PoReconciliationService.class,
            keyIdName = "ids")
    public ApiResult<Object> cancelReceive(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = poReconciliationScmService.cancelReceive(id);
            }catch (Exception e){
                log.error("对账单 取消单据签收失败",e);
                PoReconciliationEntity entity = poReconciliationScmService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "对账单不存在, 取消单据签收失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 查询待供方确认数据
     * @author Will
     * @date: 2024/1/27 15:15
     * @return ApiResult<List<AddPoReconciliationViewDTO>>
     */
    @PostMapping("/viewToBeSupplierConfirm")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "srm:poReconciliation:scm:paging",
            tableAlias = "pr"
    )
    public ApiResult<List<PoReconciliationDetailDTO.AddPoReconciliationViewDTO>> viewToBeSupplierConfirm(@RequestBody PermissionsDTO dto) {
        return success(poReconciliationScmService.viewToBeSupplierConfirm(dto));
    }

    /**
     * 下载模板
     * @author will
     * @date 2025/6/13 12:11
     * @param response
     * @return ApiResult<Void>
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载模板")
    @GetMapping("/downloadTemplate")
    public ApiResult<Void> downloadTemplate(HttpServletResponse response) {
        poReconciliationScmService.downloadTemplate(response);
        return success();
    }

    /**
     * 明细批量导入
     * @author will
     * @date 2025/6/13 11:52
     * @param excelFile
     * @param response
     * @return ApiResult<ImportDTO>
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "新增明细导入")
    @PostMapping("/importFile")
    public ApiResult<PoReconciliationDetailDTO.ImportDTO> importFile(@RequestParam(value = "excelFile") MultipartFile excelFile,@RequestParam(value = "id") String id, HttpServletResponse response) {
        PoReconciliationDetailDTO.ImportDTO result = poReconciliationScmService.importFile(excelFile,id, response);
        return success(result);
    }


    /**
     * 采购对账单-导出明细Excel
     * @author will
     * @date 2025/8/12 16:19
     * @param dto
     * @param response
     * @return ApiResult<Boolean>
     */
    @PostMapping("/exportDetailList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "srm:poReconciliation:scm:paging",
            tableAlias = "pr"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "采购对账单-导出明细Excel数据")
    @WebAdvanceQuery(handler = PoReconciliationScmQueryHandler.class)
    public ApiResult<Boolean> exportDetailList(@RequestBody @Validated PoReconciliationDTO.PagingParamDTO dto, HttpServletResponse response) {
        return success(poReconciliationScmService.exportDetailList(dto,response));
    }


}
