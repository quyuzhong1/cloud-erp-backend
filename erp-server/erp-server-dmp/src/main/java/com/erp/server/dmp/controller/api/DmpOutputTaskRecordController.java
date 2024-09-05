package com.erp.server.dmp.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpOutputTaskDTO;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.server.dmp.query.DmpOutputTaskRecordQueryHandler;
import com.erp.server.dmp.query.DmpTaskQueryHandler;
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
import com.erp.server.dmp.service.DmpOutputTaskRecordService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;

import java.util.List;

/**
 * 推送任务记录
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@RestController
@LogSystemModule("推送任务记录")
@RequestMapping("/dmpOutputTaskRecord")
public class DmpOutputTaskRecordController extends BaseController {

    @Resource
    private DmpOutputTaskRecordService dmpOutputTaskRecordService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "推送任务记录新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpOutputTaskRecordDTO.AddDTO dto) {
        return success(dmpOutputTaskRecordService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "推送任务记录修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpOutputTaskRecord:update",
        serviceClass = DmpOutputTaskRecordService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpOutputTaskRecordDTO.UpdateDTO dto) {
        dmpOutputTaskRecordService.update(dto);
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
        List<DmpOutputTaskRecordDTO.TabListDTO> tabList = dmpOutputTaskRecordService.tabList(dto);
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
    @WebAdvanceQuery(handler = DmpOutputTaskRecordQueryHandler.class)
    public ApiResult<PagingVO<DmpOutputTaskRecordDTO.PagingDTO>> paging(@RequestBody @Validated PagingDTO<DmpOutputTaskRecordDTO.PagingParamDTO> dto) {
        PagingVO<DmpOutputTaskRecordDTO.PagingDTO> pagingVO = dmpOutputTaskRecordService.paging(dto);
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
        Boolean flag = dmpOutputTaskRecordService.exportExcel(dto);
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
    public ApiResult batchNoNeedSync(@RequestBody BaseIdsDTO.IdsDTO dto) {
        Boolean flag = dmpOutputTaskRecordService.batchNoNeedSync(dto.getIds());
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
    public ApiResult<BaseResultDTO.AddDTO> addOutputBlack(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean flag = dmpOutputTaskRecordService.addOutputBlack(dto);
        return flag ? success() : failure();
    }
}
