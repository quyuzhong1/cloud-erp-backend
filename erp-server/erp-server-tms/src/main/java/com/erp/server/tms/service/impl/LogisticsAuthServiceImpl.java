package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.wrapper.FeignQuery;
import com.common.core.constant.SqlConstants;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.oms.enums.AuthTypeEnum;
import com.erp.model.tms.dto.LogisticsAuthDTO;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.entity.LogisticsAuthFieldEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.erp.model.tms.enums.LogisticsAuthStatusEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.tms.handler.LogisticsRegistry;
import com.erp.server.tms.mapper.LogisticsAuthMapper;
import com.erp.server.tms.service.*;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 物流授权表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@Service
public class LogisticsAuthServiceImpl extends SuperServiceImpl<LogisticsAuthMapper, LogisticsAuthEntity> implements LogisticsAuthService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private LogisticsRegistry logisticsRegistry;
    @Resource
    private LogisticsSupplierService logisticsSupplierService;

    @Resource
    private LogisticsChannelService logisticsChannelService;

    @Resource
    private LogisticsAuthFieldService logisticsAuthFieldService;

    @Lazy
    @Resource
    private AsyncService  asyncService;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private DmpTaskFeign dmpTaskFeign;



    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsAuthDTO.AddDTO addDTO) {
        LogisticsSupplierEntity supplierEntity = logisticsSupplierService.getById(addDTO.getMainId());
        if (Objects.isNull(supplierEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流商");
        }
        LogisticsAuthEntity logisticsAuthEntity = new LogisticsAuthEntity();
        BeanMapperUtils.copy(addDTO, logisticsAuthEntity);
        // 数据处理
        handleData(logisticsAuthEntity);
        boolean save = super.save(logisticsAuthEntity);
        if (!save) {
            throw new ServiceException("物流授权单保存失败");
        }
        supplierEntity.setAuthTime(LocalDateTime.now());
        supplierEntity.setAuthStatus(LogisticsAuthStatusEnum.ALREADY.getCode());
        logisticsSupplierService.updateById(supplierEntity);
        //保存或者修改授权字段
        logisticsAuthFieldService.saveOrUpdateAuthField(logisticsAuthEntity.getId(), addDTO.getFieldMap());
        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "物流授权单", logisticsAuthEntity.getId());
        operateLogService.addModuleOperateLog(msg, null, logisticsAuthEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(logisticsAuthEntity.getId(), logisticsAuthEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.UpdateDTO update(LogisticsAuthDTO.UpdateDTO updateDTO) {
        LogisticsAuthEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "物流授权单"));
        LogisticsSupplierEntity supplierEntity = logisticsSupplierService.getById(updateDTO.getMainId());
        if (Objects.isNull(supplierEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流商");
        }

        LogisticsAuthEntity logisticsAuthEntity = BeanMapperUtils.map(LogisticsAuthEntity.class, updateDTO);
        // 数据处理
        handleData(logisticsAuthEntity);
        boolean save = super.updateById(logisticsAuthEntity);
        if (!save) {
            throw new ServiceException("物流授权单保存失败");
        }
        supplierEntity.setAuthTime(LocalDateTime.now());
        supplierEntity.setAuthStatus(LogisticsAuthStatusEnum.ALREADY.getCode());
        logisticsSupplierService.updateById(supplierEntity);
        //保存或者修改授权字段
        logisticsAuthFieldService.saveOrUpdateAuthField(logisticsAuthEntity.getId(), updateDTO.getFieldMap());
        return new BaseResultDTO.UpdateDTO(logisticsAuthEntity.getId(), logisticsAuthEntity.getId());

    }

    @Override
    public LogisticsAuthDTO.ViewDTO view(String mainId) {
        LogisticsAuthEntity authEntity = this.getByMainId("", mainId);
        if (Objects.isNull(authEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流授权");
        }
        LogisticsAuthDTO.ViewDTO view = new LogisticsAuthDTO.ViewDTO();
        BeanMapperUtils.copy(authEntity, view);
        List<LogisticsAuthFieldEntity> authFieldList = logisticsAuthFieldService.listByLogisticsAuthId(authEntity.getId());
        Map<String, String> map = new HashMap<>();
        for (LogisticsAuthFieldEntity item : authFieldList) {
            map.put(item.getFieldCode(), item.getFieldValue());
        }
        view.setFieldMap(map);
        return view;
    }

    @Override
    public LogisticsAuthEntity getByMainId(String id, String mainId) {
        return this.lambdaQuery().ne(StringUtils.isNotBlank(id), LogisticsAuthEntity::getId, id).eq(LogisticsAuthEntity::getMainId, mainId).last(SqlConstants.LIMIT_1).one();
    }

    @Override
    public LogisticsSupplierDTO.AuthDTO getAuthByChannelId(String channelId) {
        return baseMapper.getAuthByChannelId(channelId);
    }



    @Override
    public LogisticsSupplierDTO.AuthDTO getAuthBySupplierId(String logisticsSupplierId) {
        List<LogisticsSupplierDTO.AuthDTO> list = baseMapper.listAuthBySupplierId(Collections.singletonList(logisticsSupplierId));
        return list.stream().findFirst().orElse(null);
    }

    @Override
    public List<LogisticsSupplierDTO.AuthChannelViewDTO> listAuthChannelView(List<String> channelIdList) {
        if (CollectionUtil.isEmpty(channelIdList)) {
            return Collections.emptyList();
        }
        return baseMapper.listAuthChannelView(channelIdList);
    }

    @Override
    public ApiResult<Object>authLogistics(String logisticsPlatform, Map<String, String> authConfig) {
        LogisticsService service = logisticsRegistry.getHandler(logisticsPlatform);
        if (Objects.isNull(service)){
            return ApiResult.error(-1,"功能未开发");
        }
        return service.authorization(authConfig);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLogisticsAuthStatus(String mainId, String authStatus) {
        LogisticsSupplierEntity supplierEntity = logisticsSupplierService.getById(mainId);
        if (Objects.isNull(supplierEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流商");
        }
        supplierEntity.setAuthStatus(authStatus);
        logisticsSupplierService.updateById(supplierEntity);
    }

    @Override
    public List<LogisticsAuthEntity> listByMainIds(List<String> supplierIds) {
        if (CollectionUtils.isEmpty(supplierIds)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(LogisticsAuthEntity::getMainId, supplierIds).list();
    }

    @Override
    public Map<String, String> addShopAuth(Map<String, String> authMap, String logisticsPlatform) {
        //获取oms已授权店铺
        ApiResult<List<ShopAuthEntity>> result = null;
        try {
            if (LogisticsPlatformEnum.SHOPEE.getCode().equals(logisticsPlatform)){
                result = shopInfoFeign.getShopListByParam(AuthTypeEnum.SHOP.getCode(), AuthStatusEnum.ALREADY.getCode(),"");
            }else if(LogisticsPlatformEnum.TIK_TOK.getCode().equals(logisticsPlatform)){
                result = shopInfoFeign.getShopListByParam("", AuthStatusEnum.ALREADY.getCode(),LogisticsPlatformEnum.TIK_TOK.getCode());
            }else if(LogisticsPlatformEnum.TIK_TOK_FULLY.getCode().equals(logisticsPlatform)){
                result = shopInfoFeign.getShopListByParam("", AuthStatusEnum.ALREADY.getCode(),LogisticsPlatformEnum.TIK_TOK_FULLY.getCode());
            }else if(LogisticsPlatformEnum.MERCADOLIBRE.getCode().equals(logisticsPlatform)){
                result = shopInfoFeign.getShopListByParam("", AuthStatusEnum.ALREADY.getCode(),LogisticsPlatformEnum.MERCADOLIBRE.getCode());
            }else if(LogisticsPlatformEnum.MERCADOLIBRE_LOCAL.getCode().equals(logisticsPlatform)){
                result = shopInfoFeign.getShopListByParam("", AuthStatusEnum.ALREADY.getCode(),LogisticsPlatformEnum.MERCADOLIBRE_LOCAL.getCode());
            }else if(LogisticsPlatformEnum.AMZ_MULTI_CHANNEL.getCode().equals(logisticsPlatform)){
                result = shopInfoFeign.getShopListByParam("", AuthStatusEnum.ALREADY.getCode(),LogisticsPlatformEnum.AMAZON.getCode());
            }else{
                throw new ServiceException("不支持的平台，请联系IT处理");
            }
        } catch (Exception e) {
            log.error("erp-oms服务接口getShopeeShopList异常：{}", e.getMessage());
        }
        if (Objects.isNull(result) || CollUtil.isEmpty(result.getData()) || !result.isSuccess() ){
            throw new ServiceException("请先完成店铺授权后再执行物流授权");
        }
        ShopAuthEntity shopAuthEntity = null;
        //美客多校验账号店铺是否存在授权
        if(LogisticsPlatformEnum.MERCADOLIBRE.getCode().equals(logisticsPlatform) || LogisticsPlatformEnum.MERCADOLIBRE_LOCAL.getCode().equals(logisticsPlatform) || LogisticsPlatformEnum.AMZ_MULTI_CHANNEL.getCode().equals(logisticsPlatform)) {

            String shopAccount = authMap.get("shopAccount");
            if (CharSequenceUtil.isBlank(shopAccount)) {
                throw new ServiceException("请输入账号");
            }
            List<ShopAuthEntity> authEntityList = result.getData();
            if (CollUtil.isEmpty(authEntityList)) {
                throw new ServiceException("请先完成店铺授权后再执行物流授权");
            }
            List<String> shopIds = authEntityList.stream().map(ShopAuthEntity::getShopId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            List<ShopInfoEntity> shopInfoEntityList = FeignQuery.getByIds(ShopInfoEntity.class, shopIds);
            if (CollUtil.isEmpty(shopInfoEntityList)) {
                throw new ServiceException("店铺信息未找到");
            }
            ShopInfoEntity shopInfoEntity = shopInfoEntityList.stream().filter(e -> shopAccount.equals(e.getAccount()) && AuthStatusEnum.ALREADY.getCode().equals(e.getAuthStatus())).findFirst().orElse(null);
            if (Objects.isNull(shopInfoEntity)) {
                throw new ServiceException("请先完成店铺授权后再执行物流授权");
            }
            shopAuthEntity = authEntityList.stream().filter(e -> shopInfoEntity.getId().equals(e.getShopId())).findFirst().orElse(null);
        }
        //根据主店铺获取子店铺token
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum;
        if (LogisticsPlatformEnum.SHOPEE.getCode().equals(logisticsPlatform)){
            appClientEnum = AppClientEnum.SHOPEE_ACCESS_TOKEN;
        }else if(LogisticsPlatformEnum.TIK_TOK.getCode().equals(logisticsPlatform)){
            appClientEnum = AppClientEnum.TIKTOK_ACCESS_TOKEN;
        }else if(LogisticsPlatformEnum.TIK_TOK_FULLY.getCode().equals(logisticsPlatform)){
            appClientEnum = AppClientEnum.TIKTOK_FULLY_ACCESS_TOKEN;
        }else if(LogisticsPlatformEnum.MERCADOLIBRE.getCode().equals(logisticsPlatform)){
            appClientEnum = AppClientEnum.MERCADO_ACCESS_TOKEN;
        }else if(LogisticsPlatformEnum.MERCADOLIBRE_LOCAL.getCode().equals(logisticsPlatform)){
            appClientEnum = AppClientEnum.MERCADO_LOCAL_ACCESS_TOKEN;
        }else if(LogisticsPlatformEnum.AMZ_MULTI_CHANNEL.getCode().equals(logisticsPlatform)){
            appClientEnum = AppClientEnum.AMAZON_ACCESS_TOKEN;
        }else{
            throw new ServiceException("不支持的平台，请联系IT处理");
        }
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        CfgAppClientEntity cfgAppClient = null;
        try {
            cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        }catch (Exception e){
            log.error("erp-dmp服务获取配置信息异常：{}",e.getMessage());
        }
        if (Objects.isNull(cfgAppClient)) {
            throw new ServiceException("请先完成中台授权后再执行物流授权");
        }
        if (Objects.isNull(shopAuthEntity)){
            shopAuthEntity = result.getData().get(0);
        }
        if (LogisticsPlatformEnum.SHOPEE.getCode().equals(logisticsPlatform)){
            authMap.put("shopId",shopAuthEntity.getShopeeId());
            authMap.put("token",shopAuthEntity.getAccessToken());
            authMap.put("host",cfgAppClient.getUrl());
        }else if(LogisticsPlatformEnum.TIK_TOK.getCode().equals(logisticsPlatform) || LogisticsPlatformEnum.TIK_TOK_FULLY.getCode().equals(logisticsPlatform)){
            authMap.put("shopId",shopAuthEntity.getShopId());
        }else if(LogisticsPlatformEnum.MERCADOLIBRE.getCode().equals(logisticsPlatform) || LogisticsPlatformEnum.MERCADOLIBRE_LOCAL.getCode().equals(logisticsPlatform) || LogisticsPlatformEnum.AMZ_MULTI_CHANNEL.getCode().equals(logisticsPlatform)){
            authMap.put("shopId",shopAuthEntity.getShopId());
            authMap.put("token",shopAuthEntity.getAccessToken());
        }

        return authMap;
    }

    @Override
    public List<LogisticsSupplierDTO.AuthDTO> listAuthBySupplierId(List<String> logisticsSupplierIds) {
        return baseMapper.listAuthBySupplierId(logisticsSupplierIds);
    }

    @Override
    public List<String> listAllChannelByOverseas() {
        return this.baseMapper.listAllChannelByOverseas(OmsPlatformEnum.allPlatform());
    }

    public LogisticsAuthEntity getDbByMainId(String mainId){
        return this.lambdaQuery().eq(LogisticsAuthEntity::getMainId, mainId).last(SqlConstants.LIMIT_1).one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancel(String mainId) {
        LogisticsAuthEntity entity = this.getDbByMainId(mainId);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流授权");
        }
        LogisticsSupplierEntity supplierEntity = logisticsSupplierService.getById(mainId);
        if (Objects.isNull(supplierEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流商");
        }
        String authStatus = supplierEntity.getAuthStatus();
        if (!LogisticsAuthStatusEnum.ALREADY.getCode().equals(authStatus)) {
            throw new ServiceException(ApiError.ERROR_CANCEL_CONDITION);
        }
        this.removeById(entity.getId());
        supplierEntity.setAuthStatus(LogisticsAuthStatusEnum.NOT.getCode());
        logisticsSupplierService.updateById(supplierEntity);
        return BatchResultDTO.success(supplierEntity.getId(), supplierEntity.getSupplierName(), OperationTypeEnum.UPDATE_STATUS);

    }

    @Override
    public LogisticsAuthEntity getByChannelId(String channelId) {
        LogisticsChannelEntity channelEntity = logisticsChannelService.getById(channelId);
        if (Objects.nonNull(channelEntity)) {
            LogisticsAuthEntity logisticsAuthEntity = this.getByMainId(null,channelEntity.getMainId());
            if(Objects.nonNull(logisticsAuthEntity)){
                return logisticsAuthEntity;
            }
        }
        return new LogisticsAuthEntity();
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(LogisticsAuthEntity logisticsAuthEntity) {
        
        String mainId = logisticsAuthEntity.getMainId();
        LogisticsSupplierEntity logisticsSupplier = logisticsSupplierService.getById(mainId);
        LogisticsAuthEntity authEntity = this.getByMainId(logisticsAuthEntity.getId(), mainId);
        if (Objects.nonNull(authEntity)) {
            throw new ServiceException("物流商该平台授权信息已存在");
        }
        if (Objects.isNull(logisticsSupplier)) {
            throw new ServiceException("物流商不存在");
        }
        logisticsAuthEntity.setName(logisticsSupplier.getSupplierName());

    }

    /**
     * @param id
     * @param mainId
     * @param logisticsPlatform
     * @return
     */
    private LogisticsAuthEntity getByMainIdAndPlatform(String id, String mainId, String logisticsPlatform) {
        return this.lambdaQuery().ne(StringUtils.isNotBlank(id), LogisticsAuthEntity::getId, id).
                eq(LogisticsAuthEntity::getMainId, mainId).
                eq(LogisticsAuthEntity::getLogisticsPlatform, logisticsPlatform).
                last(SqlConstants.LIMIT_1).one();
    }

    @Override
    public Map<String, String> getLogisticsAuthConfig(String authId,String shopId,String logisticsPlatform) {
        Map<String, String> map = new HashMap<>();
        List<LogisticsAuthFieldEntity> fieldEntities = null;
        if (LogisticsPlatformEnum.ALI_EXPRESS.getCode().equals(logisticsPlatform) || LogisticsPlatformEnum.SHOPEE.getCode().equals(logisticsPlatform)
                || LogisticsPlatformEnum.TIK_TOK.getCode().equals(logisticsPlatform)  || LogisticsPlatformEnum.MERCADOLIBRE.getCode().equals(logisticsPlatform)
                || LogisticsPlatformEnum.MERCADOLIBRE_LOCAL.getCode().equals(logisticsPlatform)){
            LogisticsService service = logisticsRegistry.getHandler(logisticsPlatform);
            return service.getLogisticsAuthConfigByShopId(shopId);
        }else {
            if (StringUtils.isNoneBlank(authId)) {
                map.put("id", authId);
                LogisticsAuthEntity authEntity = this.getById(authId);
                if (Objects.isNull(authEntity)) return null;
                map.put("logisticsPlatform", authEntity.getLogisticsPlatform());
                fieldEntities = logisticsAuthFieldService.listByLogisticsAuthId(authId);
            }
            if (CollectionUtils.isNotEmpty(fieldEntities)) {
                fieldEntities.forEach(logisticsAuthFieldEntity -> {
                    map.put(logisticsAuthFieldEntity.getFieldCode(), logisticsAuthFieldEntity.getFieldValue());
                });
            }
        }
        return map;
    }


    @Override
    public void syncUpdateSaleChannel(String logisticsPlatform, Map<String, String> authConfig) {
        authConfig.put("logisticsPlatform", logisticsPlatform);
        asyncService.asyncUpdateSaleChannel(authConfig);
    }
}
