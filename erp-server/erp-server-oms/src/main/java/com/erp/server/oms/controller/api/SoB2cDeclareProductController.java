package com.erp.server.oms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.erp.model.oms.entity.SoB2cDeclareProductEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.server.oms.service.SoB2cService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;

import org.springframework.context.annotation.Lazy;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.SoB2cDeclareProductService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.SoB2cDeclareProductDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * B2C销售订单申报产品信息表
 *
 * @author zdy
 * @since 2024-05-09
 */
@Slf4j
@RestController
@LogSystemModule("B2C销售订单申报产品信息表")
@RequestMapping("/soB2cDeclareProduct")
public class SoB2cDeclareProductController extends BaseController {

    @Resource
    private SoB2cDeclareProductService soB2cDeclareProductService;

    @Resource
    @Lazy
    private SoB2cService soB2cService;
    /**
     * 获取销售订单申报信息
     * @author zdy
     * @date:  2024-05-09
     * @param id
     * @return ApiResult<SoB2cDeclareProductDTO.ViewDTO>
     */
    @GetMapping("/listBySoId")
    @LogAction(value = LogActionEnum.INSERT, desc = "B2C销售订单申报产品信息表新增")
    public ApiResult<List<SoB2cDeclareProductEntity>> listBySoId(@RequestParam(value = "id") String id) {
        return success(soB2cDeclareProductService.listBySoId(id));
    }
    /**
    * 新增
    * @author zdy
    * @date:  2024-05-09
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "B2C销售订单申报产品信息表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SoB2cDeclareProductDTO.AddDTO dto) {
        return success(soB2cDeclareProductService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2024-05-09
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "B2C销售订单申报产品信息表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:soB2cDeclareProduct:update",
        serviceClass = SoB2cDeclareProductService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SoB2cDeclareProductDTO.UpdateDTO dto) {
        soB2cDeclareProductService.update(dto);
        return success();
    }


    /**
     * 批量修改报关
     * @author zdy
     * @date:  2024-05-09
     * @param dtoList
     * @return ApiResult
     */
    @PostMapping("/batchUpdate")
    @LogAction(value = LogActionEnum.UPDATE, desc = "B2C销售订单申报产品信息表批量修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soB2cDeclareProduct:batchUpdate",
            serviceClass = SoB2cDeclareProductService.class,
            keyIdName = "id")
    public ApiResult<?> batchUpdate(@RequestBody @Validated List<SoB2cDeclareProductDTO.UpdateDTO> dtoList) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dtoList.size());
        for (SoB2cDeclareProductDTO.UpdateDTO dto : dtoList) {
            BatchResultDTO submit = null;
            try {
                Boolean result = soB2cDeclareProductService.update(dto);
                if (result){
                    submit = new BatchResultDTO();
                    submit.setSuccess(Boolean.TRUE);
                    submit.setMsg("操作成功");
                }
            } catch (Exception e) {
                log.error("B2C销售订单 批量更新报关异常", e);

                SoB2cEntity entity = soB2cService.getById(dto.getSoId());
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(dto.getId(), dto.getSoId(), "B2C销售订单不存在, 批量更新报关失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(dto.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

}
