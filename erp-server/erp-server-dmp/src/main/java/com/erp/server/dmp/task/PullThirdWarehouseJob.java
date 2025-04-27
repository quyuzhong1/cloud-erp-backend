package com.erp.server.dmp.task;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.entity.ThirdWarehouseEntity;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.dmp.service.ThirdWarehouseService;
import com.sdk.oms.temu.dto.TemuResp;
import com.sdk.oms.temu.dto.TemuShopInfoDTO;
import com.sdk.oms.temu.dto.TemuWarehouseDTO;
import com.sdk.oms.temu.service.TemuClient;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * 拉取第三方仓库数据
 * @Author lrp
 * @Date 2025-04-27
 **/
@Component
@Slf4j
@EnableScheduling
public class PullThirdWarehouseJob {

    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Resource
    private ThirdWarehouseService thirdWarehouseService;

    @Resource
    private TemuClient temuClient;

    /**
     * 拉取第三方仓库数据
     **/
    @XxlJob("PullThirdWarehouseJob")
    public ReturnT<String> pullThirdWarehouseJob() {
        XxlJobHelper.log("====拉取第三方仓库数据 开始任务=====");
        long start = System.currentTimeMillis();
        //查询店铺授权
        ApiResult<List<ShopAuthEntity>> result = shopInfoFeign.getAuthShopByPlatformType(PlatformDictEnum.TE_MU.getCode());
        if (result == null || result.getData() == null) {
            XxlJobHelper.log("拉取第三方仓库数据失败，未获取到店铺授权信息");
            return ReturnT.FAIL;
        }
        List<ShopAuthEntity> shopAuthList = result.getData();
        List<ThirdWarehouseEntity> dbList = thirdWarehouseService.list();
        List<ThirdWarehouseEntity> addList = new ArrayList<>();
        List<ThirdWarehouseEntity> updateList = new ArrayList<>();
        Set<String> ignoreIdMap = new HashSet<>();
        for (ShopAuthEntity shopAuthEntity : shopAuthList) {
            String extendData = shopAuthEntity.getExtendData();
            JSONObject jsonObject = JSONUtil.parseObj(extendData);
            String clientId = jsonObject.getStr("clientId");
            String clientSecret = jsonObject.getStr("clientSecret");
            if(clientId == null || clientSecret == null) {
                XxlJobHelper.log("拉取第三方仓库数据失败，clientId或clientSecret为空");
                continue;
            }
            TemuResp<TemuWarehouseDTO> temuResp =  temuClient.getWarehouseList(new TemuShopInfoDTO(shopAuthEntity.getAreaCode(), clientId, clientSecret, shopAuthEntity.getAccessToken()));
            if (temuResp == null || !temuResp.getSuccess() ) {
                XxlJobHelper.log("拉取第三方仓库数据失败");
                continue;
            }
            List<TemuWarehouseDTO.WarehouseListDTO> warehouseListDTOS = temuResp.getResult().getWarehouseList();
            for (TemuWarehouseDTO.WarehouseListDTO warehouseListDTO : warehouseListDTOS) {
                if(ignoreIdMap.contains(warehouseListDTO.getWarehouseId())) {
                    continue;
                }
                ignoreIdMap.add(warehouseListDTO.getWarehouseId());
                ThirdWarehouseEntity dbEntity = dbList.stream().filter(v->v.getSysType().equals(PlatformDictEnum.TE_MU.getCode()) && v.getWarehouseId().equals(warehouseListDTO.getWarehouseId())).findFirst().orElse(null);
                if(dbEntity == null) {
                    ThirdWarehouseEntity thirdWarehouseEntity = new ThirdWarehouseEntity();
                    thirdWarehouseEntity.setSysType(PlatformDictEnum.TE_MU.getCode());
                    thirdWarehouseEntity.setWarehouseId(warehouseListDTO.getWarehouseId());
                    thirdWarehouseEntity.setName(warehouseListDTO.getWarehouseName());
                    addList.add(thirdWarehouseEntity);
                } else {
                    if(!dbEntity.getName().equals(warehouseListDTO.getWarehouseName())){
                        dbEntity.setName(warehouseListDTO.getWarehouseName());
                        updateList.add(dbEntity);
                    }
                }
            }
        }
        if(!addList.isEmpty()) {
            thirdWarehouseService.saveBatch(addList);
        }
        if(!updateList.isEmpty()) {
            thirdWarehouseService.updateBatchById(updateList);
        }
        long end = System.currentTimeMillis();
        XxlJobHelper.log("主线程花费时间：{}", (end - start));
        XxlJobHelper.log("=====拉取第三方仓库数据 结束任务=====");
        return ReturnT.SUCCESS;
    }

}
