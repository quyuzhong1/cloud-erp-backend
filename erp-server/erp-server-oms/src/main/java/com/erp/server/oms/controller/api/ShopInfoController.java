package com.erp.server.oms.controller.api;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.dto.ShopDTO.ShopBatchUpdateDTO;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.server.oms.query.ShopQueryHandler;
import com.erp.server.oms.service.CustomerInfoService;
import com.erp.server.oms.service.ShopCostService;
import com.erp.server.oms.service.ShopInfoService;
import com.sdk.oms.shopify.api.dto.AssociatedUserBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
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
@LogSystemModule("店铺管理")
@RequestMapping("/shop")
public class ShopInfoController extends BaseController {

    @Resource
    private ShopInfoService shopInfoService;

    @Resource
    private ShopCostService shopCostService;

    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private DmpThirdMappingFeign dmpThirdMappingFeign;
    /**
     * 店铺 分页
     *
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:shop:paging",
            tableAlias = "si"
    )
    @WebAdvanceQuery(handler = ShopQueryHandler.class)
    public ApiResult<PagingVO<ShopDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<ShopDTO.PagingParamDTO> dto) {
        PagingVO<ShopDTO.PagingViewDTO> pagingVO = shopInfoService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 添加店铺
     *
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "添加店铺")
    @PostMapping("/add")
    public ApiResult<?> add(@RequestBody @Validated ShopDTO.AddDTO dto) {
        List<ShopInfoEntity> list = shopInfoService.add(dto);
        for (ShopInfoEntity shop : list) {
            shopInfoService.saveCustom(shop);
        }
        return !CollectionUtils.isEmpty(list) ? success() : failure();
    }

    /**
     * 添加并授权店铺
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "添加并授权店铺:name={name}")
    @PostMapping("/addAndAuth")
    public ApiResult<?> addAndAuth(@RequestBody @Validated ShopDTO.AddDTO dto) {
        ShopDTO.RedirectDTO redirectDTO = shopInfoService.addAndAuth(dto);
        return success(redirectDTO);
    }


    /**
     * 修改并授权店铺
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "修改并授权店铺")
    @PostMapping("/updateAndAuth")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:shop:update",
            serviceClass = ShopInfoService.class,
            keyIdName = "id")
    public ApiResult<?> updateAndAuth(@RequestBody @Validated ShopDTO.UpdateDTO dto) {
        ShopDTO.RedirectDTO redirectDTO = shopInfoService.updateAndAuth(dto);
        return success(redirectDTO);
    }


    /**
     * 修改店铺
     *
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改店铺")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:shop:update",
            serviceClass = ShopInfoService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated ShopDTO.UpdateDTO dto) {
        ShopInfoEntity shopInfoEntity = shopInfoService.updateShop(dto);
        //如果没有选客户，就进行绑定
        shopInfoService.saveCustom(shopInfoEntity);
        return null != shopInfoEntity ? success() : failure();
    }

    /**
     * 获取店铺详情
     *
     * @return
     */
    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:shop:view",
            serviceClass = ShopInfoService.class,
            keyIdName = "id")
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
     * 店铺下拉
     * @return
     */
    @GetMapping("/listShopSelect")
    public ApiResult<List<BaseDropDownDTO.DisabledDTO>> listShopSelect() {
        List<BaseDropDownDTO.DisabledDTO> list = shopInfoService.listShopSelect();
        return success(list);
    }

    /**
     * 获取已授权店铺
     *
     * @return ApiResult<List < ShopInfoEntity>>
     * @author Will
     * @date: 2023/10/18 10:00
     */
    @PostMapping("/listAuth")
    public ApiResult<List<ShopInfoEntity>> listAuth(@RequestBody ShopDTO.PlatformDTO platformDTO) {
        List<ShopInfoEntity> list = shopInfoService.listAuth(platformDTO);
        return success(list);
    }

    /**
     * 获取店铺列表(树状级联)
     */
    @GetMapping("/listTree")
    public ApiResult<List<ShopDTO.ListTreeDTO>> listTree() {
        List<ShopDTO.ListTreeDTO> list = shopInfoService.listTree();
        return success(list);
    }

    /**
     * 店铺账号列表
     *
     * @return
     */
    @GetMapping("/accountList")
    public ApiResult accountList() {
        List<String> list = shopInfoService.accountList();
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
    public ApiResult updateStatus(@RequestBody @Validated ShopBatchUpdateDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<String> ids = dto.getIds();
        Boolean disabled = dto.getDisabled();
        for (String id : ids) {
            BatchResultDTO submit;
            if(!disabled && dto.getEnableTime() == null) {
            	submit = BatchResultDTO.fail(id, id, "修改状态为启用，启用时间必填");
            }else {
            	String flagCode = id;
                try {
                    ShopInfoEntity shop = shopInfoService.getById(id);
                    if (Objects.isNull(shop)) {
                        submit = BatchResultDTO.fail(id, id, "店铺不存在");
                    } else {
                    	if(!disabled) {
                    		shop.setEnableTime(dto.getEnableTime());
                    	}
                        //仓库下绑定第三方店铺不能修改为禁用状态
                        if (Objects.nonNull(disabled) && !Objects.equals(disabled, shop.getDisabled()) && Objects.equals(disabled, true)) {
                            Boolean flag = checkDmpThirdMapping(id);
                            if (!flag) {
                                submit = BatchResultDTO.fail(id, shop.getName(), CharSequenceUtil.format(ApiError.EXIST_THIRD_SHOP_MAPPING.msg,shop.getName()));
                            }else{
                                flagCode = shop.getName();
                                submit = shopInfoService.updateStatus(shop, disabled);
                            }
                        }else {
                            flagCode = shop.getName();
                            submit = shopInfoService.updateStatus(shop, disabled);
                        }
                    }
                } catch (Exception e) {
                    log.error("店铺更改状态失败>>>>{}", e);
                    submit = BatchResultDTO.fail(id, flagCode, e.getMessage());
                }
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
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
                    submit = BatchResultDTO.fail(id, id, "店铺不存在");
                } else {
                    submit = shopCostService.batchSetCost(shop, dto);
                    flagCode = shop.getName();
                }
            } catch (Exception e) {
                log.error("店铺设置费率失败>>>>{}", e);
                submit = BatchResultDTO.fail(id, flagCode, e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
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


    /**
     * 获取店铺授权地址的url
     *
     * @return
     */
    @PostMapping("/getShopAuthorizeUrl")
    public ApiResult getShopAuthorizeUrl(@RequestBody @Validated ShopAuthorizeUrlDTO dto) {
        String resultUrl = shopInfoService.getShopAuthorizeUrl(dto);
        return success(resultUrl);
    }

    /**
     * Shopfiy直接安装地址url
     *
     * @return
     */
    @PostMapping("/shopifyUrl")
    public ApiResult<?> shopifyUrl(@RequestBody @Validated ShopifyAuthorizeUrlDTO dto) {
        String resultUrl = shopInfoService.getShopifyAuthorizeUrl(dto);
        return success(resultUrl);
    }


    /**
     * 店铺授权
     *
     * @return
     */
    @PostMapping("/shopAuthorize")
    public ApiResult shopAuthorize(@RequestBody @Validated ShopAuthorizeDTO dto, HttpServletResponse response) {
        Boolean result = shopInfoService.shopAuthorize(dto, response);
        return result ? success() : failure();
    }

    /**
     * 检查店铺是否授权
     *
     * @param id
     * @return
     */
    @GetMapping("checkShopIsAuthorize")
    public ApiResult checkShopIsAuthorize(@RequestParam(value = "id") String id) {
        Boolean checkResult = shopInfoService.checkShopIsAuthorize(id);
        return success(checkResult);
    }

    /**
     * 取消授权
     *
     * @return
     */
    @PostMapping("/cancelAuthorize")
    public ApiResult cancelAuthorize(@RequestBody @Validated CancelAuthorizeDTO dto) {
        Boolean result = shopInfoService.cancelAuthorize(dto);
        return result ? success() : failure();
    }

    /**
     * 查询亚马逊店铺信息
     *
     * @return java.util.List<com.erp.model.oms.entity.ShopInfoEntity>
     * @Author Luo_WG
     * @Date 2023/11/1 18:56
     **/
    @GetMapping("/listShopByAmazon")
    public ApiResult<List<ShopInfoEntity>> listShopByAmazon() {
        List<ShopInfoEntity> result = shopInfoService.listShopByAmazon();
        return success(result);
    }

    /**
     * 查询当前用户权限的亚马逊店铺信息
     *
     * @return java.util.List<com.erp.model.oms.entity.ShopInfoEntity>
     * @Author Luo_WG
     * @Date 2023/11/1 18:56
     **/
    @GetMapping("/listShopByAmazonAuth")
    public ApiResult<List<ShopSysUserAuthDTO.ViewShopDTO>> listShopByAmazonAuth() {
        List<ShopSysUserAuthDTO.ViewShopDTO> result = shopInfoService.listShopByAmazonAuth();
        return success(result);
    }


    /**
     * 根据shopify平台用户id查询用户信息
     * @Author Luo_WG
     * @Date 2024/2/23 14:07
     * @param id
     * @return com.common.core.controller.vo.ApiResult
     **/
    @GetMapping("/getShopifyShopByUserId")
    public ApiResult<AssociatedUserBean> getShopifyShopByUserId(@RequestParam(value = "id") String id) {
        AssociatedUserBean bean = shopInfoService.getShopifyShopByUserId(id);
        return success(bean);
    }

    /**
     * 根据平台获取店铺
     * @param dictPlatform
     * @return
     */
    @GetMapping("/getShopifyByPlatform")
    public ApiResult<List<ShopInfoEntity>> getShopifyByPlatform(@RequestParam(value = "dictPlatform") String dictPlatform) {
        return success(shopInfoService.lambdaQuery().eq(ShopInfoEntity::getDictPlatform, dictPlatform).list());
    }

    /**
     * 删除
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除店铺")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:shop:delete",
            serviceClass = ShopInfoService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = shopInfoService.deleteByIds(dto);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    /**
     * 导出
     * @author hyj
     * @date 2024/5/23
     * @param dto
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出店铺")
    @PostMapping("/export")
    public ApiResult<Boolean> listExport(@RequestBody ShopDTO.ExportDTO dto) {
        shopInfoService.listExport(dto);
        return success(true);
    }

    /**
     * 添加店铺
     *
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "添加店铺")
    @PostMapping("/addIntenal")
    public ApiResult<?> addIntenal(@RequestBody @Validated ShopDTO.AddInternalDTO dto) {
        List<ShopInfoEntity> list = shopInfoService.addIntenal(dto);
        for (ShopInfoEntity shop : list) {
            shopInfoService.saveCustom(shop);
        }
        return !CollectionUtils.isEmpty(list) ? success() : failure();
    }


    /**
     * 修改国内店铺
     *
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改店铺")
    @PostMapping("/updateInternal")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:shop:updateInternal",
            serviceClass = ShopInfoService.class,
            keyIdName = "id")
    public ApiResult<?> updateInternal(@RequestBody @Validated ShopDTO.UpdateInternalDTO dto) {
        ShopInfoEntity shopInfoEntity = shopInfoService.updateInternalShop(dto);
        //如果没有选客户，就进行绑定
        shopInfoService.saveCustom(shopInfoEntity);
        return null != shopInfoEntity ? success() : failure();
    }

    /**
     * 区域远程分页下拉查询
     * @author will
     * @date 2024/8/28 16:50
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/pagingSelectArea")
    public ApiResult<PagingVO<ShopDTO.AreaDTO>> pagingSelectArea(@RequestBody PagingDTO<ShopDTO.AreaParamDTO> dto) {
        PagingVO<ShopDTO.AreaDTO> pagingVO = shopInfoService.pagingSelectArea(dto);
        return success(pagingVO);
    }

    /**
     * 店铺下拉查询
     * @author will
     * @date 2024/8/28 16:50
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/listSelect")
    public ApiResult<List<ShopDTO.ListDTO>> listSelect(@RequestBody ShopDTO.SelectDTO dto) {
        List<ShopDTO.ListDTO> list = shopInfoService.listSelect(dto);
        return success(list);
    }




    private Boolean checkDmpThirdMapping(String shopId) {
        ThirdMappingDTO.ViewParamDTO viewParamDTO=new ThirdMappingDTO.ViewParamDTO();
        viewParamDTO.setType(ThirdSysTypeEnum.SHOP.getCode());
        viewParamDTO.setSysId(shopId);
        return dmpThirdMappingFeign.getWhetherBind(viewParamDTO);
    }
}
