package com.erp.server.plm.controller.api;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.vo.PagingVO;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.erp.server.plm.query.CfgProductForbiddenWordQueryHandler;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.CfgProductForbiddenWordDTO;
import com.erp.model.plm.entity.CfgProductForbiddenWordEntity;
import com.erp.server.plm.service.CfgProductForbiddenWordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 产品违禁词库
 */
@Slf4j
@RestController
@LogSystemModule("违禁词库")
@RequestMapping("/cfgProductForbiddenWord")
public class CfgProductForbiddenWordController extends BaseController {

    @Resource
    private CfgProductForbiddenWordService cfgProductForbiddenWordService;

    /**
     * 新增
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "违禁词库新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgProductForbiddenWordDTO.AddDTO dto) {
        return success(cfgProductForbiddenWordService.add(dto));
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "违禁词库修改")
    public ApiResult<?> update(@RequestBody @Validated CfgProductForbiddenWordDTO.UpdateDTO dto) {
        cfgProductForbiddenWordService.update(dto);
        return success();
    }

    /**
     * 列表查询
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = CfgProductForbiddenWordQueryHandler.class)
    public ApiResult<PagingVO<CfgProductForbiddenWordDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<CfgProductForbiddenWordDTO.PagingParamDTO> dto) {
        return success(cfgProductForbiddenWordService.paging(dto));
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.DELETE, desc = "违禁词库删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<CfgProductForbiddenWordEntity> list = cfgProductForbiddenWordService.lambdaQuery().in(CfgProductForbiddenWordEntity::getId, ids).list();
        Map<String, CfgProductForbiddenWordEntity> idEntityMap = list.stream().collect(Collectors.toMap(CfgProductForbiddenWordEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = cfgProductForbiddenWordService.delete(id);
            } catch (Exception e) {
                log.error("违禁词库删除失败", e);
                CfgProductForbiddenWordEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "违禁词不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getForbiddenWord(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 启用/禁用
     */
    @PostMapping("/updateStatus")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "批量启用/禁用 ids={ids},状态值={disabled}(true=禁用,false=启用)")
    public ApiResult<List<BatchResultDTO>> updateStatus(@RequestBody @Validated UpdateStateDTO.BatchUpdateDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<CfgProductForbiddenWordEntity> list = cfgProductForbiddenWordService.lambdaQuery().in(CfgProductForbiddenWordEntity::getId, ids).list();
        Map<String, CfgProductForbiddenWordEntity> idEntityMap = list.stream().collect(Collectors.toMap(CfgProductForbiddenWordEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO result;
            try {
                result = cfgProductForbiddenWordService.updateStatus(id, dto.getDisabled());
            } catch (Exception e) {
                log.error("违禁词库启用/禁用失败", e);
                CfgProductForbiddenWordEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "违禁词不存在, 启用/禁用失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = BatchResultDTO.fail(entity.getId(), entity.getForbiddenWord(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 导出Excel数据
     */
    @PostMapping("/export")
    @LogAction(value = LogActionEnum.EXPORT, desc = "违禁词库导出Excel数据")
    @WebAdvanceQuery(handler = CfgProductForbiddenWordQueryHandler.class)
    public ApiResult<Object> exportList(@RequestBody @Validated CfgProductForbiddenWordDTO.PagingParamDTO dto) {
        Boolean flag = cfgProductForbiddenWordService.exportList(dto);
        return Boolean.TRUE.equals(flag) ? success() : failure();
    }
}
