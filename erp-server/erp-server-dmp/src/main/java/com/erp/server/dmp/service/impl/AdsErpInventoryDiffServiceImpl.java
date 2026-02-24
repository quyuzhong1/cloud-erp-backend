package com.erp.server.dmp.service.impl;

import cn.hutool.core.util.StrUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseMappingEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.dmp.service.CfgSettingService;
import com.erp.server.dmp.service.ThirdMappingService;
import com.erp.server.dmp.utils.RestCloudApiUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.doris.AdsErpInventoryDiffEntity;
import com.erp.server.dmp.mapper.doris.AdsErpInventoryDiffMapper;
import com.erp.server.dmp.service.AdsErpInventoryDiffService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.AdsErpInventoryDiffDTO;
import javax.annotation.Resource;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 平台库存差异 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2025-11-13
 */
@Slf4j
@Service
public class AdsErpInventoryDiffServiceImpl extends SuperServiceImpl<AdsErpInventoryDiffMapper, AdsErpInventoryDiffEntity> implements AdsErpInventoryDiffService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private ThirdMappingService thirdMappingService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AdsErpInventoryDiffDTO.AddDTO addDTO) {
        AdsErpInventoryDiffEntity adsErpInventoryDiffEntity = new AdsErpInventoryDiffEntity();
        BeanMapperUtils.copy(addDTO, adsErpInventoryDiffEntity);

        // 数据处理
        handleData(adsErpInventoryDiffEntity);

        log.info("开始新增平台库存差异");
        boolean save = super.save(adsErpInventoryDiffEntity);
        if(!save) {
            throw new ServiceException("平台库存差异保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "平台库存差异" , adsErpInventoryDiffEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, adsErpInventoryDiffEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(adsErpInventoryDiffEntity.getId(), adsErpInventoryDiffEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AdsErpInventoryDiffDTO.UpdateDTO addOrUpdateDTO) {
        AdsErpInventoryDiffEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "平台库存差异"));
        AdsErpInventoryDiffEntity adsErpInventoryDiffEntity =  BeanMapperUtils.map(AdsErpInventoryDiffEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(adsErpInventoryDiffEntity);
        log.info("编辑 开始修改平台库存差异数据，id：【{}】", old.getId());
        boolean save = super.updateById(adsErpInventoryDiffEntity);
        if(!save) {
            throw new ServiceException("平台库存差异保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录平台库存差异日志数据，id：【{}】", adsErpInventoryDiffEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), adsErpInventoryDiffEntity.getId(), "平台库存差异");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, adsErpInventoryDiffEntity, null, adsErpInventoryDiffEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<AdsErpInventoryDiffDTO.ListDTO> paging(PagingDTO<AdsErpInventoryDiffDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<AdsErpInventoryDiffDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public AdsErpInventoryDiffDTO.StatisticsDTO statistics(PagingDTO<AdsErpInventoryDiffDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        List<AdsErpInventoryDiffDTO.StatisticsDTO> list = baseMapper.statistics(pagingParamDTO.getParams());
        if (CollectionUtils.isNotEmpty(list)){
            if (null != list.get(0)){
                return list.get(0);
            }
        }
        return AdsErpInventoryDiffDTO.StatisticsDTO.init();
    }

    @Override
    public Boolean exportList(AdsErpInventoryDiffDTO.ExportDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("平台库存差异Excel导出", FileTaskEventEnum.EXPORT_ADS_ERP_INVENTORY_DIFF.getCode(), param);
        return true;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(AdsErpInventoryDiffEntity adsErpInventoryDiffEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public AdsErpInventoryDiffDTO.ViewDTO view(String id) {
    AdsErpInventoryDiffEntity adsErpInventoryDiffEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到平台库存差异数据"));
    AdsErpInventoryDiffDTO.ViewDTO data = BeanMapperUtils.map(AdsErpInventoryDiffDTO.ViewDTO.class, adsErpInventoryDiffEntity);
    // 数据填充处理
    fillOne(data);
    // TODO 查询明细数据（如果有的话）
    return data;
    }

    private void fillOne(AdsErpInventoryDiffDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
          return;
        }
    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<AdsErpInventoryDiffDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for(AdsErpInventoryDiffDTO.ListDTO data : list) {
        // TODO 其他如需要显示名称的字段赋值
        }
   }

    @Override
    public BatchResultDTO updateRemark(String id, String remark) {
        AdsErpInventoryDiffEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("平台库存差异数据"));

        // 删除主单数据
        log.info("删除 开平台库存差异主单数据，id：【{}】", id);
        this.lambdaUpdate()
                .set(AdsErpInventoryDiffEntity::getRemark, remark)
                .eq(AdsErpInventoryDiffEntity::getId, id)
                .update();
        // 删除日志数据
        log.info("删除 开始平台库存差异日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】更新备注操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getId(), "平台库存差异");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ADS_ERP_INVENTORY_DIFF.getCode(), entity.getId(), "更新备注平台库存差异");
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.UPDATE);
    }

    @Override
    public List<AdsErpInventoryDiffDTO.WarehouseListDTO> getCanDiffWarehouseList() {
        Map<SettingEnum, String> cfgMap = cfgSettingService.getMap(SettingEnum.ADS_CFG);
        // 核对库存差异的仓库
        List<String> warehouseList = Arrays.asList("oms");
        String warehouseListStr = cfgMap.get(SettingEnum.ADS_ERP_INVENTORY_DIFF_WAREHOUSE_LIST);
        List<String> platformList = Arrays.asList("antu");
        // 核对库存差异的平台
        String platformListStr = cfgMap.get(SettingEnum.ADS_ERP_INVENTORY_DIFF_PLATFORM_LIST);
        if (StringUtils.isNotBlank(warehouseListStr)) {
            warehouseList = Arrays.stream(warehouseListStr.split(",")).collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(platformListStr)) {
            platformList = Arrays.stream(platformListStr.split(",")).collect(Collectors.toList());
        }
        Set<AdsErpInventoryDiffDTO.WarehouseListDTO> resultSet = new HashSet<>();
        // 仓储平台
        List<ThirdMappingEntity> mapppingWarehouseList = thirdMappingService.lambdaQuery()
                .in(ThirdMappingEntity::getThirdSysType, platformList)
                .list();
        List<String> finalWarehouseList = warehouseList;
        if (CollectionUtils.isNotEmpty(mapppingWarehouseList)){
            List<AdsErpInventoryDiffDTO.WarehouseListDTO> collect = mapppingWarehouseList.stream()
                    .map(e -> new AdsErpInventoryDiffDTO.WarehouseListDTO(e.getSysId(), e.getSysName(), finalWarehouseList.contains(e.getSysId())))
                    .collect(Collectors.toList());
            resultSet.addAll(collect);
        }

        List<String> finalPlatformList = platformList;
        List<String> omsPlatformList = Arrays.stream(PlatformDictEnum.values()).map(PlatformDictEnum::getCode).filter(e -> finalPlatformList.contains(e.toLowerCase())).collect(Collectors.toList());
        omsPlatformList.addAll(finalPlatformList);

        // 销售平台
        List<ShopInfoEntity> shopList = FeignQuery.create(ShopInfoEntity.class)
                .in(ShopInfoEntity::getDictPlatform, omsPlatformList)
                .list();
        if (CollectionUtils.isNotEmpty(shopList)){
            List<AdsErpInventoryDiffDTO.WarehouseListDTO> collect = shopList.stream()
                    .filter(e-> StringUtils.isNotBlank(e.getWarehouseId()))
                    .map(e -> new AdsErpInventoryDiffDTO.WarehouseListDTO(e.getWarehouseId(), e.getWarehouseName(), finalWarehouseList.contains(e.getWarehouseId())))
                    .collect(Collectors.toList());
            resultSet.addAll(collect);
        }

        // wms绑定仓库
        List<WarehouseMappingEntity> mappingList = FeignQuery.create(WarehouseMappingEntity.class)
                .in(WarehouseMappingEntity::getDictPlatform, omsPlatformList)
                .list();
        if (CollectionUtils.isNotEmpty(mappingList)){
            List<AdsErpInventoryDiffDTO.WarehouseListDTO> collect = mappingList.stream()
                    .filter(e-> StringUtils.isNotBlank(e.getWarehouseId()) && StringUtils.isNotBlank(e.getName()))
                    .map(e -> new AdsErpInventoryDiffDTO.WarehouseListDTO(e.getWarehouseId(), e.getName(), finalWarehouseList.contains(e.getWarehouseId())))
                    .distinct()
                    .collect(Collectors.toList());
            resultSet.addAll(collect);
        }
        return new ArrayList<>(resultSet);
    }

    @Override
    public Boolean generateDiff(AdsErpInventoryDiffDTO.GenerateDiffDTO dto) {
        String checkMonth = dto.getCheckMonth();
        checkMonth = checkMonth.replace("-", "年") + "月";
        if(!cn.hutool.core.date.DateUtil.format(cn.hutool.core.date.DateUtil.offsetMonth(new Date(), -1), "yyyy年MM月").equals(checkMonth)) {
            throw new ServiceException("只允许重新生成上月核对任务");
        }
        Integer count = lambdaQuery().eq(AdsErpInventoryDiffEntity::getCheckMonth, checkMonth)
                .eq(AdsErpInventoryDiffEntity::getExecStatus, "doing").count();
        if(count != null && count > 0) {
            throw new ServiceException(dto.getCheckMonth() + "核对任务正在执行中");
        }
        boolean reCreate = RestCloudApiUtil.reCreate(dto.getCheckMonth(), "ods_erp/ods_flow_inventory_diff_recreate");
        if(reCreate) {
            lambdaUpdate().eq(AdsErpInventoryDiffEntity::getCheckMonth, checkMonth)
                    .set(AdsErpInventoryDiffEntity::getExecStatus, "doing")
                    .set(AdsErpInventoryDiffEntity::getExecStatusName, "执行中")
                    .setSql(" finish_time = null ")
                    .update();
        }
        return true;
    }
}
