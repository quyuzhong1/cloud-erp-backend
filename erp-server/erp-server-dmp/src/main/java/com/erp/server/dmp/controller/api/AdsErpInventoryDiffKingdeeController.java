package com.erp.server.dmp.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.erp.model.dmp.entity.doris.AdsErpInventoryDiffKingdeeEntity;
import com.erp.server.dmp.query.AdsErpInventoryDiffKingdeeQueryHandler;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.AdsErpInventoryDiffKingdeeService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.AdsErpInventoryDiffKingdeeDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 金蝶库存差异
 *
 * @author Jim
 * @since 2025-11-13
 */
@Slf4j
@RestController
@LogSystemModule("金蝶库存差异")
@RequestMapping("/adsErpInventoryDiffKingdee")
public class AdsErpInventoryDiffKingdeeController extends BaseController {

    @Resource
    private AdsErpInventoryDiffKingdeeService adsErpInventoryDiffKingdeeService;


    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/statistics")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:adsErpInventoryDiffKingdee:paging",
            tableAlias = "aeidk"
    )
    @WebAdvanceQuery
    public ApiResult<AdsErpInventoryDiffKingdeeDTO.StatisticsDTO> statistics(@RequestBody @Validated PagingDTO<AdsErpInventoryDiffKingdeeDTO.PagingParamDTO> dto) {
       return success(adsErpInventoryDiffKingdeeService.statistics(dto));
    }

    /**
    * 列表查询
    * @author Jim
    * @date: 2025-11-13
    * @param dto
    * @return ApiResult<PagingVO<AdsErpInventoryDiffKingdeeDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:adsErpInventoryDiffKingdee:paging",
            tableAlias = "aeidk"
    )
    @WebAdvanceQuery(handler = AdsErpInventoryDiffKingdeeQueryHandler.class)
    public ApiResult<PagingVO<AdsErpInventoryDiffKingdeeDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<AdsErpInventoryDiffKingdeeDTO.PagingParamDTO> dto) {
        return success(adsErpInventoryDiffKingdeeService.paging(dto));
    }


    /**
    * 详情
    * @author Jim
    * @date:  2025-11-13
    * @param id
    * @return ApiResult<AdsErpInventoryDiffKingdeeDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:adsErpInventoryDiffKingdee:view",
            serviceClass = AdsErpInventoryDiffKingdeeService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<AdsErpInventoryDiffKingdeeDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(adsErpInventoryDiffKingdeeService.view(id));
    }

    /**
     * 编辑备注
     * @author Jim
     * @date: 2025-11-13
     */
    @PostMapping("/updateRemark")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "编辑备注")
    public ApiResult<List<BatchResultDTO>> updateRemark(@RequestBody @Validated BaseIdsDTO.BlankRemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = adsErpInventoryDiffKingdeeService.updateRemark(id,dto.getRemark());
            }catch (Exception e){
                log.error("金蝶库存差异编辑备注",e);
                AdsErpInventoryDiffKingdeeEntity entity = adsErpInventoryDiffKingdeeService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "金蝶库存差异不存在,平台库存差异备注失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getSkuNo(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 导出Excel数据
    * @author Jim
    * @date:  2025-11-13
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:adsErpInventoryDiffKingdee:export",
            tableAlias = "aeidk"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "金蝶库存差异导出Excel数据")
    @WebAdvanceQuery(handler = AdsErpInventoryDiffKingdeeQueryHandler.class)
    public ApiResult<Boolean> exportList(@RequestBody @Validated AdsErpInventoryDiffKingdeeDTO.ExportDTO dto, HttpServletResponse response) {
        Boolean result = adsErpInventoryDiffKingdeeService.exportList(dto, response);
        return result ? ApiResult.success(result) : ApiResult.error(result.toString());
    }

    /**
     * 重新生成
     * @author Jim
     * @date: 2025-11-13
     */
    @PostMapping("/generateDiff")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "重新生成")
    public ApiResult<Boolean> generateDiff(@RequestBody @Validated AdsErpInventoryDiffKingdeeDTO.GenerateDiffDTO dto) {
        //  请求restCloud
        Boolean result = adsErpInventoryDiffKingdeeService.generateDiff(dto);
        return ApiResult.success(result);
    }
}
