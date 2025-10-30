package com.erp.server.oms.controller.feign;


import com.common.business.dto.base.*;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.ExhibitionOrderDTO;
import com.erp.model.oms.entity.ExhibitionOrderEntity;
import com.erp.server.oms.service.ExhibitionOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 展会订单信息
 *
 * @author jack
 * @since 2025-08-29
 */
@Slf4j
@RestController
@LogSystemModule("展会订单信息")
@RequestMapping("/feign/exhibitionOrder")
public class ExhibitionOrderFeignController extends BaseController {

    @Resource
    private ExhibitionOrderService exhibitionOrderService;

    /**
    * 新增
    * @author jack
    * @date:  2025-08-29
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/listFreezeQtyBySku")
    public List<ExhibitionOrderDTO.FreezeQtyBySku> listFreezeQtyBySku(@RequestBody ExhibitionOrderDTO.SearchDTO dto) {
        return exhibitionOrderService.listFreezeQtyBySku(dto);
    }

    @PostMapping("/generateDownstreamByExhibitionOrder")
    public ExhibitionOrderDTO.DownstreamDTO generateDownstreamByExhibitionOrder(@RequestBody String exhibitionOrderId){
        return exhibitionOrderService.generateDownstreamByExhibitionOrder(exhibitionOrderId);
    }

    /**
     * 展会订单审核
     * @author jack
     * @date:  2025-10-29
     * @param baseApproveParamDTO
     * @return List<BatchResultDTO>
     */
    @PostMapping("/approve")
    public List<BatchResultDTO> approve(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        List<String> ids = baseApproveParamDTO.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>();
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                ApproveOneDTO approveOneDTO = new ApproveOneDTO();
                approveOneDTO.setId(id);
                approveOneDTO.setType(baseApproveParamDTO.getType());
                approveOneDTO.setComment(baseApproveParamDTO.getComment());
                approveResult = exhibitionOrderService.approve(approveOneDTO);
            } catch (Exception e) {
                log.error("展会订单审核失败", e);
                ExhibitionOrderEntity entity = exhibitionOrderService.getById(id);
                if (entity == null) {
                    approveResult = BatchResultDTO.fail(id, id, "展会订单不存在, 审核失败");
                } else {
                    approveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
                }
            }
            resultDTOS.add(approveResult);
        }
        return resultDTOS;
    }
}
