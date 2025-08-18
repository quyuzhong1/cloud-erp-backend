package com.erp.rpc.tms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BaseResultDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.TransferDeclareDTO;
import com.erp.model.tms.entity.TransferDeclareDetailEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "erp-tms", contextId = "transferDeclare" ,configuration = {FeignErrorDecoder.class})
public interface TransferDeclareFeign {

    /**
     * 新增中转报关单
     * @Author Luo_WG
     * @Date 2024/1/25 18:31
     * @param dto
     * @return com.common.business.dto.base.BaseResultDTO.AddDTO
     **/
    @PostMapping("/feign/transferDeclare/add")
    BaseResultDTO.AddDTO add(@RequestBody TransferDeclareDTO.AddDTO dto);

    /**
     * 批量新增中转报关单
     * @param dtoList
     * @return
     */
    @PostMapping("/feign/transferDeclare/batchAdd")
    List<BaseResultDTO.AddDTO> batchAdd(@RequestBody List<TransferDeclareDTO.AddDTO> dtoList);

    /**
     * @description 根据销售订单id 获取中转报关信息
     * @param soId 销售订单id
     * @author Lambda
     * @return
     * @create 2024-01-26 9:25
     */
    @GetMapping("/feign/transferDeclare/getBySoId")
    TransferDeclareDetailEntity  getBySoId(@RequestParam("soId") String soId);

    /**
     * @description: 根据销售订单id
     */
    @PostMapping("/feign/transferDeclare/listBySoCodeList")
    List<TransferDeclareDetailEntity> listBySoCodeList(@RequestBody List<String> soCodeList);

    /**
     * @description: 根据物流渠道id查询
     * @author Will
     * @date: 2024/4/1 12:31
     * @param logisticsChannelIdList
     * @return List<TransferDeclareDetailEntity>
     */
    @PostMapping("/feign/transferDeclare/listByLogisticsChannelIdList")
    List<TransferDeclareDetailEntity> listByLogisticsChannelIdList(@RequestBody List<String> logisticsChannelIdList);

    @PostMapping("/feign/transferDeclare/b2cOrderForecast")
    TransferDeclareDTO.ShippingOrderDTO b2cOrderForecast(@RequestBody TransferDeclareDTO.B2cOrderForecastDTO b2cOrderForecastDTO);


    /**
     * 取消订单预报
     * @return
     */
    @PostMapping("/feign/transferDeclare/cancelOrderForecast")
    ApiResult<String> cancelOrderForecast(@RequestBody TransferDeclareDTO.CancelOrderForecastDTO cancelOrderForecastDTO);

    /**
     * 批量修改报关单详情上传状态
     * @param list
     * @return
     */
    @PostMapping("/feign/transferDeclare/updateTransferStatusByBatch")
    Boolean updateTransferStatusByBatch(@RequestBody List<TransferDeclareDTO.UpdateForcastStatusDTO> list);

}
