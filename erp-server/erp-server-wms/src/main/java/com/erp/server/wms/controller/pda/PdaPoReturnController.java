package com.erp.server.wms.controller.pda;

import cn.hutool.core.text.CharSequenceUtil;
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
import com.erp.model.wms.entity.PoReturnDetailEntity;
import com.erp.model.wms.entity.PoReturnEntity;
import com.erp.server.wms.service.PoReturnDetailService;
import com.erp.server.wms.service.PoReturnService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * PDA:采购退货单
 * @author Luo_WG
 * @since 2023-04-07
 */
@Slf4j
@RestController
@LogSystemModule("PDA采购退货单")
@RequestMapping("/pdaPoReturn")
public class PdaPoReturnController extends BaseController {
    @Resource
    private PoReturnService poReturnService;
    @Resource
    private PoReturnDetailService poReturnDetailService;
    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.WarehouseReceiveDTO.PagingViewDTO>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "return_user_id",
            menuCode = "wms:pdaPoReturn:paging",
            tableAlias = "pro"
    )
    public ApiResult<PagingVO<PurchaseReturnOrderDTO.PdaPagingViewDTO>> paging(@RequestBody @Validated PagingDTO<PurchaseReturnOrderDTO.PdaPagingParamDTO> dto) {
        PagingVO<PurchaseReturnOrderDTO.PdaPagingViewDTO> pagingVO = poReturnService.pdaPaging(dto);
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
            tableField = "return_user_id",
            menuCode = "wms:pdaPoReturn:paging",
            tableAlias = "pro")
    public ApiResult<List<PurchaseReturnOrderDTO.PdaReturnOrderCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<PurchaseReturnOrderDTO.PdaReturnOrderCountDTO> warehouseReceiveCountDTOS = poReturnService.pdaListCount(dto);
        return success(warehouseReceiveCountDTOS);
    }

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "新增采购退货单")
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated PurchaseReturnOrderDTO.AddDTO dto) {
        String id = poReturnService.pdaAdd(dto);
        return CharSequenceUtil.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改采购退货单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,return_user_id",
            menuCode = "wms:pdaPoReturn:update",
            serviceClass = PoReturnService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated PurchaseReturnOrderDTO.UpdateDTO dto) {
        Boolean flag = poReturnService.pdaUpdate(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 查询详情
     * @Author Luo_WG
     * @Date 2023/4/6 18:57
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.WarehouseReceiveDTO.ViewDTO>
     **/
    @LogViewService
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,return_user_id",
            menuCode = "wms:pdaPoReturn:view",
            serviceClass = PoReturnService.class,
            keyIdName = "id")
    public ApiResult<PurchaseReturnOrderDTO.ViewDTO> view(@Param("id") String id) {
        PurchaseReturnOrderDTO.ViewDTO dto = poReturnService.view(id);
        return success(dto);
    }

    /**
     * 提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交采购退货单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,return_user_id",
            menuCode = "wms:pdaPoReturn:submit",
            serviceClass = PoReturnService.class,
            keyIdName = "ids")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = poReturnService.submit(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 新增提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交采购退货单")
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,return_user_id",
            menuCode = "wms:pdaPoReturn:add",
            serviceClass = PoReturnService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated PurchaseReturnOrderDTO.AddDTO dto) {
        Boolean flag = poReturnService.pdaAddAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交采购退货单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,return_user_id",
            menuCode = "wms:pdaPoReturn:update",
            serviceClass = PoReturnService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated PurchaseReturnOrderDTO.UpdateDTO dto) {
        Boolean flag = poReturnService.pdaUpdateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核采购退货单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,return_user_id",
            menuCode = "wms:pdaPoReturn:approve",
            serviceClass = PoReturnService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<PoReturnEntity> entityList = poReturnService.listByIds(dto.getIds());
        List<PoReturnDetailEntity> poReturnDetailList = poReturnDetailService.listByMainIds(dto.getIds());
        for (String id : dto.getIds()) {
            PoReturnEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购退货单记录不存在"));
                continue;
            }
            List<PoReturnDetailEntity> detailEntityList = poReturnDetailList.stream().filter(e -> e.getMainId().equals(id)).collect(Collectors.toList());
            try {
                resultDTOS.add(poReturnService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess(),detailEntityList));
            }catch (Exception e){
                log.error("采购退货单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 批量反审核审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核采购退货单")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,return_user_id",
            menuCode = "wms:pdaPoReturn:disApprove",
            serviceClass = PoReturnService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<PoReturnEntity> entityList = poReturnService.listByIds(dto.getIds());
        List<PoReturnDetailEntity> detailList = poReturnDetailService.listByMainIds(dto.getIds());
        for (String id : dto.getIds()) {
            PoReturnEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购退货单记录不存在"));
                continue;
            }
            List<PoReturnDetailEntity> detailEntityList = detailList.stream().filter(e -> id.equals(e.getMainId())).collect(Collectors.toList());
            try {
                resultDTOS.add(poReturnService.disApprove(entity, detailEntityList));
            }catch (Exception e){
                log.error("采购退货单反审核失败",e);
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
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销采购退货单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,return_user_id",
            menuCode = "wms:pdaPoReturn:cancelProcess",
            serviceClass = PoReturnService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = poReturnService.cancelProcess(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 批量作废
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param remarkDTO idsDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.INVALID, desc = "作废采购退货单")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,return_user_id",
            menuCode = "wms:pdaPoReturn:invalid",
            serviceClass = PoReturnService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO remarkDTO) {
        Boolean flag = poReturnService.invalid(remarkDTO.getIds(), remarkDTO.getRemark());
        return flag == true ? success() : failure();
    }

    /**
     * 批量删除
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param idsDTO idsDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "删除采购退货单")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,return_user_id",
            menuCode = "wms:pdaPoReturn:delete",
            serviceClass = PoReturnService.class,
            keyIdName = "ids")
    public ApiResult delete(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        Boolean flag = poReturnService.delete(idsDTO.getIds());
        return flag == true ? success() : failure();
    }
}
