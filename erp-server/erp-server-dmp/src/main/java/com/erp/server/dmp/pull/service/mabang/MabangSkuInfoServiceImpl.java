package com.erp.server.dmp.pull.service.mabang;

import com.alibaba.fastjson.JSONObject;
import com.common.core.security.HmacSHA256Utils;
import com.common.core.utils.HttpCommonUtil;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.constant.UrlContant;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.model.dmp.entity.DmpSkuInfoEntity;
import com.erp.model.dmp.entity.MabangAppEntity;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.mabang.SkuInfoEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.*;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.erp.server.dmp.pull.service.dmp.DmpSkuInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 马帮商品
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.STOCK_DO_SEARCH_SKU_LIST)
public class MabangSkuInfoServiceImpl implements IReportSaveService {

    @Resource
    private MongoService mongoService;

    @Resource
    private DmpErrorLogService dmpErrorLogService;

    @Resource
    private DmpSkuInfoService dmpSkuInfoService;

    @Override
    public void pullDataSave(RequestDTO dto) throws Exception {
        List<SkuInfoEntity> skuInfoEntities = pullDate(dto);
        if (skuInfoEntities != null && skuInfoEntities.size() > 0) {
            for (SkuInfoEntity skuInfoEntity : skuInfoEntities) {
                OrderMongoDTO orderMongoDTO = new OrderMongoDTO();
                orderMongoDTO.setStockSku(skuInfoEntity.getStockSku());
                List<SkuInfoEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_MABANG_SKU, SkuInfoEntity.class);
                if (mongoData != null && mongoData.size() > 0) {
                    for (SkuInfoEntity mongoDatum : mongoData) {
                        // 比较数据是否相同
                        if (!mongoDatum.toString().equals(skuInfoEntity.toString())) {
                            // 修改数据
                            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(skuInfoEntity), MapUtil.class);
                            try {
                                mongoService.updateMongoData(orderMongoDTO, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_SKU, SkuInfoEntity.class);
                            } catch (Exception e) {
                                DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity();
                                dmpErrorLogEntity.setTaskId(dto.getJobTaskDTO().getId());
                                dmpErrorLogEntity.setParams("");
                                dmpErrorLogEntity.setErrorMsg("==== 马帮修改mongodb商品数据失败，[ sku = " + skuInfoEntity.getStockSku() + "], 错误信息 = " + e.getMessage());
                                dmpErrorLogEntity.setReturnMsg("");
                                dmpErrorLogService.add(dmpErrorLogEntity);
                                throw new RuntimeException("==== 马帮修改mongodb商品数据失败，[ sku = " + skuInfoEntity.getStockSku() + "], 错误信息 = " + e.getMessage());
                            }
                        }
                    }
                } else {
                    mongoService.saveMongoData(skuInfoEntity, MongoTableNameContant.ORIGINAL_MABANG_SKU);
                }
                //存储数据到中台
                analysisSku(skuInfoEntity);
            }
        }
    }

    /**
     * 请求马帮商品接口
     * @param dto
     * @return
     */
    public List<SkuInfoEntity> pullDate(RequestDTO dto) {
        List<SkuInfoEntity> infoArrayList = new ArrayList<>();
        try {
            JobTaskDTO jobTask = dto.getJobTaskDTO();
            Integer lastTime = jobTask.getLastTime();
            Integer nextTime = jobTask.getNextTime();
            if (lastTime != 0 && nextTime != 0) {
                dto.getJobTaskDTO().setLastTime(nextTime);
            } else {
                dto.getJobTaskDTO().setLastTime(Integer.parseInt(String.valueOf(System.currentTimeMillis() / 1000)));
            }
            MabangAppEntity mabangAppEntity = new MabangAppEntity();
            String url = UrlContant.MABANG_HOST;
            String method = jobTask.getApiCode();
            String appKey = mabangAppEntity.getAppKey();
            String appSecret = mabangAppEntity.getSecretKey();

            //每次最多获取100条
            Integer pageSize = 100;
            //当前页数
            Integer pageIndex = 1;
            //总页数
            Integer pageCount = 1;
            HttpCommonUtil httpCommonUtil = new HttpCommonUtil();
            while (pageIndex <= pageCount) {
                Map<String, Object> paramsMap = new HashMap();
                paramsMap.put("page", pageIndex);
                paramsMap.put("rowsPerPage", pageSize);

                // 封装传参数据
                Map<String, Object> datas = new HashMap();
                datas.put("api", method);
                datas.put("appkey", appKey);
                datas.put("version", 1);
                datas.put("timestamp", new Long(System.currentTimeMillis() / 1000).toString());
                datas.put("data", paramsMap);

                // 将传参转为Json格式
                String jsonData = JSONObject.toJSONString(datas);
                String authorization = HmacSHA256Utils.hmacSHA256(jsonData, appSecret);

                //设置请求头
                Map<String,String> headerMap = new HashMap<>();
                headerMap.put("Content-Type", "application/json");
                headerMap.put("Authorization", authorization);
                Map<String, Object> stringObjectMap = null;
                try {
                    stringObjectMap = httpCommonUtil.sendOkhttp(url, jsonData, null, headerMap, RequestMethod.POST);
                    if (stringObjectMap.get("code").equals(200)) {
                        JSONObject jsonObject = JSONObject.parseObject(String.valueOf(stringObjectMap.get("data")));
                        List<SkuInfoEntity> dataList = JSONObject.parseArray(jsonObject.get("data").toString(), SkuInfoEntity.class);
                        pageCount = Integer.valueOf(jsonObject.get("totalPage").toString());
                        infoArrayList.addAll(dataList);
                    } else {
                        log.info(" ===== 马帮拉取商品失败，错误信息：+" + stringObjectMap + " ====");
                        throw new RuntimeException(" ===== 马帮拉取商品失败，错误信息：+" + stringObjectMap + " ====");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.info("请求接口地址异常 错误信息：" + e.getMessage());
                    DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity();
                    dmpErrorLogEntity.setTaskId(jobTask.getId());
                    dmpErrorLogEntity.setParams(jsonData);
                    dmpErrorLogEntity.setErrorMsg(e.getMessage());
                    dmpErrorLogEntity.setReturnMsg(JSONObject.toJSONString(stringObjectMap));
                    dmpErrorLogService.add(dmpErrorLogEntity);
                }
                pageIndex++;
            }
        } catch (Exception e) {
            log.info(" ===== 获取马帮商品列表数据失败， 错误信息 = { " + e.getMessage() + " }");
            throw new RuntimeException(" ===== 获取马帮商品列表数据失败， 错误信息 = { " + e.getMessage() + " }");
        }
        return infoArrayList;
    }

    /**
     * 解析订单数据
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     * @return void
     **/
    public void analysisSku(SkuInfoEntity skuInfoEntity) throws Exception {
        DmpSkuInfoEntity dmpSkuInfoEntity = new DmpSkuInfoEntity();
        SimpleDateFormat sdf = new SimpleDateFormat(EnumTimePattern.y_m_dhms.toTimePattern());

        dmpSkuInfoEntity.setItemCode(skuInfoEntity.getStockSku());

        //sku编号
        dmpSkuInfoEntity.setSkuNo(skuInfoEntity.getStockSku());

        //中文名
        dmpSkuInfoEntity.setNameCn(skuInfoEntity.getNameCN());

        //英文名
        dmpSkuInfoEntity.setNameEn(skuInfoEntity.getNameEN());

        //统一成本价
        dmpSkuInfoEntity.setDefaultCost(skuInfoEntity.getDefaultCost());

        //商品状态:1.自动创建;2.待开发;3.正常;4.清仓;5.停止销售
        dmpSkuInfoEntity.setStatus(skuInfoEntity.getStatus());

        //商品创建时间
        if (StringUtils.isNotBlank(skuInfoEntity.getTimeCreated())) {
            dmpSkuInfoEntity.setSkuCreateTime(sdf.parse(skuInfoEntity.getTimeCreated()));
        }

        //商品修改时间
        if (StringUtils.isNotBlank(skuInfoEntity.getTimeModify())) {
            dmpSkuInfoEntity.setSkuUpdateTime(sdf.parse(skuInfoEntity.getTimeModify()));
        }

        //品牌
        dmpSkuInfoEntity.setBrandName(skuInfoEntity.getBrandName());

        //商品目录(一级)
        dmpSkuInfoEntity.setParentCategoryName(skuInfoEntity.getParentCategoryName());

        //商品目录(二级)
        dmpSkuInfoEntity.setCategoryName(skuInfoEntity.getCategoryName());

        //售价
        dmpSkuInfoEntity.setSalePrice(skuInfoEntity.getSalePrice());

        //申报价格
        dmpSkuInfoEntity.setDeclarePrice(skuInfoEntity.getDeclareValue());

        //开发员id
        dmpSkuInfoEntity.setDeveloperId(skuInfoEntity.getDeveloperId());

        //开发员名称
        dmpSkuInfoEntity.setDeveloperName(skuInfoEntity.getDeveloperName());

        //平台标识
        dmpSkuInfoEntity.setPlatformSign("马帮");

        dmpSkuInfoEntity.setCreateTime(new Date());

        dmpSkuInfoService.checkOrder(dmpSkuInfoEntity);
    }
}
