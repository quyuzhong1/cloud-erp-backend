package com.erp.server.oms.controller.api;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.server.oms.service.SoB2cErrorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * B2C销售订单异常信息
 *
 * @author lambda
 * @since 2023-12-20
 */
@Slf4j
@RestController
@LogSystemModule("B2C销售订单异常表")
@RequestMapping("/soB2cError")
public class SoB2cErrorController extends BaseController {

    @Resource
    private SoB2cErrorService soB2cErrorService;


    /**
     * 获取异常订单详情
     * @param dto
     * @return
     */
    @PostMapping("/view")
    public ApiResult<SoB2cErrorDTO.ViewDTO> delete(@RequestBody SoB2cErrorDTO.InfoDTO dto){
        SoB2cErrorDTO.ViewDTO result=soB2cErrorService.info(dto);
        return success(result);
    }

    /**
     * 订单标记发货失败后再次触发 ids 为销售订单id
     *
     */
    @PostMapping("/retryFalseDelivery")
    public ApiResult<List<BatchResultDTO>> retryDelivery(@RequestBody BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        //id 为销售订单id
        for (String id : dto.getIds()) {
            try {
                BatchResultDTO resultDTO = soB2cErrorService.retryFalseDelivery(id);
                resultDTOS.add(resultDTO);
            } catch (Exception e) {
                log.error("重新标记发货失败", e);
                resultDTOS.add(BatchResultDTO.fail(id, id,  CharSequenceUtil.format("标记发货失败：{}", e.getMessage())));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }







}
