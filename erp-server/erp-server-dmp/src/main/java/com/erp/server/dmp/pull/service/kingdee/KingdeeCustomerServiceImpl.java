package com.erp.server.dmp.pull.service.kingdee;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.constant.RocketMqTopic;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.enums.ApiKingdeeOrganizationEnum;
import com.erp.model.dmp.enums.RocketMqTagEnum;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.KingdeeShopMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.kingdee.KingdeeShopEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportHistoryService;
import com.erp.server.dmp.pull.service.dmp.DmpShopInfoService;
import com.erp.server.dmp.pull.service.dmp.PlatformApiTaskService;
import com.erp.server.dmp.service.mq.MQProducerService;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 金蝶商品
 */
@Slf4j
@Component
public class KingdeeCustomerServiceImpl implements IReportHistoryService<KingdeeShopEntity> {
    @Resource
    private MongoService mongoService;

    @Resource
    private DmpShopInfoService dmpShopInfoService;

    @Resource
    private PlatformApiTaskService platformApiTaskService;

    @Resource
    private MQProducerService<DmpShopInfoEntity> mqProducerService;
    @Resource
    @Qualifier("kingdeeCustomerServiceImpl")
    private IReportHistoryService reportSaveService;

    public static void main(String[] args) {
        KingdeeCustomerServiceImpl kingdeeCustomerServiceImpl = new KingdeeCustomerServiceImpl();
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(PlatformApiEnum.BD_CUSTOMER.getTaskName());
        jobTaskDTO.setApiId(5);
        jobTaskDTO.setApiName("获取订单列表");
        jobTaskDTO.setId(30L);
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(LocalDateTime.parse("2022-01-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setNextTime(LocalDateTime.parse("2023-10-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setPlatformId(1);
        jobTaskDTO.setState(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(PlatformApiEnum.BD_CUSTOMER);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        try {
            List<KingdeeShopEntity>  kingdeeSkuEntities = kingdeeCustomerServiceImpl.pullDate(requestDTO);
            System.out.println(kingdeeSkuEntities);
        }catch (Exception e) {

        }

    }

    @Override
    public void pullDataSave(RequestDTO dto) throws Exception {
        List<KingdeeShopEntity> skuEntityList = pullDate(dto);
        if (CollectionUtil.isEmpty(skuEntityList)){
            log.info("拉取管易退货订单列表数据为空 entityList.size = 0 ");
            XxlJobHelper.log("拉取管易退货订单列表数据为空 entityList.size = 0 ");
            return;
        }
        log.info("拉取管易退货订单列表数据 entityList.size = {}} ", skuEntityList.size());
        XxlJobHelper.log("拉取管易退货订单列表数据 entityList.size = {}} ", skuEntityList.size());
        List<KingdeeShopEntity> insertList = new ArrayList<>();
        List<KingdeeShopEntity> pushToMqList = new ArrayList<>();
        for (KingdeeShopEntity entity : skuEntityList) {
            KingdeeShopMongoDTO queryMongoDTO = new KingdeeShopMongoDTO(entity.getFCustId());
            List<KingdeeShopEntity> mongoData = mongoService.findMongoData(queryMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_KINGDEE_SHOP, KingdeeShopEntity.class);
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            KingdeeShopEntity mongoDatum = mongoData.get(0);
            String id = mongoDatum.get_id();
            mongoDatum.set_id(null);
            // 比较数据是否相同
            if (mongoDatum.toString().equals(entity.toString())) {
                continue;
            }
            pushToMqList.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            KingdeeShopMongoDTO updateDto = new KingdeeShopMongoDTO(id);
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_SHOP, KingdeeShopEntity.class);
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_KINGDEE_SHOP);
        }
        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("金蝶商户数据为空, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }
        // 构造订单结构
        List<DmpShopInfoEntity> mabangToMqlist = pushToMqList.parallelStream()
                .map(this::initOrderInfoEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());

        // 异步推送到MQ
        mabangToMqlist.stream().peek(msg ->
                        mqProducerService.asyncClassMsg(RocketMqTopic.DMP_TOPIC, RocketMqTagEnum.KINGDEE_SHOP_INFO_TAG.getName(),
                                msg, StrUtil.format("{}_{}", msg.getPlarformShopNo(), msg.getFinanceCode())))
                .collect(Collectors.toList());

    }

    @Override
    public void pullHistoryOrderInfo(RequestDTO requestDTO) throws Exception {
        //拉取数据 存库
        pullDataSave(requestDTO);
        // 修改任务执行结果信息
        Boolean aBoolean = platformApiTaskService.updateTaskStateById(requestDTO.getJobTaskDTO(), 1);
        if (!aBoolean) {
            throw new RuntimeException("修改任务下次执行时间失败！");
        }
    }

    private DmpShopInfoEntity initOrderInfoEntity(KingdeeShopEntity shopEntity) {
//        if (!"1".equals(shopEntity.getFUseOrgId())) {
//            return;
//        }
        DmpShopInfoEntity dmpShopInfoEntity = new DmpShopInfoEntity();
        //平台店铺编号
        dmpShopInfoEntity.setPlarformShopNo(shopEntity.getFNumber());
        //平台店铺账户
        dmpShopInfoEntity.setAccountUserName(shopEntity.getFName());
        //平台店铺标识
        dmpShopInfoEntity.setAccountStoreName(shopEntity.getFName());
        //店铺名称
        dmpShopInfoEntity.setName(shopEntity.getFName());
        //店铺站点
//        String site = "";
//        if (StrUtil.isNotEmpty(shopEntity.getFCOUNTRY_FNumber())) {
//            CountrySiteEnum countrySite = CountrySiteEnum.getByKingDeeCode(shopEntity.getFCOUNTRY_FNumber());
//            site = ObjectUtil.isNotEmpty(countrySite) ? countrySite.getKingDeeCode() : "";
//        }
//        dmpShopInfoEntity.setSite(site);
        //店铺状态:1启用 2停用
        dmpShopInfoEntity.setStatus(2);
        if ("A".equals(shopEntity.getFForbidStatus())){
            dmpShopInfoEntity.setStatus(1);
        }
        //平台名称
        dmpShopInfoEntity.setPlatformName(shopEntity.getF_ulz_Assistant_FDataValue());
        String orgName = shopEntity.getFUseOrgId_FName();
        if(StrUtil.isNotBlank(orgName)){
            dmpShopInfoEntity.setIsVijim(Boolean.TRUE);
            if (orgName.contains("优至胜") || orgName.contains("小隼")) {
                dmpShopInfoEntity.setIsVijim(Boolean.FALSE);
            }
        }
        dmpShopInfoEntity.setUseOrgId(Integer.parseInt(shopEntity.getFUseOrgId()));
        dmpShopInfoEntity.setUseOrgName(shopEntity.getFUseOrgId_FName());
        //财务编码
//        dmpShopInfoEntity.setFinanceCode("");
        //平台标识
        dmpShopInfoEntity.setPlatformSign(PlatformEnum.KINGDEE.getDesc());
        dmpShopInfoEntity.setCountry(shopEntity.getFCOUNTRY_FNumber());
        dmpShopInfoEntity.setCustomerId(shopEntity.getFCustId());
        dmpShopInfoEntity.setCreateTime(LocalDateTime.now());
        return dmpShopInfoEntity;
    }
    /**
     * 请求金蝶云星空客户列表接口
     *
     * @param dto
     * @return
     */
    public List<KingdeeShopEntity> pullDate(RequestDTO dto) throws Exception {
        // 当前页数
        Integer pageIndex = 0;
        // 每次最多获取100条
        Integer pageSize = 10000;
        List<KingdeeShopEntity> shopEntityList = new ArrayList<>();
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        if(null == lastTime || null == nextTime){
            lastTime = LocalDateTime.parse("2021-01-01T00:00:00");
            nextTime = LocalDateTime.now();
        }
        dto.getJobTaskDTO().setLastTime(nextTime);
        //读取配置，初始化SDK
        LinkedList<String> queryFilters = new LinkedList<>();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        queryFilters.add(String.format("FModifyDate >= '%s'", sdf.format(lastTime.minusMinutes(2))));
        queryFilters.add(String.format("FModifyDate <= '%s'", sdf.format(nextTime)));
        // 客户类型为店铺
//        queryFilters.add(String.format("FCustTypeId.FNumber = '%s'", "KHLB004_SYS"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FCUSTID,FUseOrgId,FUseOrgId.FNumber,FUseOrgId.FName,FNumber,FName,FShortName,FCOUNTRY.FNumber,FWEBSITE," +
                "FGroup,FGroup.FNumber,FGroup.FName,FDescription,FInvoiceType,FCustTypeId.FDataValue,FCustTypeId.FNumber,F_ulz_Assistant.FNumber," +
                "F_ulz_Assistant.FDataValue,FDocumentStatus,FForbidStatus," +
                "FCreateDate,FModifyDate";

        Boolean dataSign = true;
        while (dataSign) {
            KingdeeApiUtils kingdeeApiUtils = new KingdeeApiUtils(dto.getPlatformApiEnum().getTaskName());
            List<Map<String, Object>> result = kingdeeApiUtils.queryList(filterStr, fieldKeys, pageSize, pageIndex, 0);
            XxlJobHelper.log("获取金蝶店铺数据第[{}]页 有{}条记录", pageIndex, pageSize);
            if (result.size() < pageSize){
                dataSign = false;
            }
            if (CollectionUtil.isEmpty(result)) {
                return Collections.emptyList();
            }
            List<KingdeeShopEntity> entityList = result.stream().map(shopEntity ->
                    BeanUtil.toBean(shopEntity, KingdeeShopEntity.class)).collect(Collectors.toList());
            shopEntityList.addAll(entityList);
            pageIndex ++;
        }
        return shopEntityList;
    }
}
