package com.erp.server.oms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.RefundOrderDTO;
import com.erp.model.oms.dto.SoReturnDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.SoB2cReturnService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.SoB2cReturnDTO;

import java.util.List;

/**
 * b2c退货订单
 *
 * @author lrp
 * @since 2024-10-09
 */
@Slf4j
@RestController
@LogSystemModule("b2c退货订单")
@RequestMapping("/soB2cReturn")
@Validated
public class SoB2cReturnController extends BaseController {

    @Resource
    private SoB2cReturnService soB2cReturnService;

    /**
     * 退款订单分页
     *
     * @return
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<SoB2cReturnDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<SoB2cReturnDTO.PagingParamDTO> dto) {
        PagingVO<SoB2cReturnDTO.PagingViewDTO> pagingVO = soB2cReturnService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 标记已退货
     *
     * @return
     */
    @PostMapping("/markReturned")
    public ApiResult<Boolean> markReturned(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        soB2cReturnService.markReturned(idsDTO);
        return success();
    }

    /**
     * 下推退货通知单view
     *
     * @return
     */
    @PostMapping("/generateSoB2cReturnNoticeView")
    public ApiResult<List<SoB2cReturnDTO.GenerateSoReturnNoticeView>> generateSoReturnNoticeView(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        return success(soB2cReturnService.generateSoReturnNoticeView(idsDTO.getIds()));
    }
    /**
     * 下推退货通知单
     *
     * @return
     */
    @PostMapping("/generateSoB2cReturnNotice")
    public ApiResult<Boolean> generateSoB2cReturnNotice(@RequestBody @Valid List<SoB2cReturnDTO.GenerateSoReturnNoticeView> list) {
        soB2cReturnService.generateSoB2cReturnNotice(list);
        return success();
    }

    /**
     * 绑定退货入库订单View
     * @return
     */
    @PostMapping("/bindReturnInstockView")
    public ApiResult<List<SoB2cReturnDTO.BindReturnInstockViewDTO>> bindReturnInstockView(@RequestBody @Valid BaseIdsDTO.IdsDTO idsDTO) {
        return success(soB2cReturnService.bindReturnInstockView(idsDTO.getIds()));
    }

    /**
     * 匹配退货入库单
     *
     * @return
     */
    @PostMapping("/matchSoReturnInstock")
    public ApiResult<SoB2cReturnDTO.MatchResultDTO> matchSoReturnInstock(@RequestBody @Valid SoB2cReturnDTO.MatchDTO matchDTO) {
        return success(soB2cReturnService.matchSoReturnInstock(matchDTO));
    }
    /**
     * 绑定退货入库订单保存
     * @return
     */
    @PostMapping("/bindReturnInstock")
    public ApiResult<Boolean> bindReturnInstock(@RequestBody @Valid List<SoB2cReturnDTO.BindReturnInstockViewDTO> list) {
        return success(soB2cReturnService.bindReturnInstock(list));
    }
}
