package com.erp.server.wms.controller.api;


import com.common.business.annotation.Idempotent;
import com.common.business.dto.base.BaseResultDTO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.AllocateCargoBillPrintDTO;
import com.erp.server.wms.service.AllocateCargoBillPrintService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * 配货单打印
 *
 * @author Luo_WG
 * @since 2023-12-13
 */
@Slf4j
@RestController
@LogSystemModule("配货单")
@RequestMapping("/allocateCargoBill")
public class AllocateCargoBillPrintController extends BaseController {

    @Resource
    private AllocateCargoBillPrintService allocateCargoBillPrintService;

    /**
     * 扫描波次号或拣货车编号
     * @param businessCode 参数
     * @see BaseResultDTO.AddDTO
     */
    @GetMapping("/scanWaveOrPickingCarCode")
    public ApiResult<AllocateCargoBillPrintDTO.ScanWaveDTO> scanWaveOrPickingCarCode(@RequestParam("businessCode") String businessCode){
        return success(allocateCargoBillPrintService.scanWaveOrPickingCarCode(businessCode));
    }

    /**
     * 打印
     * @param waveId 波次id
     * @see BaseResultDTO.AddDTO
     */
    @GetMapping("/print")
    @Idempotent
    public void print(@RequestParam("waveId") String waveId, HttpServletResponse response){
         allocateCargoBillPrintService.print(waveId, response);
    }
}
