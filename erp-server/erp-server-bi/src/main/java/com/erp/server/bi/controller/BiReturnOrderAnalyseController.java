package com.erp.server.bi.controller;


import com.erp.common.business.annotation.DataPermission;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.DataAttributeEnum;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.dto.ReturnOrderFilterDTO;
import com.erp.model.bi.vo.DateReturnOrderVO;
import com.erp.model.bi.vo.ReturnOrderAnalyseTableVO;
import com.erp.server.bi.service.BiReturnOrderAnalyseService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 退货分析相关/一级模块
 *
 * @Author Luo_WG
 * @Date 2022/12/16 11:07
 **/
@RestController
@RequestMapping("bi/return/order")
public class BiReturnOrderAnalyseController extends BaseController {

    @Resource
    private BiReturnOrderAnalyseService biReturnOrderAnalyseService;

    /**
     * 退货分析-类别
     * @Author Luo_WG
     * @Date 2022/12/20 16:48
     * @param dto dto
     * @return com.erp.common.dto.base.ApiResult<com.erp.common.vo.PagingVO<com.erp.model.bi.vo.ReturnOrderAnalyseTableVo>>
     **/
    @PostMapping("/returnOrderAnalByCategoryPaging")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:return:order:returnOrderAnalByCategoryPaging", tableAlias = "doi")
    public ApiResult<PagingVO<ReturnOrderAnalyseTableVO>> returnOrderAnalByCategoryPaging(@RequestBody @Validated PagingDTO<BiFilterDTO> dto) {
        PagingVO<ReturnOrderAnalyseTableVO> returnOrderAnalyseTableVoPagingVO = biReturnOrderAnalyseService.returnOrderAnalByCategoryPaging(dto);
        return success(returnOrderAnalyseTableVoPagingVO);
    }

    /**
     * 退货分析-店铺
     * @Author Luo_WG
     * @Date 2022/12/20 16:48
     * @param dto dto
     * @return com.erp.common.dto.base.ApiResult<com.erp.common.vo.PagingVO<com.erp.model.bi.vo.ReturnOrderAnalyseTableVo>>
     **/
    @PostMapping("/returnOrderAnalByShopPaging")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:return:order:returnOrderAnalByShopPaging", tableAlias = "doi")
    public ApiResult<PagingVO<ReturnOrderAnalyseTableVO>> returnOrderAnalByShopPaging(@RequestBody @Validated PagingDTO<BiFilterDTO> dto) {
        PagingVO<ReturnOrderAnalyseTableVO> returnOrderAnalyseTableVoPagingVO = biReturnOrderAnalyseService.returnOrderAnalByShopPaging(dto);
        return success(returnOrderAnalyseTableVoPagingVO);
    }

    /**
     * 退货分析-平台
     * @Author Luo_WG
     * @Date 2022/12/20 16:48
     * @param dto dto
     * @return com.erp.common.dto.base.ApiResult<com.erp.common.vo.PagingVO<com.erp.model.bi.vo.ReturnOrderAnalyseTableVo>>
     **/
    @PostMapping("/returnOrderAnalByPlatformPaging")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:return:order:returnOrderAnalByPlatformPaging", tableAlias = "doi")
    public ApiResult<PagingVO<ReturnOrderAnalyseTableVO>> returnOrderAnalByPlatformPaging(@RequestBody @Validated PagingDTO<BiFilterDTO> dto) {
        PagingVO<ReturnOrderAnalyseTableVO> returnOrderAnalyseTableVoPagingVO = biReturnOrderAnalyseService.returnOrderAnalByPlatformPaging(dto);
        return success(returnOrderAnalyseTableVoPagingVO);
    }

    /**
     * 退货分析-事业部
     * @Author Luo_WG
     * @Date 2022/12/20 16:48
     * @param dto dto
     * @return com.erp.common.dto.base.ApiResult<com.erp.common.vo.PagingVO<com.erp.model.bi.vo.ReturnOrderAnalyseTableVo>>
     **/
    @PostMapping("/returnOrderAnalByDeptPaging")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:return:order:returnOrderAnalByDeptPaging", tableAlias = "doi")
    public ApiResult<PagingVO<ReturnOrderAnalyseTableVO>> returnOrderAnalByDeptPaging(@RequestBody @Validated PagingDTO<BiFilterDTO> dto) {
        PagingVO<ReturnOrderAnalyseTableVO> returnOrderAnalyseTableVoPagingVO = biReturnOrderAnalyseService.returnOrderAnalByDeptPaging(dto);
        return success(returnOrderAnalyseTableVoPagingVO);
    }

    /**
     * 退货分析-日期
     * @Author Luo_WG
     * @Date 2022/12/20 16:48
     * @param dto dto
     * @return com.erp.common.dto.base.ApiResult<com.erp.common.vo.PagingVO<com.erp.model.bi.vo.ReturnOrderAnalyseTableVo>>
     **/
    @PostMapping("/returnOrderAnalByDate")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:return:order:returnOrderAnalByDate", tableAlias = "doi")
    public ApiResult<PagingVO<DateReturnOrderVO>> returnOrderAnalByDate(@RequestBody @Validated PagingDTO<ReturnOrderFilterDTO> dto) {
        PagingVO<DateReturnOrderVO> dateReturnOrderVOPagingVO = biReturnOrderAnalyseService.returnOrderAnalByDate(dto);
        return success(dateReturnOrderVOPagingVO);
    }
}
