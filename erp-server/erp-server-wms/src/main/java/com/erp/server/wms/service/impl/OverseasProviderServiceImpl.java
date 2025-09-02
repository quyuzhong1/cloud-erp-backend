package com.erp.server.wms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.dto.DmpInoutDTO;
import com.erp.model.dmp.dto.PlatformTaskDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.ShippingCalculationDTO;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import com.erp.model.wms.dto.third.ThirdWarehouseCalculateFeeReq;
import com.erp.model.wms.dto.third.ThirdWarehouseCalculateFeeResponse;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.model.wms.enums.SptWarehouseStatusEnum;
import com.erp.model.wms.enums.SptWarehouseTypeEnum;
import com.erp.model.wms.entity.OverseasTransferWarehouseEntity;
import com.erp.model.wms.enums.SptWarehouseStatusEnum;
import com.erp.model.wms.enums.SptWarehouseTypeEnum;
import com.erp.rpc.dmp.feign.DmpInoutTaskFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.wms.convert.ThirdWarehouseConverter;
import com.erp.server.wms.handler.ThirdWarehouseRegistry;
import com.erp.server.wms.mapper.OverseasProviderMapper;
import com.erp.server.wms.service.*;
import io.seata.common.util.StringUtils;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * <p>
 * 海外物流商 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@Service
public class OverseasProviderServiceImpl extends SuperServiceImpl<OverseasProviderMapper, OverseasProviderEntity> implements OverseasProviderService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private ThirdWarehouseRegistry thirdWarehouseRegistry;

    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;

    @Resource
    private OverseasTransferWarehouseService overseasTransferWarehouseService;

    @Resource
    private DmpInoutTaskFeign dmpInoutTaskFeign;

    @Resource
    @Qualifier("thirdWarehouseExecutorPool")
    private ExecutorService thirdWarehouseExecutorPool;
    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(OverseasProviderDTO.UpdateDTO updateDTO) {
        OverseasProviderEntity old = super.getById(updateDTO.getId());
        //修改明细数据
        overseasProviderWarehouseService.update(updateDTO, old.getId());
        // 记录主单操作日志
        log.info("编辑 开始记录海外物流商日志数据，单号：【{}】", old.getCode());
        String msg = CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), old.getCode(), "海外物流商");
        operateLogService.addModuleOperateLogByObj(old, old, ModuleTypeEnum.OVERSEAS_PROVIDER.getCode(), old.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<OverseasProviderDTO.ListDTO> paging(PagingDTO<OverseasProviderDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<OverseasProviderDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());

        return new PagingVO(pageData);
    }

    private void fillList(List<OverseasProviderDTO.ListDTO> list) {
        for (OverseasProviderDTO.ListDTO listDTO : list) {
            listDTO.setAuthStatusName(AuthStatusEnum.getName(listDTO.getAuthStatus()));
            Map<String,Object> authMap = listDTO.getAuthJson();
            if (Objects.nonNull(authMap) && authMap.containsKey("refreshExpireIn")){
                listDTO.setAuthExpireTime(LocalDateTime.parse(
                        (String)authMap.get("refreshExpireIn"),
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME
                ));
            }
        }
    }

    @Override
    public OverseasProviderDTO.ViewDTO view(String id) {
        OverseasProviderEntity entity = this.getById(id);
        OverseasProviderDTO.ViewDTO viewDTO = new OverseasProviderDTO.ViewDTO();
        BeanMapperUtils.copy(entity, viewDTO);
        viewDTO.setAuthStatusName(AuthStatusEnum.getName(viewDTO.getAuthStatus()));
        List<OverseasProviderWarehouseEntity> overseasProviderWarehouseEntities = overseasProviderWarehouseService.listByMainIds(Collections.singletonList(id));
        List<OverseasProviderWarehouseDTO.ViewDTO> warehouseList = BeanMapper.copyList(overseasProviderWarehouseEntities, OverseasProviderWarehouseDTO.ViewDTO.class);
        if(CollUtil.isNotEmpty(warehouseList)){
            warehouseList.forEach(v->{
                v.setPlatformWarehouseTypeName(SptWarehouseTypeEnum.STANDARD.getName());
                v.setPlatformWarehouseStatusName(SptWarehouseStatusEnum.getName(v.getPlatformWarehouseStatus()));
            });
        }
        viewDTO.setDetailList(warehouseList);
        return viewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean authorize(OverseasProviderDTO.AuthorizeParamDTO dto) {
        List<OverseasProviderEntity> overseasProviderEntityList = this.list();
        if(overseasProviderEntityList.stream().filter(v->v.getAuthStatus().equals(AuthStatusEnum.ALREADY.getCode())).anyMatch(v->v.getAuthJson().equals(dto.getAuthJson()))){
            throw new ServiceException("相同授权信息已授权，无法重复授权");
        }
        ThirdWarehouseService thirdWarehouseService = thirdWarehouseRegistry.getHandler(getPlatFormCodeById(dto.getId()));
        boolean result = thirdWarehouseService.authorize(dto);
        if(result){
            OverseasProviderEntity entity = this.getById(dto.getId());
            entity.setId(dto.getId());
            entity.setAuthTime(LocalDateTime.now());
            entity.setAuthStatus(AuthStatusEnum.ALREADY.getCode());
            entity.setAuthJson(dto.getAuthJson());
            entity.setEnableDate(dto.getEnabledDate());
            this.updateById(entity);
            dmpTaskFeign.createThirdWarehouseTask(entity);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean cancelAuthorize(String id) {
        //清空授权信息
        OverseasProviderEntity entity = this.getById(id);
        entity.setAuthTime(null);
        entity.setAuthStatus(AuthStatusEnum.CANCEL.getCode());
        this.updateById(entity);
        log.error("用户【{}】取消海外仓【{}】的授权",UserContext.getDefaultLoginUser().getUserName(),entity.getShortName());
        //删除数据同步任务
        String platformCode = this.getPlatFormCodeById(id);
        dmpTaskFeign.removePlatformTask(new PlatformTaskDTO.AddDTO(id,null, platformCode));
        dmpTaskFeign.removeThirdWarehouseTask(entity);
        return true;
    }

    public String getPlatFormCodeById(String id){
        OverseasProviderEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到物流商信息"));
        return entity.getCode();
    }

    @Override
    public List<OverseasProviderDTO.WarehouseDTO> listProviderWarehouseByIds(List<String> warehouseIds) {
        if (CollectionUtils.isEmpty(warehouseIds)) {
            return Collections.emptyList();
        }
        List<OverseasProviderWarehouseEntity> overseasProviderWarehouseEntities = overseasProviderWarehouseService.listByWarehouseIds(warehouseIds);
        List<OverseasProviderDTO.WarehouseDTO> warehouseDTOS = BeanMapper.copyList(overseasProviderWarehouseEntities, OverseasProviderDTO.WarehouseDTO.class);
        return warehouseDTOS;
    }

    @Override
    public Map<String, List<OverseasProviderDTO.ListWithWarehouseDTO>> mapByWarehouseIds() {
        List<OverseasProviderDTO.ListWithWarehouseDTO> list = baseMapper.selectListWithWarehouse(true);
        if (CollectionUtils.isEmpty(list)){
            return Collections.emptyMap();
        }
        return list.stream().collect(Collectors.groupingBy(OverseasProviderDTO.ListWithWarehouseDTO::getWarehouseId));
    }

    @Override
    public List<OverseasProviderDTO.ListWithWarehouseDTO> listAllMatch() {
        return baseMapper.selectListWithWarehouse(true);
    }

    @Override
    public OverseasProviderEntity getByPlatformCode(String code) {
        return lambdaQuery().eq(OverseasProviderEntity::getCode,code).last("LIMIT 1").one();
    }

    @Override
    public OverseasProviderDTO.FeignDTO getOverseasWarehouse(OverseasProviderDTO.FeignDTO feignDTO) {
        return baseMapper.getOverseasWarehouse(feignDTO);
    }

    @Override
    public void add(OverseasProviderDTO.AddDTO dto) {
        OmsPlatformEnum omsPlatformEnum = OmsPlatformEnum.getByCode(dto.getCode());
        if(Objects.isNull(omsPlatformEnum)){
            throw new ServiceException("不支持的服务商平台");
        }
        dto.setName(omsPlatformEnum.getName());
        OverseasProviderEntity existShortName = this.lambdaQuery().eq(OverseasProviderEntity::getShortName,dto.getShortName()).one();
        if(Objects.nonNull(existShortName)){
            throw new ServiceException("已存在相同仓库简称");
        }
        OverseasProviderEntity existAccount = this.lambdaQuery().eq(OverseasProviderEntity::getPlatformAccount,dto.getPlatformAccount()).eq(OverseasProviderEntity::getCode,dto.getCode()).one();
        if(Objects.nonNull(existAccount)){
            throw new ServiceException("已存在相同账号");
        }
        OverseasProviderEntity add = BeanUtil.copyProperties(dto,OverseasProviderEntity.class);
        this.save(add);
        String msg = CharSequenceUtil.format("用户【{}】新增三方仓信息 ", UserContext.getDefaultLoginUser().getUserName());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.OVERSEAS_PROVIDER.getCode(), add.getId(), "新增");
    }

    @Override
    public OverseasProviderDTO.AuthorizeViewDTO authorizeView(BaseIdDTO dto) {
        OverseasProviderEntity entity = this.getById(dto.getId());
        OverseasProviderDTO.AuthorizeViewDTO authorizeViewDTO = BeanUtil.copyProperties(entity,OverseasProviderDTO.AuthorizeViewDTO.class);
        Map<String, Object> authJson = entity.getAuthJson();
        authorizeViewDTO.setAppKey(authJson.get("appKey").toString());
        authorizeViewDTO.setAppToken(authJson.get("appToken").toString());
        authorizeViewDTO.setEmail(authJson.getOrDefault("email","").toString());
        authorizeViewDTO.setDomain(authJson.getOrDefault("domain","").toString());
        authorizeViewDTO.setToken(authJson.getOrDefault("token","").toString());
        authorizeViewDTO.setShopAccount(authJson.getOrDefault("shopAccount","").toString());
        return authorizeViewDTO;
    }

    @Override
    public void updateThirdWarehouse(OverseasProviderDTO.UpdateThirdWarehouseDTO dto) {
        OverseasProviderEntity entity = this.getById(dto.getId());
        if(Objects.isNull(entity)){
            throw new ServiceException("海外仓为空");
        }
        BeanUtil.copyProperties(dto,entity);
        OverseasProviderEntity existShortName = this.lambdaQuery().eq(OverseasProviderEntity::getShortName,dto.getShortName()).ne(OverseasProviderEntity::getId,entity.getId()).one();
        if(Objects.nonNull(existShortName)){
            throw new ServiceException("已存在相同仓库简称");
        }
        OverseasProviderEntity existAccount = this.lambdaQuery().eq(OverseasProviderEntity::getPlatformAccount,dto.getPlatformAccount()).eq(OverseasProviderEntity::getCode,entity.getCode()).ne(OverseasProviderEntity::getId,entity.getId()).one();
        if(Objects.nonNull(existAccount)){
            throw new ServiceException("已存在相同账号");
        }
        boolean result = this.updateById(entity);
        if(result){
            String msg = CharSequenceUtil.format("用户【{}】编辑平台账号修改为【{}】,仓库简称修改为【{}】 ", UserContext.getDefaultLoginUser().getUserName(), entity.getPlatformAccount(),entity.getShortName());
            operateLogService.addModuleOperateLog(msg,  ModuleTypeEnum.OVERSEAS_PROVIDER.getCode(), entity.getId(), "编辑操作");
        }
    }

    @Override
    public List<BatchResultDTO> delete(String id) {
        OverseasProviderEntity entity = this.getById(id);
        if(Objects.isNull(entity)){
            throw new ServiceException("海外仓为空");
        }
        if(entity.getAuthStatus().equals(AuthStatusEnum.ALREADY.getCode())){
            throw new ServiceException("已授权不能删除");
        }
        this.removeById(id);
        String msg = CharSequenceUtil.format("用户【{}】删除海外仓信息 ", UserContext.getDefaultLoginUser().getUserName());
        operateLogService.addModuleOperateLog(msg,  ModuleTypeEnum.OVERSEAS_PROVIDER.getCode(), entity.getId(), "删除");
        return Collections.singletonList(BatchResultDTO.success(entity.getId(), entity.getName(),"删除成功"));
    }

    @Override
    public List<String> getShortName(String platformCode) {
        return this.lambdaQuery().eq(OverseasProviderEntity::getCode,platformCode).eq(OverseasProviderEntity::getAuthStatus,AuthStatusEnum.ALREADY.getCode()).list().stream().map(OverseasProviderEntity::getShortName).collect(Collectors.toList());
    }

    @Override
    public OverseasProviderEntity getByWarehouseId(String warehouseId) {
        OverseasProviderWarehouseEntity overseasProviderWarehouseEntity = overseasProviderWarehouseService.getByWarehouseId(warehouseId);
        if(Objects.isNull(overseasProviderWarehouseEntity)){
            return null;
        }
        return this.getById(overseasProviderWarehouseEntity.getMainId());
    }

    @Override
    public PagingVO<SkuMappingDTO.SyncWarehouseProductView> pageWarehouseProduct(PagingDTO<AdvanceQueryContainer> advanceQueryDTO) {
        Page query = new Page(advanceQueryDTO.getCurrPage(), advanceQueryDTO.getPageSize());
        IPage<SkuMappingDTO.SyncWarehouseProductView> pageData = baseMapper.pageWarehouseProduct(query, advanceQueryDTO.getParams());
        List<SkuMappingDTO.SyncWarehouseProductView> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        List<DmpInoutDTO.CommonDTO> commonDTOList = new ArrayList<>();
        list.forEach(v->{
            DmpInoutDTO.CommonDTO commonDTO = new DmpInoutDTO.CommonDTO();
            commonDTO.setSystemCode(v.getWarehouseProvideCode());
            commonDTO.setBillType(BusinessTypeEnum.PRODUCT.getCode());
            commonDTO.setNextLevelId(v.getAuthId());
            commonDTOList.add(commonDTO);
        });

        List<DmpInoutDTO.LastOneDTO> lastOneDTOS = dmpInoutTaskFeign.newInputTaskList(commonDTOList);
        list.forEach(v->{
            v.setWarehouseProvideName(PlatformDictEnum.getNameByCode(v.getWarehouseProvideCode()));
            v.setAuthStatusName(AuthStatusEnum.getName(v.getAuthStatus()));
            DmpInoutDTO.LastOneDTO lastOneDTO = lastOneDTOS.stream().filter(o->o.getNextLevelId().equals(v.getAuthId())).findFirst().orElse(new DmpInoutDTO.LastOneDTO());
            v.setSyncResult(lastOneDTO.getStatusName());
            v.setLastSyncTime(lastOneDTO.getLatestUpdateTime());
        });
        return new PagingVO<>(pageData);
    }

    @Override
    public List<ShippingCalculationDTO.ListDTO> getCalculateFeeBatch(ShippingCalculationDTO.PagingParamDTO params) {
        if (CharSequenceUtil.isBlank(params.getFromWarehouseId())){
            return Collections.emptyList();
        }
        OverseasProviderWarehouseEntity providerWarehouseEntity = overseasProviderWarehouseService.getByWarehouseId(params.getFromWarehouseId());
        if (Objects.isNull(providerWarehouseEntity)){
            return Collections.emptyList();
        }
        String platform = getPlatFormCodeById(providerWarehouseEntity.getMainId());
        List<ThirdWarehouseCalculateFeeReq> list = getCalculateFeeReq(platform, providerWarehouseEntity, params);
        if (CollUtil.isEmpty(list)){
            return Collections.emptyList();
        }
        List<ShippingCalculationDTO.ListDTO> listDTOList = new ArrayList<>();
        ThirdWarehouseService service = thirdWarehouseRegistry.getHandler(platform);
        List<Future<List<ShippingCalculationDTO.ListDTO>>> futureList = new ArrayList<>();
        for (ThirdWarehouseCalculateFeeReq calculateFeeReq : list){
            Future<List<ShippingCalculationDTO.ListDTO>> future = thirdWarehouseExecutorPool.submit(() -> {
                ApiResult<List<ThirdWarehouseCalculateFeeResponse>> calculateFeeBatch = service.getCalculateFeeBatch(calculateFeeReq, providerWarehouseEntity.getMainId());
                if (calculateFeeBatch.isSuccess()){
                    String countryCode = calculateFeeReq.getCountryCode();
                    List<ThirdWarehouseCalculateFeeResponse> responseList = calculateFeeBatch.getData();
                    List<ShippingCalculationDTO.ListDTO> dtoList = ThirdWarehouseConverter.INSTANCE.responseToShippingDTO(responseList);
                    if (CollUtil.isNotEmpty(dtoList)){
                        dtoList.forEach(e -> e.setToCountry(countryCode));
                    }
                    return dtoList;
                }else {
                    return Collections.emptyList();
                }
            });
            futureList.add(future);
        }
        for (Future<List<ShippingCalculationDTO.ListDTO>> future : futureList){
            try {
                List<ShippingCalculationDTO.ListDTO> dtoList = future.get();
                if (CollUtil.isNotEmpty(dtoList)){
                    listDTOList.addAll(dtoList);
                }
            } catch (InterruptedException e) {
                // 恢复线程的中断状态，确保中断标志不会被忽略
                Thread.currentThread().interrupt();
                log.error("线程被中断", e);
                throw new ServiceException("线程被中断", e);
            } catch (ExecutionException e) {
                log.error("线程任务执行异常", e);
                throw new ServiceException("线程任务执行异常", e.getCause());
            } catch (ThreadDeath td) {
                log.error("捕获到 ThreadDeath，线程终止", td);
                throw td; // 重新抛出以允许线程正常终止
            }
        }
        return listDTOList;
    }

    @Override
    public void productPushSettings(OverseasProviderDTO.ProductPushSettingDTO dto) {
        OverseasProviderEntity overseasProviderEntity = this.getById(dto.getId());
        if(dto.getIsProductSync() && StringUtils.isBlank(dto.getOwnerCode())){
            throw new ServiceException("API推送开启时，货主编码不能为空");
        }
        overseasProviderEntity.setIsProductSync(dto.getIsProductSync());
        overseasProviderEntity.setOwnerCode(dto.getOwnerCode());
        this.updateById(overseasProviderEntity);
    }

    @Override
    public List<OverseasProviderDTO.ListDTO> listAuthorizedThirdWarehouse() {
        List<OverseasProviderEntity> entities = this.lambdaQuery()
                .eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .list();
        if (CollUtil.isEmpty(entities)) {
            return new ArrayList<>();
        }
        return BeanUtil.copyToList(entities,OverseasProviderDTO.ListDTO.class);
    }

    @Override
    public OverseasProviderEntity getByPlatformCodeAndShortName(String sysType, String thirdShortName) {
        if(StringUtils.isBlank(sysType) || StringUtils.isBlank(thirdShortName)){
            return null;
        }
        return lambdaQuery()
                .eq(OverseasProviderEntity::getCode, sysType)
                .eq(OverseasProviderEntity::getShortName, thirdShortName)
                .last("limit 1")
                .one();
    }

    private List<ThirdWarehouseCalculateFeeReq> getCalculateFeeReq(String platform, OverseasProviderWarehouseEntity providerWarehouseEntity, ShippingCalculationDTO.PagingParamDTO params) {
        if (PlatformDictEnum.GOOD_CANG.getCode().equals(platform) || PlatformDictEnum.DA_MAI.getCode().equals(platform)){
            //邮政编码不能为空
            if (CharSequenceUtil.isBlank(params.getPostCode())){
                return Collections.emptyList();
            }
            if (CollUtil.isEmpty(params.getToCountryList())){
                return Collections.emptyList();
            }
            return getGucangCalculateFeeReq(providerWarehouseEntity, params);
        }else if (PlatformDictEnum.ANTU.getCode().equals(platform) || PlatformDictEnum.SPT.getCode().equals(platform)){
            if (CollUtil.isEmpty(params.getToCountryList())){
                return Collections.emptyList();
            }
            if (Objects.isNull(params.getWeight())){
                return Collections.emptyList();
            }
            if (CollUtil.isEmpty(params.getChannelCodeList())){
                return Collections.emptyList();
            }
            return getAntuCalculateFeeReq(providerWarehouseEntity, params);
        }
        return Collections.emptyList();
    }

    private List<ThirdWarehouseCalculateFeeReq> getAntuCalculateFeeReq(OverseasProviderWarehouseEntity providerWarehouseEntity, ShippingCalculationDTO.PagingParamDTO params) {
        List<ThirdWarehouseCalculateFeeReq> list = new ArrayList<>();
        List<String> toCountryList = params.getToCountryList().stream().filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<String> channelCodeList = params.getChannelCodeList();
        BigDecimal weight = params.getWeight();
        if ("g".equals(params.getWeightUnit())){
            weight = MathUtil.divide(params.getWeight(), BigDecimal.valueOf(1000));
        }
        for (String country : toCountryList){
            list.add(ThirdWarehouseCalculateFeeReq.builder()
                    .warehouseCode(providerWarehouseEntity.getPlatformWarehouseCode())
                    .countryCode(country)
                    .shippingMethod(channelCodeList)
                    .postCode(params.getPostCode())
                    .weight(weight)
                    .length(params.getLength())
                    .width(params.getWidth())
                    .height(params.getHeight())
                    .province(params.getProvince())
                    .city(params.getCity()).build());
        }
        return list;
    }

    private List<ThirdWarehouseCalculateFeeReq> getGucangCalculateFeeReq(OverseasProviderWarehouseEntity providerWarehouseEntity, ShippingCalculationDTO.PagingParamDTO params) {
        List<ThirdWarehouseCalculateFeeReq> list = new ArrayList<>();
        List<String> toCountryList = params.getToCountryList().stream().filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<String> channelCodeList = params.getChannelCodeList();
        String channelCode = CollUtil.isNotEmpty(channelCodeList) && 1 == channelCodeList.size() ? channelCodeList.get(0) : null;
        BigDecimal weight = params.getWeight();
        if ("g".equals(params.getWeightUnit())){
            weight = MathUtil.divide(params.getWeight(), BigDecimal.valueOf(1000));
        }
        for (String country : toCountryList){
            list.add(ThirdWarehouseCalculateFeeReq.builder()
                            .warehouseCode(providerWarehouseEntity.getPlatformWarehouseCode())
                            .countryCode(country)
                            .channelCode(channelCode)
                            .postCode(params.getPostCode())
                            .weight(weight)
                            .length(params.getLength())
                            .width(params.getWidth())
                            .height(params.getHeight())
                            .province(params.getProvince())
                            .city(params.getCity()).build());
        }
        return list;
    }

    @Override
    public OverseasProviderEntity getAlreadyAuthById(String id) {
        return lambdaQuery().eq(BaseEntity::getId,id).eq(OverseasProviderEntity::getAuthStatus,AuthStatusEnum.ALREADY.getCode()).one();
    }
}
