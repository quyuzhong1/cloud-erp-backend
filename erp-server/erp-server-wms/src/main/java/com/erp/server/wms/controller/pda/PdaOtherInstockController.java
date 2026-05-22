package com.erp.server.wms.controller.pda;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.OtherInstockDTO;
import com.erp.model.wms.entity.OtherInstockEntity;
import com.erp.server.wms.service.OtherInstockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * PDA:其他入库单
 * @Author Luo_WG
 * @Date 2023/8/10 10:08
 **/
@RestController
@LogSystemModule("PDA其他入库单")
@RequestMapping(value = "/pdaOtherInstock")
@Slf4j
public class PdaOtherInstockController extends BaseController {

    @Resource
    private OtherInstockService otherInstockService;

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/8/23 9:57
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.OtherInstockDTO.PdaListDTO>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "warehouse_keeper_id",
            warehouseTableField = "oi.warehouse_id",
            menuCode = "wms:pdaOtherInstock:paging",
            tableAlias = "oi"
    )
    public ApiResult<PagingVO<OtherInstockDTO.PdaListDTO>> paging(@RequestBody @Validated PagingDTO<OtherInstockDTO.PdaSearchParamDTO> dto) {
        PagingVO<OtherInstockDTO.PdaListDTO> pagingVO = otherInstockService.PdaPaging(dto);
        return success(pagingVO);
    }

    /**
     * 列表数量
     * @Author Luo_WG
     * @Date 2023/8/23 10:43
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.OtherInstockDTO.PdaListStatusCountDTO>> 
     **/
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "warehouse_keeper_id",
            warehouseTableField = "oi.warehouse_id",
            menuCode = "wms:pdaOtherInstock:paging",
            tableAlias = "oi"
    )
    public ApiResult<List<OtherInstockDTO.PdaListStatusCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<OtherInstockDTO.PdaListStatusCountDTO> list = otherInstockService.pdaListCount(dto);
        return success(list);
    }

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/8/23 10:43
     * @param dto
     * @return com.common.core.controller.vo.ApiResult 
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "新增其他入库单")
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:pdaOtherInstock:add",
            serviceClass = OtherInstockService.class,
            keyIdName = "id")
    public ApiResult add(@RequestBody @Validated OtherInstockDTO.AddDTO dto) {
        String id = otherInstockService.add(dto);
        return CharSequenceUtil.isNotBlank(id) ? success() : failure();
    }

    /**
     * 新增并提交
     * @author Luo_WG
     * @date: 2023/5/10 19:59
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交其他入库单")
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:pdaOtherInstock:add",
            serviceClass = OtherInstockService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated OtherInstockDTO.AddDTO dto) {
        String id = otherInstockService.addAndSubmit(dto);
        return CharSequenceUtil.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改
     * @author Luo_WG
     * @date: 2023/5/10 20:02
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改其他入库单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:pdaOtherInstock:update",
            serviceClass = OtherInstockService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated OtherInstockDTO.UpdateDTO dto) {
        Boolean flag = otherInstockService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改并提交
     * @author Luo_WG
     * @date: 2023/5/10 20:02
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交其他入库单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:pdaOtherInstock:update",
            serviceClass = OtherInstockService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated OtherInstockDTO.UpdateDTO dto) {
        Boolean flag = otherInstockService.updateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 提交
     * @author Luo_WG
     * @date: 2023/5/10 20:00
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交其他入库单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:pdaOtherInstock:submit",
            serviceClass = OtherInstockService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = otherInstockService.submit(id, Boolean.TRUE);
            } catch (Exception e) {
                log.error("其他入库单 提交审核失败", e);
                OtherInstockEntity entity = otherInstockService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "其他入库单不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 查看详情
     * @author Luo_WG
     * @date: 2023/5/10 20:10
     * @param id
     * @return ApiResult
     */
    @LogViewService
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:pdaOtherInstock:view",
            serviceClass = OtherInstockService.class,
            keyIdName = "id")
    public ApiResult<OtherInstockDTO.ViewDTO> view(@RequestParam("id") String id) {
        OtherInstockDTO.ViewDTO dto = otherInstockService.view(id);
        return success(dto);
    }


    /**
     * 删除
     * @author Luo_WG
     * @date: 2023/5/10 20:09
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除其他入库单")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:pdaOtherInstock:delete",
            serviceClass = OtherInstockService.class,
            keyIdName = "ids")
    public ApiResult delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = otherInstockService.delete(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 作废
     * @author Luo_WG
     * @date: 2023/5/10 20:11
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INVALID, desc = "作废其他入库单")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:pdaOtherInstock:invalid",
            serviceClass = OtherInstockService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean flag = otherInstockService.invalid(dto.getIds(),dto.getRemark());
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @author Luo_WG
     * @date: 2023/5/10 20:11
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核其他入库单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:pdaOtherInstock:approve",
            serviceClass = OtherInstockService.class,
            keyIdName = "ids")
    public ApiResult approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<String> ids = dto.getIds();
        for (String id : ids) {
            BatchResultDTO resultDTO;
            String flagCode = id;
            try {
                OtherInstockEntity entity = otherInstockService.getById(id);
                if (Objects.isNull(entity)) {
                    resultDTO = BatchResultDTO.fail(id,flagCode, "其他入库单不存在");
                } else {
                    flagCode = entity.getCode();
                    resultDTO = otherInstockService.approve(id,dto.getType(),dto.getComment(), true);
                }
            } catch (Exception e) {
                log.error("其他入库单审核失败>>>>{}", e);
                resultDTO = BatchResultDTO.fail(id,flagCode, e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 批量反审核
     * @author Luo_WG
     * @date: 2023/5/10 20:12
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核其他入库单")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:pdaOtherInstock:disApprove",
            serviceClass = OtherInstockService.class,
            keyIdName = "ids")
    public ApiResult disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<String> ids = dto.getIds();
        for (String id : ids) {
            BatchResultDTO resultDTO;
            String flagCode = id;
            try {
                OtherInstockEntity entity = otherInstockService.getById(id);
                if (Objects.isNull(entity)) {
                    resultDTO = BatchResultDTO.fail(id,flagCode, "其他入库单不存在");
                } else {
                    flagCode = entity.getCode();
                    resultDTO = otherInstockService.disApprove(id, true);
                }
            } catch (Exception e) {
                log.error("其他入库单反审核失败>>>>{}", e);
                resultDTO = BatchResultDTO.fail(id,flagCode, e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 取消流程
     * @author Luo_WG
     * @date: 2023/5/10 20:24
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销其他入库单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:pdaOtherInstock:cancelProcess",
            serviceClass = OtherInstockService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = otherInstockService.cancelProcess(new ApproveDTO.BatchCancelProcessDTO(dto.getIds()));
        return result == true ? success() : failure();
    }
}
