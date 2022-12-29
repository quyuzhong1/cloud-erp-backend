package com.erp.server.bi.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.bi.entity.BiDictEntity;
import com.erp.model.bi.vo.SalesPlatformEnumVO;
import com.erp.model.bi.vo.SelectShowVO;
import com.erp.model.bi.vo.ShopDropDownVO;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.model.dmp.entity.DmpSkuInfoEntity;
import com.erp.model.dmp.enums.SalesPlatformEnum;
import com.erp.server.bi.enums.*;
import com.erp.server.bi.service.BiDataSourceCustomService;
import com.erp.server.bi.service.BiDictService;
import com.erp.server.bi.service.DmpShopInfoService;
import com.erp.server.bi.service.DmpSkuInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BI 下拉列表
 *
 * @Author Cloud
 * @Date 2022/12/19 11:26
 **/

@RestController
@RequestMapping("bi/drop/down")
public class BiDropDownListController extends BaseController {

    @Resource
    private BiDictService biDictService;

    @Resource
    private BiDataSourceCustomService biDataSourceCustomService;

    @Resource
    private DmpSkuInfoService dmpSkuInfoService;

    @Resource
    private DmpShopInfoService dmpShopInfoService;

    /**
     * 平台下拉列表
     *
     * @return 单条数据
     */
    @GetMapping("/platform/list")
    public ApiResult<List<SalesPlatformEnumVO>> listPlatformDropDown() {
        List<SalesPlatformEnumVO> result = Arrays.stream(SalesPlatformEnum.values())
                .map(x -> new SalesPlatformEnumVO(x.getCode(),x.getName(),x.getDesc()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 站点下拉列表
     * @return
     */
    @GetMapping("/site/list")
    public ApiResult<List<ShopDropDownVO>> listSiteDropDown() {
        List<DmpShopInfoEntity> list = dmpShopInfoService.lambdaQuery()
                .eq(DmpShopInfoEntity::getStatus, 1)
                .list();
        if(CollectionUtil.isEmpty(list)){
            return success(new ArrayList<>());
        }
        List<ShopDropDownVO> result = list.stream()
                .map(x -> new ShopDropDownVO(x.getSite()))
                .distinct()
                .filter(x -> StrUtil.isNotEmpty(x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 启用禁用下拉框
     * @return
     */
    @GetMapping("/state/list")
    public ApiResult<List<SelectShowVO>> listStateDropDown() {
        List<SelectShowVO> result = Arrays.stream(BiStateEnum.values())
                .map(x -> new SelectShowVO().setCode(x.getCode()).setName(x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 店铺标识下拉框
     * @return
     */
    @GetMapping("/storeSign/list")
    public ApiResult<List<SelectShowVO>> listStoreSignDropDown() {
        List<SelectShowVO> result = Arrays.stream(StoreSignEnum.values())
                .map(x -> new SelectShowVO().setName(x.getCode()).setDesc(x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 订单状态下拉框
     * @return
     */
    @GetMapping("/orderStatus/list")
    public ApiResult<List<SelectShowVO>> listOrderStatusDropDown() {
        List<SelectShowVO> result = Arrays.stream(OrderStateEnum.values())
                .map(x -> new SelectShowVO().setCode(x.getCode()).setName(x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 时间类型下拉列表
     * @return
     */
    @GetMapping("/time/type/list")
    public ApiResult<List<SelectShowVO>> listTimeTypeDropDown() {
        List<SelectShowVO> result = Arrays.stream(TimeTypeEnum.values())
                .map(x -> new SelectShowVO(x.getCode(),x.getName(),x.getDesc()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 结算类型下拉列表
     * @return
     */
    @GetMapping("/settle/method/list")
    public ApiResult<List<SelectShowVO>> listSettleMethodDropDown() {
        List<SelectShowVO> result = Arrays.stream(SettleMethodEnum.values())
                .map(x -> new SelectShowVO(x.getCode(),x.getName(),x.getDesc()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 币种下拉列表
     * @return
     */
    @GetMapping("/currency/list")
    public ApiResult<List<SelectShowVO>> listCurrencyDropDown() {
        List<BiDictEntity> biDictList = biDictService.listEntityByType(DictEnum.CURRENCY.getType());
        List<SelectShowVO> result = biDictList.stream()
                .map(x -> new SelectShowVO().setName(x.getValue()).setDesc(x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 模块配置-数据来源下拉列表
     * @return
     */
    @GetMapping("/dataSource/list")
    public ApiResult<List<SelectShowVO>> listDataSourceDropDown() {
        List<SelectShowVO> result = Arrays.stream(DataTypeEnum.values())
                .map(x -> new SelectShowVO(x.getCode(),x.getName(),x.getDesc()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 模块配置-数据指标下拉列表
     * @return
     */
    @GetMapping("/targetName/list")
    public ApiResult<List<SelectShowVO>> listTargetNameDropDown(@RequestParam("dataSource") Integer dataSource) {
        List<SelectShowVO> result = new ArrayList<>();
        List<String> list = biDataSourceCustomService.listTargetNameByDataSource(dataSource);
        if (CollectionUtils.isNotEmpty(list)) {
             result = list.stream().map(x -> new SelectShowVO().setName(x).setDesc(x))
                    .collect(Collectors.toList());
        }
        return success(result);
    }

    /**
     * 模块配置-数据维度下拉列表
     * @return
     */
    @GetMapping("/dataDimension/list")
    public ApiResult<List<SelectShowVO>> listDataDimensionDropDown() {
        List<SelectShowVO> result = Arrays.stream(BiDataSourceCustomTypeEnum.values())
                .map(x -> new SelectShowVO(x.getCode(),x.getName(),x.getDesc()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 指标分类-指标名称下拉列表
     * @return
     */
    @GetMapping("/all/targetName/list")
    public ApiResult<List<SelectShowVO>> listAllTargetNameDropDown() {
        List<SelectShowVO> result = new ArrayList<>();
        List<String> list = biDataSourceCustomService.listAllTargetNameDropDown();
        if (CollectionUtils.isNotEmpty(list)) {
            result = list.stream().map(x -> new SelectShowVO().setName(x).setDesc(x))
                    .collect(Collectors.toList());
        }
        return success(result);
    }

    /**
     * 指标分类-指标分类下拉列表
     * @return
     */
    @GetMapping("/all/targetType/list")
    public ApiResult<List<SelectShowVO>> listAllTargetTypeDropDown() {
        List<SelectShowVO> result = new ArrayList<>();
        List<String> list = biDataSourceCustomService.listAllTargetTypeDropDown();
        if (CollectionUtils.isNotEmpty(list)) {
            result = list.stream().map(x -> new SelectShowVO().setName(x).setDesc(x))
                    .collect(Collectors.toList());
        }
        return success(result);
    }

    /**
     * 店铺下拉框
     */
    @GetMapping("/shop/list")
    public ApiResult<List<ShopDropDownVO>> listShopDropDown() {
        List<DmpShopInfoEntity> list = dmpShopInfoService.lambdaQuery()
                .eq(DmpShopInfoEntity::getStatus, 1)
                .list();
        if(CollectionUtil.isEmpty(list)){
            return success(new ArrayList<>());
        }
        List<ShopDropDownVO> result = list.stream()
                .map(x -> new ShopDropDownVO(x.getName()))
                .distinct()
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 品类
     */
    @GetMapping("/category/list")
    public ApiResult<List<ShopDropDownVO>> listCategoryDropDown() {
        List<DmpSkuInfoEntity> list = dmpSkuInfoService.list();
        if(CollectionUtil.isEmpty(list)){
            return success(new ArrayList<>());
        }
        List<ShopDropDownVO> result = list.stream().map(x -> new ShopDropDownVO(x.getParentCategoryName()))
                .distinct()
                .filter(x -> StrUtil.isNotEmpty(x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 品牌
     */
    @GetMapping("/brand/list")
    public ApiResult<List<ShopDropDownVO>> listBrandDropDown() {
        List<DmpSkuInfoEntity> list = dmpSkuInfoService.list();
        if(CollectionUtil.isEmpty(list)){
            return success(new ArrayList<>());
        }
        List<ShopDropDownVO> result = list.stream().map(x -> new ShopDropDownVO(x.getBrandName()))
                .distinct()
                .filter(x -> StrUtil.isNotEmpty(x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 销售监控类型下拉列表
     * @return
     */
    @GetMapping("/biSalesMonitoring/type/list")
    public ApiResult<List<SelectShowVO>> listBiSalesMonitoringTypeDropDown() {
        List<BiDictEntity> biDictList = biDictService.listEntityByType(DictEnum.SALESMONITORINGTYPE.getType());
        List<SelectShowVO> result = biDictList.stream()
                .map(x -> new SelectShowVO().setCode(Integer.valueOf(x.getValue())).setName(x.getName()).setDesc(x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }
}
