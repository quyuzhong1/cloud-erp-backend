package com.erp.server.dmp.pull.service.mabang;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.security.HmacSHA256Utils;
import com.common.core.utils.HttpCommonUtil;
import com.common.core.utils.MapUtil;
import com.common.core.utils.StrUtils;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.constant.UrlContant;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.model.dmp.entity.MabangAppEntity;
import com.erp.model.dmp.enums.ErpPlatformSignEnum;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.mabang.ShopEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.erp.server.dmp.pull.service.dmp.DmpShopInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
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

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

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
    public List<ShopEntity> pullDate(RequestDTO dto) {
        List<ShopEntity> infoArrayList = new ArrayList<>();
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        String st = "";
        String sd = "";
        if (dto.getJobTaskDTO().getLastTime() != null && dto.getJobTaskDTO().getNextTime() != null) {
            dto.getJobTaskDTO().setLastTime(nextTime);
        } else {
            LocalDateTime date = LocalDateTime.now();
            dto.getJobTaskDTO().setLastTime(date);
        }

        MabangAppEntity mabangAppEntity = new MabangAppEntity();

        Map<String, Object> paramsMap = new HashMap();
        // 封装传参数据
        Map<String, Object> datas = new HashMap();
        datas.put("api", dto.getJobTaskDTO().getApiCode());
        datas.put("appkey", mabangAppEntity.getAppKey());
        datas.put("version", 1);
        datas.put("timestamp", new Long(System.currentTimeMillis() / 1000L).toString());
        datas.put("data", paramsMap);

        // 将传参转为Json格式
        String jsonData = JSONObject.toJSONString(datas);
        String authorization = HmacSHA256Utils.hmacSHA256(jsonData, mabangAppEntity.getSecretKey());

        //设置请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Authorization", authorization);
        HttpCommonUtil httpCommonUtil = new HttpCommonUtil();
        Map<String, Object> stringObjectMap = null;
        try {
            stringObjectMap = httpCommonUtil.sendOkhttp(UrlContant.MABANG_HOST, jsonData, null, headerMap, RequestMethod.POST);
            if (stringObjectMap.get("code").equals(200)) {
                JSONObject jsonObject = JSONObject.parseObject(String.valueOf(stringObjectMap.get("data")));
                List<ShopEntity> dataList = JSONObject.parseArray(jsonObject.get("data").toString(), ShopEntity.class);
                infoArrayList.addAll(dataList);
            } else {
                log.info(" ===== 马帮拉取店铺信息失败，错误信息：+" + stringObjectMap + " ==== 时间戳：" + new Date().getTime() + "");
                throw new RuntimeException(" ===== 马帮拉取店铺信息失败，错误信息：+" + stringObjectMap + " ====");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info("请求接口地址异常 错误信息：" + e.getMessage());
            Integer errorCount = dto.getJobTaskDTO().getErrorCount();
            if (errorCount < 3) {
                dto.getJobTaskDTO().setErrorCount(errorCount + 1);
                redisTemplate.boundListOps(dto.getJobTaskDTO().getTaskName()).leftPush(JSONObject.toJSONString(dto.getJobTaskDTO()));
            } else {
                DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity();
                dmpErrorLogEntity.setTaskId(dto.getJobTaskDTO().getId());
                dmpErrorLogEntity.setParams(jsonData);
                dmpErrorLogEntity.setErrorMsg(e.getMessage());
                dmpErrorLogEntity.setReturnMsg(JSONObject.toJSONString(stringObjectMap));
                dmpErrorLogEntity.setCreateTime(LocalDateTime.now());
                dmpErrorLogService.add(dmpErrorLogEntity);
            }
        }

        return infoArrayList;
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
