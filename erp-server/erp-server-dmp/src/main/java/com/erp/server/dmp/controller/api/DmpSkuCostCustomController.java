package com.erp.server.dmp.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.dmp.dto.DmpSkuCostCustomDTO;
import com.erp.model.dmp.entity.DmpOrderItemSplitEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.service.DmpOrderItemSplitService;
import com.erp.server.dmp.service.DmpSkuCostCustomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * sku自定义成本表
 *
 * @author will
 * @since 2024-01-04
 */
@Slf4j
@RestController
@LogSystemModule("sku自定义成本表")
@RequestMapping("/dmpSkuCostCustom")
public class DmpSkuCostCustomController extends BaseController {

    @Resource
    private DmpSkuCostCustomService dmpSkuCostCustomService;
    @Resource
    private DmpOrderItemSplitService dmpOrderItemSplitService;

    /**
    * 新增
    * @author will
    * @date:  2024-01-04
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "sku自定义成本表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpSkuCostCustomDTO.AddDTO dto) {
        return success(dmpSkuCostCustomService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-01-04
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "sku自定义成本表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpSkuCostCustom:update",
        serviceClass = DmpSkuCostCustomService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpSkuCostCustomDTO.UpdateDTO dto) {
        dmpSkuCostCustomService.update(dto);
        return success();
    }


    @GetMapping("/test")
    public void test(@RequestParam String id){
        List<DmpOrderItemSplitEntity> itemEntityList = dmpOrderItemSplitService.listByIds(Arrays.asList(id));
        List<DmpOrderItemSplitEntity> itemEntityList1 = dmpOrderItemSplitService.splitOrderItem(itemEntityList, PlatformEnum.MABANG.getDesc());
        System.out.println(itemEntityList1);
    }

}
