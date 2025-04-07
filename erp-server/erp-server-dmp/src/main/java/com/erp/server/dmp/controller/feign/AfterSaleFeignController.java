package com.erp.server.dmp.controller.feign;

import com.common.business.dto.base.BaseResultDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.AfterSaleDTO;
import com.erp.model.dmp.dto.AfterSaleProgressDTO;
import com.erp.model.dmp.dto.ThridUserInfoDTO;
import com.erp.server.dmp.service.AfterSaleService;
import com.erp.server.dmp.service.ThridUserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author jack
 * @description:售后申请
 */
@Slf4j
@RestController
@RequestMapping("feign/afterSale")
public class AfterSaleFeignController extends BaseController {

    @Resource
    private AfterSaleService afterSaleService;


    @Resource
    private ThridUserInfoService thridUserInfoService;


    /**
     * @return
     * @author jack
     * @date: 2025-04-06
     */
    @PostMapping("/addThridUser")
    public BaseResultDTO.AddDTO addThridUser(@RequestBody @Validated ThridUserInfoDTO.AddDTO dto) {
        return thridUserInfoService.add(dto);
    }
    /**
     * 新增
     * @author jack
     * @date:  2025-04-06
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/add")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated AfterSaleDTO.AddDTO dto) {
        return success(afterSaleService.add(dto));
    }

    /**
     * 获取寄修进度
     * @author jack
     * @date:  2025-04-06
     * @return ApiResult
     */
    @PostMapping("/getRepairRecord")
    public ApiResult<List<AfterSaleProgressDTO.RepairRecordListDTO>> getRepairRecord(@RequestBody @Validated AfterSaleDTO.ProgressDTO dto) {
        return success(afterSaleService.getRepairProgress(dto));
    }


    /**
     * 获取寄修历史
     * @author jack
     * @date:  2025-04-06
     * @return ApiResult
     */
    @PostMapping("/getRepairHistory")
    public ApiResult<List<AfterSaleProgressDTO.RepairHistoryListDTO>> getRepairHistory(@RequestBody @Validated AfterSaleDTO.ProgressDTO dto) {
        return success(afterSaleService.getRepairHistory(dto));
    }

}
