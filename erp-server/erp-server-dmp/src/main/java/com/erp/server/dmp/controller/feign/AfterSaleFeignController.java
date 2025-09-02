package com.erp.server.dmp.controller.feign;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.AfterSaleDTO;
import com.erp.model.dmp.dto.AfterSaleProgressDTO;
import com.erp.model.dmp.dto.ThridUserInfoDTO;
import com.erp.server.dmp.service.AfterSaleService;
import com.erp.server.dmp.service.ThridUserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public ApiResult<ThridUserInfoDTO.AddResultDTO> addThridUser(@RequestBody @Validated ThridUserInfoDTO.AddDTO dto) {
        dto.setType("wx");
        return success(thridUserInfoService.add(dto));
    }
    /**
     * 新增
     * @author jack
     * @date:  2025-04-06
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/add")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody AfterSaleDTO.AddDTO dto) {
        return success(afterSaleService.addAndSubmit(dto));
    }

    /**
     * 获取寄修进度
     * @author jack
     * @date:  2025-04-06
     * @return ApiResult
     */
    @PostMapping("/getRepairRecord")
    public ApiResult<AfterSaleProgressDTO.RepairRecordDTO>  getRepairRecord(@RequestBody @Validated AfterSaleDTO.ProgressDTO dto) {
        return success(afterSaleService.getRepairProgress(dto));
    }


    /**
     * 获取寄修历史
     * @author jack
     * @date:  2025-04-06
     * @return ApiResultwo
     */
    @PostMapping("/getRepairHistory")
    public ApiResult<List<AfterSaleProgressDTO.RepairHistoryListDTO>> getRepairHistory(@RequestBody @Validated AfterSaleDTO.ThridUserDTO dto) {
        return success(afterSaleService.getRepairHistory(dto));
    }

    /**
     * 根据订单编号查询明细
     * @Author jack
     * @since 2025-04-07
     */
    @GetMapping("/getDetailByPlatformCode")
    ApiResult<List<AfterSaleDTO.DropDownDTO>> getDetailByPlatformCode(@RequestParam("platformCode") String platformCode){
        return success(afterSaleService.getDetailByPlatformCode(platformCode));
    }

    /**
     * 获取节点配置信息
     * @Author jack
     * @since 2025-04-07
     */
    @GetMapping("/getNodeList")
    ApiResult<List<AfterSaleDTO.NodeDTO>> getNodeList(){
        return success(afterSaleService.getNodeList());
    }


    /**
     * 更新客户运单号
     * @author jack
     * @date:  2025-04-06
     * @return ApiResult
     */
    @PostMapping("/udpateTrackNo")
    public ApiResult<Boolean> udpateTrackNo(@RequestBody @Validated AfterSaleDTO.UpdateTrackNoDTO dto) {
        Boolean b = afterSaleService.udpateTrackNo(dto);
        return Boolean.TRUE.equals(b) ? success(b) : failure(b);
    }

    @PostMapping("/code2Session")
    ApiResult<ThridUserInfoDTO.CodeToSessionResp> code2Session(@RequestBody ThridUserInfoDTO.CodeToSessionDTO dto){
        return success(thridUserInfoService.code2Session(dto));
    }

    @GetMapping("/invalidByCode")
    public ApiResult<BatchResultDTO> invalidByCode(@RequestParam("code") String code){
        return success(afterSaleService.invalidByCode(code));
    }

    /**
     * 详情
     * @author jack
     * @date:  2025-04-06
     * @param id
     * @return ApiResult<AfterSaleDTO.ViewDTO>>
     */
    @GetMapping("/view")
    public ApiResult<AfterSaleDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(afterSaleService.view(id));
    }

}
