package com.erp.server.dmp.pull.service.gyy;

import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.HttpCommonUtil;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.constant.UrlContant;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.model.dmp.entity.GyyAppEntity;
import com.erp.model.dmp.enums.ErpPlatformSignEnum;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.gyy.GyyShopInfoEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.erp.server.dmp.pull.service.dmp.DmpShopInfoService;
import com.erp.server.dmp.utils.GyyUtils;
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
 * 管易云店铺
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.GY_ERP_SHOP_GET)
public class GyyShopInfoServiceImpl implements IReportSaveService<GyyShopInfoEntity> {

    @Resource
    private MongoService mongoService;

    @Resource
    private DmpErrorLogService dmpErrorLogService;

    @Resource
    private DmpShopInfoService dmpShopInfoService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    @Qualifier("gyyShopInfoServiceImpl")
    private IReportSaveService reportSaveService;

    public static void main(String[] args) {
        GyyShopInfoServiceImpl gyyShopInfoService = new GyyShopInfoServiceImpl();
        PlatformApiEnum platformApiEnum = PlatformApiEnum.getEnumByType("gy.erp.shop.get");
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode("gy.erp.shop.get");
        jobTaskDTO.setApiId(12);
        jobTaskDTO.setApiName("管易云查询店铺列表");
        jobTaskDTO.setId(36L);
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(null);
        jobTaskDTO.setNextTime(null);
        jobTaskDTO.setPlatformId(1);
        jobTaskDTO.setState(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(platformApiEnum);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        List<GyyShopInfoEntity> orderEntities = gyyShopInfoService.pullDate(requestDTO);
        System.out.println(orderEntities);
    }


    @Override
    public void pullDataSave(RequestDTO dto) throws Exception {
        List<GyyShopInfoEntity> gyyShopInfoEntityList = pullDate(dto);
        if (gyyShopInfoEntityList != null && gyyShopInfoEntityList.size() > 0) {
            for (GyyShopInfoEntity gyyShopInfoEntity : gyyShopInfoEntityList) {
                OrderMongoDTO orderMongoDTO = new OrderMongoDTO();
                orderMongoDTO.setId(gyyShopInfoEntity.getId());
                List<GyyShopInfoEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_GYY_SHOP, GyyShopInfoEntity.class);
                if (mongoData != null && mongoData.size() > 0) {
                    for (GyyShopInfoEntity mongoDatum : mongoData) {
                        // 比较数据是否相同
                        if (!mongoDatum.toString().equals(gyyShopInfoEntity.toString())) {
                            // 修改数据
                            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(gyyShopInfoEntity), MapUtil.class);
                            try {
                                mongoService.updateMongoData(orderMongoDTO, mapUtil, MongoTableNameContant.ORIGINAL_GYY_SHOP, GyyShopInfoEntity.class);
                            } catch (Exception e) {
                                DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity();
                                dmpErrorLogEntity.setTaskId(dto.getJobTaskDTO().getId());
                                dmpErrorLogEntity.setParams("");
                                dmpErrorLogEntity.setErrorMsg("==== 管易云修改mongodb店铺数据失败，[ 店铺编号 = " + gyyShopInfoEntity.getCode() + "], 错误信息 = " + e.getMessage());
                                dmpErrorLogEntity.setReturnMsg("");
                                dmpErrorLogEntity.setCreateTime(LocalDateTime.now());
                                dmpErrorLogService.add(dmpErrorLogEntity);
                                throw new RuntimeException("==== 管易云修改mongodb店铺数据失败，[ 店铺编号 = " + gyyShopInfoEntity.getCode() + "], 错误信息 = " + e.getMessage());
                            }
                        }
                    }
                } else {
                    mongoService.saveMongoData(gyyShopInfoEntity, MongoTableNameContant.ORIGINAL_GYY_SHOP);
                }
                //存储数据到中台
                reportSaveService.analysisOrder(gyyShopInfoEntity);
            }
        }
    }

    /**
     * 请求管易云店铺接口
     *
     * @param dto
     * @return
     */
    public List<GyyShopInfoEntity> pullDate(RequestDTO dto) {
        List<GyyShopInfoEntity> infoArrayList = new ArrayList<>();
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

        GyyAppEntity gyyAppEntity = new GyyAppEntity();

        //每次最多获取100条
        Integer pageSize = 100;
        //当前页数
        Integer pageIndex = 1;
        //总页数
        Integer pageCount = 1;
        //总条数
        Integer totalCount = 0;
        HttpCommonUtil httpCommonUtil = new HttpCommonUtil();
        while (pageIndex <= pageCount) {
            // 封装传参数据
            Map<String, Object> datas = new HashMap();
            datas.put("method", dto.getJobTaskDTO().getApiCode());
            datas.put("appkey", gyyAppEntity.getAppKey());
            datas.put("sessionkey", gyyAppEntity.getSessionKey());
            datas.put("page_no", pageIndex);
            datas.put("page_size", pageSize);
            String str = JSONObject.toJSONString(datas);
            String sign = GyyUtils.sign(str, gyyAppEntity.getSecretKey());
            datas.put("sign", sign);
            // 将传参转为Json格式
            String jsonData = JSONObject.toJSONString(datas);
            //设置请求头
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("Content-Type", "application/json");

            Map<String, Object> stringObjectMap = null;
            try {
                stringObjectMap = httpCommonUtil.sendOkhttp(UrlContant.GYY_HOST, jsonData, null, headerMap, RequestMethod.POST);
                if (Boolean.valueOf(stringObjectMap.get("success").toString())) {
                    List<GyyShopInfoEntity> dataList = JSONObject.parseArray(String.valueOf(stringObjectMap.get("shops")), GyyShopInfoEntity.class);
                    totalCount = Integer.valueOf(stringObjectMap.get("total").toString());
                    pageCount = (totalCount + pageSize - 1) / pageSize;
                    infoArrayList.addAll(dataList);
                } else {
                    log.info(" ===== 管易云拉取店铺失败，错误信息：+" + stringObjectMap + " ====");
                    throw new RuntimeException(" ===== 管易云拉取店铺失败，错误信息：+" + stringObjectMap + " ====");
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
                break;
            }
            pageIndex++;
        }
        return infoArrayList;
    }

    /**
     * 解析店铺数据
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     **/
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void analysisOrder(GyyShopInfoEntity shopInfoEntity) {
        DmpShopInfoEntity dmpShopInfoEntity = new DmpShopInfoEntity();

        //平台店铺编号
        dmpShopInfoEntity.setPlarformShopNo(shopInfoEntity.getCode());

        //平台店铺账户
        dmpShopInfoEntity.setAccountUserName("");

        //平台店铺标识
        dmpShopInfoEntity.setAccountStoreName(shopInfoEntity.getNick());

        //店铺名称
        dmpShopInfoEntity.setName(shopInfoEntity.getName());

        //店铺站点
        dmpShopInfoEntity.setSite("CN");

        //店铺状态:1启用 2停用
        dmpShopInfoEntity.setStatus(1);

        //平台名称
        dmpShopInfoEntity.setPlatformName(shopInfoEntity.getTypeName());

        //财务编码
        dmpShopInfoEntity.setFinanceCode("");

        //平台标识
        dmpShopInfoEntity.setPlatformSign("管易云");

        dmpShopInfoService.checkOrder(dmpShopInfoEntity);
    }
}
