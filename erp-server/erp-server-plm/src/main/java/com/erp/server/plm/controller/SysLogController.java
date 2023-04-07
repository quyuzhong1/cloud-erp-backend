package com.erp.server.plm.controller;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.SysLogSelectDTO;
import com.erp.model.plm.dto.SysLogShowDTO;
import com.erp.model.plm.enums.SysLogClassPathEnum;
import com.erp.server.plm.service.SysLogService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 操作日志
 * @author Will
 * @version 1.0
 * @date 2022/12/5 20:36
 */
@RestController
@RequestMapping("sys/log")
public class SysLogController extends BaseController {

    @Autowired
    private SysLogService sysLogService;

    /**
     * 操作日志-列表查询
     * @author Will
     * @date: 2022/12/5 21:29
     * @param dto
     * @return ApiResult<PagingVO<SysLogShowDTO>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<SysLogShowDTO>> paging(@RequestBody @Validated PagingDTO<SysLogSelectDTO> dto){
        PagingVO<SysLogShowDTO> pagingVO = sysLogService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 操作日志-非分页查询
     * @author Will
     * @date: 2023/1/6 16:57
     * @param dto
     * @return ApiResult<List<SysLogShowDTO>>
     */
    @PostMapping("/list")
    public ApiResult<List<SysLogShowDTO>> list(@RequestBody @Validated SysLogSelectDTO dto){
        List<SysLogShowDTO> list = sysLogService.listSysLog(dto);
        return success(list);
    }

    /**
     * 操作日志-类路径 (0：SKU,1：SPU,2：任务列表)
     * @author Will
     * @date: 2022/12/7 13:26
     * @return ApiResult
     */
    @GetMapping("/getClassPath")
    public ApiResult getClassPath(@Param("type") Integer type) {
        switch (type) {
            case 0:
                return success(SysLogClassPathEnum.PRODUCTDETAILENTITY.getDesc());
            case 1:
                return success(SysLogClassPathEnum.PRODUCTINFOENTITY.getDesc());
            case 2:
                return success(SysLogClassPathEnum.PROJECTTASKENTITY.getDesc());
            default:
                return failure();
        }
    }
}
