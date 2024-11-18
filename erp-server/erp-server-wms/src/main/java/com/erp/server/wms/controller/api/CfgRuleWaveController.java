package com.erp.server.wms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.CfgRuleWaveDTO;
import com.erp.model.wms.entity.CfgRuleWaveEntity;
import com.erp.server.wms.service.CfgRuleWaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 波次规则
 *
 * @author will
 * @since 2024-06-20
 */
@Slf4j
@RestController
@LogSystemModule("波次规则")
@RequestMapping("/cfgRuleWave")
public class CfgRuleWaveController extends BaseController {

    @Resource
    private CfgRuleWaveService cfgRuleWaveService;


    /**
     * 分页查询
     * @author will
     * @date 2024/6/25 15:48
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<CfgRuleWaveDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<CfgRuleWaveDTO.PagingParamDTO> dto) {
        PagingVO<CfgRuleWaveDTO.ListDTO> pagingVO = cfgRuleWaveService.paging(dto);
        return success(pagingVO);
    }

    /**
    * 新增
    * @author will
    * @date:  2024-06-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "波次规则新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgRuleWaveDTO.AddDTO dto) {
        return success(cfgRuleWaveService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-06-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "波次规则修改")
    public ApiResult update(@RequestBody @Validated CfgRuleWaveDTO.UpdateDTO dto) {
        cfgRuleWaveService.update(dto);
        return success();
    }

    /**
     * 查看详情
     * @author will
     * @date 2024/6/24 18:28
     * @param id
     * @return ApiResult<ViewDTO>
     */
    @GetMapping("/view")
    public ApiResult<CfgRuleWaveDTO.ViewDTO> view(@RequestParam("id") String id) {
        CfgRuleWaveDTO.ViewDTO dto = cfgRuleWaveService.view(id);
        return success(dto);
    }

    /**
     * 更新波次规则状态
     * @author will
     * @date 2024/6/24 16:52
     * @param dto
     * @return ApiResult<?>
     */
    @PostMapping("/updateStatus")
    @LogAction(value = LogActionEnum.UPDATE, desc = "波次规则状态更新")
    public ApiResult<List<BatchResultDTO>> updateStatus(@RequestBody @Validated CfgRuleWaveDTO.UpdateStatusDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIdList().size());
        for (String id : dto.getIdList()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = cfgRuleWaveService.updateStatus(id,dto.getDisabled());
            }catch (Exception e){
                log.error("波次规则状态更新失败",e);
                CfgRuleWaveEntity entity = cfgRuleWaveService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "波次规则不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getName(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 删除
     * @author will
     * @date 2024/6/24 18:20
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.DELETE, desc = "波次规则删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = cfgRuleWaveService.delete(id);
            }catch (Exception e){
                log.error("波次规则删除失败",e);
                CfgRuleWaveEntity entity = cfgRuleWaveService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "波次规则不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getName(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 执行规则
     * @author will
     * @date 2024/6/25 15:42
     * @param dto
     * @return ApiResult<?>
     */
    @PostMapping("/executeRule")
    @LogAction(value = LogActionEnum.EXECUTE, desc = "执行规则")
    public ApiResult executeRule(@RequestBody @Validated BaseIdDTO dto) {
        cfgRuleWaveService.executeRule(dto.getId());
        return success();
    }
}
