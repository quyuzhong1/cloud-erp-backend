package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cReturnEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.SoB2cReturnStatusEnum;
import com.erp.model.oms.enums.SoB2cReturnTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.SoReturnInstockDetailDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoReturnInstockDetailEntity;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.rpc.wms.feign.SoReturnInstockFeign;
import com.erp.rpc.wms.feign.SoReturnNoticeFeign;
import com.erp.server.oms.mapper.SoB2cReturnMapper;
import com.erp.server.oms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import jodd.util.StringUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.SoB2cReturnDTO;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

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
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private ShopInfoService shopInfoService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SoOutstockFeign soOutstockFeign;

    @Resource
    private SoReturnInstockFeign soReturnInstockFeign;

    @Resource
    private SoReturnNoticeFeign soReturnNoticeFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SoB2cReturnDTO.AddDTO addDTO) {
        SoB2cReturnEntity soB2cReturnEntity = new SoB2cReturnEntity();
        BeanMapperUtils.copy(addDTO, soB2cReturnEntity);

        // 数据处理
        handleData(soB2cReturnEntity);

        log.info("开始新增b2c退货订单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        soB2cReturnEntity.setCode(code);
        boolean save = super.save(soB2cReturnEntity);
        if(!save) {
            throw new ServiceException("b2c退货订单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "b2c退货订单" , soB2cReturnEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, soB2cReturnEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(soB2cReturnEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SoB2cReturnDTO.UpdateDTO updateDTO) {
        SoB2cReturnEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "b2c退货订单"));
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
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), soB2cReturnEntity.getCode(), "b2c退货订单");
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
        this.lambdaUpdate().in(BaseEntity::getId,ids).set(SoB2cReturnEntity::getStatus,SoB2cReturnStatusEnum.TO_BE_RETURNED.code).set(SoB2cReturnEntity::getSysReturnTime, LocalDateTime.now()).update();
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
            soReturnInstockDetailEntityList = soReturnInstockDetailEntityList.stream().filter(v-> StringUtils.isBlank(v.getSoReturnDetailId())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(soReturnInstockDetailEntityList)){
                throw new ServiceException("退货入库单【{}】已经绑定退货订单，无法重复绑定",soReturnInstockEntity.getCode());
            }
            instockQty = instockQty+soReturnInstockDetailEntityList.stream().map(v->v.getRealQty()).reduce(MathUtil.ZERO, Integer::sum);
            skuNo = soReturnInstockDetailEntityList.get(0).getSkuNo();
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

        List<SoReturnInstockDetailEntity> updateList = new ArrayList<>();
        for (SoB2cReturnDTO.BindReturnInstockViewDTO bindReturnInstockViewDTO : list) {
            List<String> currentInstockCodes = bindReturnInstockViewDTO.getReturnInstockCodes();
            List<SoReturnInstockEntity> currentInstockList = soReturnInstockEntityList.stream().filter(v->currentInstockCodes.contains(v.getCode())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(currentInstockList)){
                continue;
            }
            List<String> instockMainIds = currentInstockList.stream().map(v->v.getId()).collect(Collectors.toList());
            List<SoReturnInstockDetailEntity> currentInstockDetailList = soReturnInstockDetailEntityList.stream().filter(v->instockMainIds.contains(v.getMainId()) && v.getSkuId().equals(bindReturnInstockViewDTO.getSkuId())).collect(Collectors.toList());
            currentInstockDetailList.forEach(v->v.setSoReturnDetailId(bindReturnInstockViewDTO.getDetailId()));
            updateList.addAll(currentInstockDetailList);
        }
        //清空原先绑定的单号
        List<String> detailIds = list.stream().map(v->v.getDetailId()).collect(Collectors.toList());
        soReturnInstockFeign.clearSoReturnAndUpdate(new SoReturnInstockDetailDTO.ClearSoReturnAndUpdateDTO(detailIds,updateList));
        return true;
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
        String key = DictBasicTypeEnum.PLATFORM.getType();
        List<DictBasicDTO.ViewDTO> dictBasicList = dictBasicService.getByKey(key);
        List<String> shopIds = list.stream().map(SoB2cReturnDTO.GenerateSoReturnNoticeView::getShopId).filter(StringUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<ShopInfoEntity>shopInfoEntityList = CollectionUtils.isNotEmpty(shopIds)?shopInfoService.listByIds(shopIds):new ArrayList<>();
        List<String> skuIds = list.stream().map(SoB2cReturnDTO.GenerateSoReturnNoticeView::getSkuId).filter(StringUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        List<String> soIds = list.stream().map(SoB2cReturnDTO.GenerateSoReturnNoticeView::getSoId).filter(StringUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<String> ids = list.stream().map(SoB2cReturnDTO.GenerateSoReturnNoticeView::getId).filter(StringUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SoOutstockDetailEntity> allSoOutstockDetailEntityList = soOutstockFeign.listDetailBySoIds(soIds);
        for (SoB2cReturnDTO.GenerateSoReturnNoticeView pagingViewDTO : list) {
            String dictPlatform = pagingViewDTO.getPlatform();
            String platformName = dictBasicList.stream().filter(d -> d.getValue().equals(dictPlatform)).
                    map(DictBasicDTO.ViewDTO::getName).findFirst().orElse("");
            pagingViewDTO.setPlatformName(platformName);
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
        String key = DictBasicTypeEnum.PLATFORM.getType();
        List<DictBasicDTO.ViewDTO> dictBasicList = dictBasicService.getByKey(key);
        List<String> shopIds = list.stream().map(SoB2cReturnDTO.PagingViewDTO::getShopId).filter(StringUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<ShopInfoEntity>shopInfoEntityList = CollectionUtils.isNotEmpty(shopIds)?shopInfoService.listByIds(shopIds):new ArrayList<>();
        List<String> skuIds = list.stream().map(SoB2cReturnDTO.PagingViewDTO::getSkuId).filter(StringUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        List<String> soIds = list.stream().map(SoB2cReturnDTO.PagingViewDTO::getSoId).filter(StringUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<String> ids = list.stream().map(SoB2cReturnDTO.PagingViewDTO::getId).filter(StringUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SoOutstockDetailEntity> allSoOutstockDetailEntityList = soOutstockFeign.listDetailBySoIds(soIds);
        List<SoReturnInstockDetailEntity> allSoReturnInstockDetailList = soReturnInstockFeign.getSoReturnInstockByReturnIds(ids);
        for (SoB2cReturnDTO.PagingViewDTO pagingViewDTO : list) {
            String dictPlatform = pagingViewDTO.getPlatform();
            String platformName = dictBasicList.stream().filter(d -> d.getValue().equals(dictPlatform)).
                    map(DictBasicDTO.ViewDTO::getName).findFirst().orElse("");
            pagingViewDTO.setPlatformName(platformName);
            ShopInfoEntity shopInfoEntity = shopInfoEntityList.stream().filter(v->v.getId().equals(pagingViewDTO.getShopId())).findFirst().orElse(new ShopInfoEntity());
            pagingViewDTO.setShopName(shopInfoEntity.getName());
            pagingViewDTO.setTypeName(SoB2cReturnTypeEnum.getName(pagingViewDTO.getType()));
            pagingViewDTO.setStatusName(SoB2cReturnStatusEnum.getName(pagingViewDTO.getStatus()));
            SkuVO skuVO = skuVOS.stream().filter(v->v.getSkuId().equals(pagingViewDTO.getSkuId())).findFirst().orElse(new SkuVO());
            pagingViewDTO.setProductName(skuVO.getSkuName());
            List<SoOutstockDetailEntity> soOutstockDetailEntityList = allSoOutstockDetailEntityList.stream().filter(v->v.getSoId().equals(pagingViewDTO.getSoId()) && v.getSkuId().equals(pagingViewDTO.getSkuId())).collect(Collectors.toList());
            pagingViewDTO.setOutQty(soOutstockDetailEntityList.stream().map(v->v.getActualQty()).reduce(MathUtil.ZERO, Integer::sum));

            List<SoReturnInstockDetailEntity> soReturnInstockDetailEntityList = allSoReturnInstockDetailList.stream().filter(v->v.getReturnId().equals(pagingViewDTO.getId())).collect(Collectors.toList());
            String instockCode = soReturnInstockDetailEntityList.stream().map(v->v.getCode()).collect(Collectors.joining());
            pagingViewDTO.setInstockCode(instockCode);
            pagingViewDTO.setInstockQty(soReturnInstockDetailEntityList.stream().filter(v->v.getSkuId().equals(pagingViewDTO.getSkuId())).map(v->v.getRealQty()).reduce(MathUtil.ZERO, Integer::sum));
            if(CollectionUtils.isNotEmpty(soReturnInstockDetailEntityList)){
                pagingViewDTO.setSysInstockTime(soReturnInstockDetailEntityList.get(0).getApproveTime());
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
