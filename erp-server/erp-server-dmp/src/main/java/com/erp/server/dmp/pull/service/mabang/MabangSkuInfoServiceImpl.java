package com.erp.server.dmp.pull.service.mabang;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpSkuInfoEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.mabang.SkuInfoEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpSkuInfoService;
import com.erp.server.dmp.utils.MabangApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 马帮商品
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.STOCK_DO_SEARCH_SKU_LIST)
public class MabangSkuInfoServiceImpl implements IReportSaveService<SkuInfoEntity> {

    @Resource
    private MongoService mongoService;

    @Resource
    private DmpSkuInfoService dmpSkuInfoService;

    @Override
    public void pullDataSave(RequestDTO dto) throws Exception {
        List<SkuInfoEntity> entityList = pullDate(dto);
        if (CollectionUtil.isEmpty(entityList)) {
            log.info("拉取马帮SKU信息列表数据为空 entityList.size = 0 ");
            return;
        }
        log.info("拉取马帮SKU信息列表数据 entityList.size = {} ", entityList.size());
        List<SkuInfoEntity> insertList = new ArrayList<>();
        List<SkuInfoEntity> pushToMqList = new ArrayList<>();
        for (SkuInfoEntity entity : entityList) {
            OrderMongoDTO orderMongoDTO = new OrderMongoDTO(entity.getId());
            List<SkuInfoEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_MABANG_SKU, SkuInfoEntity.class);
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            SkuInfoEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            if (mongoDatum.toString().equals(entity.toString())) {
                continue;
            }
            pushToMqList.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            mongoService.updateMongoData(orderMongoDTO, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_SKU, SkuInfoEntity.class);
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_MABANG_SKU);
        }
        // 不需要 推送到MQ

    }

    /**
     * 请求马帮商品接口
     * @param dto
     * @return
     */
    private List<SkuInfoEntity> pullDate(RequestDTO dto) throws Exception {
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        dto.getJobTaskDTO().setLastTime(nextTime);
        return MabangApiUtils.querySkuList(dto.getPlatformApiEnum().getTaskName(), lastTime, nextTime);
    }

    /**
     * 解析订单数据
     **/
    public void analysisOrder(SkuInfoEntity skuInfoEntity) throws Exception {
        DmpSkuInfoEntity dmpSkuInfoEntity = new DmpSkuInfoEntity();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
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
            dmpSkuInfoEntity.setSkuCreateTime(LocalDateTime.parse(skuInfoEntity.getTimeCreated(), sdf));
        }
        //商品修改时间
        if (StringUtils.isNotBlank(skuInfoEntity.getTimeModify())) {
            dmpSkuInfoEntity.setSkuUpdateTime(LocalDateTime.parse(skuInfoEntity.getTimeModify(), sdf));
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
        dmpSkuInfoEntity.setPlatformSign(PlatformEnum.MABANG.getDesc());

        dmpSkuInfoEntity.setCreateTime(LocalDateTime.now());

        dmpSkuInfoService.checkOrder(dmpSkuInfoEntity);
    }
}
