package com.erp.server.plm.controller;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.vo.BomOperateVO;
import com.erp.server.plm.service.BomOperateLogService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * bom 操作记录日志表(BomOperateLog)表控制层
 *
 * @author yl
 * @since 2023-01-09 11:42:04
 */
@RestController
@RequestMapping("plm/bom/operate")
public class BomOperateLogController extends BaseController {
    /**
     * 服务对象
     */
    @Resource
    private BomOperateLogService bomOperateLogService;


    /**
     * 获取bom 的操作记录
     *
     * @param
     * @return 新增结果
     */
    @PostMapping("/log")
    public ApiResult<PagingVO<List<BomOperateVO>>> getOperateLog(@RequestBody @Validated PagingDTO<BaseIdDTO> dto) {
        PagingVO<List<BomOperateVO>> pagingVO = bomOperateLogService.paging(dto);
        return success(pagingVO);
    }


}

