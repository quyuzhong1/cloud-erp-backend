package com.erp.server.plm.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.plm.service.ProductChangeService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.plm.dto.ProductChangeDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.plm.entity.ProductChangeEntity;

/**
 * 产品变更信息表
 *
 * @author lrp
 * @since 2026-02-03
 */
@Slf4j
@RestController
@LogSystemModule("产品变更信息表")
@RequestMapping("/productChange")
public class ProductChangeController extends BaseController {

    @Resource
    private ProductChangeService productChangeService;

    /**
    * 新增
    * @author lrp
    * @date:  2026-02-03
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "产品变更信息表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated ProductChangeDTO.AddDTO dto) {
        return success(productChangeService.add(dto));
    }

    /**
    * 修改
    * @author lrp
    * @date:  2026-02-03
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "产品变更信息表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "plm:productChange:update",
        serviceClass = ProductChangeService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated ProductChangeDTO.UpdateDTO dto) {
        productChangeService.update(dto);
        return success();
    }


    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:productChange:paging",
            tableAlias = ""
    )
    public ApiResult<List<ProductChangeDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(productChangeService.tabList(dto));
    }

    /**
    * 列表查询
    * @author lrp
    * @date: 2026-02-03
    * @param dto
    * @return ApiResult<PagingVO<ProductChangeDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:productChange:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<ProductChangeDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<ProductChangeDTO.PagingParamDTO> dto) {
        return success(productChangeService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author lrp
    * @date:  2026-02-03
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated ProductChangeDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = productChangeService.addAndSubmit(dto);
        return success(result);
    }

    /**
    * 修改并提交审核
    * @author lrp
    * @date:  2026-02-03
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:productChange:updateAndSubmit",
            serviceClass = ProductChangeService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated ProductChangeDTO.UpdateDTO dto) {
        productChangeService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author lrp
    * @date:  2026-02-03
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:productChange:submit",
            serviceClass = ProductChangeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "产品变更信息表提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<ProductChangeEntity> list = productChangeService.lambdaQuery().in(ProductChangeEntity::getId, ids).list();
		Map<String, ProductChangeEntity> idEntityMap = list.stream().collect(Collectors.toMap(ProductChangeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = productChangeService.submit(id);
            }catch (Exception e){
                log.error("产品变更信息单 提交审核失败",e);
                ProductChangeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "产品变更信息单不存在, 提交失败");
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
    * 审核
    * @author lrp
    * @date:  2026-02-03
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:productChange:approve",
            serviceClass = ProductChangeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "产品变更信息表审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<ProductChangeEntity> list = productChangeService.lambdaQuery().in(ProductChangeEntity::getId, ids).list();
		Map<String, ProductChangeEntity> idEntityMap = list.stream().collect(Collectors.toMap(ProductChangeEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = productChangeService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("产品变更信息单审核失败",e);
                ProductChangeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "产品变更信息单不存在, 审核失败");
                    resultDTOS.add(approveResult);
                    continue;
                }
                approveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(approveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 反审核
    * @author lrp
    * @date:  2026-02-03
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:productChange:disApprove",
            serviceClass = ProductChangeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "产品变更信息表反审核")
    public ApiResult<List<BatchResultDTO>> batchDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<ProductChangeEntity> list = productChangeService.lambdaQuery().in(ProductChangeEntity::getId, ids).list();
		Map<String, ProductChangeEntity> idEntityMap = list.stream().collect(Collectors.toMap(ProductChangeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = productChangeService.disApprove(id);
            }catch (Exception e){
                log.error("产品变更信息单反审核失败",e);
                ProductChangeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "产品变更信息单不存在, 反审核失败");
                    resultDTOS.add(disApproveResult);
                    continue;
                }
                disApproveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(disApproveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
    * 删除
    * @author lrp
    * @date:  2026-02-03
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:productChange:delete",
            serviceClass = ProductChangeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "产品变更信息表删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<ProductChangeEntity> list = productChangeService.lambdaQuery().in(ProductChangeEntity::getId, ids).list();
		Map<String, ProductChangeEntity> idEntityMap = list.stream().collect(Collectors.toMap(ProductChangeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = productChangeService.delete(id);
            }catch (Exception e){
                log.error("产品变更信息单删除失败",e);
                ProductChangeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "产品变更信息单不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 撤销
    * @author lrp
    * @date:  2026-02-03
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:productChange:cancelProcess",
            serviceClass = ProductChangeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "产品变更信息表撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<ProductChangeEntity> list = productChangeService.lambdaQuery().in(ProductChangeEntity::getId, ids).list();
        Map<String, ProductChangeEntity> idEntityMap = list.stream().collect(Collectors.toMap(ProductChangeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = productChangeService.cancelProcess(id);
            }catch (Exception e){
                log.error("产品变更信息单撤回流程失败",e);
                ProductChangeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "产品变更信息单不存在, 撤回流程失败");
                    resultDTOS.add(cancelResult);
                    continue;
                }
                cancelResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(cancelResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 详情
    * @author lrp
    * @date:  2026-02-03
    * @param id
    * @return ApiResult<ProductChangeDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:productChange:view",
            serviceClass = ProductChangeService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<ProductChangeDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(productChangeService.view(id));
    }

    /**
    * 导出Excel数据
    * @author lrp
    * @date:  2026-02-03
    * @param dto
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:productChange:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "产品变更信息表导出Excel数据")
    public ApiResult<Boolean> exportList(@RequestBody @Validated ProductChangeDTO.PagingParamDTO dto) {
        productChangeService.exportList(dto);
        return success(true);
    }

    /**
     * 下载导入模板
     */
    @GetMapping("/downloadTemplate")
    public ApiResult<Object> downloadTemplate(HttpServletResponse response) {
        productChangeService.downloadTemplate(response);
        return success();
    }

    /**
     * 导入Excel数据
     * @author wuht
     * @date: 2025-10-11
     * @param dto 导入参数
     * @return ApiResult<Boolean>
     */
    @PostMapping("/import")
    @LogAction(value = LogActionEnum.IMPORT, desc = "产品信息变更导入Excel数据")
    public ApiResult<Boolean> importExcel(@RequestBody @Validated BaseDTO.ImportDTO dto) {
        // 异步导入任务
        productChangeService.importExcel(dto);
        return success(true);
    }

    /**
     * 批量新增
     * @author lrp
     * @date:  2026-02-03
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/batchAdd")
    @LogAction(value = LogActionEnum.INSERT, desc = "产品变更信息表新增")
    public ApiResult<BaseResultDTO.AddDTO> batchAdd(@RequestBody @Validated ProductChangeDTO.BatchAddDTO dto) {
        return success(productChangeService.batchAdd(dto));
    }

    @PostMapping("/getProductChangeFieldEnum")
    public ApiResult<List<ProductChangeDTO.ProductChangeFieldDTO>> getProductChangeFieldEnum() {
        return success(productChangeService.getProductChangeFieldEnum());
    }
}
