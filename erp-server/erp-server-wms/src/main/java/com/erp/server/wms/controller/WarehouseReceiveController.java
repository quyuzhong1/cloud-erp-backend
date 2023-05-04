package com.erp.server.wms.controller;

import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.server.wms.service.PurchaseReturnOrderService;
import com.erp.server.wms.service.WarehouseReceiveService;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.common.core.controller.BaseController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * 采购收货单
 * @Author Luo_WG
 * @Date 2023/4/6 18:56
 **/
@RestController
@RequestMapping("/warehouseReceive")
public class WarehouseReceiveController extends BaseController {

    @Resource
    private WarehouseReceiveService warehouseReceiveService;

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
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated WarehouseReceiveDTO.AddDTO dto) {
        String id = warehouseReceiveService.add(dto);
        return StringUtils.isNotBlank(id) == true ? success() : failure();
    }

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/update")
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
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "receive_user_id",
            menuCode = "scm:warehouseReceive:view",
            serviceClass = PurchaseReturnOrderService.class,
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
    @PostMapping("/submit")
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
    @PostMapping("/addAndSubmit")
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
    @PostMapping("/updateAndSubmit")
    public ApiResult updateAndSubmit(@RequestBody @Validated WarehouseReceiveDTO.UpdateDTO dto) {
        Boolean flag = warehouseReceiveService.updateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     * @param baseApproveParamDTO baseApproveParamDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/approve")
    public ApiResult approve(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        Boolean flag = warehouseReceiveService.approve(baseApproveParamDTO);
        return flag == true ? success() : failure();
    }

    /**
     * 批量反审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/disApprove")
    public ApiResult disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = warehouseReceiveService.disApprove(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 取消流程
     * @Author Luo_WG
     * @Date 2023/4/13 18:58
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/cancelProcess")
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
    @PostMapping("/invalid")
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
    @PostMapping("/delete")
    public ApiResult delete(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        Boolean flag = warehouseReceiveService.delete(idsDTO.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 导出
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @param dto dto
     * @param response response
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping(value = "/exportExcel")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "receive_user_id",
            menuCode = "wms:warehouseReceive:paging",
            tableAlias = "wr"
    )
    public ApiResult exportExcel(@RequestBody WarehouseReceiveDTO.PagingParamDTO dto, HttpServletResponse response) {
        Boolean flag = warehouseReceiveService.exportExcel(dto, response);
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
    @PostMapping("/generateReceive")
    public ApiResult generateReceive(@RequestBody @Validated PurchaseOrderDTO.ListGenerateReceiveDTO dto) {
        Boolean flag = warehouseReceiveService.generateReceive(dto);
        return flag == true ? success() : failure();
    }

}
