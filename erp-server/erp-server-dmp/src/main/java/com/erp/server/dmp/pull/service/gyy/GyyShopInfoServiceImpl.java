package com.erp.server.dmp.pull.service.gyy;

import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.MapUtil;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.gyy.GyyShopInfoEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.erp.server.dmp.pull.service.dmp.DmpShopInfoService;
import com.erp.server.dmp.utils.GyyApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

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
    @Resource
    @Qualifier("gyyShopInfoServiceImpl")
    private IReportSaveService reportSaveService;

    public static void main(String[] args) {
        GyyShopInfoServiceImpl gyyShopInfoService = new GyyShopInfoServiceImpl();
        PlatformApiEnum platformApiEnum = PlatformApiEnum.GY_ERP_SHOP_GET;
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(platformApiEnum.getTaskName());
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
        List<GyyShopInfoEntity> orderEntities = null;
        try {
            orderEntities = gyyShopInfoService.pullDate(requestDTO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
    private List<GyyShopInfoEntity> pullDate(RequestDTO dto) throws Exception {
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        dto.getJobTaskDTO().setLastTime(nextTime);
        return GyyApiUtils.queryShopList(dto.getPlatformApiEnum().getTaskName(), lastTime, nextTime);
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
