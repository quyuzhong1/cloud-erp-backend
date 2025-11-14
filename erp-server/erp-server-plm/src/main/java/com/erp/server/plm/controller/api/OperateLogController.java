package com.erp.server.plm.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.OperateLogSelectDTO;
import com.erp.model.plm.dto.OperateLogShowDTO;
import com.erp.model.plm.enums.SysLogClassPathEnum;
import com.erp.server.plm.service.OperateLogService;
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
public class OperateLogController extends BaseController {

    @Autowired
    private OperateLogService operateLogService;

    /**
     * 操作日志-列表查询
     * @author Will
     * @date: 2022/12/5 21:29
     * @param dto
     * @return ApiResult<PagingVO<OperateLogShowDTO>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<OperateLogShowDTO>> paging(@RequestBody @Validated PagingDTO<OperateLogSelectDTO> dto){
        PagingVO<OperateLogShowDTO> pagingVO = operateLogService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 操作日志-非分页查询
     * @author Will
     * @date: 2023/1/6 16:57
     * @param dto
     * @return ApiResult<List<OperateLogShowDTO>>
     */
    @PostMapping("/list")
    public ApiResult<List<OperateLogShowDTO>> list(@RequestBody @Validated OperateLogSelectDTO dto){
        List<OperateLogShowDTO> list = operateLogService.listSysLog(dto);
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

    /**
     * 操作日志-产品变更历史分页查询
     * @author zdy
     * @date: 2025/9/16 16:57
     * @param dto
     * @return ApiResult<PagingVO<OperateLogShowDTO.HistoryDTO>>
     */
    @PostMapping("/productChange/paging")
    public ApiResult<PagingVO<OperateLogShowDTO.HistoryDTO>> getProductChangePaging(@RequestBody @Validated PagingDTO<OperateLogShowDTO.PagingParamDTO> dto) {
        PagingVO<OperateLogShowDTO.HistoryDTO> pagingVO = operateLogService.getProductChangeHistory(dto);
        return success(pagingVO);
    }
}
