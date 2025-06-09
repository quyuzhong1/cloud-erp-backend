package com.erp.server.oms.controller.feign;


import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.anno.LogAction;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.entity.SoPriceChangeEntity;
import com.erp.model.oms.entity.SoPriceEntity;
import com.erp.server.oms.service.SoPriceChangeService;
import com.erp.server.oms.service.SoPriceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 销售价目表feign
 * @author will
 * @date 2025/3/31 18:13
 */
@RestController
@RequestMapping("feign/soPrice")
@Slf4j
public class SoPriceFeignController extends BaseController {
    @Resource
    private SoPriceService soPriceService;
    @Resource
    private SoPriceChangeService soPriceChangeService;

    /**
     * 销售价目表审核
     * @author will
     * @date 2025/3/31 18:12
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核销售价目")
    @PostMapping("/approve")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoPriceEntity> entityList = soPriceService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SoPriceEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"销售价目不存在"));
                continue;
            }
            try {
                resultDTOS.add(soPriceService.approve(entity,new ApproveOneDTO(id, dto.getType(),dto.getComment())));
            }catch (Exception e){
                log.error("销售价目审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 销售调价表审核
     * @author will
     * @date 2025/3/31 18:14
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核销售调价单")
    @PostMapping("/approveChange")
    public ApiResult<List<BatchResultDTO>> approveChange(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoPriceChangeEntity> entityList = soPriceChangeService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SoPriceChangeEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"销售调价单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(soPriceChangeService.approve(entity,new ApproveOneDTO(id, dto.getType(),dto.getComment())));
            }catch (Exception e){
                log.error("销售调价审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
