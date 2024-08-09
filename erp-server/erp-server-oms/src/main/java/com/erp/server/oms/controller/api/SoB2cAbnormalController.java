package com.erp.server.oms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.SoB2cAbnormalDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.server.oms.query.SoB2cAbnormalQueryHandler;
import com.erp.server.oms.service.SoB2cAbnormalService;
import com.erp.server.oms.service.SoB2cService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 异常订单
 * @author Will
 * @date: 2024/4/25 18:52
 */
@Slf4j
@RestController
@LogSystemModule("异常订单")
@RequestMapping("/soB2cAbnormal")
public class SoB2cAbnormalController extends BaseController {

    @Resource
    private SoB2cAbnormalService soB2cAbnormalService;

    @Resource
    private SoB2cService soB2cService;

    /**
     * 异常订单分页查询
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = SoB2cAbnormalQueryHandler.class)
    public ApiResult<PagingVO<SoB2cAbnormalDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SoB2cAbnormalDTO.PagingParamDTO> dto) {
        return success(soB2cAbnormalService.abnormalPaging(dto));
    }


    /**
     * 异常订单导出
     * @author Will
     * @date: 2024/4/22 19:50
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出B2C销售异常订单信息")
    @PostMapping(value = "/abnormalExportExcel")
    public ApiResult abnormalExportExcel(@RequestBody SoB2cAbnormalDTO.PagingParamDTO dto) {
        Boolean flag = soB2cAbnormalService.abnormalExportExcel(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 批量重试
     * @author Will
     * @date: 2024/4/26 19:28
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "批量重试")
    @PostMapping(value = "/batchRetry")
    public ApiResult<List<BatchResultDTO>> batchRetry(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>();
        //id去重
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
        for (String id : ids) {
            try {
                List<BatchResultDTO>  resultList = soB2cAbnormalService.batchRetry(id);
                resultDTOS.addAll(resultList);
            }catch (Exception e){
                log.error("b2c销售订单 批量重试失败",e);
                BatchResultDTO batchResultDTO;
                SoB2cEntity entity = soB2cService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    batchResultDTO = BatchResultDTO.fail(id, id, "b2c销售订单不存在, 批量重试失败");
                    resultDTOS.add(batchResultDTO);
                    continue;
                }
                batchResultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
                resultDTOS.add(batchResultDTO);
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
