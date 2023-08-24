package com.erp.server.oms.controller.api;


import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.ShopDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.server.oms.service.ShopCostService;
import com.erp.server.oms.service.ShopInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 店铺管理
 *
 * @author Lambda
 * @since 2023-06-28
 */
@Slf4j
@RestController
@RequestMapping("/shop")
public class ShopInfoController extends BaseController {

    @Resource
    private ShopInfoService shopInfoService;

    @Resource
    private ShopCostService shopCostService;


    /**
     * 店铺 分页
     *
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<ShopDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<ShopDTO.PagingParamDTO> dto) {
        PagingVO<ShopDTO.PagingViewDTO> pagingVO = shopInfoService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 添加店铺
     *
     * @return
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated ShopDTO.AddDTO dto) {
        Boolean result = shopInfoService.add(dto);
        return result ? success() : failure();
    }


    /**
     * 修改店铺
     *
     * @return
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated ShopDTO.UpdateDTO dto) {
        String id = shopInfoService.updateShop(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }


    /**
     * 获取店铺详情
     *
     * @return
     */
    @PostMapping("/view")
    public ApiResult<ShopDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        ShopDTO.ViewDTO view = shopInfoService.view(dto.getId());
        return success(view);
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
     * 启用或者禁用店铺
     *
     * @param
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-08-22 14:37
     */
    @PostMapping("/updateStatus")
    public ApiResult updateStatus(@RequestBody @Validated UpdateStateDTO.BatchUpdateDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<String> ids = dto.getIds();
        Boolean disabled = dto.getDisabled();
        for (String id : ids) {
            BatchResultDTO submit;
            String flagCode = id;
            try {
                ShopInfoEntity shop = shopInfoService.getById(id);
                if (Objects.isNull(shop)) {
                    submit = BatchResultDTO.fail(id, "店铺不存在");
                } else {
                    submit = shopInfoService.updateStatus(shop, disabled);
                    flagCode = shop.getName();
                }
            } catch (Exception e) {
                log.error("店铺更改状态失败>>>>{}", e);
                submit = BatchResultDTO.fail(flagCode, e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return success(resultDTOS);
    }

    /**
     * 店铺批量费用设置
     *
     * @return
     */
    @PostMapping("/batchSetCost")
    public ApiResult batchSetCost(@RequestBody @Validated ShopDTO.BatchSetCostDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<String> ids = dto.getIds();
        for (String id : ids) {
            BatchResultDTO submit;
            String flagCode = id;
            try {
                ShopInfoEntity shop = shopInfoService.getById(id);
                if (Objects.isNull(shop)) {
                    submit = BatchResultDTO.fail(id, "店铺不存在");
                } else {
                    submit = shopCostService.batchSetCost(shop, dto);
                    flagCode = shop.getName();
                }
            } catch (Exception e) {
                log.error("店铺设置费率失败>>>>{}", e);
                submit = BatchResultDTO.fail(flagCode, e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return success(resultDTOS);
    }


    /**
     * 单个店铺费用设置
     *
     * @return
     */
    @PostMapping("/setCost")
    public ApiResult setCost(@RequestBody @Validated ShopDTO.SetCostDTO dto) {
        Boolean result = shopCostService.setCost(dto);
        return result ? success() : failure();
    }

    /**
     * 单个店铺费用详情
     *
     * @return
     */
    @PostMapping("/viewCost")
    public ApiResult<ShopDTO.ViewCostDTO> viewCost(@RequestBody @Validated BaseIdDTO dto) {
        ShopDTO.ViewCostDTO result = shopCostService.viewCost(dto.getId());
        return success(result);
    }


}
