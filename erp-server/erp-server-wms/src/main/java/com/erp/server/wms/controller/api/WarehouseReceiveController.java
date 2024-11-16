package com.erp.server.wms.controller.api;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveEntity;
import com.erp.server.wms.service.WarehouseReceiveDetailService;
import com.erp.server.wms.service.WarehouseReceiveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * 采购收货单
 * @Author Luo_WG
 * @Date 2023/4/6 18:56
 **/
@Slf4j
@RestController
@LogSystemModule("采购收货单")
@RequestMapping("/warehouseReceive")
public class WarehouseReceiveController extends BaseController {

    @Resource
    private WarehouseReceiveService warehouseReceiveService;
    @Resource
    private WarehouseReceiveDetailService warehouseReceiveDetailService;

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.WarehouseReceiveDTO.PagingViewDTO>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "receive_user_id",
            menuCode = "wms:warehouseReceive:paging",
            tableAlias = "wr"
    )
    public ApiResult<PagingVO<WarehouseReceiveDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<WarehouseReceiveDTO.PagingParamDTO> dto) {
        PagingVO<WarehouseReceiveDTO.PagingViewDTO> pagingVO = warehouseReceiveService.paging(dto);
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
            tableField = "receive_user_id",
            menuCode = "wms:warehouseReceive:paging",
            tableAlias = "wr"
    )
    public ApiResult<List<WarehouseReceiveDTO.WarehouseReceiveCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<WarehouseReceiveDTO.WarehouseReceiveCountDTO> warehouseReceiveCountDTOS = warehouseReceiveService.listCount(dto);
        return success(warehouseReceiveCountDTOS);
    }

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "新增采购收货单")
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated WarehouseReceiveDTO.AddDTO dto) {
        String id = warehouseReceiveService.add(dto);
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
            menuCode = "wms:warehouseReceive:update",
            serviceClass = WarehouseReceiveService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated WarehouseReceiveDTO.UpdateDTO dto) {
        Boolean flag = warehouseReceiveService.update(dto);
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
            tableField = "receive_user_id",
            menuCode = "wms:warehouseReceive:view",
            serviceClass = WarehouseReceiveService.class,
            keyIdName = "id")
    public ApiResult<WarehouseReceiveDTO.ViewDTO> view(@RequestParam("id") String id) {
        WarehouseReceiveDTO.ViewDTO dto = warehouseReceiveService.view(id);
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
            menuCode = "wms:warehouseReceive:submit",
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
            menuCode = "wms:warehouseReceive:add",
            serviceClass = WarehouseReceiveService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated WarehouseReceiveDTO.AddDTO dto) {
        Boolean flag = warehouseReceiveService.addAndSubmit(dto);
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
            menuCode = "wms:warehouseReceive:update",
            serviceClass = WarehouseReceiveService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated WarehouseReceiveDTO.UpdateDTO dto) {
        Boolean flag = warehouseReceiveService.updateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     * @param dto baseApproveParamDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核采购收货单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:warehouseReceive:approve",
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
            menuCode = "wms:warehouseReceive:disApprove",
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
            menuCode = "wms:warehouseReceive:cancelProcess",
            serviceClass = WarehouseReceiveService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = warehouseReceiveService.cancelProcess(dto.getIds());
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
            menuCode = "wms:warehouseReceive:invalid",
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
            menuCode = "wms:warehouseReceive:delete",
            serviceClass = WarehouseReceiveService.class,
            keyIdName = "ids")
    public ApiResult delete(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        Boolean flag = warehouseReceiveService.delete(idsDTO.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 导出
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出采购收货单")
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody WarehouseReceiveDTO.PagingParamDTO dto) {
        Boolean flag = warehouseReceiveService.exportExcel(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 下推入库单列表查询
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping(value = "/generateStockInView")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:warehouseReceive:generateStockIn",
            serviceClass = WarehouseReceiveService.class,
            keyIdName = "ids")
    public ApiResult<List<WarehouseReceiveDTO.GenerateStockInViewDTO>> generateStockInView(@RequestBody BaseIdsDTO.IdsDTO dto) {
        List<WarehouseReceiveDTO.GenerateStockInViewDTO> generateStockInViewDTOS = warehouseReceiveService.generateStockInView(dto.getIds());
        return success(generateStockInViewDTOS);
    }

    /**
     * 下推入库单保存
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @param dtos dtos
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "下推入库单保存")
    @PostMapping(value = "/generateStockIn")
    public ApiResult generateStockIn(@RequestBody WarehouseReceiveDTO.ListGenerateStockInDTO dtos) {
        Boolean flag = warehouseReceiveService.generateStockIn(dtos.getList());
        return flag == true ? success() : failure();
    }

    /**
     * 采购订单-关联的收货单据
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @param purchaseOrderId purchaseOrderId
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping(value = "/purchaseOrderRefReceive")
    public ApiResult<List<WarehouseReceiveDTO.OrderRefReceiveDTO>> purchaseOrderRefReceive(@RequestBody @RequestParam("purchaseOrderId") String purchaseOrderId) {
        List<WarehouseReceiveDTO.OrderRefReceiveDTO> orderRefReceiveDTOS = warehouseReceiveService.purchaseOrderRefReceive(purchaseOrderId);
        return success(orderRefReceiveDTOS);
    }

    /**
     * 下推收货单保存
     * @author Will
     * @date: 2023/3/15 18:26
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "下推收货单保存")
    @PostMapping("/generateReceive")
    public ApiResult generateReceive(@RequestBody @Validated PurchaseOrderDTO.ListGenerateReceiveDTO dto) {
        Boolean flag = warehouseReceiveService.generateReceive(dto);
        return flag == true ? success() : failure();
    }
    /***
     * 清洗入库状态
     * @return
     */
    @PostMapping("/instockStatusCleanJob")
    public ApiResult instockStatusCleanJob(){
        warehouseReceiveService.instockStatusCleanJob();
        return success();
    }
}
