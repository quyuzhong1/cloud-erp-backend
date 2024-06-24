package com.erp.server.oms.controller.feign;


import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.entity.SoChangeEntity;
import com.erp.server.oms.service.SoChangeService;
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
 * 采购单
 *
 * @Author Luo_WG
 * @Date 2023/5/15 9:12
 **/
@RestController
@RequestMapping("feign/soChange")
@Slf4j
public class SoChangeFeignController extends BaseController {
    @Resource
    private SoChangeService soChangeService;

    /**
     * 销售变更审核
     * @Author Luo_WG
     * @Date 2023/7/4 12:28
     * @param dto
     * @return java.lang.Boolean
     **/
    @PostMapping("/approve")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoChangeEntity> entityList = soChangeService.listByIds(ids);
        for (String id : ids) {
            SoChangeEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"销售变更单不存在"));
                continue;
            }
            try {
                resultDTOS.add(soChangeService.approve(dto, entity));
            }catch (Exception e){
                log.error("销售变更单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
