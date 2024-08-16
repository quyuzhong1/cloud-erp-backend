package com.erp.server.tms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.entity.InitFirstMileAllocationDetailEntity;
import com.erp.model.tms.entity.InitFirstMileAllocationEntity;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.server.tms.query.InitFirstMileAllocationQueryHandler;
import com.erp.server.tms.service.InitFirstMileAllocationDetailService;
import com.erp.server.tms.service.TmsFirstMileReconciliationService;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.tms.service.InitFirstMileAllocationService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.InitFirstMileAllocationDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 期初头程分摊
 *
 * @author zdy
 * @since 2024-08-13
 */
@Slf4j
@RestController
@LogSystemModule("期初头程分摊")
@RequestMapping("/initFirstMileAllocation")
public class InitFirstMileAllocationController extends BaseController {

    @Resource
    private InitFirstMileAllocationService initFirstMileAllocationService;
    @Resource
    private InitFirstMileAllocationDetailService initFirstMileAllocationDetailService;

    /**
    * 新增
    * @author zdy
    * @date:  2024-08-13
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "期初头程分摊新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated InitFirstMileAllocationDTO.AddDTO dto) {
        return success(initFirstMileAllocationService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2024-08-13
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "期初头程分摊修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:initFirstMileAllocation:update",
        serviceClass = InitFirstMileAllocationService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated InitFirstMileAllocationDTO.UpdateDTO dto) {
        initFirstMileAllocationService.update(dto);
        return success();
    }


    /**
     * tab 列表
     *
     * @param dto
     * @author zdy
     * @date 2024-8-13 10:54
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:initFirstMileAllocation:paging",
            tableAlias = "lb"
    )
    public ApiResult<List<InitFirstMileAllocationDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<InitFirstMileAllocationDTO.TabListDTO> tabList = initFirstMileAllocationService.tabList(dto);
        return success(tabList);
    }


    /**
     * 分页
     *
     * @param dto
     * @author zdy
     * @date 2024-8-13 10:54
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:initFirstMileAllocation:paging",
            tableAlias = "a"
    )
    @WebAdvanceQuery(handler = InitFirstMileAllocationQueryHandler.class)
    public ApiResult<PagingVO<InitFirstMileAllocationDTO.PagingVO>> paging(@RequestBody @Valid PagingDTO<InitFirstMileAllocationDTO.PagingParamDTO> dto) {
        PagingVO<InitFirstMileAllocationDTO.PagingVO> pagingVO = initFirstMileAllocationService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 批量审核
     * @author zdy
     * @date: 2024/8/15 17:54
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "批量审核期初头程分摊")
    @PostMapping("/approve")
//    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//            tableField = "create_user_id",
//            menuCode = "scm:initFirstMileAllocation:approve",
//            serviceClass = InitFirstMileAllocationService.class,
//            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<InitFirstMileAllocationEntity> entityList = initFirstMileAllocationService.listByIds(ids);
        for (String id : ids) {
            InitFirstMileAllocationEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"期初头程分摊记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(initFirstMileAllocationService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess()));
            }catch (Exception e){
                log.error("期初头程分摊记录审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 反审核
     */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:initFirstMileAllocation:disApprove",
            serviceClass = InitFirstMileAllocationService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<InitFirstMileAllocationEntity> entityList = initFirstMileAllocationService.listByIds(ids);
        for (String id : ids) {
            InitFirstMileAllocationEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"期初头程分摊记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(initFirstMileAllocationService.disApprove(entity));
            }catch (Exception e){
                log.error("期初头程分摊记录反审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    /**
     * 撤销
     */
    @PostMapping("/cancel")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:initFirstMileAllocation:cancel",
            serviceClass = InitFirstMileAllocationService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> cancel(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<InitFirstMileAllocationEntity> entityList = initFirstMileAllocationService.listByIds(ids);
        for (String id : ids) {
            InitFirstMileAllocationEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"期初头程分摊记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(initFirstMileAllocationService.cancel(entity));
            }catch (Exception e){
                log.error("期初头程分摊记录撤销失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    /**
     * 提交审核
     */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:initFirstMileAllocation:submit",
            serviceClass = InitFirstMileAllocationService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<InitFirstMileAllocationEntity> entityList = initFirstMileAllocationService.listByIds(ids);
        for (String id : ids) {
            InitFirstMileAllocationEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"期初头程分摊记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(initFirstMileAllocationService.submit(entity));
            }catch (Exception e){
                log.error("期初头程分摊记录提交审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    /**
     * 修改并提交审核
     *
     * @param dto DTO
     * @return ApiResult<Void>
     * @author zdy
     * {@code @date:}2024-03-25
     */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsFirstMileReconciliation:updateAndSubmit",
            serviceClass = TmsFirstMileReconciliationService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated InitFirstMileAllocationDTO.UpdateDTO dto) {
        initFirstMileAllocationService.updateAndSubmit(dto);
        return success();
    }
    /**
     * 删除记录
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:initFirstMileAllocation:submit",
            serviceClass = InitFirstMileAllocationService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<InitFirstMileAllocationEntity> entityList = initFirstMileAllocationService.listByIds(ids);
        for (String id : ids) {
            InitFirstMileAllocationEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"期初头程分摊记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(initFirstMileAllocationService.delete(entity));
            }catch (Exception e){
                log.error("期初头程分摊记录删除失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    /**
     * 生成头程对账单
     */
    @PostMapping("/generateFirstMileReconciliation")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:initFirstMileAllocation:generateReconciliation",
            serviceClass = InitFirstMileAllocationService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> generateReconciliation(@RequestBody @Valid InitFirstMileAllocationDTO.ReconciliationDTO dto) {
        List<BatchResultDTO> resultDTOS = initFirstMileAllocationService.generateReconciliation(dto);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    /**
     * 导出Excel
     *
     * @param dto
     * @author zdy
     * @date 2024-8-15 10:54
     */
    @PostMapping("/exportExcel")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "create_user_id",
//            menuCode = "tms:initFirstMileAllocation:exportExcel",
//            tableAlias = "a"
//    )
    @WebAdvanceQuery(handler = InitFirstMileAllocationQueryHandler.class)
    public ApiResult<?> exportExcel(@RequestBody @Valid InitFirstMileAllocationDTO.PagingParamDTO dto, HttpServletResponse response) {
        initFirstMileAllocationService.exportExcel(dto, response);
        return success();
    }
    /**
     * 下载模板
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载期初导入模板")
    @GetMapping("/downloadTemplate")
    public ApiResult<?> downloadTemplate(HttpServletResponse response) {
        initFirstMileAllocationService.downloadTemplate(response);
        return success();
    }
    /**
     * 导入Excel
     * @author zdy
     * @date: 2024/8/14 9:39
     * @param excelFile
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入Excel")
    @PostMapping("/importFile")
    public ApiResult<?> importFile(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean flag = initFirstMileAllocationService.importFile(excelFile,response);
        return flag ? success() : failure();
    }
    /**
     * 详情
     *
     * @return
     */
    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:initFirstMileAllocation:view",
            tableAlias = "a"
    )
    public ApiResult<InitFirstMileAllocationDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        InitFirstMileAllocationDTO.ViewDTO result = initFirstMileAllocationService.view(dto.getId());
        return success(result);
    }

}
