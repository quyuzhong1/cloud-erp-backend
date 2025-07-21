package com.erp.server.tms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.*;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.DictBasicDTO;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.DictBasicEnum;
import com.erp.model.tms.enums.LogisticsAuthStatusEnum;
import com.erp.model.tms.enums.LogisticsSupplierTypeEnum;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.wms.feign.WmsFbaOverseasFeign;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.convert.LogisticsServiceConverter;
import com.erp.server.tms.convert.LogisticsSupplierConverter;
import com.erp.server.tms.mapper.LogisticsSupplierMapper;
import com.erp.server.tms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_LOGISTICS_SUPPLIER;

/**
 * <p>
 * 物理商表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@Service
public class LogisticsSupplierServiceImpl extends SuperServiceImpl<LogisticsSupplierMapper, LogisticsSupplierEntity> implements LogisticsSupplierService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private DictBasicService dictBasicService;


    @Resource
    private LogisticsWarehouseService logisticsWarehouseService;

    @Resource
    private LogisticsChannelService logisticsChannelService;

    @Resource
    @Lazy
    private LogisticsSaleChannelService logisticsSaleChannelService;


    @Resource
    private WmsFbaOverseasFeign wmsFbaOverseasFeign;


    @Resource
    private LogisticsAuthService logisticsAuthService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    @Lazy
    private LogisticsBaseService logisticsBaseService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsSupplierDTO.AddDTO addDTO) {
        LogisticsSupplierEntity logisticsSupplierEntity = new LogisticsSupplierEntity();
        BeanMapperUtils.copy(addDTO, logisticsSupplierEntity);

        // 数据处理
        handleData(logisticsSupplierEntity);

        boolean save = super.save(logisticsSupplierEntity);
        if (!save) {
            throw new ServiceException("物流商保存失败");
        }
        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "物理商单", logisticsSupplierEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LOGISTICS_SUPPLIER.getCode(), logisticsSupplierEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(logisticsSupplierEntity.getId(), logisticsSupplierEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsSupplierDTO.UpdateDTO updateDTO) {
        LogisticsSupplierEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "物流商单"));
        LogisticsSupplierEntity logisticsSupplierEntity = BeanMapperUtils.map(LogisticsSupplierEntity.class, updateDTO);

        // 数据处理
        handleData(logisticsSupplierEntity);
        log.info("编辑 开始修改物流商单数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsSupplierEntity);
        if (!save) {
            throw new ServiceException("物流商单保存失败");
        }
        String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), logisticsSupplierEntity.getId(), "物理商单");
        operateLogService.addModuleOperateLogByObj(old, logisticsSupplierEntity, ModuleTypeEnum.LOGISTICS_SUPPLIER.getCode(), logisticsSupplierEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<LogisticsSupplierDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<LogisticsSupplierDTO.TabListDTO> list = baseMapper.tabList(dto.getPermissionSql());
        List<DictBasicDTO.ViewDTO> typeList = dictBasicService.getByKey(DictBasicEnum.LOGISTICS_SUPPLIER.getType());
        List<LogisticsSupplierDTO.TabListDTO> resultList = new ArrayList<>(typeList.size());
        for (DictBasicDTO.ViewDTO item : typeList) {
            LogisticsSupplierDTO.TabListDTO tab = new LogisticsSupplierDTO.TabListDTO();
            String type = item.getCode();
            tab.setTabFlag(type);
            tab.setTabName(item.getName());
            Integer count = list.stream().filter(l -> l.getTabFlag().equals(type)).
                    map(LogisticsSupplierDTO.TabListDTO::getCount).findFirst().orElse(0);
            tab.setCount(count);
            resultList.add(tab);

        }
        return resultList;
    }

    @Override
    public PagingVO<LogisticsSupplierDTO.PagingViewDTO> paging(PagingDTO<LogisticsSupplierDTO.PagingParamDTO> dto) {
        LogisticsSupplierDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<LogisticsSupplierDTO.PagingViewDTO> list = pageData.getRecords();
        fillPagingData(list,params);
        return new PagingVO<>(pageData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        LogisticsSupplierEntity entity = super.getById(id);
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "物流商"));
        //TODO 检查订单是否引用
        this.removeById(id);
        List<LogisticsWarehouseEntity> logisticsWarehouseList = logisticsWarehouseService.listByLogisticsSupplierId(id);
        List<String> mainIdList = new ArrayList<>(10);
        mainIdList.add(id);
        if (CollectionUtils.isNotEmpty(logisticsWarehouseList)) {
            List<String> LogisticsWarehouseIdList = logisticsWarehouseList.stream().map(LogisticsWarehouseEntity::getId).collect(Collectors.toList());
            logisticsWarehouseService.removeByIds(LogisticsWarehouseIdList);
        }
        //删除渠道根据来源id
        logisticsChannelService.removeByMainIdList(mainIdList);
        return BatchResultDTO.success(entity.getId(), entity.getSupplierName(), OperationTypeEnum.DELETE);

    }

    /**
     * 物流商物流渠道同步
     *
     * @return
     * @parms id
     * @author yl
     * @date 2023-11-15
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO sync(String id) {
        LogisticsSupplierEntity logisticsSupplier = this.getById(id);
        if (Objects.isNull(logisticsSupplier)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流商");
        }
        String authStatus = logisticsSupplier.getAuthStatus();
        String alreadyCode = LogisticsAuthStatusEnum.ALREADY.getCode();
        if (!alreadyCode.equals(authStatus)) {
            throw new ServiceException(ApiError.NOT_SYNC_BY_NOT_AUTH);
        }
        LogisticsAuthEntity authEntity = logisticsAuthService.getByMainId("", id);
        if (Objects.isNull(authEntity)) {
            throw new ServiceException(ApiError.NOT_SYNC_BY_NOT_AUTH);
        }
        String logisticsPlatform = authEntity.getLogisticsPlatform();
        //同步第三方渠道
        logisticsBaseService.syncSingleChannel(logisticsPlatform);
        List<LogisticsSaleChannelEntity> saleChannelList = logisticsSaleChannelService.listByLogisticsPlatform(logisticsPlatform,"tms");
        List<String> syncSourceIdList = saleChannelList.stream().map(LogisticsSaleChannelEntity::getId).collect(Collectors.toList());
        //这个是删除的同步来源ids
        List<String> deleteSyncSourceIdList = saleChannelList.stream().filter(BaseEntity::getIsDeleted).map(LogisticsSaleChannelEntity::getId).collect(Collectors.toList());
        List<LogisticsChannelEntity> channelList = logisticsChannelService.listBySyncSourceIds(syncSourceIdList, id);
        List<LogisticsChannelEntity> allChannels = logisticsChannelService.listByMainId(id);
        //这个是对应删除的渠道id集合
        List<String> deleteChannelIdList = channelList.stream().filter(c -> deleteSyncSourceIdList.contains(c.getSyncSourceId())).map(LogisticsChannelEntity::getId).collect(Collectors.toList());

        //这个是海外仓物流
        List<LogisticsSaleChannelEntity> warehouseLogisticsList = saleChannelList.stream().filter(s -> StringUtils.isNotBlank(s.getOverseasWarehouseId()) && !s.getIsDeleted()).collect(Collectors.toList());
        Integer totalSize = warehouseLogisticsList.size();
        if (CollectionUtils.isNotEmpty(warehouseLogisticsList)) {
            syncWarehouseLogistics(id, warehouseLogisticsList, channelList);
        }
        Boolean trueFlag = Boolean.TRUE;
        Integer zeroFlag = MathUtil.ZERO;

        //这个不是海外仓物流(海外仓数据为空，筛选出来物流渠道数据)
        List<LogisticsSaleChannelEntity> logisticsList = saleChannelList.stream().filter(s -> StringUtils.isBlank(s.getOverseasWarehouseId()) && !s.getIsDeleted()).collect(Collectors.toList());
        totalSize = totalSize + logisticsList.size();
        //根据原始渠道进行更新现有渠道数据
        if (CollectionUtils.isNotEmpty(logisticsList)) {
            List<LogisticsChannelEntity> saveOrUpdateList = new ArrayList<>(logisticsList.size());
            for (LogisticsSaleChannelEntity saleChannel : logisticsList) {
                String syncSourceId = saleChannel.getId();
                LogisticsChannelEntity channelEntity = channelList.stream().filter(c -> c.getSyncSourceId().equals(syncSourceId)).
                        findFirst().orElse(new LogisticsChannelEntity());
                channelEntity.setSyncSourceId(syncSourceId);
                channelEntity.setCode(saleChannel.getCode());
                channelEntity.setMainId(id);
                if (StringUtils.isBlank(channelEntity.getName())){
                    channelEntity.setName(saleChannel.getCnName());
                }
                channelEntity.setEffectiveTime(saleChannel.getAging());
                String channelId = channelEntity.getId();
                //表示新增
                if (StringUtils.isBlank(channelId)) {
                    channelEntity.setDisabled(trueFlag);
                } else {
                    //表示修改
                    if (!zeroFlag.equals(saleChannel.getChannelStatus())) {
                        channelEntity.setDisabled(trueFlag);
                    }
                }
                saveOrUpdateList.add(channelEntity);
            }
            logisticsChannelService.saveOrUpdateBatch(saveOrUpdateList);
        }
        if (CollectionUtils.isNotEmpty(deleteChannelIdList)) {
            logisticsChannelService.removeByIdList(deleteChannelIdList);
        }
        //对比现有已开启渠道校验是否需要禁用
        List<LogisticsChannelEntity> noSyncList = allChannels.stream().filter(e -> !syncSourceIdList.contains(e.getSyncSourceId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(noSyncList)){
            List<String> channelIds = noSyncList.stream().map(LogisticsChannelEntity::getId).distinct().collect(Collectors.toList());
            logisticsChannelService.updateStatusByIds(channelIds, Boolean.TRUE);
        }
        return BatchResultDTO.success(logisticsSupplier.getId(), logisticsSupplier.getSupplierName(), "同步成功" + totalSize + "个渠道");

    }

    /**
     * 同步海外仓物流商
     * logisticsSupplierId
     *
     * @param warehouseLogisticsList
     */
    public void syncWarehouseLogistics(String logisticsSupplierId, List<LogisticsSaleChannelEntity> warehouseLogisticsList, List<LogisticsChannelEntity> channelList) {
        List<LogisticsWarehouseEntity> logisticsWarehouseList = logisticsWarehouseService.listByLogisticsSupplierId(logisticsSupplierId);
        //海外仓库id
        List<String> overseasWarehouseIdList = warehouseLogisticsList.stream().
                map(LogisticsSaleChannelEntity::getOverseasWarehouseId).distinct().collect(Collectors.toList());
        List<OverseasProviderWarehouseEntity> overseasWarehouseList = wmsFbaOverseasFeign.listWarehouseByIds(overseasWarehouseIdList);
        String sourceType = SourceTypeEnum.LOGISTICS_WAREHOUSE.getCode();
        Map<String, List<LogisticsSaleChannelEntity>> map = warehouseLogisticsList.stream().collect(Collectors.groupingBy(LogisticsSaleChannelEntity::getOverseasWarehouseId));
        Boolean trueFlag = Boolean.TRUE;
        Integer zeroFlag = MathUtil.ZERO;
        for (Map.Entry<String, List<LogisticsSaleChannelEntity>> item : map.entrySet()) {
            String overseasWarehouseId = item.getKey();
            OverseasProviderWarehouseEntity overseasProviderWarehouse = overseasWarehouseList.stream().
                    filter(o -> o.getId().equals(overseasWarehouseId)).findFirst().orElse(null);
            LogisticsWarehouseEntity logisticsWarehouse = logisticsWarehouseList.stream().
                    filter(l -> l.getOverseasWarehouseId().equals(overseasWarehouseId)).findFirst().orElse(null);
            String id;
            Boolean nonNull = Objects.nonNull(logisticsWarehouse);
            if (nonNull) {
                id = logisticsWarehouse.getId();
            } else {
                logisticsWarehouse = new LogisticsWarehouseEntity();
                id = IdWorker.getIdStr();
                logisticsWarehouse.setOverseasWarehouseId(overseasWarehouseId);
                logisticsWarehouse.setMainId(logisticsSupplierId);
                logisticsWarehouse.setId(id);
                if (Objects.nonNull(overseasProviderWarehouse)) {
                    logisticsWarehouse.setOverseasWarehouseName(overseasProviderWarehouse.getPlatformWarehouseName());
                    logisticsWarehouse.setOverseasWarehouseCode(overseasProviderWarehouse.getPlatformWarehouseCode());
                }
                logisticsWarehouseService.save(logisticsWarehouse);
            }
            List<LogisticsSaleChannelEntity> saleChannelList = item.getValue();
            if (CollectionUtils.isNotEmpty(saleChannelList)) {
                List<LogisticsChannelEntity> saveOrUpdateList = new ArrayList<>(saleChannelList.size());
                for (LogisticsSaleChannelEntity saleChannel : saleChannelList) {
                    String syncSourceId = saleChannel.getId();
                    LogisticsChannelEntity channelEntity = channelList.stream().filter(c -> c.getSyncSourceId().equals(syncSourceId)).
                            findFirst().orElse(new LogisticsChannelEntity());
                    channelEntity.setSyncSourceId(syncSourceId);
                    channelEntity.setCode(saleChannel.getCode());
                    channelEntity.setMainId(logisticsSupplierId);
                    channelEntity.setSourceType(sourceType);
                    channelEntity.setSourceId(id);
                    if (CharSequenceUtil.isBlank(channelEntity.getName())){
                        channelEntity.setName(saleChannel.getCnName());
                    }
                    channelEntity.setEffectiveTime(saleChannel.getAging());
                    channelEntity.setCarrierType(saleChannel.getCarrierType());
                    String channelId = channelEntity.getId();
                    //表示新增
                    if (StringUtils.isBlank(channelId)) {
                        channelEntity.setDisabled(trueFlag);
                    } else {
                        //表示修改
                        if (!zeroFlag.equals(saleChannel.getChannelStatus())) {
                            channelEntity.setDisabled(trueFlag);
                        }
                    }
                    saveOrUpdateList.add(channelEntity);
                }
                logisticsChannelService.saveOrUpdateBatch(saveOrUpdateList);
            }


        }


    }

    @Override
    public Boolean export(LogisticsSupplierDTO.ExportDTO dto) {
        downloadTaskFeign.saveExportTask("物流商列表", EXPORT_TMS_LOGISTICS_SUPPLIER.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public List<BaseDropDownDTO.DisabledDTO> listAll(Boolean filterDisabled) {
        List<LogisticsSupplierEntity> list = this.list();
        List<BaseDropDownDTO.DisabledDTO> resultList = LogisticsSupplierConverter.INSTANCE.convertBySupplierDown(list);
        if(filterDisabled){
            resultList = resultList.stream().filter(v->!v.getDisabled()).collect(Collectors.toList());
        }
        return resultList;
    }
    @Override
    public List<BaseDropDownDTO.DisabledDTO> listAllShort(Boolean filterDisabled) {
        List<LogisticsSupplierEntity> list = this.list();
        List<BaseDropDownDTO.DisabledDTO> resultList = LogisticsServiceConverter.INSTANCE.convertBySupplierShortDown(list);
        if(filterDisabled){
            resultList = resultList.stream().filter(v->!v.getDisabled()).collect(Collectors.toList());
        }
        return resultList;
    }

    @Override
    public Boolean updateDisabledBySupplierId(LogisticsSupplierDTO.UpdateDisabledDTO dto) {
        return this.lambdaUpdate().eq(LogisticsSupplierEntity::getSupplierId, dto.getSupplierId()).
                set(LogisticsSupplierEntity::getDisabled, dto.getDisabled()).update();
    }

    @Override
    public List<LogisticsSupplierDTO.ListChildTreeDTO> tree() {
        List<LogisticsSupplierEntity> dbList = list();
        List<LogisticsSupplierDTO.ListChildTreeDTO> list = LogisticsSupplierConverter.INSTANCE.convertTree(dbList);
        List<LogisticsChannelEntity> allChannelList = logisticsChannelService.list();
        for (LogisticsSupplierDTO.ListChildTreeDTO item : list) {
            String id = item.getId();
            List<LogisticsChannelEntity> channelList = allChannelList.stream().
                    filter(c -> c.getMainId().equals(id)).sorted(Comparator.comparing(LogisticsChannelEntity::getDisabled)).
                    collect(Collectors.toList());
            List<LogisticsSupplierDTO.ListChildTreeDTO> childrenList = LogisticsChannelConverter.INSTANCE.convertTree(channelList);
            item.setChildren(childrenList);
        }
        return list;
    }

    @Override
    public List<LogisticsSupplierDTO.LogisticsSupplierListDTO> listLogisticsChannel(List<String> logisticsSupplierIdList) {
        List<LogisticsSupplierDTO.LogisticsSupplierListDTO> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(logisticsSupplierIdList)) {
            return resultList;
        }
        List<LogisticsSupplierDTO.LogisticsSupplierListDTO> list = baseMapper.listLogisticsChannel(logisticsSupplierIdList);

        for (String  logisticsSupplierId: logisticsSupplierIdList) {
            LogisticsSupplierDTO.LogisticsSupplierListDTO resultDTO = new LogisticsSupplierDTO.LogisticsSupplierListDTO();
            //相同物流商直接赋值
            LogisticsSupplierDTO.LogisticsSupplierListDTO logisticsSupplierListDTO = list.stream().filter(obj -> CharSequenceUtil.equals(obj.getLogisticsSupplierId(), logisticsSupplierId)).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(logisticsSupplierListDTO)) {
                BeanMapperUtils.copy(logisticsSupplierListDTO,resultDTO);
            }
            resultDTO.setLogisticsSupplierId(logisticsSupplierId);
            resultList.add(resultDTO);
        }
        return resultList;
    }

    @Override
    public LogisticsSupplierDTO.ViewDTO detail(String id) {
        return baseMapper.detail(id);
    }

    @Override
    public List<LogisticsSupplierEntity> listByName(List<String> supplierNameList) {
        if(CollectionUtils.isEmpty(supplierNameList)){
            return new ArrayList<>();
        }
        return this.lambdaQuery().in(LogisticsSupplierEntity::getSupplierName, supplierNameList).list();
    }

    @Override
    public List<LogisticsSupplierEntity> listByShortName(List<String> shortSupplierNameList) {
        if(CollectionUtils.isEmpty(shortSupplierNameList)){
            return new ArrayList<>();
        }
        return this.lambdaQuery().in(LogisticsSupplierEntity::getShortName, shortSupplierNameList).list();
    }

    @Override
    public PagingVO<LogisticsSupplierDTO.PagingViewDTO> exportLogisticsSupplier(PagingDTO<LogisticsSupplierDTO.ExportDTO> dto) {
        LogisticsSupplierDTO.ExportDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<LogisticsSupplierDTO.PagingViewDTO> page = baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()), params);
        fillPagingData(page.getRecords(), dto.getParams());
        return new PagingVO<>(page);
    }



    /**
     * 填充分页数据
     *
     * @param list
     */
    private void fillPagingData(List<LogisticsSupplierDTO.PagingViewDTO> list,LogisticsSupplierDTO.PagingParamDTO params) {
        if (CollectionUtils.isEmpty(list)){
            return;
        }
        List<String> supplierIds = list.stream().map(LogisticsSupplierDTO.PagingViewDTO::getId).distinct().collect(Collectors.toList());
        //物流仓库列表
        List<LogisticsWarehouseEntity> logisticsWarehouseList = logisticsWarehouseService.listByLogisticsSupplierIds(supplierIds);
        //渠道列表
        List<LogisticsChannelDTO.BaseDTO> allChannelList = logisticsChannelService.listBaseByMainIdList(supplierIds, params);
        List<LogisticsAuthEntity> authList = logisticsAuthService.listByMainIds(supplierIds);
        for (LogisticsSupplierDTO.PagingViewDTO item : list) {
            LogisticsSupplierTypeEnum type = item.getType();
            item.setTypeName(type.getName());
            Boolean disabled = item.getDisabled();
            String disabledName = Objects.nonNull(disabled) && !disabled ? "启用" : "禁用";
            item.setDisabledName(disabledName);
            String authStatus = item.getAuthStatus();
            String authStatusName = LogisticsAuthStatusEnum.getName(authStatus);
            item.setAuthStatusName(authStatusName);
            //获取服务商编号
            LogisticsAuthEntity authEntity = authList.stream().filter(e -> Objects.nonNull(e) && e.getMainId().equals(item.getId())).findFirst().orElse(null);
            if (Objects.nonNull(authEntity)) {
                String logisticsPlatform = authEntity.getLogisticsPlatform();
                item.setLogisticsPlatform(logisticsPlatform);
                String printDelivery = Objects.requireNonNull(LogisticsPlatformEnum.getByCode(logisticsPlatform)).getPrintDelivery();
                if ("N".equals(printDelivery)) {
                    item.setIsPrintPlatform(Boolean.FALSE);
                } else {
                    item.setIsPrintPlatform(Boolean.TRUE);
                }
            } else {
                item.setIsPrintPlatform(Boolean.TRUE);
            }
            //包装仓库/渠道信息
            List<LogisticsSupplierDTO.ChannelViewDTO> viewList = new ArrayList<>();
            List<LogisticsWarehouseEntity> warehouseEntityList = logisticsWarehouseList.stream()
                    .filter(e -> StringUtils.isNotBlank(e.getMainId()) && e.getMainId().equals(item.getId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(warehouseEntityList)){
                //封装仓库下的渠道
                List<String> hasWarehouseChannelId = new ArrayList<>();
                for(LogisticsWarehouseEntity logisticsWarehouseEntity: warehouseEntityList){
                    LogisticsSupplierDTO.ChannelViewDTO channelView = new LogisticsSupplierDTO.ChannelViewDTO();
                    channelView.setWarehouseId(logisticsWarehouseEntity.getOverseasWarehouseId());
                    channelView.setWarehouseName(logisticsWarehouseEntity.getOverseasWarehouseName());
                    List<LogisticsChannelDTO.BaseDTO> channelList = allChannelList.stream().filter(c -> StringUtils.isNotEmpty(c.getSourceId())
                                    && c.getSourceId().equals(logisticsWarehouseEntity.getId()) && c.getMainId().equals(item.getId()))
                            .collect(Collectors.toList());
                    hasWarehouseChannelId.addAll(channelList.stream().map(v->v.getId()).collect(Collectors.toList()));
                    channelView.setChannelList(channelList);
                    viewList.add(channelView);
                }
                //封装没有仓库的渠道
                List<LogisticsChannelDTO.BaseDTO> otherChannel = allChannelList.stream().filter(c -> StringUtils.isNotEmpty(c.getMainId())
                        && c.getMainId().equals(item.getId()) && !hasWarehouseChannelId.contains(c.getId()))
                        .sorted(Comparator.comparing(LogisticsChannelDTO.BaseDTO::getDisabled)).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(otherChannel)){
                    LogisticsSupplierDTO.ChannelViewDTO channelView = new LogisticsSupplierDTO.ChannelViewDTO();
                    channelView.setWarehouseId("");
                    channelView.setWarehouseName("");
                    channelView.setChannelList(otherChannel);
                    viewList.add(channelView);
                }
            }else {
                LogisticsSupplierDTO.ChannelViewDTO channelView = new LogisticsSupplierDTO.ChannelViewDTO();
                channelView.setWarehouseId("");
                channelView.setWarehouseName("");
                List<LogisticsChannelDTO.BaseDTO> channelList = allChannelList.stream().filter(c -> StringUtils.isNotEmpty(c.getMainId())
                                && c.getMainId().equals(item.getId()))
                        .sorted(Comparator.comparing(LogisticsChannelDTO.BaseDTO::getDisabled)).collect(Collectors.toList());
                channelView.setChannelList(channelList);
                viewList.add(channelView);
            }
            item.setChannelViewDTOList(viewList);
        }
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(LogisticsSupplierEntity logisticsSupplierEntity) {
        String logisticsSupplier = "物流供应商";
        String supplierId = logisticsSupplierEntity.getSupplierId();
        SupplierEntity supplier = scmTaskFeign.getSupplierById(supplierId);
        if (Objects.isNull(supplier)) {
            throw new ServiceException("供应商不存在");
        }
        //供应商分类名
        String supplierCategoryName = supplier.getCategoryName();
        if (!logisticsSupplier.equals(supplierCategoryName)) {
            throw new ServiceException("供应商分类不为物流供应商");
        }
        logisticsSupplierEntity.setSupplierName(supplier.getName());

    }
}
