package com.erp.server.dmp.controller.api;


import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.dmp.dto.AdsPushTaskDTO;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.model.dmp.entity.doris.AdsPushTaskEntity;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.server.dmp.query.AdsPushTaskQueryHandler;
import com.erp.server.dmp.service.AdsPushTaskService;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * ads推送任务
 *
 * @author shukai
 * @since 2025-10-28
 */
@Slf4j
@RestController
@LogSystemModule("ads推送任务")
@RequestMapping("/adsPushTask")
public class AdsPushTaskController extends BaseController {

    @Resource
    private AdsPushTaskService adsPushTaskService;

    /**
    * 新增
    * @author shukai
    * @date:  2025-10-28
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "ads推送任务新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated AdsPushTaskDTO.AddDTO dto) {
        return success(adsPushTaskService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2025-10-28
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "ads推送任务修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:adsPushTask:update",
        serviceClass = AdsPushTaskService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated AdsPushTaskDTO.UpdateDTO dto) {
        adsPushTaskService.update(dto);
        return success();
    }

    /**
     * 获取 tab列表
     * @Author Luo_WG
     * @Date 2024/9/3 15:15
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.dmp.dto.DmpOutputTaskDTO.TabListDTO>>
     **/
    @PostMapping("/tabList")
    public ApiResult<List<DmpOutputTaskRecordDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
    	List<DmpOutputTaskRecordDTO.TabListDTO> tabList = adsPushTaskService.tabList(dto);
        return success(tabList);
    }

    /**
     * 推送任务列表分页查询
     * @Author Luo_WG
     * @Date 2024/9/3 14:28
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.dmp.dto.DmpOutputTaskDTO.PagingDTO>>
     **/
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = AdsPushTaskQueryHandler.class)
    public ApiResult<PagingVO<DmpOutputTaskRecordDTO.PagingDTO>> paging(@RequestBody @Validated PagingDTO<DmpOutputTaskRecordDTO.PagingParamDTO> dto) {
    	PagingVO<DmpOutputTaskRecordDTO.PagingDTO> pagingVO = adsPushTaskService.paging(dto);
        return success(pagingVO);
    }
    
    /**
     * 导出
     * @Author Luo_WG
     * @Date 2024/9/5 17:30
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody DmpOutputTaskRecordDTO.ExpotParamDTO dto) {
        Boolean flag = adsPushTaskService.exportExcel(dto);
        return flag == true ? success() : failure();
    }
    
    /**
     * 无需同步
     * @Author Luo_WG
     * @Date 2024/9/5 16:29
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.UPDATE_STATUS, desc = "修改为无需同步")
    @PostMapping(value = "/batchNoNeedSync")
    public ApiResult batchNoNeedSync(@RequestBody BaseIdsDTO.RemarkDTO dto) {
        Boolean flag = adsPushTaskService.batchNoNeedSync(dto.getIds() , dto.getRemark());
        return flag == true ? success() : failure();
    }

    /**
     * 加入黑名单
     * @Author Luo_WG
     * @Date 2024/9/5 16:27
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.dto.base.BaseResultDTO.AddDTO>
     **/
    @PostMapping("/addOutputBlack")
    @LogAction(value = LogActionEnum.INSERT, desc = "加入黑名单")
    public ApiResult<BaseResultDTO.AddDTO> addOutputBlack(@RequestBody @Validated DmpOutputTaskRecordDTO.AddOutputBlackDTO dto) {
        Boolean flag = adsPushTaskService.addOutputBlack(dto);
        return flag ? success() : failure();
    }

    /**
     * 取消黑名单
     * @Author Luo_WG
     * @Date 2024/9/11 19:27
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/cancelOutputBlack")
    @LogAction(value = LogActionEnum.DELETE, desc = "取消黑名单")
    public ApiResult<?> cancelOutputBlack(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = adsPushTaskService.cancelOutputBlack(id);
            } catch (Exception e) {
                log.error("取消黑名单 取消失败", e);
                resultDTO = BatchResultDTO.fail(id, id, e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    
    /**
     * 重新同步（批量同步）
     * @Author Luo_WG
     * @Date 2024/9/6 15:58
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping(value = "/batchSync")
    @LogAction(value = LogActionEnum.UPDATE, desc = "重新同步")
    public ApiResult batchSync(@RequestBody BaseIdsDTO.IdsDTO dto) {
        Boolean flag = adsPushTaskService.batchSync(dto.getIds());
        return flag == true ? success() : failure();
    }
}
