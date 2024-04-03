package com.erp.rpc.tms.feign;

import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.dto.TransferDeclareDTO;
import com.erp.model.tms.entity.TransferDeclareDetailEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "erp-tms", contextId = "transferDeclare")
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
     * @description 根据销售订单id 获取中转报关信息
     * @param soId 销售订单id
     * @author Lambda
     * @return
     * @create 2024-01-26 9:25
     */
    @GetMapping("/feign/transferDeclare/getBySoId")
    TransferDeclareDetailEntity  getBySoId(@RequestParam("soId") String soId);

    /**
     * 修改出库状态
     * @Author Luo_WG
     * @Date 2024/2/1 18:39
     * @param dto
     * @return java.lang.Boolean
     **/
    @PostMapping("/feign/transferDeclare/updateOutstockStatus")
    Boolean updateOutstockStatus(@RequestBody TransferDeclareDTO.UpdateOutstockStatusDTO dto);


    /**
     * @description: 根据物流渠道id查询
     * @author Will
     * @date: 2024/4/1 12:31
     * @param logisticsChannelIdList
     * @return List<TransferDeclareDetailEntity>
     */
    @PostMapping("/feign/transferDeclare/listByLogisticsChannelIdList")
    List<TransferDeclareDetailEntity> listByLogisticsChannelIdList(@RequestBody List<String> logisticsChannelIdList);
}
