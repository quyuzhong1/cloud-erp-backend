package com.erp.server.oms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.EnumCacheUtils;
import com.erp.model.oms.dto.SoB2cReturnDTO;
import com.erp.model.oms.enums.SoB2cReturnReasonEnum;
import com.erp.model.oms.enums.SoB2cReturnTypeEnum;
import com.erp.model.plm.dto.DictControllerDTO;
import com.erp.model.wms.enums.ReturnTypeEnum;
import com.erp.server.oms.query.SoB2cReturnQueryHandler;
import com.erp.server.oms.service.SoB2cReturnService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
     * 分页
     *
     * @return
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = SoB2cReturnQueryHandler.class)
    public ApiResult<PagingVO<SoB2cReturnDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<SoB2cReturnDTO.PagingParamDTO> dto) {
        PagingVO<SoB2cReturnDTO.PagingViewDTO> pagingVO = soB2cReturnService.paging(dto);
        return success(pagingVO);
    }
    /**
     * 退款订单导出
     *
     * @return
     */
    @PostMapping("/export")
    @WebAdvanceQuery(handler = SoB2cReturnQueryHandler.class)
    public ApiResult export(@RequestBody @Validated SoB2cReturnDTO.PagingParamDTO dto) {
        soB2cReturnService.exportExcel(dto);
        return success();
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
    /**
     * 删除
     * @return
     */
    @PostMapping("/delete")
    public ApiResult<Boolean> delete(@RequestBody @Valid BaseIdsDTO.IdsDTO idsDTO) {
        return success(soB2cReturnService.delete(idsDTO.getIds()));
    }

    @GetMapping("getSoB2cReturnReason")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> getSoB2cReturnReason(@RequestParam(value = "type")String type) {
        List<SoB2cReturnReasonEnum> soB2cReturnReasonEnumList = SoB2cReturnReasonEnum.listByType(type);
        List<BaseDropDownDTO.CommonDTO> list = new ArrayList<>();
        soB2cReturnReasonEnumList.forEach(v->{
            list.add(new BaseDropDownDTO.CommonDTO(v.getCode(),v.getName()));
        });
        return success(list);
    }

    @GetMapping("getSoReturnType")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> getSoReturnType() {
        List<ReturnTypeEnum> returnTypeEnumList = Arrays.asList(ReturnTypeEnum.values());
        List<SoB2cReturnTypeEnum> soB2cReturnTypeEnumList = Arrays.asList(SoB2cReturnTypeEnum.values());
        List<BaseDropDownDTO.CommonDTO> list = new ArrayList<>();
        returnTypeEnumList.forEach(v->{
            list.add(new BaseDropDownDTO.CommonDTO(v.getCode(),v.getName()));
        });
        soB2cReturnTypeEnumList.forEach(v->{
            list.add(new BaseDropDownDTO.CommonDTO(v.getCode(),v.getName()));
        });
        return success(list);
    }
}
