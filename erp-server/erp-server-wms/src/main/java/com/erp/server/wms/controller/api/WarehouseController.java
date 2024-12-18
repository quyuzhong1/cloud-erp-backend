package com.erp.server.wms.controller.api;


import cn.hutool.core.text.CharSequenceUtil;
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
import com.common.core.utils.BeanMapper;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.WarehouseDTO.WarehouseUpdateStateDTO;
import com.erp.server.wms.query.WarehouseQueryHandler;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.server.wms.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 仓库管理
 *
 * @author Lambda
 * @since 2023-03-15
 */
@Slf4j
@RestController
@LogSystemModule("仓库列表")
@RequestMapping("/warehouse")
public class WarehouseController extends BaseController {

    @Resource
    private WarehouseService warehouseService;


    /**
     * 仓库分页列表
     *
     * @param
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:warehouse:paging",
            tableAlias = "warehouse"
    )
    @WebAdvanceQuery(handler = WarehouseQueryHandler.class)
    public ApiResult<PagingVO<WarehouseDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<WarehouseDTO.PagingParamDTO> dto) {
        PagingVO<WarehouseDTO.PagingViewDTO> pagingVO = warehouseService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 添加仓库
     *
     * @param
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增仓库")
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:warehouse:add",
            serviceClass = WarehouseService.class,
            keyIdName = "id")
    public ApiResult add(@RequestBody @Validated WarehouseDTO.AddDTO dto) {
        String id = warehouseService.add(dto);
        return CharSequenceUtil.isNotBlank(id) ? success() : failure();
    }

    /**
     * 提交并审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交仓库")
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:warehouse:add",
            serviceClass = WarehouseService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated WarehouseDTO.AddDTO dto) {
        Boolean result = warehouseService.addAndSubmit(dto);
        return result == true ? success() : failure();
    }

    /**
     * 仓库提交审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交仓库")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:warehouse:submit",
            serviceClass = WarehouseService.class,
            keyIdName = "id")
    public ApiResult submit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean result = warehouseService.submit(dto.getIds());
        return result == true ? success() : failure();
    }


    /**
     * 启用仓库
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "启用仓库:id={id},状态值={state}(true=禁用,false=启用)")
    @PostMapping("/updateStatus")
    public ApiResult updateStatus(@RequestBody @Validated WarehouseUpdateStateDTO dto) {
        Boolean result = warehouseService.updateStatus(dto);
        return result == true ? success() : failure();
    }


    /**
     * 修改仓库
     *
     * @param
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改仓库")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:warehouse:update",
            serviceClass = WarehouseService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated WarehouseDTO.UpdateDTO dto) {
        String id  = warehouseService.updateWarehouse(dto);
        return CharSequenceUtil.isNotBlank(id) ? success() : failure();
    }




    /**
     * 修改并提交
     *
     * @param
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交仓库")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:warehouse:update",
            serviceClass = WarehouseService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated WarehouseDTO.UpdateDTO dto) {
        Boolean result = warehouseService.updateAndSubmit(dto);
        return result == true ? success() : failure();
    }


    /**
     * 仓库详情
     *
     * @param
     * @return
     */
    @LogViewService
    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:warehouse:view",
            serviceClass = WarehouseService.class,
            keyIdName = "id")
    public ApiResult<WarehouseDTO.UpdateDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        WarehouseDTO.UpdateDTO view = warehouseService.view(dto.getId());
        return success(view);
    }

    /**
     * 审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核仓库")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:warehouse:approve",
            serviceClass = WarehouseService.class,
            keyIdName = "id")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<WarehouseEntity> entityList = warehouseService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            WarehouseEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"仓库记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(warehouseService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess()));
            }catch (Exception e){
                log.error("仓库审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getName(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 反审核
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-03-22 11:56
     */
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核仓库")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:warehouse:disApprove",
            serviceClass = WarehouseService.class,
            keyIdName = "id")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<WarehouseEntity> entityList = warehouseService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            WarehouseEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"仓库记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(warehouseService.disApprove(entity));
            }catch (Exception e){
                log.error("仓库反审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getName(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 删除仓库
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除仓库")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:warehouse:delete",
            serviceClass = WarehouseService.class,
            keyIdName = "id")
    public ApiResult delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean result = warehouseService.deleteByIds(dto.getIds());
        return result == true ? success() : failure();
    }


    /**
     * 导出
     * 仓库数据
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出仓库数据")
    @PostMapping("/exportWarehouse")
    public ApiResult exportWarehouse(@RequestBody @Valid WarehouseDTO.ExportDTO dto) {
        warehouseService.exportWarehouse(dto);
        return success();
    }

    /**
     * 导入仓库
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入仓库")
    @PostMapping("/importFile")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = warehouseService.importFile(excelFile, response);
        return result == true ? success() : failure();
    }

    /**
     * 下载模板
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载模板仓库数据")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        warehouseService.downloadTemplate(response);
        return success();
    }


    /**
     * 仓库列表
     */
    @GetMapping("/list")
    public ApiResult<List<WarehouseDTO.ListDTO>> list() {
        List<WarehouseDTO.ListDTO> list = warehouseService.listApproveWarehouse();
        return success(list);
    }


    /**
     * 远程分页下拉查询
     * @author Will
     * @date: 2024/5/24 13:06
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/selectPaging")
    public ApiResult<PagingVO<WarehouseDTO.ListDTO>> selectPaging(@RequestBody PagingDTO<WarehouseDTO.SelectDTO> dto) {
        PagingVO<WarehouseDTO.ListDTO> pagingVO = warehouseService.selectPaging(dto);
        return success(pagingVO);
    }

    /**
     * 根据sku查询仓库库存
     * @author Will
     * @date: 2024/5/23 18:25
     * @param dto
     * @return ApiResult<List<ListDTO>>
     */
    @PostMapping("/listWarehouseInventoryQty")
    public ApiResult<List<WarehouseDTO.ListInventoryQtyDTO>> listWarehouseInventoryQty(@RequestBody @Valid WarehouseDTO.ListInventoryQtyParamDTO dto) {
        List<WarehouseDTO.ListInventoryQtyDTO> list = warehouseService.listWarehouseInventoryQty(dto);
        return success(list);
    }

    /**
     * 仓库列表(有参)
     */
    @PostMapping("/listWarehouseByParams")
    public ApiResult<List<WarehouseDTO.ListDTO>> listWarehouseByParams(@RequestBody @Valid WarehouseDTO.ListParamDTO dto) {
        List<WarehouseDTO.ListDTO> list = warehouseService.listWarehouseByParams(dto);
        return success(list);
    }

    /**
     * 仓库列表(所有关联海外服务商)
     *
     * @Author Jim
     * @date 2023-11-29
     */
    @GetMapping("/listOverseasWarehouse")
    public ApiResult<List<WarehouseDTO.ListDTO>> listOverseasWarehouse() {
        List<WarehouseDTO.ListDTO> list = warehouseService.listOverseasWarehouse();
        return success(list);
    }

    /**
     * 仓库列表(树状)
     */
    @GetMapping("/listTree")
    public ApiResult<List<WarehouseDTO.ListTreeDTO>> listTree() {
        List<WarehouseDTO.ListTreeDTO> list = warehouseService.listTree();
        return success(list);
    }

    /**
     * 仓库列表(树状有参)
     */
    @PostMapping("/listTreeByParams")
    public ApiResult<List<WarehouseDTO.ListTreeDTO>> listTreeByParams(@RequestBody @Valid WarehouseDTO.ListParamDTO dto) {
        List<WarehouseDTO.ListTreeDTO> list = warehouseService.listTreeByParams(dto);
        return success(list);
    }

    /**
     * 仓库分页列表-无权限
     *
     * @param
     * @return
     */
    @PostMapping("/pageList")
    public ApiResult<PagingVO<WarehouseDTO.PagingNoPermissionDTO>> pageList(@RequestBody @Validated PagingDTO<WarehouseDTO.PagingDTO> dto) {
        PagingVO<WarehouseDTO.PagingNoPermissionDTO> pagingVO = warehouseService.pagingNoPermission(dto);
        return success(pagingVO);
    }

    /**
     * 盘点-产品添加分页列表
     *
     * @param
     * @return
     */

    @PostMapping("/paging/product")
    public ApiResult<PagingVO<WarehouseDTO.PagingProductViewDTO>> pagingProduct(@RequestBody @Validated PagingDTO<WarehouseDTO.PagingProductDTO> dto) {
        PagingVO<WarehouseDTO.PagingProductViewDTO> pagingVO = warehouseService.pagingProduct(dto);
        return success(pagingVO);
    }

    /**
     * 查询所有仓库信息(不带权限控制)
     */
    @GetMapping("/listWarehouseWithCaches")
    public ApiResult<List<WarehouseDTO.ListDTO>> listWarehouseWithCaches(){
        List<WarehouseEntity> warehouseEntities = warehouseService.listWarehouseWithCaches();
        List<WarehouseDTO.ListDTO> listDTOS = BeanMapper.copyList(warehouseEntities, WarehouseDTO.ListDTO.class);
        return success(listDTOS);
    }
}
