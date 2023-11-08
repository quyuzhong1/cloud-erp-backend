package com.erp.server.plm.controller.api;/**
 * @author Lambda
 * @Classname LogisticsController
 * @Description TODO
 * @Date 2023-11-06 12:24
 * @Created by yl
 */

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.server.plm.service.LogisticsProductService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 物流产品
 *
 * @Author yl
 * @Date 2023-11-06 12:24
 */
@RestController
@LogSystemModule("物流产品")
@RequestMapping("logistics/product")
public class LogisticsProductController extends BaseController {

    @Resource
    private LogisticsProductService logisticsProductService;


    /**
     * 分页列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<LogisticsProductDTO.PagingVO>> paging(@RequestBody @Valid PagingDTO<LogisticsProductDTO.PagingParamDTO> dto) {
        PagingVO<LogisticsProductDTO.PagingVO> pagingVO = logisticsProductService.paging(dto);
        return success(pagingVO);

    }

    /**
     * 详情
     *
     * @param id
     * @return
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<LogisticsProductDTO.ViewDTO> view(@RequestParam(value = "id") String id) {
        LogisticsProductDTO.ViewDTO viewDTO = logisticsProductService.view(id);
        return success(viewDTO);

    }

    /**
     * 修改
     *
     * @param dto
     * @return
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改销售出库单")
    public ApiResult update(@RequestBody @Valid LogisticsProductDTO.UpdateDTO dto) {
        Boolean updateResult = logisticsProductService.update(dto);
        return updateResult?success():failure();

    }
}
