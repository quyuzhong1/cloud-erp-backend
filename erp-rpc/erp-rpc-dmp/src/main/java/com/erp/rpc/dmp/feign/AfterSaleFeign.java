package com.erp.rpc.dmp.feign;


import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.AfterSaleDTO;
import com.erp.model.dmp.dto.AfterSaleProgressDTO;
import com.erp.model.dmp.dto.ThridUserInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 售后申请小程序端接口
 *
 * @author jack
 * @since 2025-04-07
 */
@FeignClient(value = "erp-dmp", path = "feign/afterSale", contextId = "AfterSaleFeign",configuration = {FeignErrorDecoder.class})
public interface AfterSaleFeign {

    /**
     * @return
     * @author jack
     * @date: 2025-04-06
     */
    @PostMapping("/addThridUser")
    ApiResult<ThridUserInfoDTO.AddResultDTO> addThridUser(@RequestBody @Validated ThridUserInfoDTO.AddDTO dto);


    /**
     * 新增
     * @author jack
     * @date:  2025-04-06
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/add")
    ApiResult<BaseResultDTO.AddDTO> add(@RequestBody AfterSaleDTO.AddDTO dto);
    /**
     *
     * 获取寄修进度
     * @Author jack
     * @since 2025-04-07
     **/
    @PostMapping("/getRepairRecord")
    ApiResult<AfterSaleProgressDTO.RepairRecordDTO>  getRepairRecord(@RequestBody  @Validated  AfterSaleDTO.ProgressDTO dto);
    /**
     * 获取寄修历史
     * @Author jack
     * @since 2025-04-07
     */
    @PostMapping("/getRepairHistory")
    ApiResult<List<AfterSaleProgressDTO.RepairHistoryListDTO>> getRepairHistory(@RequestBody  @Validated  AfterSaleDTO.ThridUserDTO dto);
    /**
     * 根据订单编号查询明细
     * @Author jack
     * @since 2025-04-07
     */
    @GetMapping("/getDetailByPlatformCode")
    ApiResult<List<AfterSaleDTO.DropDownDTO>> getDetailByPlatformCode(@RequestParam("platformCode") String platformCode);

    @GetMapping("/getNodeList")
    ApiResult<List<AfterSaleDTO.NodeDTO>> getNodeList();

    /**
     * 更新客户运单号
     * @author jack
     * @date:  2025-04-06
     * @return ApiResult
     */
    @PostMapping("/udpateTrackNo")
    ApiResult<Boolean> udpateTrackNo(@RequestBody @Validated AfterSaleDTO.UpdateTrackNoDTO dto);

    @PostMapping("/code2Session")
    ApiResult<ThridUserInfoDTO.CodeToSessionResp> code2Session(ThridUserInfoDTO.CodeToSessionDTO dto);

    @GetMapping("/invalidByCode")
    ApiResult<BatchResultDTO> invalidByCode(@RequestParam("code") String code);

    @GetMapping("/view")
    ApiResult<AfterSaleDTO.ViewDTO> view(@RequestParam("id") String id);
}