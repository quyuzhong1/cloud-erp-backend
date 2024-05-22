package com.erp.server.dmp.controller.api;


import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.OperateLogDTO;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogSystemModule;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.controller.vo.ApiResult;

/**
 * 操作日志表
 *
 * @author hyj
 * @since 2024-05-22
 */
@Slf4j
@RestController
@LogSystemModule("操作日志表")
@RequestMapping("/operateLog")
public class OperateLogController extends BaseController {

    @Resource
    private OperateLogService operateLogService;

//    /**
//    * 新增
//    * @author hyj
//    * @date:  2024-05-22
//    * @param dto
//    * @return ApiResult<String>
//    */
//    @PostMapping("/add")
//    @LogAction(value = LogActionEnum.INSERT, desc = "操作日志表新增")
//    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated OperateLogDTO.AddDTO dto) {
//        return success(operateLogService.add(dto));
//    }
//
//    /**
//    * 修改
//    * @author hyj
//    * @date:  2024-05-22
//    * @param dto
//    * @return ApiResult
//    */
//    @PostMapping("/update")
//    @LogAction(value = LogActionEnum.UPDATE, desc = "操作日志表修改")
//        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//        tableField = "create_user_id",
//        menuCode = "dmp:operateLog:update",
//        serviceClass = OperateLogService.class,
//        keyIdName = "id")
//    public ApiResult<?> update(@RequestBody @Validated OperateLogDTO.UpdateDTO dto) {
//        operateLogService.update(dto);
//        return success();
//    }


    /**
     * 操作日志-列表查询
     * @author Will
     * @date: 2023/3/17 10:58
     * @param dto
     * @return ApiResult<PagingVO<listDTO>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<OperateLogDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<OperateLogDTO.SearchDTO> dto){
        PagingVO<OperateLogDTO.ListDTO> pagingVO = operateLogService.paging(dto);
        return success(pagingVO);
    }
}
