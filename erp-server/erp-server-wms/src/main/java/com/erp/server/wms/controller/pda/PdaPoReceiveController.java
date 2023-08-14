package com.erp.server.wms.controller.pda;

import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.SoReturnInstockDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.server.wms.service.SoReturnInstockService;
import com.erp.server.wms.service.WarehouseReceiveService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * PDA:采购收货单
 * @Author Luo_WG
 * @Date 2023/8/11 10:08
 **/
@RestController
@RequestMapping(value = "/pdaPoReceive")
public class PdaPoReceiveController extends BaseController {
    @Resource
    private WarehouseReceiveService warehouseReceiveService;

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
            menuCode = "wms:pdaPoReceive:paging",
            tableAlias = "wr"
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
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated WarehouseReceiveDTO.AddDTO dto) {
        String id = warehouseReceiveService.pdaAdd(dto);
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
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soReturnInstock:view",
            serviceClass = SoReturnInstockService.class,
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
     * @param baseApproveParamDTO baseApproveParamDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:warehouseReceive:approve",
            serviceClass = WarehouseReceiveService.class,
            keyIdName = "ids")
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
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:warehouseReceive:disApprove",
            serviceClass = WarehouseReceiveService.class,
            keyIdName = "ids")
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
    @PostMapping("/delete")
    public ApiResult delete(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        Boolean flag = warehouseReceiveService.delete(idsDTO.getIds());
        return flag == true ? success() : failure();
    }
}
