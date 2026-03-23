package com.erp.server.wms.controller.api;


import cn.hutool.core.util.ObjectUtil;
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
import com.erp.model.wms.dto.SamplingPlanDTO;
import com.erp.model.wms.entity.QcSamplingPlanEntity;
import com.erp.server.wms.service.QcSamplingPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 抽样方案表
 *
 * @author zdy
 * @since 2026-03-20
 */
@Slf4j
@RestController
@LogSystemModule("抽样方案表")
@RequestMapping("/samplingScheme")
public class QcSamplingPlanController extends BaseController {

    @Resource
    private QcSamplingPlanService qcSamplingPlanService;

    /**
    * 新增
    * @author zdy
    * @date:  2026-03-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "抽样方案表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SamplingPlanDTO.AddDTO dto) {
        return success(qcSamplingPlanService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2026-03-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    public ApiResult<Boolean> update(@RequestBody @Validated SamplingPlanDTO.UpdateDTO dto) {
        Boolean update = qcSamplingPlanService.update(dto);
        return update ? success(true) : success(false);
    }


    /**
    * 列表查询
    * @author zdy
    * @date: 2026-03-20
    * @param dto
    * @return ApiResult<PagingVO<SamplingPlanDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:samplingScheme:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<SamplingPlanDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SamplingPlanDTO.PagingParamDTO> dto) {
        return success(qcSamplingPlanService.paging(dto));
    }


    /**
    * 详情
    * @author zdy
    * @date:  2026-03-20
    * @param id
    * @return ApiResult<SamplingPlanDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:samplingScheme:view",
            serviceClass = QcSamplingPlanService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<SamplingPlanDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(qcSamplingPlanService.view(id));
    }

    /**
     * 启用禁用
     * @author zdy
     * @date:  2026-03-20
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/updateStatus")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "批量启用/禁用 ids={ids},状态值={disabled}(true=禁用,false=启用)")
    public ApiResult updateStatus(@RequestBody @Validated UpdateStateDTO.BatchUpdateDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<QcSamplingPlanEntity> list = qcSamplingPlanService.lambdaQuery().in(QcSamplingPlanEntity::getId, ids).list();
        Map<String, QcSamplingPlanEntity> idEntityMap = list.stream().collect(Collectors.toMap(QcSamplingPlanEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = qcSamplingPlanService.updateStatus(id,dto.getDisabled());
            }catch (Exception e){
                log.error("抽样方案单启用/禁用失败",e);
                QcSamplingPlanEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "抽样方案单不存在, 启用/禁用失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 删除
     * @author zdy
     * @date:  2026-03-20
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.DELETE, desc = "抽样方案单删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<QcSamplingPlanEntity> list = qcSamplingPlanService.lambdaQuery().in(QcSamplingPlanEntity::getId, ids).list();
        Map<String, QcSamplingPlanEntity> idEntityMap = list.stream().collect(Collectors.toMap(QcSamplingPlanEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = qcSamplingPlanService.delete(id);
            }catch (Exception e){
                log.error("抽样方案单删除失败",e);
                QcSamplingPlanEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "抽样方案单不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getId(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

}
