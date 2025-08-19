package com.erp.server.oms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.SoB2cDeclareProductDTO;
import com.erp.model.oms.entity.SoB2cDeclareProductEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.server.oms.service.SoB2cDeclareProductService;
import com.erp.server.oms.service.SoB2cService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
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
     * @param dto
     * @return ApiResult<SoB2cDeclareProductDTO.ViewDTO>
     */
    @PostMapping("/listBySoIds")
    public ApiResult<List<SoB2cDeclareProductDTO.ViewDTO>> listBySoIds(@RequestBody @Validated SoB2cDeclareProductDTO.ListDTO dto) {
        List<SoB2cDeclareProductDTO.ViewDTO> list = soB2cDeclareProductService.listViewBySoIds(dto.getIds());
        return success(list);
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
    @LogAction(value = LogActionEnum.UPDATE, desc = "B2C销售订单申报产品信息表批量修改 id={id}")
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
                submit = soB2cDeclareProductService.update(dto);
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
    /**
     *  申报信息导出
     * @author zdy
     * @date: 2024/5/11 10 10:45
     * @param dto
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "申报信息-导出")
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody @Validated SoB2cDeclareProductDTO.ListDTO dto) {
        Boolean flag = soB2cDeclareProductService.exportExcel(dto);
        return flag ? success() : failure();
    }

    /**
     * 详情
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<SoB2cDeclareProductEntity> view(@RequestParam(value = "id") String id) {
        SoB2cDeclareProductEntity entity = soB2cDeclareProductService.getById(id);
        return success(entity);
    }
}
