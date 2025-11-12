package com.erp.server.dmp.controller.api;


import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.dmp.dto.AdsErpOutstockDiffFlowDTO;
import com.erp.server.dmp.query.AdsErpOutstockDiffFlowQueryHandler;
import com.erp.server.dmp.service.AdsErpOutstockDiffFlowService;

import lombok.extern.slf4j.Slf4j;

/**
 * 第三方仓出库单据差异表
 *
 * @author shukai
 * @since 2025-11-12
 */
@Slf4j
@RestController
@LogSystemModule("第三方仓出库单据差异表")
@RequestMapping("/adsErpOutstockDiffFlow")
public class AdsErpOutstockDiffFlowController extends BaseController {

    @Resource
    private AdsErpOutstockDiffFlowService adsErpOutstockDiffFlowService;

    /**
     * 分页查询，菜单code = dmp:adsErpOutstockDiffFlow:paging
     * 
     * @author Will
     * @date: 2023/11/13 15:12
     * @param dto
     * @return ApiResult<PagingVO<AdsErpOutstockDiffFlowDTO.ViewDTO>>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = AdsErpOutstockDiffFlowQueryHandler.class)
    public ApiResult<PagingVO<AdsErpOutstockDiffFlowDTO.PagingDTO>> paging(@RequestBody @Validated PagingDTO<AdsErpOutstockDiffFlowDTO.PagingParamDTO> dto) {
        return success(adsErpOutstockDiffFlowService.paging(dto));
    }
    
    /**
     *  统计
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/total")
    @WebAdvanceQuery(handler = AdsErpOutstockDiffFlowQueryHandler.class)
    public ApiResult<AdsErpOutstockDiffFlowDTO.TotalDTO> total(@RequestBody @Validated PagingDTO<AdsErpOutstockDiffFlowDTO.PagingParamDTO> dto) {
        return success(adsErpOutstockDiffFlowService.total(dto));
    }
    
    /**
     * 重新生成
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "平台单据差异重新生成")
    @PostMapping(value = "/reCreate")
    public ApiResult<Boolean> reCreate(@RequestBody @Validated AdsErpOutstockDiffFlowDTO.ReCreateDTO dto) {
        return success(adsErpOutstockDiffFlowService.reCreate(dto));
    }
    
    /**
     *  导出Excel
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "平台单据差异信息导出")
    @PostMapping(value = "/exportExcel")
    public ApiResult<Boolean> exportExcel(@RequestBody @Validated AdsErpOutstockDiffFlowDTO.ExpotParamDTO dto) {
        return success(adsErpOutstockDiffFlowService.exportExcel(dto));
    }
    
    
    /**
    * 新增
    * @author shukai
    * @date:  2025-11-12
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "第三方仓出库单据差异表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated AdsErpOutstockDiffFlowDTO.AddDTO dto) {
        return success(adsErpOutstockDiffFlowService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2025-11-12
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "第三方仓出库单据差异表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:adsErpOutstockDiffFlow:update",
        serviceClass = AdsErpOutstockDiffFlowService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated AdsErpOutstockDiffFlowDTO.UpdateDTO dto) {
        adsErpOutstockDiffFlowService.update(dto);
        return success();
    }



}
