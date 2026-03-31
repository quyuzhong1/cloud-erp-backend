package com.erp.server.tms.listener;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.tms.dto.DictBasicDTO;
import com.erp.model.tms.dto.excel.ImportLogisticsThirdChannelRefExcelDTO;
import com.erp.model.tms.entity.BasicQueryLogisticsProviderEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.erp.model.tms.enums.LogisticsThirdChannelRefPushTypeEnum;
import com.erp.model.tms.enums.TrackPlatformTypeEnum;
import com.erp.server.tms.service.DictBasicService;
import com.erp.server.tms.service.LogisticsChannelService;
import com.erp.server.tms.service.LogisticsSupplierService;
import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LogisticsThirdChannelRefListener extends AnalysisEventListener<ImportLogisticsThirdChannelRefExcelDTO> {
    private List<LogisticsSupplierEntity> logisticsSupplierEntities;
    private Map<String, LogisticsChannelEntity> logisticsChannelMap;
    private Map<String, BasicQueryLogisticsProviderEntity> queryLogisticsProviderMap;
    /**
     * 错误信息
     */
    @Getter
    private List<ImportLogisticsThirdChannelRefExcelDTO> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private final List<ImportLogisticsThirdChannelRefExcelDTO> dataList = new ArrayList<>();

    /**
     * 成功信息
     */
    @Getter
    private List<ImportLogisticsThirdChannelRefExcelDTO> successList = new ArrayList<>();

    private final LogisticsSupplierService logisticsSupplierService = SpringUtil.getBean(LogisticsSupplierService.class);
    private final LogisticsChannelService logisticsChannelService = SpringUtil.getBean(LogisticsChannelService.class);
    private final DictBasicService dictBasicService = SpringUtil.getBean(DictBasicService.class);

    public LogisticsThirdChannelRefListener(List<LogisticsSupplierEntity> logisticsSupplierEntities,Map<String, LogisticsChannelEntity> logisticsChannelMap, Map<String, BasicQueryLogisticsProviderEntity> queryLogisticsProviderMap) {
        this.logisticsSupplierEntities=logisticsSupplierEntities;
        this.logisticsChannelMap=logisticsChannelMap;
        this.queryLogisticsProviderMap=queryLogisticsProviderMap;

    }

   /**
    * @description: 每解析一行数据回调一遍
    * @author Will
    * @date: 2023/3/7 11:22
    * @param excelDTO 导入信息
    * @param analysisContext
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(ImportLogisticsThirdChannelRefExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();

        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }

        String mainDictPlatformName = excelDTO.getMainDictPlatformName().trim();
        PlatformDictEnum platformDict = PlatformDictEnum.getByName(mainDictPlatformName);
        if (Objects.isNull(platformDict)) {
            errorMsgList.add("服务商不存在");
        }else {
            excelDTO.setMainDictPlatform(platformDict.getCode());
        }
        //查询服务商
        String platformType = TrackPlatformTypeEnum.getCode(excelDTO.getPlatformTypeName());
        if (CharSequenceUtil.isBlank(platformType)) {
            errorMsgList.add("服务商不存在");
        }else {
            excelDTO.setPlatformType(platformType);
        }

        //我司物流商合我司渠道
        String logisticsSupplierName = excelDTO.getLogisticsSupplierName();
        if (CharSequenceUtil.isBlank(logisticsSupplierName)) {
            errorMsgList.add("物流商不能为空");
        }else {
            LogisticsSupplierEntity logisticsSupplierEntity = logisticsSupplierEntities.stream().filter(e -> Objects.equals(logisticsSupplierName, e.getSupplierName()) || Objects.equals(logisticsSupplierName, e.getShortName())).findFirst().orElse(null);
            if(Objects.isNull(logisticsSupplierEntity)){
                errorMsgList.add("物流商不存在");
            }else {
                excelDTO.setLogisticsSupplierId(logisticsSupplierEntity.getId());

                String logisticsChannelName = excelDTO.getLogisticsChannelName();
                if (CharSequenceUtil.isBlank(logisticsChannelName)) {
                    errorMsgList.add("物流商渠道不能为空");
                }else {
                    LogisticsChannelEntity logisticsChannelEntity = logisticsChannelMap.getOrDefault(logisticsChannelName, null);
                    if(Objects.isNull(logisticsChannelEntity)){
                        errorMsgList.add("物流商渠道不存在");
                    }else if(Objects.equals(logisticsChannelEntity.getMainId(),excelDTO.getLogisticsSupplierId())){
                        errorMsgList.add("物流商下该渠道不存在");
                    }else{
                        excelDTO.setLogisticsChannelId(logisticsChannelEntity.getId());
                    }
                }
            }
        }
        //是否推送电话
        if (CharSequenceUtil.isNotBlank(excelDTO.getPushMobileName()) && excelDTO.getPushMobileName().equals("是")){
            excelDTO.setIsPushMobile(Boolean.TRUE);
        }else if (CharSequenceUtil.isNotBlank(excelDTO.getPushMobileName()) && excelDTO.getPushMobileName().equals("否")){
            excelDTO.setIsPushMobile(Boolean.FALSE);
        }else {
            errorMsgList.add("是否推送电话必须是是/否");
        }

        //查询物流商
        String thirdSupplierName = excelDTO.getThirdSupplierName();
        if (CharSequenceUtil.isBlank(thirdSupplierName)) {
            errorMsgList.add("查询物流商（中文）不能为空");
        }else {
            BasicQueryLogisticsProviderEntity basicQueryLogisticsProviderEntity = queryLogisticsProviderMap.getOrDefault(thirdSupplierName, null);
            if(Objects.isNull(basicQueryLogisticsProviderEntity)){
                errorMsgList.add("查询物流商不存在");
            }else if(!Objects.equals(basicQueryLogisticsProviderEntity.getTrackPlatformType(),platformType)){
                errorMsgList.add("查询服务商与查询物流商基础信息配置不一致");
            }else if(!Objects.equals(basicQueryLogisticsProviderEntity.getIsRegisterPhone(),excelDTO.getIsPushMobile())){
                errorMsgList.add("是否推送电话与查询物流商基础信息配置不一致");
            }
        }

        //推送类型
        String pushType = LogisticsThirdChannelRefPushTypeEnum.getCodeByName(excelDTO.getPushTypeName());
        if (CharSequenceUtil.isBlank(pushType)) {
            errorMsgList.add("推送类型不存在");
        }else {
            excelDTO.setPushType(pushType);
        }
        //添加数据用于判断是否为空
//        dataList.add(excelDTO);
        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        dataList.add(excelDTO);

    }

    public List<ImportLogisticsThirdChannelRefExcelDTO> getExcelDateList(){
        return dataList;
    }

    /**
     * @description: 数据全部解析完后删除明细
     * @author Will
     * @date: 2023/3/7 15:32
     * @param analysisContext
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        //统一处理基础校验成功数据
        if (CollectionUtils.isNotEmpty(dataList)) {
            //查询我司物流商
            List<LogisticsSupplierEntity> supplierEntityList = logisticsSupplierService.list();
            //查询我司渠道
            List<LogisticsChannelEntity> channelEntityList = logisticsChannelService.list();
            List<ShopInfoEntity> shopInfoEntityList = FeignQuery.list(ShopInfoEntity.class);
            List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey("channelSalesPlatform");
            //处理数据
            for (ImportLogisticsThirdChannelRefExcelDTO e : dataList) {
                List<String> errorMsgList = new ArrayList<>();
                supplierEntityList.stream().filter(supplierEntity -> supplierEntity.getSupplierName().equals(e.getLogisticsSupplierName())).findFirst().ifPresent(supplierEntity -> {
                    e.setLogisticsSupplierId(supplierEntity.getId());
                });
//                if (CharSequenceUtil.isBlank(e.getLogisticsSupplierId())){
//                    errorMsgList.add(CharSequenceUtil.format("供应商【{}】未匹配到", e.getLogisticsSupplierName()));
//                }
                channelEntityList.stream().filter(channelEntity -> channelEntity.getName().equals(e.getLogisticsChannelName()) && channelEntity.getMainId().equals(e.getLogisticsSupplierId())).findFirst().ifPresent(channelEntity -> {
                    e.setLogisticsChannelId(channelEntity.getId());
                });
//                if (CharSequenceUtil.isBlank(e.getLogisticsChannelId())){
//                    errorMsgList.add(CharSequenceUtil.format("渠道【{}】未匹配到", e.getLogisticsChannelName()));
//                }
                if (LogisticsThirdChannelRefPushTypeEnum.SHOP_SENDER.getCode().equals(e.getPushType()) && e.getIsPushMobile()){
                    shopInfoEntityList.stream().filter(shopInfoEntity -> shopInfoEntity.getName().equals(e.getShopName())).findFirst().ifPresent(shopInfoEntity -> {
                        e.setShopId(shopInfoEntity.getId());
                    });
                    if (CharSequenceUtil.isBlank(e.getShopId())){
                        errorMsgList.add(CharSequenceUtil.format("店铺【{}】未匹配到", e.getShopName()));
                    }
                }else if (LogisticsThirdChannelRefPushTypeEnum.PLATFORM_SENDER.getCode().equals(e.getPushType()) && e.getIsPushMobile()){
                    if (CharSequenceUtil.isBlank(e.getShopName())){
                        errorMsgList.add("平台不能为空");
                    }
                    DictBasicDTO.ViewDTO dict = dictList.stream().filter(f -> f.getName().equals(e.getShopName())).findFirst().orElse(null);
                    if (Objects.isNull(dict)){
                        errorMsgList.add(CharSequenceUtil.format("平台【{}】未匹配到", e.getShopName()));
                    }else {
                        e.setDictPlatform(dict.getCode());
                    }
                }else if (LogisticsThirdChannelRefPushTypeEnum.RECEIVER.getCode().equals(e.getPushType())){
                    if (CharSequenceUtil.isBlank(e.getMobile()) && e.getIsPushMobile()){
                        errorMsgList.add("手机号不能为空");
                    }
                }
                if (!errorMsgList.isEmpty()) {
                    e.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                    errorList.add(e);
                    continue;
                }
                successList.add(e);
            }
        }
    }
}
