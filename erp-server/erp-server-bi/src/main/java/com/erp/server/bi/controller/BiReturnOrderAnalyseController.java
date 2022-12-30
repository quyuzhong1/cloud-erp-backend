package com.erp.server.bi.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.dto.ReturnOrderFilterDTO;
import com.erp.model.bi.vo.DateBarAndLineVO;
import com.erp.model.bi.vo.DateReturnOrderVO;
import com.erp.model.bi.vo.ReturnOrderAnalyseTableVO;
import com.erp.server.bi.service.BiReturnOrderAnalyseService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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
     * @return com.erp.common.dto.base.ApiResult<com.erp.common.vo.List<com.erp.model.bi.vo.ReturnOrderAnalyseTableVo>>
     **/
    @PostMapping("/returnOrderAnalByCategoryPaging")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:return:order:returnOrderAnalByCategoryPaging", tableAlias = "doi")
    public ApiResult<List<ReturnOrderAnalyseTableVO>> returnOrderAnalByCategoryPaging(@RequestBody @Validated BiFilterDTO dto) {
        List<ReturnOrderAnalyseTableVO> returnOrderAnalyseTableVoList = biReturnOrderAnalyseService.returnOrderAnalByCategoryPaging(dto);
        return success(returnOrderAnalyseTableVoList);
    }

    /**
     * 退货分析-店铺
     * @Author Luo_WG
     * @Date 2022/12/20 16:48
     * @param dto dto
     * @return com.erp.common.dto.base.ApiResult<com.erp.common.vo.List<com.erp.model.bi.vo.ReturnOrderAnalyseTableVo>>
     **/
    @PostMapping("/returnOrderAnalByShopPaging")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:return:order:returnOrderAnalByShopPaging", tableAlias = "doi")
    public ApiResult<List<ReturnOrderAnalyseTableVO>> returnOrderAnalByShopPaging(@RequestBody @Validated BiFilterDTO dto) {
        List<ReturnOrderAnalyseTableVO> returnOrderAnalyseTableVoList = biReturnOrderAnalyseService.returnOrderAnalByShopPaging(dto);
        return success(returnOrderAnalyseTableVoList);
    }

    /**
     * 退货分析-平台
     * @Author Luo_WG
     * @Date 2022/12/20 16:48
     * @param dto dto
     * @return com.erp.common.dto.base.ApiResult<com.erp.common.vo.List<com.erp.model.bi.vo.ReturnOrderAnalyseTableVo>>
     **/
    @PostMapping("/returnOrderAnalByPlatformPaging")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:return:order:returnOrderAnalByPlatformPaging", tableAlias = "doi")
    public ApiResult<List<ReturnOrderAnalyseTableVO>> returnOrderAnalByPlatformPaging(@RequestBody @Validated BiFilterDTO dto) {
        List<ReturnOrderAnalyseTableVO> returnOrderAnalyseTableVoList = biReturnOrderAnalyseService.returnOrderAnalByPlatformPaging(dto);
        return success(returnOrderAnalyseTableVoList);
    }

    /**
     * 退货分析-事业部
     * @Author Luo_WG
     * @Date 2022/12/20 16:48
     * @param dto dto
     * @return com.erp.common.dto.base.ApiResult<com.erp.common.vo.List<com.erp.model.bi.vo.ReturnOrderAnalyseTableVo>>
     **/
    @PostMapping("/returnOrderAnalByDeptPaging")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:return:order:returnOrderAnalByDeptPaging", tableAlias = "doi")
    public ApiResult<List<ReturnOrderAnalyseTableVO>> returnOrderAnalByDeptPaging(@RequestBody @Validated BiFilterDTO dto) {
        List<ReturnOrderAnalyseTableVO> returnOrderAnalyseTableVoList = biReturnOrderAnalyseService.returnOrderAnalByDeptPaging(dto);
        return success(returnOrderAnalyseTableVoList);
    }

    /**
     * 退货分析-日期
     * @Author Luo_WG
     * @Date 2022/12/20 16:48
     * @param dto dto
     * @return com.erp.common.dto.base.ApiResult<com.erp.common.vo.List<com.erp.model.bi.vo.ReturnOrderAnalyseTableVo>>
     **/
    @PostMapping("/returnOrderAnalByDate")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:return:order:returnOrderAnalByDate", tableAlias = "doi")
    public ApiResult<DateBarAndLineVO> returnOrderAnalByDate(@RequestBody @Validated ReturnOrderFilterDTO dto) {
        DateBarAndLineVO dateBarAndLineVO = biReturnOrderAnalyseService.returnOrderAnalByDate(dto);
        return success(dateBarAndLineVO);
    }
}
