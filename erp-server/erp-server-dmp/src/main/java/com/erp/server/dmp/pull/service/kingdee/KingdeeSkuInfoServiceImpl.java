package com.erp.server.dmp.pull.service.kingdee;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.erp.common.business.constant.RocketMqTopic;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpSkuInfoEntity;
import com.erp.model.dmp.enums.ApiKingdeeOrganizationEnum;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.RocketMqTagEnum;
import com.erp.model.dmp.kingdee.KingdeeSkuEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.service.mq.MQProducerService;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 金蝶商品
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.BD_MATERIAL)
public class KingdeeSkuInfoServiceImpl implements IReportSaveService<KingdeeSkuEntity> {

    @Resource
    private MongoService mongoService;

    @Resource
    private MQProducerService<DmpSkuInfoEntity> mqProducerService;

    @Override
    public void pullDataSave(RequestDTO dto) throws Exception {
        List<KingdeeSkuEntity> entityList = pullDate(dto);
        if (CollectionUtil.isEmpty(entityList)) {
            log.info("拉取金蝶SKU信息列表数据为空 entityList.size = 0 ");
            return;
        }
        log.info("拉取金蝶SKU信息列表数据 entityList.size = {} ", entityList.size());
        List<KingdeeSkuEntity> insertList = new ArrayList<>();
        List<KingdeeSkuEntity> pushToMqList = new ArrayList<>();
        for (KingdeeSkuEntity entity : entityList) {
            OrderMongoDTO orderMongoDTO = new OrderMongoDTO(entity.getFMaterialId());
            List<KingdeeSkuEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_KINGDEE_SKU, KingdeeSkuEntity.class);
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            KingdeeSkuEntity mongoDatum = mongoData.get(0);
            String id = mongoDatum.get_id();
            mongoDatum.set_id(null);
            // 比较数据是否相同
            if (mongoDatum.toString().equals(entity.toString())) {
                continue;
            }
            pushToMqList.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            OrderMongoDTO updateDto = new OrderMongoDTO(id);
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_SKU, KingdeeSkuEntity.class);
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_KINGDEE_SKU);
        }
        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("金蝶SKU信息, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }
        // 构造订单结构
        List<DmpSkuInfoEntity> mabangToMqlist = pushToMqList.parallelStream()
                .map(this::initOrderInfoEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());

        // 异步推送到MQ
        mabangToMqlist.stream().peek(msg ->
                        mqProducerService.asyncClassMsg(RocketMqTopic.DMP_TOPIC, RocketMqTagEnum.KINGDEE_SKU_INFO_TAG.getName(),
                                msg, StrUtil.format("{}_{}", msg.getSkuNo(), msg.getItemCode())))
                .collect(Collectors.toList());

    }

    /**
     * 请求金蝶云星空订单接口
     *
     * @param dto
     * @return
     */
    public List<KingdeeSkuEntity> pullDate(RequestDTO dto) throws Exception{
        List<KingdeeSkuEntity> infoArrayList = new ArrayList<>();
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        dto.getJobTaskDTO().setLastTime(nextTime);
        LinkedList<String> queryFilters = new LinkedList<>();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        queryFilters.add(String.format("FModifyDate >= '%s'", sdf.format(lastTime.minusMinutes(2))));
        queryFilters.add(String.format("FModifyDate <= '%s'", sdf.format(nextTime)));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FUseOrgId,FUseOrgId.FName,FNumber,FMaterialId,FName,FSpecification,FCreateDate,FModifyDate," +
                "FDocumentStatus,FForbidStatus,FRefStatus,FPurPrice_CMK,F_PRVD_Assistant.FDataValue," +
                "F_PRVD_Assistant1.FDataValue,FSalePrice_CMK,F_SSRQ,FErpClsID";

        Boolean dataSign = true;
        //当前页数
        Integer pageIndex = 0;

        //每次最多获取100条
        Integer pageSize = 10000;
        while (dataSign) {
            KingdeeApiUtils kingdeeApiUtils = new KingdeeApiUtils(dto.getPlatformApiEnum().getTaskName(), 1);
            List<Map<String, Object>> result = kingdeeApiUtils.queryList(filterStr, fieldKeys, pageSize, pageIndex, 0);
            XxlJobHelper.log("获取金蝶SKU数据第[{}]页 有{}条记录", pageIndex, pageSize);
            if (result.size() < pageSize){
                dataSign = false;
            }
            if (CollectionUtil.isEmpty(result)) {
                return Collections.emptyList();
            }
            List<KingdeeSkuEntity> entityList = result.stream().map(shopEntity ->
                    BeanUtil.toBean(shopEntity, KingdeeSkuEntity.class)).collect(Collectors.toList());
            infoArrayList.addAll(entityList);
            pageIndex ++;
        }
        return infoArrayList;
    }

    /**
     * 解析商品数据
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     **/
    public DmpSkuInfoEntity initOrderInfoEntity(KingdeeSkuEntity skuInfoEntity) {
        if (StrUtil.isEmpty(skuInfoEntity.getFUseOrgId()) ||
                ApiKingdeeOrganizationEnum.ORGANIZATION_YZS.getCode().equals(skuInfoEntity.getFUseOrgId()) ||
                ApiKingdeeOrganizationEnum.ORGANIZATION_XX.getCode().equals(skuInfoEntity.getFUseOrgId())
        ){
            return null;
        }
        DmpSkuInfoEntity dmpSkuInfoEntity = new DmpSkuInfoEntity();
        dmpSkuInfoEntity.setItemCode(skuInfoEntity.getFMaterialId());
        //sku编号
        dmpSkuInfoEntity.setSkuNo(skuInfoEntity.getFNumber());
        //中文名
        dmpSkuInfoEntity.setNameCn(skuInfoEntity.getFName());
        //英文名
        dmpSkuInfoEntity.setNameEn("");
        //统一成本价
        dmpSkuInfoEntity.setDefaultCost(new BigDecimal(skuInfoEntity.getFPurPrice_CMK()));
        Integer status = 3;
        if (skuInfoEntity.getFForbidStatus().equals("C")) {
            status = 5;
        }
        //商品状态:1.自动创建;2.待开发;3.正常;4.清仓;5.停止销售
        dmpSkuInfoEntity.setStatus(status);
        //商品创建时间
        dmpSkuInfoEntity.setSkuCreateTime(skuInfoEntity.getFCreateDate());
        //商品修改时间
        dmpSkuInfoEntity.setSkuUpdateTime(skuInfoEntity.getFModifyDate());
        //品牌
        dmpSkuInfoEntity.setBrandName("");
        //商品目录(一级)
        if (StringUtils.isNotBlank(skuInfoEntity.getF_PRVD_Assistant()) && !"null".equals(skuInfoEntity.getF_PRVD_Assistant())) {
            dmpSkuInfoEntity.setParentCategoryName(skuInfoEntity.getF_PRVD_Assistant());
        } else {
            dmpSkuInfoEntity.setParentCategoryName("");
        }
        //商品目录(二级)
        if (StringUtils.isNotBlank(skuInfoEntity.getF_PRVD_Assistant1()) && !"null".equals(skuInfoEntity.getF_PRVD_Assistant1())) {
            dmpSkuInfoEntity.setCategoryName(skuInfoEntity.getF_PRVD_Assistant1());
        } else {
            dmpSkuInfoEntity.setCategoryName("");
        }
        //售价
        dmpSkuInfoEntity.setSalePrice(new BigDecimal(skuInfoEntity.getFSalePrice_CMK()));
        //申报价格
        dmpSkuInfoEntity.setDeclarePrice(BigDecimal.ZERO);
        //开发员id
        dmpSkuInfoEntity.setDeveloperId("");
        //开发员名称
        dmpSkuInfoEntity.setDeveloperName("");
        //平台标识
        dmpSkuInfoEntity.setPlatformSign(PlatformEnum.KINGDEE.getDesc());
        //企业id
        dmpSkuInfoEntity.setCompanyId(skuInfoEntity.getFUseOrgId());
        //企业名称
        dmpSkuInfoEntity.setCompanyName(skuInfoEntity.getFUseOrgName());
        //上市时间
        if (!"null".equals(skuInfoEntity.getFSSRQ()) && StrUtil.isNotEmpty(skuInfoEntity.getFSSRQ())){
            dmpSkuInfoEntity.setListingTime(LocalDateTime.parse(skuInfoEntity.getFSSRQ()));
        }
        String itemProperty = "";
        switch (skuInfoEntity.getFErpClsID()) {
            case "1" :
                itemProperty = "外购";
                break;
            case "2" :
                itemProperty = "自制";
                break;
            case "3" :
                itemProperty = "委外";
                break;
            case "6" :
                itemProperty = "服务";
                break;
            default:
                itemProperty = "";
                break;
        }
        //物料属性
        dmpSkuInfoEntity.setItemProperty(itemProperty);
        dmpSkuInfoEntity.setCreateTime(LocalDateTime.now());
        return  dmpSkuInfoEntity;
    }
}
