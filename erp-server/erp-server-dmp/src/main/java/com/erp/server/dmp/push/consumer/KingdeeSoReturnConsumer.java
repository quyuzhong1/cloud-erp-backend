package com.erp.server.dmp.push.consumer;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.enums.SyncKingdeeOperateEnum;
import com.common.core.enums.ApiError;
import com.common.core.utils.FastJsonUtil;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.ApiSendStatusEnum;
import com.erp.model.dmp.enums.KingdeeDocStatusEnum;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.server.dmp.push.service.business.KingdeeSoReturnConsumerService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.push.service.kingdee.impl.KingdeeCommonServiceImpl;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.erp.server.dmp.utils.KingdeeUtils;
import com.kingdee.bos.webapi.entity.SaveParam;
import com.kingdee.bos.webapi.entity.SaveResult;
import com.kingdee.bos.webapi.sdk.K3CloudApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 对接金蝶销售出库
 *
 * @Author Luo_WG
 * @Date 2023/6/1 14:45
 **/
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_so_return_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_SO_RETURN, consumeMode = ConsumeMode.ORDERLY)
public class KingdeeSoReturnConsumer implements RocketMQListener<Map<String, Object>> {

    @Resource
    private KingdeeSoReturnConsumerService kingdeeSoReturnConsumerService;

    public static void main(String[] args) {
        //模块类型
        Integer type = ApiModuleTypeEnum.SO_RETURN.getCode();
        KingdeeCommonService kingdeeCommonService = new KingdeeCommonServiceImpl();
        Map<String, Object> map = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.SAL_RETURNSTOCK.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
//        queryFilters.add(String.format("FDocumentStatus = '%s'", "C"));
        queryFilters.add(String.format("FBillNo = '%s'", "CGTH23070600017"));
        queryFilters.add(StrUtil.format("FDocumentStatus in ({})", "'B','C','D'"));
        String filterStr = String.join(" and ", queryFilters);


        K3CloudApi client = new K3CloudApi();
        JSONObject json = JSONUtil.parseObj("{ \"FBillTypeID\" :{ \"FNUMBER\" : \"XSTHD01_SYS\" },\n" +
                "\"FStockOrgId\" :{ \"FNumber\" : \"113\" },\n" +
                "\"FSaleOrgId\" :{ \"FNumber\" : \"112\" },\n" +
                "\"FBillNo\" : \"XSTH23072800002\",\n" +
                "\"FDate\" : \"2023-07-28\",\n" +
                "\"FSaledeptid\" :{ \"FNumber\" : \"\" },\n" +
                "\"FSalesManId\" :{ \"FNumber\" : \"zf_GW000117_1\" },\n" +
                "\"FRetcustId\" :{ \"FNumber\" : \"CUST23070900002\" },\n" +
                "\"FSettleOrgId\" :{ \"FNumber\" : \"112\" },\n" +
                "\"FSettleCurrId\" :{ \"FNumber\" : \"PRE001\" },\n" +
                "\"FReturnReason\" :{ \"FNumber\" : \"\" },\n" +
                "\"SubHeadEntity\" :{ \"FExchangeTypeId\" :{ \"FNumber\" : \"HLTX01_SYS\" },\n" +
                "\"FExchangeRate\" : \"1\" },\n" +
                "\"F_ULZ_data_sources\" : \"UlanziERP\",\n" +
                "\"FSettleTypeId\" :{ \"FNumber\" : \"JSFS01_SYS\" },\n" +
                "\"FChageCondition\" :{ \"FNumber\" : \"\" },\n" +
                "\"FStockerId\" :{ \"FNumber\" : \"00029\" },\n" +
                "\"FEntity\" :[{ \"FMaterialId\" :{ \"FNumber\" : \"2109\" },\n" +
                "\"FStockId\" :{ \"FNumber\" : \"jp-tmjdc\" },\n" +
                "\"FIsFree\" : \"\",\n" +
                "\"FReturnType\" :{ \"FNumber\" : \"THLX01_SYS\" },\n" +
                "\"FOwnerTypeID\" : \"BD_OwnerOrg\",\n" +
                "\"FOwnerId\" :{ \"FNumber\" : \"112\" },\n" +
                "\"FDeliveryDate\" : \"2023-07-28\",\n" +
                "\"FNote\" : \"\",\n" +
                "\"FSrcBillTypeID\" : \"\",\n" +
                "\"FSrcBillNo\" : \"\",\n" +
                "\"FSalUnitID\" :{ \"FNumber\" : \"Pcs\" },\n" +
                "\"FStockstatusId\" :{ \"FNumber\" : \"KCZT01_SYS\" },\n" +
                "\"FUnitID\" :{ \"FNumber\" : \"Pcs\" },\n" +

                "\"FPrice\" : \"\",\n" +
                "\"FTaxPrice\" : 0,\n" +
                "\"FMustqty\" : 4,\n" +
                "\"FAmount\" : \"\",\n" +
                "\"FSalUnitQty\" : 4,\n" +
                "\"FPriceUnitQty\" : \"\",\n" +
                "\"FRealQty\" : 4,\n" +
                "\"FOrderNo\" : \"\",\n" +
                "\"FStockLocId\" :{ \"FSTOCKLOCID__FF100014\" :{ \"FNumber\" : \"\" }}}]}");

        //判断金蝶系统是否已存在该数据
        SaveParam param = new SaveParam(json);
        SaveResult result;
        try {
            result = client.save(KingdeePushModuleEnum.SAL_RETURNSTOCK.getCode(), param);
            if (!result.isSuccessfully()) {
                throw new RuntimeException("【保存】出错:" + JSONUtil.toJsonStr(result.getResult().getResponseStatus().getErrors()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void onMessage(Map<String, Object> map) {
        try {
            kingdeeSoReturnConsumerService.executeConsumer(map);
        }catch (Exception e){
            log.error("KingdeeSoReturnConsumer>>>onMessage>>>map ={}", map, e);

        }

    }


}
