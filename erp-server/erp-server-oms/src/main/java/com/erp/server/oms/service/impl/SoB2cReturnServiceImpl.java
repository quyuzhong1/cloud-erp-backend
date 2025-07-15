package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.*;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.SoB2cReturnReasonEnum;
import com.erp.model.oms.enums.SoB2cReturnSourceTypeEnum;
import com.erp.model.oms.enums.SoB2cReturnStatusEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.ReturnTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.SoReturnInstockDetailDTO;
import com.erp.model.wms.enums.ReturnReasonEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.*;
import com.erp.server.oms.convert.B2cReturnConverter;
import com.erp.server.oms.mapper.SoB2cReturnMapper;
import com.erp.server.oms.service.*;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_SO_B2C_RETURN;

/**
 * <p>
 * b2c退货订单 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-10-09
 */
@Slf4j
@Service
public class SoB2cReturnServiceImpl extends SuperServiceImpl<SoB2cReturnMapper, SoB2cReturnEntity> implements SoB2cReturnService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private ShopInfoService shopInfoService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private SoOutstockFeign soOutstockFeign;

    @Resource
    private SoReturnInstockFeign soReturnInstockFeign;

    @Resource
    private SoReturnNoticeFeign soReturnNoticeFeign;
    @Resource
    private SoB2cReturnDetailService soB2cReturnDetailService;
    @Lazy
    @Resource
    private SoB2cService soB2cService;
    @Resource
    private InventoryFeign inventoryFeign;
    @Resource
    private SoReturnReceiveFeign soReturnReceiveFeign;
    @Resource
    private WmsTaskFeign wmsTaskFeign;
    @Resource
    private SysUserFeign sysUserFeign;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SoB2cReturnDTO.AddDTO addDTO) {
        SoB2cReturnEntity soB2cReturnEntity = new SoB2cReturnEntity();
        BeanMapperUtils.copy(addDTO, soB2cReturnEntity);

        // 数据处理
        handleData(soB2cReturnEntity);

        log.info("开始新增b2c退货订单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.THD);
        soB2cReturnEntity.setCode(code);
        boolean save = super.save(soB2cReturnEntity);
        if(!save) {
            throw new ServiceException("b2c退货订单保存失败");
        }

        // 操作日志
        String msg =  CharSequenceUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "b2c退货订单" , soB2cReturnEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C_RETURN.getCode(), soB2cReturnEntity.getId(), "新增操作");
        soB2cReturnDetailService.add(addDTO.getDetailList(), soB2cReturnEntity.getId());

        return new BaseResultDTO.AddDTO(soB2cReturnEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SoB2cReturnDTO.UpdateDTO updateDTO) {
        SoB2cReturnEntity old = super.getById(updateDTO.getId());
        if(null == old){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "b2c退货订单");
        }
        SoB2cReturnEntity soB2cReturnEntity =  BeanMapperUtils.map(SoB2cReturnEntity.class, updateDTO);

        // 数据处理
        handleData(soB2cReturnEntity);
        log.info("编辑 开始修改b2c退货订单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(soB2cReturnEntity);
        if(!save) {
            throw new ServiceException("b2c退货订单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录b2c退货订单日志数据，单号：【{}】", soB2cReturnEntity.getCode());
            String msg =  CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), soB2cReturnEntity.getCode(), "b2c退货订单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, soB2cReturnEntity, null, soB2cReturnEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<SoB2cReturnDTO.PagingViewDTO> paging(PagingDTO<SoB2cReturnDTO.PagingParamDTO> dto) {
        SoB2cReturnDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<SoB2cReturnDTO.PagingViewDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<SoB2cReturnDTO.PagingViewDTO> pageData = baseMapper.paging(query, params);
        List<SoB2cReturnDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        //填充数据
        fillDb(list);
        return new PagingVO<>(pageData);
    }

    @Override
    public void markReturned(BaseIdsDTO.IdsDTO idsDTO) {
        List<String> ids = idsDTO.getIds();
        List<SoB2cReturnEntity> list = this.listByIds(ids);
        if(list.stream().anyMatch(v->v.getStatus().equals(SoB2cReturnStatusEnum.RETURNED.code))){
            throw new ServiceException("只有待退货可以标记退货");
        }
        list.forEach(v->{
            operateLogService.addModuleOperateLog("变更退货订单的退货状态从【待退货】到【已退货】", ModuleTypeEnum.SO_B2C_RETURN.getCode(), v.getId(), "状态变更");
        });
        this.lambdaUpdate().in(BaseEntity::getId,ids).set(SoB2cReturnEntity::getStatus,SoB2cReturnStatusEnum.RETURNED.code).set(SoB2cReturnEntity::getSysReturnTime, LocalDateTime.now()).update();
    }

    @Override
    public List<SoB2cReturnDTO.GenerateSoReturnNoticeView> generateSoReturnNoticeView(List<String> ids) {
        List<SoB2cReturnDTO.GenerateSoReturnNoticeView> list = baseMapper.generateSoReturnNoticeView(ids);
        if(list.stream().anyMatch(v->!v.getStatus().equals(SoB2cReturnStatusEnum.RETURNED.getCode()))){
            throw new ServiceException("只有已退货可以下推退货通知单");
        }
        //填充数据
        fillGenerateSoReturnNoticeView(list);
        return list;
    }

    @Override
    public void generateSoB2cReturnNotice(List<SoB2cReturnDTO.GenerateSoReturnNoticeView> list) {
        soReturnNoticeFeign.generateSoB2cReturnNotice(list);
    }

    @Override
    public SoB2cReturnDTO.MatchResultDTO matchSoReturnInstock(SoB2cReturnDTO.MatchDTO matchDTO) {
        List<String> returnCodes = matchDTO.getReturnInstockCodes();
        String skuId = matchDTO.getSkuId();
        List<SoReturnInstockEntity> soReturnInstockEntityList = FeignQuery.create(SoReturnInstockEntity.class).in(SoReturnInstockEntity::getCode,returnCodes).list();
        if(CollectionUtils.isEmpty(soReturnInstockEntityList)){
            throw new ServiceException("退货入库单号为空");
        }
        List<String> instockIds = soReturnInstockEntityList.stream().map(v->v.getId()).collect(Collectors.toList());
        List<SoReturnInstockDetailEntity> allSoReturnInstockDetailEntityList = FeignQuery.create(SoReturnInstockDetailEntity.class).in(SoReturnInstockDetailEntity::getMainId,instockIds).list();
        if(CollectionUtils.isEmpty(allSoReturnInstockDetailEntityList)){
            throw new ServiceException("退货入库明细为空");
        }
        SoB2cReturnDetailEntity soB2cReturnDetailEntity = soB2cReturnDetailService.getById(matchDTO.getDetailId());
        List<SoB2cReturnDetailEntity> allDetailList = soB2cReturnDetailService.listByMainIds(Arrays.asList(soB2cReturnDetailEntity.getMainId()));
        List<String> allDetailIds = allDetailList.stream().map(v->v.getId()).collect(Collectors.toList());
        String skuNo = null;
        Integer instockQty = 0;
        for (String returnCode : returnCodes) {
            SoReturnInstockEntity soReturnInstockEntity = soReturnInstockEntityList.stream().filter(v->v.getCode().equals(returnCode)).findFirst().orElse(null);
            if(Objects.isNull(soReturnInstockEntity)){
                throw new ServiceException("退货入库单号为空");
            }
            List<SoReturnInstockDetailEntity> soReturnInstockDetailEntityList = allSoReturnInstockDetailEntityList.stream().filter(v->v.getMainId().equals(soReturnInstockEntity.getId()) && v.getSkuId().equals(skuId)).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(soReturnInstockDetailEntityList)){
                throw new ServiceException("{}退货入库单与退货订单不匹配",soReturnInstockEntity.getCode());
            }
            instockQty = instockQty+soReturnInstockDetailEntityList.stream().map(v->v.getRealQty()).reduce(MathUtil.ZERO, Integer::sum);
            skuNo = soReturnInstockDetailEntityList.get(0).getSkuNo();
            soReturnInstockDetailEntityList = soReturnInstockDetailEntityList.stream().filter(v-> StringUtils.isNotBlank(v.getSoReturnDetailId()) && !allDetailIds.contains(v.getSoReturnDetailId())).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(soReturnInstockDetailEntityList)){
                throw new ServiceException("退货入库单【{}】已经绑定退货订单，无法重复绑定",soReturnInstockEntity.getCode());
            }
        }
        return new SoB2cReturnDTO.MatchResultDTO(skuNo,instockQty);
    }

    @Override
    public List<SoB2cReturnDTO.BindReturnInstockViewDTO> bindReturnInstockView(List<String> ids) {
        List<SoB2cReturnDTO.BindReturnInstockViewDTO> soB2cReturnEntityList = this.baseMapper.bindReturnInstockView(ids);
        if(CollectionUtils.isEmpty(soB2cReturnEntityList)){
            throw new ServiceException("退货单为空");
        }
        fillBindReturnInstockView(soB2cReturnEntityList);
        return soB2cReturnEntityList;
    }

    @Override
    public Boolean bindReturnInstock(List<SoB2cReturnDTO.BindReturnInstockViewDTO> list) {
        if(CollectionUtils.isEmpty(list)){
            return true;
        }
        List<String> allInstockCodes = list.stream().flatMap(dto -> dto.getReturnInstockCodes().stream()).collect(Collectors.toList());
        List<SoReturnInstockEntity> soReturnInstockEntityList = FeignQuery.create(SoReturnInstockEntity.class).in(SoReturnInstockEntity::getCode,allInstockCodes).list();
        List<String> instockIds = soReturnInstockEntityList.stream().map(v->v.getId()).collect(Collectors.toList());
        List<SoReturnInstockDetailEntity> soReturnInstockDetailEntityList = FeignQuery.create(SoReturnInstockDetailEntity.class).in(SoReturnInstockDetailEntity::getMainId,instockIds).list();
        List<String> soIds = soReturnInstockEntityList.stream().filter(e -> Objects.nonNull(e) && OrderTypeEnum.B2C.getCode().equals(e.getType())).map(SoReturnInstockEntity::getSoId).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntityList = CollectionUtils.isNotEmpty(soIds) ? soB2cService.listByIds(soIds) : null;
        List<SoReturnInstockDetailEntity> updateList = new ArrayList<>();
        List<SoReturnInstockEntity> updateMainList = new ArrayList<>();
        for (SoB2cReturnDTO.BindReturnInstockViewDTO bindReturnInstockViewDTO : list) {
            List<String> currentInstockCodes = bindReturnInstockViewDTO.getReturnInstockCodes();
            List<SoReturnInstockEntity> currentInstockList = soReturnInstockEntityList.stream().filter(v->currentInstockCodes.contains(v.getCode())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(currentInstockList)){
                continue;
            }
            currentInstockList.forEach(v->{
                if (CollectionUtils.isNotEmpty(soB2cEntityList)){
                    SoB2cEntity soB2cEntity = soB2cEntityList.stream().filter(e -> Objects.equals(e.getId(), v.getSoId())).findFirst().orElse(null);
                    v.setPlatformOrderCode(Objects.nonNull(soB2cEntity) ? soB2cEntity.getPlatformCode() : "");
                }
                v.setSoReturnId(bindReturnInstockViewDTO.getId());
                v.setSoReturnCode(bindReturnInstockViewDTO.getCode());
            });
            updateMainList.addAll(currentInstockList);
            List<String> instockMainIds = currentInstockList.stream().map(v->v.getId()).collect(Collectors.toList());
            List<SoReturnInstockDetailEntity> currentInstockDetailList = soReturnInstockDetailEntityList.stream().filter(v->instockMainIds.contains(v.getMainId()) && v.getSkuId().equals(bindReturnInstockViewDTO.getSkuId())).collect(Collectors.toList());
            currentInstockDetailList.forEach(v->v.setSoReturnDetailId(bindReturnInstockViewDTO.getDetailId()));
            updateList.addAll(currentInstockDetailList);
        }
        //清空原先绑定的单号
        List<String> detailIds = list.stream().map(v->v.getDetailId()).collect(Collectors.toList());
        soReturnInstockFeign.clearSoReturnAndUpdate(new SoReturnInstockDetailDTO.ClearSoReturnAndUpdateDTO(detailIds,updateMainList,updateList));
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> ids) {
        List<SoB2cReturnEntity> soB2cReturnEntityList = this.listByIds(ids);
        List<String> autoAddList = soB2cReturnEntityList.stream().filter(v->v.getSourceType().equals(SoB2cReturnSourceTypeEnum.AUTO_ADD.code)).map(v->v.getCode()).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(autoAddList)){
            throw new ServiceException("{}自动新增单据不可删除",autoAddList);
        }
        List<SoReturnNoticeEntity> soReturnNoticeEntities = FeignQuery.create(SoReturnNoticeEntity.class).in(SoReturnNoticeEntity::getSourceId,ids).list();
        if(CollectionUtils.isNotEmpty(soReturnNoticeEntities)){
            List<String> errorCodes = soReturnNoticeEntities.stream().map(v->v.getSourceCode()).collect(Collectors.toList());
            throw new ServiceException("{}已下推退货通知单不可删除",errorCodes);
        }
        List<SoReturnInstockEntity> soReturnInstockEntityList = FeignQuery.create(SoReturnInstockEntity.class).in(SoReturnInstockEntity::getSoReturnId,ids).list();
        if(CollectionUtils.isNotEmpty(soReturnInstockEntityList)){
            List<String> errorCodes = soReturnInstockEntityList.stream().map(v->v.getSoReturnCode()).collect(Collectors.toList());
            throw new ServiceException("{}已关联退货入库单不可删除",errorCodes);
        }
        this.removeByIds(ids);
        soB2cReturnDetailService.deleteByMainIds(ids);
        return true;
    }

    @Override
    public void exportExcel(SoB2cReturnDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("b2c退货订单导出", EXPORT_OMS_SO_B2C_RETURN.getCode(), dto);
    }

    @Override
    public List<SoB2cReturnEntity> listBySoIds(List<String> soIds) {
        if(CollectionUtils.isEmpty(soIds)){
            return new ArrayList<>();
        }

        return this.lambdaQuery().in(SoB2cReturnEntity::getSoId,soIds).list();
    }

    @Override
    public List<SoB2cReturnDetailEntity> listDetailBySoIds(List<String> soIds) {
        if(CollectionUtils.isEmpty(soIds)){
            return new ArrayList<>();
        }
        return this.baseMapper.listDetailBySoIds(soIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateSoB2cReturnBySo(List<SoB2cDTO.GenerateSoB2cReturnViewDTO> list) {
        if(CollectionUtils.isEmpty(list)){
            return true;
        }
        Map<String,List<SoB2cDTO.GenerateSoB2cReturnViewDTO>> map = list.stream().collect(Collectors.groupingBy(v->v.getId()));
        List<SoB2cReturnDTO.AddDTO> addList = new ArrayList<>();
        map.forEach((soId,val)->{
            SoB2cDTO.GenerateSoB2cReturnViewDTO generateSoB2cReturnViewDTO = val.get(0);
            SoB2cReturnDTO.AddDTO addDTO = B2cReturnConverter.INSTANCE.generateSoB2cReturnViewDTOToAddDTO(generateSoB2cReturnViewDTO);
            addDTO.setDetailList(B2cReturnConverter.INSTANCE.generateSoB2cReturnDetailViewDTOToAddDTO(val));
            addList.add(addDTO);
        });
        addList.forEach(this::add);
        return true;
    }

    @Override
    public SoB2cReturnEntity getByPlatformReturnCode(String platformReturnNo) {
        if(StringUtils.isBlank(platformReturnNo)){
            return null;
        }
        return lambdaQuery().eq(SoB2cReturnEntity::getPlatformReturnNo,platformReturnNo).last("limit 1").one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addByPlatform(SoB2cReturnEntity soB2cReturnEntity, List<SoB2cReturnDetailEntity> soB2cReturnDetailEntityList) {
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.THD);
        soB2cReturnEntity.setCode(code);
        this.save(soB2cReturnEntity);

        //操作日志
        operateLogService.addModuleOperateLog(String.format("平台自动新增退货单【%s】", code), ModuleTypeEnum.SO_B2C_RETURN.getCode(), soB2cReturnEntity.getId(), "新增操作");

        soB2cReturnDetailEntityList.forEach(v->v.setMainId(soB2cReturnEntity.getId()));
        soB2cReturnDetailService.saveBatch(soB2cReturnDetailEntityList);
    }

    @Override
    public List<SoB2cReturnDTO.ReturnLogisticsDTO> logisticsCodePreview(List<String> ids) {
        if (CollUtil.isEmpty(ids)){
            return Collections.emptyList();
        }
        List<SoB2cReturnDTO.ReturnLogisticsDTO> returnLogisticsDTOS = baseMapper.selectLogisticsCodePreview(ids);
        if (CollUtil.isEmpty(returnLogisticsDTOS)){
            return Collections.emptyList();
        }
        List<String> shopIds = returnLogisticsDTOS.stream().map(SoB2cReturnDTO.ReturnLogisticsDTO::getShopId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoEntityList = CollUtil.isNotEmpty(shopIds) ? shopInfoService.listByIds(shopIds) : Collections.emptyList();
        Map<String, String> shopMap = shopInfoEntityList.stream().collect(Collectors.toMap(ShopInfoEntity::getId, ShopInfoEntity::getName));
        for (SoB2cReturnDTO.ReturnLogisticsDTO returnLogisticsDTO : returnLogisticsDTOS) {
            returnLogisticsDTO.setDictPlatformName(PlatformDictEnum.getNameByCode(returnLogisticsDTO.getDictPlatform()));
            returnLogisticsDTO.setShopName(shopMap.get(returnLogisticsDTO.getShopId()));
        }
        return returnLogisticsDTOS;
    }

    @Override
    public BatchResultDTO updateLogisticsCode(SoB2cReturnDTO.ReturnLogisticsDTO dto, SoB2cReturnEntity returnEntity) {
        this.lambdaUpdate().eq(SoB2cReturnEntity::getId, dto.getId())
                .set(SoB2cReturnEntity::getReturnLogisticCode, dto.getReturnLogisticCode()).update();
        //记录操作日志
        String msg = CharSequenceUtil.format("用户【{}】更新退货订单【{}】物流单号由【{}】改为【{}】", UserContext.getDefaultLoginUser().getUserName(), returnEntity.getCode(), returnEntity.getReturnLogisticCode(), dto.getReturnLogisticCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C_RETURN.getCode(), returnEntity.getId(), "修改操作");
        return BatchResultDTO.success(dto.getId(), dto.getCode(), "修改成功");
    }

    @Override
    public List<SoB2cReturnDTO.ReturnInstockDTO> returnInstockPreview(List<String> detailIds) {
        if (CollUtil.isEmpty(detailIds)){
            return Collections.emptyList();
        }
        List<SoB2cReturnDetailEntity> detailEntityList = soB2cReturnDetailService.listByIds(detailIds);
        List<String> ids = detailEntityList.stream().map(SoB2cReturnDetailEntity::getMainId).distinct().collect(Collectors.toList());
        //校验数据
        this.listByIds(ids).forEach(v->{
            if (!v.getStatus().equals(SoB2cReturnStatusEnum.RETURNED.code)){
                throw new ServiceException("只有已退货的订单才能下推入库单");
            }
        });
        //退货订单明细没有下推退货通知单，且没有关联退货入库单时，明细允许下推退货入库单
        List<SoReturnNoticeDetailEntity> returnNoticeDetailEntityList = soReturnNoticeFeign.listDetailBySourceDetailIds(detailIds);
        if (CollUtil.isNotEmpty(returnNoticeDetailEntityList)){
            throw new ServiceException("明细已下推退货通知单，无法下推退货入库单");
        }
        List<SoReturnInstockDetailEntity> returnInstockDetailEntityList = soReturnInstockFeign.listDetailBySoReturnDetailIds(detailIds);
        if (CollUtil.isNotEmpty(returnInstockDetailEntityList)){
            throw new ServiceException("明细已关联退货入库单，无法下推退货入库单");
        }
        List<SoB2cReturnDTO.ReturnInstockDTO> returnLogisticsDTOS = baseMapper.selectReturnInstockPreview(detailIds);
        if (CollUtil.isEmpty(returnLogisticsDTOS)){
            return Collections.emptyList();
        }
        List<String> shopIds = returnLogisticsDTOS.stream().map(SoB2cReturnDTO.ReturnInstockDTO::getShopId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoEntityList = CollUtil.isNotEmpty(shopIds) ? shopInfoService.listByIds(shopIds) : Collections.emptyList();
        Map<String, String> shopMap = shopInfoEntityList.stream().collect(Collectors.toMap(ShopInfoEntity::getId, ShopInfoEntity::getName));
        List<String> skuIds = returnLogisticsDTOS.stream().map(SoB2cReturnDTO.ReturnInstockDTO::getSkuId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        List<String> soIds = returnLogisticsDTOS.stream().map(SoB2cReturnDTO.ReturnInstockDTO::getSoId).filter(StringUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SoOutstockDetailEntity> soOutstockDetailEntityList = soOutstockFeign.listDetailBySoIds(soIds);
        Map<String, String> skuMap = skuVOS.stream().collect(Collectors.toMap(SkuVO::getSkuId, SkuVO::getSkuName));
        for (SoB2cReturnDTO.ReturnInstockDTO returnInstockDTO : returnLogisticsDTOS) {
            returnInstockDTO.setDictPlatformName(PlatformDictEnum.getNameByCode(returnInstockDTO.getDictPlatform()));
            returnInstockDTO.setShopName(shopMap.get(returnInstockDTO.getShopId()));
            returnInstockDTO.setProductName(skuMap.get(returnInstockDTO.getSkuId()));
            List<SoOutstockDetailEntity> soOutstockDetailEntityList1 = soOutstockDetailEntityList.stream().filter(v->v.getSoId().equals(returnInstockDTO.getSoId()) && v.getSkuId().equals(returnInstockDTO.getSkuId())).collect(Collectors.toList());
            returnInstockDTO.setOutQty(soOutstockDetailEntityList1.stream().map(v->v.getActualQty()).reduce(MathUtil.ZERO, Integer::sum));
        }
        return returnLogisticsDTOS;
    }

    @Override
    public List<SoDetailDTO.AddDetailView> listAddDetailView(listAddDetailViewDTO dto) {
        List<SoDetailDTO.AddDetailView> list = baseMapper.listAddDetailView(dto);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<String> soIds = list.stream().map(SoDetailDTO.AddDetailView::getSourceId).distinct().collect(Collectors.toList());
        List<SoB2cReturnDetailEntity> soReturnDetailEntities = soB2cReturnDetailService.listByMainIds(Collections.singletonList(dto.getId()));
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockFeign.listDetailBySoIds(soIds);
        List<String> skuIdList = list.stream().map(SoDetailDTO.AddDetailView::getSkuId).distinct().collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> productDetailEntitys = plmTaskFeign.getByIdList(skuIdList);
        InventoryQtyDTO.FindSkuInventoryParamDTO paramDTO = new InventoryQtyDTO.FindSkuInventoryParamDTO();
        paramDTO.setSkuIds(skuIdList);
        paramDTO.setWarehouseId(list.get(MathUtil.ZERO).getWarehouseId());
        paramDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        //从wms 获取到sku 的即时库存信息
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = inventoryFeign.listSkuInventory(paramDTO);
        //获取退货单id
        List<String> returnMainIds = list.stream().map(SoDetailDTO.AddDetailView::getMainId).distinct().collect(Collectors.toList());
        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = soReturnReceiveFeign.listDetailBySourceIds(returnMainIds);
        List<String> orgIds = list.stream().map(SoDetailDTO.AddDetailView::getInventoryOrgId).collect(Collectors.toList());

        List<String> warehouseIdList = list.stream().map(SoDetailDTO.AddDetailView::getWarehouseId).collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(warehouseIdList);

        //组织列表
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(orgIds);

        for (SoDetailDTO.AddDetailView addDetailView : list) {
            String warehouseName = warehouseList.stream().filter(w -> w.getId().equals(addDetailView.getWarehouseId())).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            addDetailView.setWarehouseName(warehouseName);
            BaseIdDTO.CodeDTO codeDTO = orgList.stream().filter(req -> addDetailView.getInventoryOrgId().equals(req.getId())).findFirst().orElse(new BaseIdDTO.CodeDTO());
            addDetailView.setInventoryOrgName(codeDTO.getName());
            ProductDetailEntity productDetailEntity = productDetailEntitys.stream().filter(entityClass -> entityClass.getId().equals(addDetailView.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            addDetailView.setVariantProperty(productDetailEntity.getVariantProperty());
            addDetailView.setProductName(productDetailEntity.getName());
            //获取退货数量
            Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getId().equals(addDetailView.getId())).map(SoB2cReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            //获取已出库数量
            Integer actualQty = soOutstockDetailEntities.stream().filter(req -> addDetailView.getSoId().equals(req.getSoId()) && req.getSkuId().equals(addDetailView.getSkuId()) && ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            //即时库存
            Integer curInventoryQty = skuInventoryTotalList.stream().filter(s -> s.getSkuId().equals(addDetailView.getSkuId())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(0);
            addDetailView.setAvailableQty(getAvailableQty(curInventoryQty, addDetailView.getSalesQty()));
            addDetailView.setDeliveryQty(actualQty);
            addDetailView.setUnDeliveryQty(addDetailView.getSalesQty() - actualQty);
            addDetailView.setReturnQty(returnQty);
            Integer receiveQty = soReturnReceiveDetailEntities.stream().filter(req -> addDetailView.getId().equals(req.getSourceDetailId()) && req.getSkuId().equals(addDetailView.getSkuId()) && ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            addDetailView.setReceiveQty(receiveQty);
            addDetailView.setMustQty(returnQty);
            addDetailView.setReturnTypeDictName(ReturnTypeEnum.getName(addDetailView.getReturnTypeDict()));
            addDetailView.setReturnReasonDictName(ReturnReasonEnum.getName(addDetailView.getReturnReasonDict()));
        }
        return list;
    }
    /**
     * 获取可用数量
     *
     * @param curInventoryQty 即时库存数量
     * @param salesQty        销售数量
     * @return java.lang.Integer
     * @Author Luo_WG
     * @Date 2023/5/17 11:06
     **/
    private Integer getAvailableQty(Integer curInventoryQty, Integer salesQty) {
        /**
         * 可出数量
         * 根据可用即时库存计算可出数量，
         * 当可用即时库存数量大于销售数量时 可出数量=销售数量；
         * 若可用即时库存数量小于销售数量，可出数量=即时可用库存数量
         */
        Integer availableQty = 0;
        Boolean isGre = curInventoryQty > salesQty;
        if (isGre) {
            availableQty = salesQty;
        } else {
            availableQty = curInventoryQty;
        }
        return availableQty;
    }
    private void fillBindReturnInstockView(List<SoB2cReturnDTO.BindReturnInstockViewDTO> soB2cReturnEntityList) {
        List<String> skuIds = soB2cReturnEntityList.stream().map(SoB2cReturnDTO.BindReturnInstockViewDTO::getSkuId).filter(StringUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        for (SoB2cReturnDTO.BindReturnInstockViewDTO bindReturnInstockViewDTO : soB2cReturnEntityList) {
            SkuVO skuVO = skuVOS.stream().filter(v->v.getSkuId().equals(bindReturnInstockViewDTO.getSkuId())).findFirst().orElse(new SkuVO());
            bindReturnInstockViewDTO.setProductName(skuVO.getSkuName());
        }
        List<String> detailIds = soB2cReturnEntityList.stream().map(v->v.getDetailId()).collect(Collectors.toList());
        List<SoReturnInstockDetailEntity> soReturnInstockDetailEntityList = FeignQuery.create(SoReturnInstockDetailEntity.class).in(SoReturnInstockDetailEntity::getSoReturnDetailId,detailIds).list();
        if(CollectionUtils.isEmpty(soReturnInstockDetailEntityList)){
            return;
        }
        List<String> returnMainIds = soReturnInstockDetailEntityList.stream().map(v->v.getMainId()).distinct().collect(Collectors.toList());
        List<SoReturnInstockEntity> soReturnInstockEntityList = FeignQuery.getByIds(SoReturnInstockEntity.class,returnMainIds);

        for (SoB2cReturnDTO.BindReturnInstockViewDTO bindReturnInstockViewDTO : soB2cReturnEntityList) {
            SkuVO skuVO = skuVOS.stream().filter(v->v.getSkuId().equals(bindReturnInstockViewDTO.getSkuId())).findFirst().orElse(new SkuVO());
            bindReturnInstockViewDTO.setProductName(skuVO.getSkuName());
            List<SoReturnInstockDetailEntity> currentReturnInstockList = soReturnInstockDetailEntityList.stream().filter(v->v.getSoReturnDetailId().equals(bindReturnInstockViewDTO.getDetailId())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(currentReturnInstockList)){
                continue;
            }
            Integer instockQty = currentReturnInstockList.stream().map(v->v.getRealQty()).reduce(MathUtil.ZERO,Integer::sum);
            bindReturnInstockViewDTO.setInstockQty(instockQty);
            bindReturnInstockViewDTO.setMatchSkuNo(bindReturnInstockViewDTO.getSkuNo());
            List<String> currentInstockMainIds = currentReturnInstockList.stream().map(v->v.getMainId()).collect(Collectors.toList());
            List<SoReturnInstockEntity>  currentInstockList = soReturnInstockEntityList.stream().filter(v->currentInstockMainIds.contains(v.getId())).collect(Collectors.toList());
            List<String> instockCodes = currentInstockList.stream().map(v->v.getCode()).collect(Collectors.toList());
            bindReturnInstockViewDTO.setReturnInstockCodes(instockCodes);
        }

    }

    private void fillGenerateSoReturnNoticeView(List<SoB2cReturnDTO.GenerateSoReturnNoticeView> list) {
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        List<String> shopIds = list.stream().map(SoB2cReturnDTO.GenerateSoReturnNoticeView::getShopId).filter(StringUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<ShopInfoEntity>shopInfoEntityList = CollectionUtils.isNotEmpty(shopIds)?shopInfoService.listByIds(shopIds):new ArrayList<>();
        List<String> skuIds = list.stream().map(SoB2cReturnDTO.GenerateSoReturnNoticeView::getSkuId).filter(StringUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        List<String> soIds = list.stream().map(SoB2cReturnDTO.GenerateSoReturnNoticeView::getSoId).filter(StringUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SoOutstockDetailEntity> allSoOutstockDetailEntityList = soOutstockFeign.listDetailBySoIds(soIds);
        for (SoB2cReturnDTO.GenerateSoReturnNoticeView pagingViewDTO : list) {
            pagingViewDTO.setPlatformName(PlatformDictEnum.getNameByCode(pagingViewDTO.getPlatform()));
            ShopInfoEntity shopInfoEntity = shopInfoEntityList.stream().filter(v->v.getId().equals(pagingViewDTO.getShopId())).findFirst().orElse(new ShopInfoEntity());
            pagingViewDTO.setShopName(shopInfoEntity.getName());
            SkuVO skuVO = skuVOS.stream().filter(v->v.getSkuId().equals(pagingViewDTO.getSkuId())).findFirst().orElse(new SkuVO());
            pagingViewDTO.setProductName(skuVO.getSkuName());
            List<SoOutstockDetailEntity> soOutstockDetailEntityList = allSoOutstockDetailEntityList.stream().filter(v->v.getSoId().equals(pagingViewDTO.getSoId()) && v.getSkuId().equals(pagingViewDTO.getSkuId())).collect(Collectors.toList());
            pagingViewDTO.setOutQty(soOutstockDetailEntityList.stream().map(v->v.getActualQty()).reduce(MathUtil.ZERO, Integer::sum));

        }
    }

    private void fillDb(List<SoB2cReturnDTO.PagingViewDTO> list) {
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        List<String> shopIds = list.stream().map(SoB2cReturnDTO.PagingViewDTO::getShopId).filter(StringUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<ShopInfoEntity>shopInfoEntityList = CollectionUtils.isNotEmpty(shopIds)?shopInfoService.listByIds(shopIds):new ArrayList<>();
        List<String> skuIds = list.stream().map(SoB2cReturnDTO.PagingViewDTO::getSkuId).filter(StringUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        List<String> soIds = list.stream().map(SoB2cReturnDTO.PagingViewDTO::getSoId).filter(StringUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<String> ids = list.stream().map(SoB2cReturnDTO.PagingViewDTO::getId).filter(StringUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SoOutstockDetailEntity> allSoOutstockDetailEntityList = soOutstockFeign.listDetailBySoIds(soIds);
        List<SoReturnInstockDetailEntity> allSoReturnInstockDetailList = soReturnInstockFeign.getSoReturnInstockByReturnIds(ids);
        for (SoB2cReturnDTO.PagingViewDTO pagingViewDTO : list) {
            pagingViewDTO.setPlatformName(PlatformDictEnum.getNameByCode(pagingViewDTO.getPlatform()));
            ShopInfoEntity shopInfoEntity = shopInfoEntityList.stream().filter(v->v.getId().equals(pagingViewDTO.getShopId())).findFirst().orElse(new ShopInfoEntity());
            pagingViewDTO.setShopName(shopInfoEntity.getName());
            pagingViewDTO.setTypeName(ReturnTypeEnum.getName(pagingViewDTO.getType()));
            pagingViewDTO.setStatusName(SoB2cReturnStatusEnum.getName(pagingViewDTO.getStatus()));
            SkuVO skuVO = skuVOS.stream().filter(v->v.getSkuId().equals(pagingViewDTO.getSkuId())).findFirst().orElse(new SkuVO());
            pagingViewDTO.setProductName(skuVO.getSkuName());
            List<SoOutstockDetailEntity> soOutstockDetailEntityList = allSoOutstockDetailEntityList.stream().filter(v->v.getSoId().equals(pagingViewDTO.getSoId()) && v.getSkuId().equals(pagingViewDTO.getSkuId())).collect(Collectors.toList());
            pagingViewDTO.setOutQty(soOutstockDetailEntityList.stream().map(v->v.getActualQty()).reduce(MathUtil.ZERO, Integer::sum));
            List<SoReturnInstockDetailEntity> allSoReturnInstockDetailEntityList = allSoReturnInstockDetailList.stream().filter(v->v.getSoReturnDetailId().equals(pagingViewDTO.getDetailId())).collect(Collectors.toList());
            List<SoReturnInstockDetailEntity> soReturnInstockDetailEntityList = allSoReturnInstockDetailList.stream().filter(v->v.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getCode()) &&v.getSoReturnDetailId().equals(pagingViewDTO.getDetailId())).collect(Collectors.toList());
            String instockCode = allSoReturnInstockDetailEntityList.stream().map(v->v.getCode()).collect(Collectors.joining(","));
            pagingViewDTO.setInstockCode(instockCode);

            if(StringUtils.isBlank(ReturnReasonEnum.getName(pagingViewDTO.getReason()))){
                if(StringUtils.isNotBlank(SoB2cReturnReasonEnum.getName(pagingViewDTO.getReason()))){
                    pagingViewDTO.setReason(SoB2cReturnReasonEnum.getName(pagingViewDTO.getReason()));
                }
            }else{
                pagingViewDTO.setReason(ReturnReasonEnum.getName(pagingViewDTO.getReason()));
            }

            pagingViewDTO.setInstockQty(soReturnInstockDetailEntityList.stream().filter(v->v.getSkuId().equals(pagingViewDTO.getSkuId())).map(v->v.getRealQty()).reduce(MathUtil.ZERO, Integer::sum));
            if(CollectionUtils.isNotEmpty(soReturnInstockDetailEntityList)){
                pagingViewDTO.setSysInstockTime(soReturnInstockDetailEntityList.stream().filter(v->Objects.nonNull(v.getApproveTime())).findFirst().orElse(new SoReturnInstockDetailEntity()).getApproveTime());
            }
            if(pagingViewDTO.getInstockQty() == 0){
                pagingViewDTO.setInstockStatusName("未入库");
            }else if (pagingViewDTO.getInstockQty() < pagingViewDTO.getReturnQty()){
                pagingViewDTO.setInstockStatusName("部分入库");
            }else if (pagingViewDTO.getInstockQty().equals(pagingViewDTO.getReturnQty())){
                pagingViewDTO.setInstockStatusName("已入库");
            }else {
                pagingViewDTO.setInstockStatusName("超出退货");
            }
        }
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SoB2cReturnEntity soB2cReturnEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
