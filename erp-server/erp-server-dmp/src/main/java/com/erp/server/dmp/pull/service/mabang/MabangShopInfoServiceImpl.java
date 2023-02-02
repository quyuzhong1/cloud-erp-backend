package com.erp.server.dmp.pull.service.mabang;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.MapUtil;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.mabang.ShopEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.erp.server.dmp.pull.service.dmp.DmpShopInfoService;
import com.erp.server.dmp.utils.MabangApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 马帮店铺
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.SYS_GET_SHOP_LIST)
public class MabangShopInfoServiceImpl implements IReportSaveService<ShopEntity> {
    @Resource
    private MongoService mongoService;

    @Resource
    private DmpErrorLogService dmpErrorLogService;

    @Resource
    private DmpShopInfoService dmpShopInfoService;

    @Resource
    @Qualifier("mabangShopInfoServiceImpl")
    private IReportSaveService reportSaveService;

    @Override
    public void pullDataSave(RequestDTO dto) throws Exception {
        List<ShopEntity> shopEntityList = pullDate(dto);
        if (shopEntityList != null && shopEntityList.size() > 0) {
            for (ShopEntity shopEntity : shopEntityList) {
                OrderMongoDTO orderMongoDTO = new OrderMongoDTO();
                orderMongoDTO.setId(shopEntity.getId());
                List<ShopEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_MABANG_SHOP, ShopEntity.class);
                if (mongoData != null && mongoData.size() > 0) {
                    for (ShopEntity mongoDatum : mongoData) {
                        // 比较数据是否相同
                        if (!mongoDatum.toString().equals(shopEntity.toString())) {
                            // 修改数据
                            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(shopEntity), MapUtil.class);
                            try {
                                mongoService.updateMongoData(orderMongoDTO, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_SHOP, ShopEntity.class);
                            } catch (Exception e) {
                                DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity();
                                dmpErrorLogEntity.setTaskId(dto.getJobTaskDTO().getId());
                                dmpErrorLogEntity.setParams("");
                                dmpErrorLogEntity.setErrorMsg("==== 马帮修改mongodb店铺数据失败，[ 店铺名称 = " + shopEntity.getName() + "], 错误信息 = " + e.getMessage());
                                dmpErrorLogEntity.setReturnMsg("");
                                dmpErrorLogEntity.setCreateTime(LocalDateTime.now());
                                dmpErrorLogService.add(dmpErrorLogEntity);
                                throw new RuntimeException("==== 马帮修改mongodb店铺数据失败，[ 店铺名称 = " + shopEntity.getName() + "], 错误信息 = " + e.getMessage());
                            }
                        }
                    }
                } else {
                    mongoService.saveMongoData(shopEntity, MongoTableNameContant.ORIGINAL_MABANG_SHOP);
                }
                //存储数据到中台
                reportSaveService.analysisOrder(shopEntity);
            }
        }
    }

    /**
     * 请求马帮店铺信息接口
     * @param dto
     * @return
     */
    private List<ShopEntity> pullDate(RequestDTO dto) throws Exception {
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        dto.getJobTaskDTO().setLastTime(nextTime);
        return MabangApiUtils.queryShopList(dto.getPlatformApiEnum().getTaskName());
    }

    /**
     * 解析店铺数据
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     * @return void
     **/
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void analysisOrder(ShopEntity shopEntity) {
        DmpShopInfoEntity dmpShopInfoEntity = new DmpShopInfoEntity();

        //平台店铺编号
        dmpShopInfoEntity.setPlarformShopNo(shopEntity.getId());

        //平台店铺账户
        dmpShopInfoEntity.setAccountUserName(shopEntity.getAccountUsername());

        //平台店铺标识
        dmpShopInfoEntity.setAccountStoreName(shopEntity.getAccountStoreName());

        //店铺名称
        dmpShopInfoEntity.setName(shopEntity.getName());

        // 店铺站点
        if(StrUtil.isNotBlank(shopEntity.getAmazonsite())){
            dmpShopInfoEntity.setSite(shopEntity.getAmazonsite());
        }

        //店铺状态
        dmpShopInfoEntity.setStatus(shopEntity.getStatus());

        //平台名称
        dmpShopInfoEntity.setPlatformName(shopEntity.getPlatformName());

        // 财务编码
        dmpShopInfoEntity.setFinanceCode(shopEntity.getFinanceCode());

        //平台标识
        dmpShopInfoEntity.setPlatformSign("马帮");

        dmpShopInfoService.checkOrder(dmpShopInfoEntity);
    }
}
