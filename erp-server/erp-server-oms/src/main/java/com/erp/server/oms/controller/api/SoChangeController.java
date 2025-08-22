package com.erp.server.oms.controller.api;


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
import com.erp.model.oms.dto.SoChangeDTO;
import com.erp.model.oms.dto.SoChangeDetailDTO;
import com.erp.model.oms.entity.SoChangeEntity;
import com.erp.model.oms.entity.SoReturnEntity;
import com.erp.server.oms.query.SoChangeQueryHandler;
import com.erp.server.oms.service.SoChangeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 销售管理-销售变更管理
 *
 * @author lambda
 * @since 2023-05-10
 */
@RestController
@LogSystemModule("销售变更单")
@RequestMapping("/soChange")
@Slf4j
public class SoChangeController extends BaseController {

    @Resource
    private SoChangeService soChangeService;

    /**
     * 获取 tab列表
     *
     * @return
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:soChange:paging",
            tableAlias = "sc"
    )
    public ApiResult<List<SoChangeDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<SoChangeDTO.TabListDTO> tabList = soChangeService.tabList(dto);
        return success(tabList);
    }


    /**
     * 分页列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "si.warehouse_id",
            menuCode = "oms:soChange:paging",
            tableAlias = "sc"
    )
    @WebAdvanceQuery(handler = SoChangeQueryHandler.class)
    public ApiResult<PagingVO<SoChangeDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<SoChangeDTO.PagingParamDTO> dto) {
        PagingVO<SoChangeDTO.PagingViewDTO> pagingVO = soChangeService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 根据销售订单详情id 获取到对应销售变更的详情
     *
     * @param
     * @return
     */
    @PostMapping("/getViewBySoDetailIds")
    public ApiResult<SoChangeDTO.ViewDTO> getViewBySoDetailIds(@RequestBody List<String>  soDetailIds) {
        SoChangeDTO.ViewDTO view = soChangeService.getViewBySoDetailIds(soDetailIds);
        return success(view);
    }

    /**
     * 根据销售订单详情id 检测能否变更
     *
     * @param
     * @return
     */
    @PostMapping("/checkBySoDetailIds")
    public ApiResult<List<String>> checkBySoDetailIds(@RequestBody List<String>  soDetailIds) {
        List<String> soDetailIdList = soChangeService.checkBySoDetailIds(soDetailIds);
        return success(soDetailIdList);
    }


    /**
     * 根据销售订单id 获取到对应的产品信息
     *
     * @param soId
     * @return
     */
    @GetMapping("/listSoSkuBySoId")
    public ApiResult<List<SoChangeDetailDTO.SoDetailViewDTO>> listSoSkuBySoId(@RequestParam("soId") String soId) {
        List<SoChangeDetailDTO.SoDetailViewDTO> resultList = soChangeService.listSoSkuBySoId(soId);
        return success(resultList);
    }


    /**
     * 创建
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "创建销售变更单")
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated SoChangeDTO.AddDTO dto) {
        String id = soChangeService.add(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 提交
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交销售变更单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soChange:submit",
            serviceClass = SoChangeService.class,
            keyIdName = "ids"
    )
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = soChangeService.submit(dto.getIds());
        return result ? success() : failure();
    }

    /**
     * 新增并提交
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交销售变更单")
    @PostMapping("/addAndSubmit")
    public ApiResult<Void> addAndSubmit(@RequestBody @Validated SoChangeDTO.AddDTO dto) {
        Boolean result = soChangeService.addAndSubmit(dto);
        return result ? success() : failure();
    }

    /**
     * 详情
     *
     * @param dto
     * @return
     */
    @LogViewService
    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soChange:view",
            serviceClass = SoChangeService.class,
            keyIdName = "id"
    )
    public ApiResult<SoChangeDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        SoChangeDTO.ViewDTO view = soChangeService.view(dto.getId());
        return success(view);
    }


    /**
     * 修改
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改销售变更单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soChange:update",
            serviceClass = SoChangeService.class,
            keyIdName = "id"
    )
    public ApiResult update(@RequestBody @Validated SoChangeDTO.UpdateDTO dto) {
        String id = soChangeService.updateSoChange(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();

    }

    /**
     * 修改并提交
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交销售变更单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soChange:update",
            serviceClass = SoChangeService.class,
            keyIdName = "id"
    )
    public ApiResult updateAndSubmit(@RequestBody @Validated SoChangeDTO.UpdateDTO dto) {
        Boolean result = soChangeService.updateAndSubmit(dto);
        return result ? success() : failure();
    }

    /**
     * 审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核销售变更单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soChange:approve",
            serviceClass = SoChangeService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoChangeEntity> entityList = soChangeService.listByIds(ids);
        for (String id : ids) {
            SoChangeEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"销售变更单不存在"));
                continue;
            }
            try {
                resultDTOS.add(soChangeService.approve(dto, entity));
            }catch (Exception e){
                log.error("销售变更单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 撤销流程
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销销售变更单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soChange:cancelProcess",
            serviceClass = SoChangeService.class,
            keyIdName = "ids"
    )
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = soChangeService.cancelProcess(dto.getIds());
        return result ? success() : failure();
    }


    /**
     * 删除销售变更单
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "批量删除记录")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soChange:delete",
            serviceClass = SoChangeService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOList = soChangeService.deleteByIds(dto.getIds(), true);
        return resultDTOList.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOList) : failure(resultDTOList);
    }

    /**
     * 作废
     *
     * @param dto
     * @return ApiResult
     * @author Will
     * @date: 2023/5/10 20:11
     */
    @LogAction(value = LogActionEnum.INVALID, desc = "作废销售变更单:ids={ids},备注={remark}")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soChange:invalid",
            serviceClass = SoChangeService.class,
            keyIdName = "ids"
    )
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean result = soChangeService.invalid(dto.getIds(), dto.getRemark());
        return result ? success() : failure();
    }

    /**
     * 导出
     * 数据
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出销售变更单")
    @PostMapping("/export")
    public ApiResult exportWarehouse(@RequestBody @Valid SoChangeDTO.PagingParamDTO dto) {
        Boolean result = soChangeService.exportExcel(dto);
        return result ? success() : failure();
    }

    /**
     * 销售订单关联的销售变更单
     *
     * @param soId
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-05-23 18:01
     */
    @GetMapping("/listSoRefSoChange")
    public ApiResult<List<SoChangeDTO.SoRefDTO>> listSoRefSoChange(@RequestParam("soId") String soId) {
        List<SoChangeDTO.SoRefDTO> list = soChangeService.listSoRefSoChangeBySoId(soId);
        return success(list);
    }


}
