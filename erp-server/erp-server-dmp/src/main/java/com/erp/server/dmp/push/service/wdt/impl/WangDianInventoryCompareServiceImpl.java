package com.erp.server.dmp.push.service.wdt.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.SyncOperateEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.ThirdWarehouseDTO;
import com.erp.model.dmp.enums.InventorySyncModeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.WdtCompareInventoryDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.InventoryFeign;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.push.consumer.wangdian.WdtOtherInventoryStockConsumer;
import com.erp.server.dmp.push.service.CommonService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.push.service.wdt.WangDianInventoryCompareService;
import com.erp.server.dmp.service.ThirdWarehouseService;
import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.api.wms.StockAPI;
import com.sdk.wangdian.sdk.api.wms.dto.StockSearch2Request;
import com.sdk.wangdian.sdk.api.wms.dto.StockSearch2Response;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;
import com.sdk.wangdian.server.WangDianClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WangDianInventoryCompareServiceImpl implements WangDianInventoryCompareService {

    @Resource
    private CommonService commonService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private WangDianClientService clientService;

    @Resource
    private ThirdWarehouseService thirdWarehouseService;

    @Resource
    private InventoryFeign inventoryFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private WdtOtherInventoryStockConsumer wdtOtherInventoryStockConsumer;

    private static final String LOCK = "wdt:push:product:spu:";

    @Override
    public ApiResult<?> executeConsumer(WdtCompareInventoryDTO pushDTOS) {
        List<String> erpWarehouseIds = pushDTOS.getErpWarehouseList();
        List<String> skuIds = pushDTOS.getSkuIdList();
        List<String> skuNos = pushDTOS.getSkuNoList();
        //过滤费用、服务类SKU，不同步旺店通更新
        List<SkuVO> noInventorySku = plmTaskFeign.getNoInventorySku();
        skuNos.removeAll(noInventorySku.stream().map(SkuVO::getSkuNo).collect(Collectors.toList()));
        if(CollectionUtils.isEmpty(skuNos)){
            log.warn("WangDianInventoryCompareServiceImpl -->过滤费用、服务类SKU后，无需同步旺店通更新库存的SKU列表为空");
            return ApiResult.success();
        }
        //查询旺店通库存
        ThirdWarehouseDTO.QueryMapParamDTO queryMapParamDTO = new ThirdWarehouseDTO.QueryMapParamDTO();
        queryMapParamDTO.setSysType(ThirdSysTypeEnum.WDT.getCode());
        queryMapParamDTO.setCategory(ThirdSysTypeEnum.WAREHOUSE.getCode());
        queryMapParamDTO.setInventorySyncMode(InventorySyncModeEnum.INVENTORY.getCode());
        queryMapParamDTO.setSysIdList(erpWarehouseIds);
        //获取旺店通更新库存的仓库列表
        List<ThirdWarehouseDTO.QueryMapDTO> queryMapDTOList = thirdWarehouseService.listQueryMapping(queryMapParamDTO);
        if (CollectionUtils.isEmpty(queryMapDTOList)) {
            log.warn("WangDianInventoryCompareServiceImpl -->旺店通更新库存的仓库列表为空");
            return ApiResult.success();
        }
        StockAPI stockAPI = clientService.  get(StockAPI.class);
        List<StockSearch2Response.Detail> wdtInventoryList = new ArrayList<>();
        int pageSize = 1000;
        for (ThirdWarehouseDTO.QueryMapDTO queryMapDTO : queryMapDTOList) {
            if (CharSequenceUtil.isBlank(queryMapDTO.getThirdCode())) {
                continue;
            }
            StockSearch2Request request = new StockSearch2Request();
            request.setWarehouseNo(queryMapDTO.getThirdCode());
            request.setSpecNos(skuNos);

            Pager pager = new Pager();
            pager.setPageNo(0);
            pager.setPageSize(pageSize);
            pager.setCalcTotal(true);
            boolean hasNext = true;
            while (hasNext) {
                StockSearch2Response response;
                try {
                    response = stockAPI.search2(request, pager);
                    log.warn("WangDianInventoryCompareServiceImpl -->旺店通查询库存, 仓库: {},  响应: {}", queryMapDTO.getThirdCode(), JSONUtil.toJsonStr(response));
                } catch (Exception e) {
                    log.error("WangDianInventoryCompareServiceImpl -->旺店通查询库存异常, 仓库: {}, 异常信息: {}", queryMapDTO.getThirdCode(), e.getMessage(), e);
                    hasNext = false;
                    continue;
                }
                if (Objects.isNull(response) || CollectionUtils.isEmpty(response.getDetailList())) {
                    hasNext = false;
                    continue;
                }
                Integer totalCount = response.getTotal();
                if (totalCount <= (pager.getPageNo() + 1) * pageSize) {
                    hasNext = false;
                }
                pager.setPageNo(pager.getPageNo() + 1);
                List<StockSearch2Response.Detail> current = response.getDetailList();
                current.forEach(v->v.setErpWarehouseId(queryMapDTO.getSysId()));
                wdtInventoryList.addAll(current);
            }
        }
        if(CollectionUtils.isEmpty(wdtInventoryList)){
            log.warn("WangDianInventoryCompareServiceImpl -->旺店通查询库存结果为空");
            return ApiResult.success();
        }
        //查询ERP库存
        InventoryQtyDTO.SkuInventoryStatusParamDTO dto = new InventoryQtyDTO.SkuInventoryStatusParamDTO();
        dto.setWarehouseIdList(erpWarehouseIds);
        dto.setInventoryStatusList(Collections.singletonList(InventoryStatusEnum.USABLE.getCode()));
        dto.setSkuIdList(skuIds);
        //查询库存
        List<InventoryQtyDTO.InventoryDTO> inventoryDTOS = inventoryFeign.listWarehouseInventoryByParam(dto);
        List<CreateOtherStockoutRequest> createOtherStockoutRequestList = new ArrayList<>();
        List<CreateOtherStockinRequest> createOtherStockinRequestList = new ArrayList<>();
        for (ThirdWarehouseDTO.QueryMapDTO queryMapDTO : queryMapDTOList) {
            String warehouseId = queryMapDTO.getSysId();
            for (String skuNo : skuNos) {
                InventoryQtyDTO.InventoryDTO inventoryDTO = inventoryDTOS.stream().filter(e -> warehouseId.equals(e.getWarehouseId())&& e.getSkuNo().equals(skuNo)).findFirst().orElse(null);
                StockSearch2Response.Detail wdtInventory = wdtInventoryList.stream().filter(e ->warehouseId.equals(e.getErpWarehouseId())&& e.getSpecNo().equals(skuNo)).findFirst().orElse(null);
                if(Objects.isNull(inventoryDTO) || Objects.isNull(wdtInventory)){
                    log.warn("WangDianInventoryCompareServiceImpl -->查询库存结果为空, skuNo: {}, inventoryDTO: {}, wdtInventory: {}", skuNo, JSONUtil.toJsonStr(inventoryDTO), JSONUtil.toJsonStr(wdtInventory));
                    continue;
                }
                if(Objects.isNull(wdtInventory.getDefect()) && wdtInventory.getDefect()){
                    log.warn("WangDianInventoryCompareServiceImpl -->defect 为True, skuNo: {}, inventoryDTO: {}, wdtInventory: {}", skuNo, JSONUtil.toJsonStr(inventoryDTO), JSONUtil.toJsonStr(wdtInventory));
                    continue;
                }
                //库存量
                BigDecimal stockNum = wdtInventory.getStockNum();
                int erpUsableQty = inventoryDTO.getQty();
                //- 差异等于0：无需处理
                if (erpUsableQty == stockNum.intValue()) {
                    log.warn("WangDianInventoryCompareServiceImpl -->库存量一致，无需处理, skuNo: {}, erpUsableQty: {}, wdtStockNum: {}", skuNo, erpUsableQty, stockNum);
                    continue;
                }
                if(erpUsableQty > stockNum.intValue()){
                    //其他入库
                    CreateOtherStockinRequest createOtherStockinRequest = createOtherStockinRequestList.stream().filter(v->v.getSysWarehouseId().equals(warehouseId)).findFirst().orElse(null);
                    if(Objects.isNull(createOtherStockinRequest)){
                        createOtherStockinRequest = new CreateOtherStockinRequest();
                        createOtherStockinRequestList.add(createOtherStockinRequest);
                        createOtherStockinRequest.setReason("数大臣分货单触发库存同步生成");
                        createOtherStockinRequest.setRemark("数大臣分货单触发库存同步生成");
                        createOtherStockinRequest.setOperateCode(SyncOperateEnum.OPERATE_APPROVE.getCode());
                        createOtherStockinRequest.setWarehouseNo(queryMapDTO.getThirdCode());
                        createOtherStockinRequest.setTargetPlatformName(PlatformEnum.WANGDIAN.getName());
                        createOtherStockinRequest.setSysWarehouseId(queryMapDTO.getSysId());
                        createOtherStockinRequest.setSourcePlatformName(PlatformEnum.ERP_DMP.getName());
                        createOtherStockinRequest.setSourceId(pushDTOS.getId());
                        createOtherStockinRequest.setOuterNo(pushDTOS.getCode());
                        createOtherStockinRequest.setIsCheck(true);
                        List<CreateOtherStockinRequest.GoodsList> goodsList = new ArrayList<>();
                        CreateOtherStockinRequest.GoodsList goods = new CreateOtherStockinRequest.GoodsList();
                        goods.setSpecNo(wdtInventory.getSpecNo());
                        goods.setNum(new BigDecimal(Math.abs(erpUsableQty - stockNum.intValue())));
                        goodsList.add(goods);
                        createOtherStockinRequest.setGoodsList(goodsList);
                    }else{
                        List<CreateOtherStockinRequest.GoodsList> goodsList = createOtherStockinRequest.getGoodsList();
                        CreateOtherStockinRequest.GoodsList goods = new CreateOtherStockinRequest.GoodsList();
                        goods.setSpecNo(wdtInventory.getSpecNo());
                        goods.setNum(new BigDecimal(Math.abs(erpUsableQty - stockNum.intValue())));
                        goodsList.add(goods);
                        createOtherStockinRequest.setGoodsList(goodsList);
                    }
                }else{
                    //其他出库
                    CreateOtherStockoutRequest createOtherStockoutRequest = createOtherStockoutRequestList.stream().filter(v->v.getSysWarehouseId().equals(warehouseId)).findFirst().orElse(null);
                    if(Objects.isNull(createOtherStockoutRequest)){
                        createOtherStockoutRequest = new CreateOtherStockoutRequest();
                        createOtherStockoutRequestList.add(createOtherStockoutRequest);
                        createOtherStockoutRequest.setReason("数大臣分货单触发库存同步生成");
                        createOtherStockoutRequest.setRemark("数大臣分货单触发库存同步生成");
                        createOtherStockoutRequest.setOperateCode(SyncOperateEnum.OPERATE_APPROVE.getCode());
                        createOtherStockoutRequest.setWarehouseNo(queryMapDTO.getThirdCode());
                        createOtherStockoutRequest.setTargetPlatformName(PlatformEnum.WANGDIAN.getName());
                        createOtherStockoutRequest.setSysWarehouseId(queryMapDTO.getSysId());
                        createOtherStockoutRequest.setSourcePlatformName(PlatformEnum.ERP_DMP.getName());
                        createOtherStockoutRequest.setSourceId(pushDTOS.getId());
                        createOtherStockoutRequest.setOuterNo(pushDTOS.getCode());
                        createOtherStockoutRequest.setIsCheck(true);
                        List<CreateOtherStockoutRequest.GoodsList> goodsList = new ArrayList<>();
                        CreateOtherStockoutRequest.GoodsList goods = new CreateOtherStockoutRequest.GoodsList();
                        goods.setSpecNo(wdtInventory.getSpecNo());
                        goods.setNum(new BigDecimal(Math.abs(erpUsableQty - stockNum.intValue())));
                        goodsList.add(goods);
                        createOtherStockoutRequest.setGoodsList(goodsList);
                    }else{
                        List<CreateOtherStockoutRequest.GoodsList> goodsList = createOtherStockoutRequest.getGoodsList();
                        CreateOtherStockoutRequest.GoodsList goods = new CreateOtherStockoutRequest.GoodsList();
                        goods.setSpecNo(wdtInventory.getSpecNo());
                        goods.setNum(new BigDecimal(Math.abs(erpUsableQty - stockNum.intValue())));
                        goodsList.add(goods);
                        createOtherStockoutRequest.setGoodsList(goodsList);
                    }
                }
            }
        }
        try {
            for (CreateOtherStockoutRequest createOtherStockoutRequest : createOtherStockoutRequestList) {
                ApiResult<Object> result = wdtOtherInventoryStockConsumer.pushWdtOutStock(createOtherStockoutRequest,createOtherStockoutRequest.getWarehouseNo());
                if(!result.isSuccess()){
                    return result;
                }
            }
        }catch (Exception e){
            log.error("WangDianInventoryCompareServiceImpl -->推送旺店通其他出库单异常, 异常信息: {}", e.getMessage(), e);
            return ApiResult.error("推送旺店通其他出库单异常: {}", e.getMessage());
        }
        try {
            for (CreateOtherStockinRequest createOtherStockinRequest : createOtherStockinRequestList) {
                ApiResult<Object> result = wdtOtherInventoryStockConsumer.pushWdtInstock(createOtherStockinRequest,createOtherStockinRequest.getWarehouseNo());
                if(!result.isSuccess()){
                    return result;
                }
            }
        }catch (Exception e){
            log.error("WangDianInventoryCompareServiceImpl -->推送旺店通其他入库单异常, 异常信息: {}", e.getMessage(), e);
            return ApiResult.error("推送旺店通其他入库单异常: {}", e.getMessage());
        }


        return ApiResult.success();
    }
}
