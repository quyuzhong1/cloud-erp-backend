package com.erp.server.dmp.pull.service.kingdee;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.enums.CountrySiteEnum;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.KingdeeShopMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import com.erp.model.dmp.enums.ErpPlatformSignEnum;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.kingdee.KingdeeShopEntity;
import com.erp.model.dmp.kingdee.KingdeeSkuEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportHistoryService;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.erp.server.dmp.pull.service.dmp.DmpShopInfoService;
import com.erp.server.dmp.pull.service.dmp.PlatformApiTaskService;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.kingdee.bos.webapi.sdk.K3CloudApi;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
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
public class KingdeeCustomerServiceImpl implements IReportHistoryService {
    @Resource
    private MongoService mongoService;

    @Resource
    private DmpErrorLogService dmpErrorLogService;

    @Resource
    private DmpShopInfoService dmpShopInfoService;

    @Resource
    private PlatformApiTaskService platformApiTaskService;

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
            return;
        }
        for (KingdeeShopEntity shopEntity : skuEntityList) {
            KingdeeShopMongoDTO shopMongoDTO = new KingdeeShopMongoDTO();
            shopMongoDTO.setCustId(shopEntity.getFCustId());
            List<KingdeeShopEntity> mongoDataList = mongoService.findMongoData(shopMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_KINGDEE_SHOP, KingdeeShopEntity.class);
            if (CollectionUtil.isEmpty(mongoDataList)) {
                mongoService.saveMongoData(shopEntity, MongoTableNameContant.ORIGINAL_KINGDEE_SHOP);
            }else {
                KingdeeShopEntity mongoShopEntity = mongoDataList.get(0);
                if (mongoShopEntity.toString().equals(shopEntity.toString())){
                    continue;
                }
                // 比较数据是否相同
                // 修改数据
                MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(shopEntity), MapUtil.class);
                try {
                    mongoService.updateMongoData(shopMongoDTO, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_SHOP, KingdeeSkuEntity.class);
                } catch (Exception e) {
                    log.error("==== 金蝶云星空修改mongo店铺数据失败，[ 商品编号 ={}, 错误信息]", shopMongoDTO.getCustId(), e);
                    DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity();
                    dmpErrorLogEntity.setTaskId(dto.getJobTaskDTO().getId());
                    dmpErrorLogEntity.setParams("");
                    dmpErrorLogEntity.setErrorMsg("==== 金蝶云星空修改mongo店铺数据失败，[ 商品编号 = " + shopMongoDTO.getCustId() + "], 错误信息 = " + e.getMessage());
                    dmpErrorLogEntity.setReturnMsg("");
                    dmpErrorLogEntity.setCreateTime(LocalDateTime.now());
                    dmpErrorLogService.add(dmpErrorLogEntity);
                    throw new RuntimeException("==== 金蝶云星空修改mongo店铺数据失败，[ 商品编号 = " + shopMongoDTO.getCustId() + "], 错误信息 ={} " , e);
                }
            }
            //存储数据到中台
            saveShopEntity(shopEntity);
        }
    }

    @Override
    public void pullHistoryOrderInfo(RequestDTO requestDTO) throws Exception {
        //拉取数据 存库
        pullDataSave(requestDTO);
        // 修改任务执行结果信息
        Boolean aBoolean = platformApiTaskService.updateTaskStateById(requestDTO.getJobTaskDTO());
        if (!aBoolean) {
            throw new RuntimeException("修改任务下次执行时间失败！");
        }
    }

    private void saveShopEntity(KingdeeShopEntity shopEntity) {
//        if (!"1".equals(shopEntity.getFUseOrgId())) {
//            return;
//        }
        DmpShopInfoEntity dmpShopInfoEntity = new DmpShopInfoEntity();
        //平台店铺编号
//        dmpShopInfoEntity.setPlarformShopNo(shopEntity.getFNumber());

        //平台店铺账户
//        dmpShopInfoEntity.setAccountUserName(shopEntity.getFName());

        //平台店铺标识
//        dmpShopInfoEntity.setAccountStoreName(shopEntity.getFName());

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
//        dmpShopInfoEntity.setStatus(2);
//        if ("A".equals(shopEntity.getFForbidStatus())){
//            dmpShopInfoEntity.setStatus(1);
//        }

        //平台名称
//        dmpShopInfoEntity.setPlatformName(shopEntity.getF_ulz_Assistant_FDataValue());
        String orgName = shopEntity.getFUseOrgId_FName();
        if(StrUtil.isNotBlank(shopEntity.getFUseOrgId_FName())){
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
//        dmpShopInfoEntity.setPlatformSign("金蝶云星空");

        dmpShopInfoEntity.setCountry(shopEntity.getFCOUNTRY_FNumber());
        dmpShopInfoEntity.setCustomerId(shopEntity.getFCustId());
        dmpShopInfoService.checkShopByKingDee(dmpShopInfoEntity);
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
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        if (dto.getJobTaskDTO().getLastTime() != null && dto.getJobTaskDTO().getNextTime() != null) {
            lastTime = lastTime.minusMinutes(2);
        } else {
            lastTime = LocalDateTime.now();
            nextTime = lastTime.minusDays(1);
        }
        String st = sdf.format(lastTime);
        String sd = sdf.format(nextTime);
        dto.getJobTaskDTO().setLastTime(nextTime);
        //读取配置，初始化SDK
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FModifyDate >= '%s'", st));
        queryFilters.add(String.format("FModifyDate <= '%s'", sd));
        // 客户类型为店铺
        queryFilters.add(String.format("FCustTypeId.FNumber = '%s'", "KHLB004_SYS"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FCUSTID,FUseOrgId,FUseOrgId.FNumber,FUseOrgId.FName,FNumber,FName,FShortName,FCOUNTRY.FNumber,FWEBSITE," +
                "FGroup,FGroup.FNumber,FGroup.FName,FDescription,FInvoiceType,FCustTypeId.FDataValue,FCustTypeId.FNumber,F_ulz_Assistant.FNumber,F_ulz_Assistant.FDataValue,FDocumentStatus,FForbidStatus," +
                "FCreateDate,FModifyDate";

//        String jsonData = "{\"CreateOrgId\":1,\"Number\":\"\",\"Id\":\"331875\",\"IsSortBySeq\":\"false\"}";
//        K3CloudApi client = new K3CloudApi();
//        String view = client.view(PlatformApiEnum.BD_CUSTOMER.taskName, jsonData);
        Boolean dataSign = true;
        List<Map<String, Object>> result = new ArrayList<>();
        while (dataSign) {
            try {
                KingdeeApiUtils kingdeeApiUtils = new KingdeeApiUtils(PlatformApiEnum.BD_CUSTOMER.taskName);
                result = kingdeeApiUtils.queryList(filterStr, fieldKeys, pageSize, pageIndex);
                XxlJobHelper.log("获取金蝶店铺数据第[{}]页 有{}条记录", pageIndex, pageSize);
                if (CollectionUtil.isEmpty(result)) {
                    return Collections.emptyList();
                }
                if (result.size() < pageSize){
                    dataSign = false;
                }
                List<KingdeeShopEntity> entityList = result.stream().map(shopEntity ->
                        BeanUtil.toBean(shopEntity, KingdeeShopEntity.class)).collect(Collectors.toList());
                shopEntityList.addAll(entityList);
            } catch (Exception e) {
                log.error("金蝶客户列表请求接口地址异常 错误信息：", e);
                DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity();
                dmpErrorLogEntity.setTaskId(dto.getJobTaskDTO().getId());
                dmpErrorLogEntity.setParams(filterStr);
                dmpErrorLogEntity.setErrorMsg(e.getMessage());
                dmpErrorLogEntity.setReturnMsg(JSONObject.toJSONString(result));
                dmpErrorLogEntity.setCreateTime(LocalDateTime.now());
                dmpErrorLogService.add(dmpErrorLogEntity);
                throw new RuntimeException("金蝶客户列表接口异常", e);
            }
            pageIndex ++;
        }
        return shopEntityList;
    }
}
