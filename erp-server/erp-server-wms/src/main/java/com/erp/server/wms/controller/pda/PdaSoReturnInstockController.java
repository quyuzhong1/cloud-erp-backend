package com.erp.server.wms.controller.pda;


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
import com.erp.model.wms.dto.SoReturnInstockDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import com.erp.server.wms.service.SoReturnInstockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * PDA:销售退货入库单
 * @author Luo_WG
 * @since 2023-05-10
 */
@Slf4j
@RestController
@LogSystemModule("PDA销售退货入库单")
@RequestMapping("/pdaSoReturnInstock")
public class PdaSoReturnInstockController extends BaseController {

    @Resource
    private SoReturnInstockService soReturnInstockService;

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/8/17 16:37
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoReturnInstockDTO.PdaPagingView>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:pdaSoReturnInstock:paging",
            tableAlias = "sri"
    )
    public ApiResult<PagingVO<SoReturnInstockDTO.PdaPagingView>> paging(@RequestBody @Validated PagingDTO<SoReturnInstockDTO.PdaPagingParam> dto) {
        PagingVO<SoReturnInstockDTO.PdaPagingView> pagingVO = soReturnInstockService.PdaPaging(dto);
        return success(pagingVO);
    }

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/8/17 18:47
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.SoReturnInstockDTO.PdaSoReturnInstockCountDTO>>
     **/
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:pdaSoReturnInstock:paging",
            tableAlias = "sri"
    )
    public ApiResult<List<SoReturnInstockDTO.PdaSoReturnInstockCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<SoReturnInstockDTO.PdaSoReturnInstockCountDTO> soReturnInstockCountDTOs = soReturnInstockService.pdaListCount(dto);
        return success(soReturnInstockCountDTOs);
    }

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "新增销售退货入库单库单")
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated SoReturnInstockDTO.Add dto) {
        String id = soReturnInstockService.pdaAdd(dto);
        return StringUtils.isNotBlank(id) == true ? success() : failure();
    }

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改销售退货入库单库单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaSoReturnInstock:update",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated SoReturnInstockDTO.Update dto) {
        Boolean flag = soReturnInstockService.pdaUpdate(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 查询详情
     * @Author Luo_WG
     * @Date 2023/4/6 18:57
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.SoReturnInstockDTO.ViewDTO>
     **/
    @LogViewService
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaSoReturnInstock:view",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "id")
    public ApiResult<SoReturnInstockDTO.View> view(@RequestParam("id") String id) {
        SoReturnInstockDTO.View dto = soReturnInstockService.pdaView(id);
        return success(dto);
    }

    /**
     * 提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交销售退货入库单库单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaSoReturnInstock:submit",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "ids")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soReturnInstockService.submit(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 新增提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交销售退货入库单库单")
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaSoReturnInstock:add",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated SoReturnInstockDTO.Add dto) {
        Boolean flag = soReturnInstockService.pdaAddAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交销售退货入库单库单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaSoReturnInstock:update",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated SoReturnInstockDTO.Update dto) {
        Boolean flag = soReturnInstockService.pdaUpdateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核销售退货入库单库单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaSoReturnInstock:approve",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoReturnInstockEntity> entityList = soReturnInstockService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SoReturnInstockEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"退货入库单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(soReturnInstockService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess()));
            }catch (Exception e){
                log.error("退货入库单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 批量反审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核销售退货入库单库单")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaSoReturnInstock:disApprove",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoReturnInstockEntity> entityList = soReturnInstockService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SoReturnInstockEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"退货入库单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(soReturnInstockService.disApprove(entity,Boolean.TRUE));
            }catch (Exception e){
                log.error("退货入库单反审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 取消流程
     * @Author Luo_WG
     * @Date 2023/4/13 18:58
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销销售退货入库单库单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaSoReturnInstock:cancelProcess",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soReturnInstockService.cancelProcess(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 批量作废
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param remarkDTO idsDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.INVALID, desc = "作废销售退货入库单库单")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaSoReturnInstock:invalid",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO remarkDTO) {
        Boolean flag = soReturnInstockService.invalid(remarkDTO.getIds(), remarkDTO.getRemark());
        return flag == true ? success() : failure();
    }

    /**
     * 批量删除
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param idsDTO idsDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "删除销售退货入库单库单")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaSoReturnInstock:delete",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "ids")
    public ApiResult delete(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        Boolean flag = soReturnInstockService.delete(idsDTO.getIds());
        return flag == true ? success() : failure();
    }
}
