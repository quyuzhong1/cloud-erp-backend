package com.erp.server.tms.controller.feign;

import com.common.business.dto.base.BaseResultDTO;
import com.common.core.anno.LogSystemModule;
import com.erp.model.tms.dto.TransferDeclareDTO;
import com.erp.model.tms.entity.TransferDeclareDetailEntity;
import com.erp.server.tms.service.TransferDeclareDetailService;
import com.erp.server.tms.service.TransferDeclareService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@LogSystemModule("中转报关单feign接口")
@RequestMapping("/feign/transferDeclare")
public class TransferDeclareFeignController {

    @Resource
    private TransferDeclareService transferDeclareService;

    @Resource
    private TransferDeclareDetailService transferDeclareDetailService;

    /**
     * 新增中转报关单
     * @Author Luo_WG
     * @Date 2024/1/25 18:31
     * @param dto
     * @return com.common.business.dto.base.BaseResultDTO.AddDTO
     **/
    @PostMapping("/add")
    public BaseResultDTO.AddDTO add(@RequestBody TransferDeclareDTO.AddDTO dto) {
        return transferDeclareService.add(dto);
    }

    /**
     * @description 根据销售订单id 获取中转报关信息
     * @param soId 销售订单id
     * @author Lambda
     * @return
     * @create 2024-01-26 9:25
     */
    @GetMapping("/getBySoId")
    public TransferDeclareDetailEntity getBySoId(@RequestParam("soId") String soId){
        return transferDeclareDetailService.getBySoId(soId);
    }

    /**
     * 修改出库状态
     * @Author Luo_WG
     * @Date 2024/2/1 18:39
     * @param soIdList
     * @param status
     * @return java.lang.Boolean
     **/
    @PostMapping("/updateOutstockStatus")
    public Boolean updateOutstockStatus(@RequestParam("soIdList") List<String> soIdList, @RequestParam("status") String status) {
        return transferDeclareDetailService.updateOutstockStatus(soIdList, status);
    }
}
