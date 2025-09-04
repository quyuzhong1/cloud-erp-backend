package com.erp.server.wms.controller.pda;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.enums.LogActionEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.server.wms.service.SoOutstockService;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * PDA:销售出库单
 * @author Luo_WG
 * @since 2023-04-07
 */
@Slf4j
@RestController
@LogSystemModule("PDA销售出库单")
@RequestMapping("/pdaSoOutstock")
public class PdaSoOutstockController extends BaseController {

    @Resource
    private SoOutstockService soOutstockService;

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/8/22 11:32
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoOutstockDTO.PdaPagingViewDTO>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "so.warehouse_id",
            menuCode = "wms:pdaSoOutstock:paging",
            tableAlias = "so"
    )
    public ApiResult<PagingVO<SoOutstockDTO.PdaPagingViewDTO>> paging(@RequestBody @Validated PagingDTO<SoOutstockDTO.PdaPagingParamDTO> dto) {
        PagingVO<SoOutstockDTO.PdaPagingViewDTO> pagingVO = soOutstockService.pdaPaging(dto);
        return success(pagingVO);
    }

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/4/17 13:14
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.WarehouseReceiveDTO.WarehouseReceiveCountDTO>>
     **/
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "so.warehouse_id",
            menuCode = "wms:pdaSoOutstock:paging",
            tableAlias = "so"
    )
    public ApiResult<List<SoOutstockDTO.PdaCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<SoOutstockDTO.PdaCountDTO> warehouseReceiveCountDTOS = soOutstockService.pdaListCount(dto);
        return success(warehouseReceiveCountDTOS);
    }

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/8/22 15:16
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "新增销售出库单")
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated SoOutstockDTO.AddDTO dto) {
        String id = soOutstockService.pdaAdd(dto);
        return CharSequenceUtil.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/8/22 15:19
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改销售出库单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaSoOutstock:update",
            serviceClass = SoOutstockService.class,
            keyIdName = "id"
    )
    public ApiResult update(@RequestBody @Validated SoOutstockDTO.UpdateDTO dto) {
        String id = soOutstockService.pdaUpdate(dto);
        return CharSequenceUtil.isNotBlank(id) ? success() : failure();
    }

    /**
     * 详情
     * @Author Luo_WG
     * @Date 2023/8/22 15:19
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.SoOutstockDTO.ViewDTO>
     **/
    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaSoOutstock:view",
            serviceClass = SoOutstockService.class,
            keyIdName = "id"
    )
    public ApiResult<SoOutstockDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        SoOutstockDTO.ViewDTO view = soOutstockService.view(dto.getId());
        return success(view);
    }

    /**
     * 批量提交
     * @Author Luo_WG
     * @Date 2023/8/22 15:19
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交销售出库单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaSoOutstock:submit",
            serviceClass = SoOutstockService.class,
            keyIdName = "ids"
    )
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = soOutstockService.submit(dto.getIds(),Boolean.TRUE);
        return result ? success() : failure();
    }

    /**
     * 新增并提交
     * @Author Luo_WG
     * @Date 2023/8/22 15:20
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交销售出库单")
    @PostMapping("/addAndSubmit")
    public ApiResult addAndSubmit(@RequestBody @Validated SoOutstockDTO.AddDTO dto) {
        Boolean result = soOutstockService.pdaAddAndSubmit(dto);
        return result ? success() : failure();
    }

    /**
     * 修改并提交
     * @Author Luo_WG
     * @Date 2023/8/22 15:47
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交销售出库单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaSoOutstock:update",
            serviceClass = SoOutstockService.class,
            keyIdName = "id"
    )
    public ApiResult updateAndSubmit(@RequestBody @Validated SoOutstockDTO.UpdateDTO dto) {
        Boolean result = soOutstockService.pdaUpdateAndSubmit(dto);
        return result ? success() : failure();
    }

    /**
     * 审核
     * @Author Luo_WG
     * @Date 2023/8/22 15:47
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核销售出库单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaSoOutstock:approve",
            serviceClass = SoOutstockService.class,
            keyIdName = "ids"
    )
    public ApiResult approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<String> ids = dto.getIds();
        for (String id : ids) {

            BatchResultDTO result;
            try {
                result = soOutstockService.approve(new ApproveOneDTO(id, dto.getType(), dto.getComment()),Boolean.TRUE);
            } catch (Exception e) {
                log.error("销售出库 审核失败>>>>{}", e);
                SoOutstockEntity entity = soOutstockService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "销售出库单不存在, 审核失败");
                    resultDTOS.add(result);
                    continue;
                }
                String message = e.getMessage();
                result = BatchResultDTO.fail(entity.getId(), entity.getCode(), message);

            }
            resultDTOS.add(result);
        }

        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success() : failure();
    }

    /**
     * 反审核
     * @Author Luo_WG
     * @Date 2023/8/22 15:47
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核销售出库单")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaSoOutstock:disApprove",
            serviceClass = SoOutstockService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoOutstockEntity> entityList = soOutstockService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SoOutstockEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"销售出库单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(soOutstockService.disApprove(entity, Boolean.TRUE));
            }catch (Exception e){
                log.error("销售出库单反审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 撤销流程
     * @Author Luo_WG
     * @Date 2023/8/22 15:48
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销销售出库单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaSoOutstock:cancelProcess",
            serviceClass = SoOutstockService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = soOutstockService.cancelProcess(dto.getIds());
        return result ? success() : failure();
    }

    /**
     * 删除
     * @Author Luo_WG
     * @Date 2023/8/22 15:48
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "删除销售出库单")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaSoOutstock:delete",
            serviceClass = SoOutstockService.class,
            keyIdName = "ids")
    public ApiResult delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean result = soOutstockService.delete(dto.getIds());
        return result ? success() : failure();
    }

    /**
     * 作废
     * @Author Luo_WG
     * @Date 2023/8/22 15:48
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.INVALID, desc = "作废销售出库单")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaSoOutstock:invalid",
            serviceClass = SoOutstockService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean result = soOutstockService.invalid(dto.getIds(), dto.getRemark());
        return result ? success() : failure();
    }
}
