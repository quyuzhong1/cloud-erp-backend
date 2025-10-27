package com.erp.server.wms.controller.pda;

import cn.hutool.core.text.CharSequenceUtil;
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
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveEntity;
import com.erp.server.wms.service.WarehouseReceiveDetailService;
import com.erp.server.wms.service.WarehouseReceiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * PDA:采购收货单
 * @Author Luo_WG
 * @Date 2023/8/11 10:08
 **/
@Slf4j
@RestController
@LogSystemModule("PDA采购收货单")
@RequestMapping(value = "/pdaPoReceive")
public class PdaPoReceiveController extends BaseController {
    @Resource
    private WarehouseReceiveService warehouseReceiveService;
    @Resource
    private WarehouseReceiveDetailService warehouseReceiveDetailService;
    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/8/11 10:17
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.WarehouseReceiveDTO.PdaPagingViewDTO>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "receive_user_id",
            warehouseTableField = "pr.delivery_warehouse_id",
            menuCode = "wms:pdaPoReceive:paging",
            tableAlias = "pr"
    )
    public ApiResult<PagingVO<WarehouseReceiveDTO.PdaPagingViewDTO>> paging(@RequestBody @Validated PagingDTO<WarehouseReceiveDTO.PdaPagingParamDTO> dto) {
        PagingVO<WarehouseReceiveDTO.PdaPagingViewDTO> pagingVO = warehouseReceiveService.pdaPaging(dto);
        return success(pagingVO);
    }

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/8/11 10:18
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.WarehouseReceiveDTO.PdaPoReceiveCountDTO>>
     **/
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "receive_user_id",
            warehouseTableField = "wr.delivery_warehouse_id",
            menuCode = "wms:pdaPoReceive:paging",
            tableAlias = "wr"
    )
    public ApiResult<List<WarehouseReceiveDTO.PdaPoReceiveCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<WarehouseReceiveDTO.PdaPoReceiveCountDTO> warehouseReceiveCountDTOS = warehouseReceiveService.pdaListCount(dto);
        return success(warehouseReceiveCountDTOS);
    }

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/8/11 10:19
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "新增采购收货单")
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated WarehouseReceiveDTO.AddDTO dto) {
        String id = warehouseReceiveService.pdaAdd(dto);
        return CharSequenceUtil.isNotBlank(id) == true ? success() : failure();
    }

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改采购收货单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:pdaPoReceive:update",
            serviceClass = WarehouseReceiveService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated WarehouseReceiveDTO.UpdateDTO dto) {
        Boolean flag = warehouseReceiveService.pdaUpdate(dto);
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
            menuCode = "wms:pdaPoReceive:view",
            serviceClass = WarehouseReceiveService.class,
            keyIdName = "id")
    public ApiResult<WarehouseReceiveDTO.ViewDTO> view(@RequestParam("id") String id) {
        WarehouseReceiveDTO.ViewDTO dto = warehouseReceiveService.pdaView(id);
        return success(dto);
    }

    /**
     * 提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交采购收货单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:pdaPoReceive:submit",
            serviceClass = WarehouseReceiveService.class,
            keyIdName = "ids")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = warehouseReceiveService.submit(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 新增提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交采购收货单")
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:pdaPoReceive:add",
            serviceClass = WarehouseReceiveService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated WarehouseReceiveDTO.AddDTO dto) {
        Boolean flag = warehouseReceiveService.pdaAddAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交采购收货单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:pdaPoReceive:update",
            serviceClass = WarehouseReceiveService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated WarehouseReceiveDTO.UpdateDTO dto) {
        Boolean flag = warehouseReceiveService.pdaUpdateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核采购收货单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:pdaPoReceive:approve",
            serviceClass = WarehouseReceiveService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<WarehouseReceiveEntity> entityList = warehouseReceiveService.listByIds(dto.getIds());
        List<WarehouseReceiveDetailEntity> receiveDetailList = warehouseReceiveDetailService.listDetailByMainIds(dto.getIds());
        for (String id : dto.getIds()) {
            WarehouseReceiveEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购收货单记录不存在"));
                continue;
            }
            List<WarehouseReceiveDetailEntity> detailEntityList = receiveDetailList.stream().filter(e -> e.getMainId().equals(id)).collect(Collectors.toList());
            try {
                resultDTOS.add(warehouseReceiveService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess(),detailEntityList));
            }catch (Exception e){
                log.error("采购收货单审核失败",e);
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
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核采购收货单")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:pdaPoReceive:disApprove",
            serviceClass = WarehouseReceiveService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<WarehouseReceiveEntity> entityList = warehouseReceiveService.listByIds(dto.getIds());
        List<WarehouseReceiveDetailEntity> receiveDetailList = warehouseReceiveDetailService.listDetailByMainIds(dto.getIds());
        for (String id : dto.getIds()) {
            WarehouseReceiveEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购收货单记录不存在"));
                continue;
            }
            List<WarehouseReceiveDetailEntity> detailEntityList = receiveDetailList.stream().filter(e -> e.getMainId().equals(id)).collect(Collectors.toList());
            try {
                resultDTOS.add(warehouseReceiveService.disApprove(entity,detailEntityList));
            }catch (Exception e){
                log.error("采购收货单反审核失败",e);
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
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销采购收货单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:pdaPoReceive:cancelProcess",
            serviceClass = WarehouseReceiveService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = warehouseReceiveService.cancelProcess(new ApproveDTO.BatchCancelProcessDTO(dto.getIds()));
        return flag == true ? success() : failure();
    }

    /**
     * 批量作废
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param remarkDTO idsDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.INVALID, desc = "作废采购收货单")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:pdaPoReceive:invalid",
            serviceClass = WarehouseReceiveService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO remarkDTO) {
        Boolean flag = warehouseReceiveService.invalid(remarkDTO.getIds(), remarkDTO.getRemark());
        return flag == true ? success() : failure();
    }

    /**
     * 批量删除
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param idsDTO idsDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "删除采购收货单")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:pdaPoReceive:delete",
            serviceClass = WarehouseReceiveService.class,
            keyIdName = "ids")
    public ApiResult delete(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        Boolean flag = warehouseReceiveService.delete(idsDTO.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 条件查询收货单
     * @Author Luo_WG
     * @Date 2023/8/18 11:07
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<WarehouseReceiveDTO.PdaPoReceive>>
     **/
    @PostMapping("/pdaList")
    public ApiResult<List<WarehouseReceiveDTO.PdaPoReceive>> pdaList(@RequestBody WarehouseReceiveDTO.PdaPoReceiveParam dto) {
        List<WarehouseReceiveDTO.PdaPoReceive> list = warehouseReceiveService.pdaList(dto);
        return success(list);
    }

    /**
     * PDA:待入库查询
     * @Author Luo_WG
     * @Date 2023/8/18 11:07
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<WarehouseReceiveDTO.PdaPoReceive>>
     **/
    @PostMapping("/waitInStockPaging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "pr.delivery_warehouse_id",
            menuCode = "wms:pdaPoReceive:waitInStockPaging"
    )
    public ApiResult<PagingVO<List<WarehouseReceiveDTO.WaitInStockPaging>>> waitInStockPaging(@RequestBody PagingDTO<WarehouseReceiveDTO.WaitInStockPagingParam> dto) {
        PagingVO<List<WarehouseReceiveDTO.WaitInStockPaging>> list = warehouseReceiveService.waitInStockPaging(dto);
        return success(list);
    }

    /**
     * PDA:待入库查询表头数量
     * @Author Luo_WG
     * @Date 2023/9/6 11:38
     * @param dto
     * @return com.common.business.vo.PagingVO<java.util.List<com.erp.model.wms.dto.WarehouseReceiveDTO.WaitInStockPaging>>
     **/
    @PostMapping("/waitInStockListCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "pr.delivery_warehouse_id",
            menuCode = "wms:pdaPoReceive:waitInStockPaging"
    )
    public ApiResult<List<WarehouseReceiveDTO.WaitInStockCountDTO>> waitInStockListCount(@RequestBody PermissionsDTO dto) {
        List<WarehouseReceiveDTO.WaitInStockCountDTO> waitInStockCountDTOS = warehouseReceiveService.waitInStockListCount(dto);
        return success(waitInStockCountDTOS);
    }
}
