package com.erp.server.oms.controller.api;


import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.ShopDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.server.oms.service.ShopInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 店铺管理
 *
 * @author Lambda
 * @since 2023-06-28
 */
@RestController
@LogSystemModule("店铺管理")
@RequestMapping("/shop")
public class ShopInfoController extends BaseController {

    @Resource
    private ShopInfoService shopInfoService;


    /**
     * 添加店铺
     *
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "添加店铺")
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated ShopDTO.AddDTO dto) {
        String id = shopInfoService.add(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }


    /**
     * 修改店铺
     *
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改店铺")
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated ShopDTO.UpdateDTO dto) {
        String id = shopInfoService.updateShop(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }


    /**
     * 获取店铺列表
     *
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<ShopInfoEntity>> list() {
        List<ShopInfoEntity> list = shopInfoService.list();
        return success(list);
    }

    /**
     * 初始同步dmp 店铺信息
     *
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE_WITHOUT_PARAMS, desc = "初始同步dmp店铺信息")
    @PostMapping("/initialSync")
    public ApiResult initialSync() {
        Boolean result = shopInfoService.initialSync();
        return result ? success() : failure();
    }

}
