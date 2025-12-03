package com.erp.server.oms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.erp.server.oms.query.KolFeedbackQueryHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.KolFeedbackService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.KolFeedbackDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import cn.hutool.core.util.ObjectUtil;
import com.erp.model.oms.entity.KolFeedbackEntity;

/**
 * KOL回片列表
 *
 * @author wuhaotian
 * @since 2025-12-01
 */
@Slf4j
@RestController
@LogSystemModule("KOL回片列表")
@RequestMapping("/kolFeedback")
public class KolFeedbackController extends BaseController {

    @Resource
    private KolFeedbackService kolFeedbackService;

    /**
    * 新增
    * @author wuhaotian
    * @date:  2025-12-01
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "KOL回片列表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated KolFeedbackDTO.AddDTO dto) {
        return success(kolFeedbackService.add(dto));
    }

    /**
    * 批量新增
    * @author wuhaotian
    * @date:  2025-12-03
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/batchAdd")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_INSERT, desc = "KOL回片列表批量新增")
    public ApiResult<?> batchAdd(@RequestBody @Validated KolFeedbackDTO.BatchAddDTO dto) {
        List<KolFeedbackDTO.AddDTO> list = dto.getList();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(list.size());

        for (KolFeedbackDTO.AddDTO addDTO : list) {
            BatchResultDTO addResult;
            try {
                BaseResultDTO.AddDTO result = kolFeedbackService.add(addDTO);
                addResult = BatchResultDTO.success(result.getId(), result.getCode(), "新增成功");
            } catch (Exception e) {
                log.error("KOL回片列表批量新增失败", e);
                String sourceCode = addDTO.getSourceCode() != null ? addDTO.getSourceCode() : "";
                addResult = BatchResultDTO.fail("", sourceCode, e.getMessage());
            }
            resultDTOS.add(addResult);
        }

        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 修改
    * @author wuhaotian
    * @date:  2025-12-01
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "KOL回片列表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:kolFeedback:update",
        serviceClass = KolFeedbackService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated KolFeedbackDTO.UpdateDTO dto) {
        kolFeedbackService.update(dto);
        return success();
    }

    /**
    * 批量修改
    * @author wuhaotian
    * @date:  2025-12-03
    * @param dtoList
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/batchUpdate")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "KOL回片列表批量修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:kolFeedback:update",
        serviceClass = KolFeedbackService.class,
        keyIdName = "id")
    public ApiResult<List<BatchResultDTO>> batchUpdate(@RequestBody @Validated List<KolFeedbackDTO.UpdateDTO> dtoList) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dtoList.size());
        
        List<String> ids = dtoList.stream().map(KolFeedbackDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<KolFeedbackEntity> list = kolFeedbackService.lambdaQuery().in(KolFeedbackEntity::getId, ids).list();
        Map<String, KolFeedbackEntity> idEntityMap = list.stream().collect(Collectors.toMap(KolFeedbackEntity::getId, e -> e));
        
        for (KolFeedbackDTO.UpdateDTO dto : dtoList) {
            BatchResultDTO updateResult;
            try {
                kolFeedbackService.update(dto);
                KolFeedbackEntity entity = idEntityMap.get(dto.getId());
                String code = entity != null ? entity.getSourceCode() : dto.getId();
                updateResult = BatchResultDTO.success(dto.getId(), code, "修改成功");
            } catch (Exception e) {
                log.error("KOL回片列表批量修改失败", e);
                KolFeedbackEntity entity = idEntityMap.get(dto.getId());
                if (entity == null) {
                    updateResult = BatchResultDTO.fail(dto.getId(), dto.getId(), "KOL回片列表不存在，修改失败");
                } else {
                    updateResult = BatchResultDTO.fail(dto.getId(), entity.getSourceCode(), e.getMessage());
                }
            }
            resultDTOS.add(updateResult);
        }
        
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 分页查询
    * @author wuhaotian
    * @date:  2025-12-01
    * @param dto
    * @return ApiResult<PagingVO<KolFeedbackDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:kolFeedback:paging",
            tableAlias = "kf")
    @WebAdvanceQuery(handler = KolFeedbackQueryHandler.class)
    public ApiResult<PagingVO<KolFeedbackDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<KolFeedbackDTO.ParamDTO> dto) {
        return success(kolFeedbackService.paging(dto));
    }

    /**
     * 获取状态统计
     * @author wuhaotian
     * @date:  2025-12-01
     * @param dto
     * @return ApiResult<List<KolFeedbackDTO.TabListDTO>>
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:kolFeedback:paging",
            tableAlias = "kf")
    public ApiResult<List<KolFeedbackDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(kolFeedbackService.tabList(dto));
    }

    /**
     * 批量删除
     * @author wuhaotian
     * @date:  2025-12-01
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/batchDelete")
    @LogAction(value = LogActionEnum.DELETE, desc = "KOL回片列表批量删除")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolFeedback:batchDelete",
            serviceClass = KolFeedbackService.class,
            keyIdName = "ids")
    public ApiResult<?> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();

        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());

        List<KolFeedbackEntity> list = kolFeedbackService.lambdaQuery().in(KolFeedbackEntity::getId, ids).list();

        Map<String, KolFeedbackEntity> idEntityMap = list.stream().collect(Collectors.toMap(KolFeedbackEntity::getId, w -> w));

        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = kolFeedbackService.delete(id);
            } catch (Exception e) {
                log.error("KOL回片列表删除失败", e);
                KolFeedbackEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "KOL回片列表不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getSourceCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }

        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 导出
     * @author wuhaotian
     * @date:  2025-12-01
     * @param dto
     */
    @PostMapping("/export")
    @LogAction(value = LogActionEnum.EXPORT, desc = "KOL回片列表导出")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:kolFeedback:export",
            tableAlias = "kf")
    public ApiResult<Boolean> export(@RequestBody @Validated PagingDTO<KolFeedbackDTO.ParamDTO> dto) {
        return success(kolFeedbackService.export(dto));
    }

    /**
     * 导入
     * @author wuhaotian
     * @date:  2025-12-01
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/import")
    @LogAction(value = LogActionEnum.IMPORT, desc = "KOL回片列表导入")
    public ApiResult<Boolean> importData(@RequestBody BaseDTO.ImportDTO dto) {
        Boolean flag = kolFeedbackService.importExcel(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 下载导入模板
     * @author wuhaotian
     * @date:  2025-12-01
     * @param response
     */
    @PostMapping("/downloadTemplate")
    public ApiResult<Object> downloadTemplate(HttpServletResponse response) {
        kolFeedbackService.downloadTemplate(response);
        return success();
    }

}
