package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.CfgInvoiceSettingDTO;
import com.erp.model.oms.dto.CfgInvoiceSettingDetailDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.mapper.CfgInvoiceSettingMapper;
import com.erp.server.oms.service.*;
import com.common.business.service.impl.SuperServiceImpl;

import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2025-04-07
 */
@Slf4j
@Service
public class CfgInvoiceSettingServiceImpl extends SuperServiceImpl<CfgInvoiceSettingMapper, CfgInvoiceSettingEntity> implements CfgInvoiceSettingService {
    @Autowired
    private OperateLogService operateLogService;

    @Resource
    private CfgInvoiceSettingDetailService cfgInvoiceSettingDetailService;

    @Resource
    private ShopInfoService shopInfoService;

    @Override
    public PagingVO<CfgInvoiceSettingDTO.PagingViewDTO> paging(PagingDTO<CfgInvoiceSettingDTO.PagingParamDTO> dto) {
        CfgInvoiceSettingDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<CfgInvoiceSettingDTO.PagingViewDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        //分页数据
        IPage<CfgInvoiceSettingDTO.PagingViewDTO> pageData = baseMapper.paging(query, params);
        List<CfgInvoiceSettingDetailDTO.ViewDetailShop> detailShopList = cfgInvoiceSettingDetailService.getDetailShop();
        Map<String, List<CfgInvoiceSettingDetailDTO.ViewDetailShop>> mainIdToDetailsMap = detailShopList.stream()
                .collect(Collectors.groupingBy(CfgInvoiceSettingDetailDTO.ViewDetailShop::getMainId));
        pageData.getRecords().forEach(item -> {
            List<CfgInvoiceSettingDetailDTO.ViewDetailShop> detailShopListByMainId = mainIdToDetailsMap.get(item.getId());
            if (CollUtil.isEmpty(detailShopListByMainId)) {
                return;
            }
            List<CfgInvoiceSettingDTO.ShopInfoDTO> shopInfoList = BeanUtil.copyToList(detailShopListByMainId, CfgInvoiceSettingDTO.ShopInfoDTO.class);
            item.setShopList(shopInfoList);
        });
        //遍历分页数据集合
        return new PagingVO(pageData);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgInvoiceSettingDTO.AddDTO dto) {
        CfgInvoiceSettingEntity entity = new CfgInvoiceSettingEntity();
        BeanMapperUtils.copy(dto, entity);
        boolean save = super.save(entity);
        if (!save) {
            throw new ServiceException("发票设置保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "VAT发票设置", entity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_VAT_INVOICE.getCode(), entity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(entity.getId(), entity.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgInvoiceSettingDTO.UpdateDTO dto) {
        CfgInvoiceSettingEntity old = super.getById(dto.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "发票设置"));
        CfgInvoiceSettingEntity cfgVatInvoiceEntity = BeanMapperUtils.map(CfgInvoiceSettingEntity.class, dto);
        cfgVatInvoiceEntity.setLeiCode(old.getLeiCode());
        cfgVatInvoiceEntity.setStartCode(old.getStartCode());
        log.info("编辑 开始修改发票设置数据，id：【{}】", old.getId());
        boolean update = super.updateById(cfgVatInvoiceEntity);
        if (!update) {
            throw new ServiceException("发票设置保存失败");
        }
        log.info("编辑 开始记录发票设置日志数据，id：【{}】", cfgVatInvoiceEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgVatInvoiceEntity.getId(), "发票设置");
        operateLogService.addModuleOperateLogByObj(old, cfgVatInvoiceEntity, ModuleTypeEnum.CFG_VAT_INVOICE.getCode(), cfgVatInvoiceEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean delete(List<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Boolean.TRUE;
        }
        return this.lambdaUpdate().in(CfgInvoiceSettingEntity::getId, ids).remove();
    }

    @Override
    public CfgInvoiceSettingDTO.ViewDTO view(String id) {
        CfgInvoiceSettingEntity old = super.getById(id);
        CfgInvoiceSettingDTO.ViewDTO dto = new CfgInvoiceSettingDTO.ViewDTO();
        if (ObjectUtil.isEmpty(old)) {
            return dto;
        }
        LambdaQueryWrapper<CfgInvoiceSettingDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CfgInvoiceSettingDetailEntity::getMainId, old.getId());
        List<CfgInvoiceSettingDetailEntity> detailEntityList = cfgInvoiceSettingDetailService.list(queryWrapper);
        if (ObjectUtil.isEmpty(detailEntityList)) {
            return dto;
        }
        List<String> shopIdList = detailEntityList.stream()
                .map(CfgInvoiceSettingDetailEntity::getShopId)
                .filter(Objects::nonNull) // 可选：去除 null 的情况
                .collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoEntityList = shopInfoService.listByIds(shopIdList);
        List<CfgInvoiceSettingDTO.ShopInfoDTO> shopInfoDTOS = shopInfoEntityList.stream()
                .map(entity -> {
                    CfgInvoiceSettingDTO.ShopInfoDTO shopInfoDTO = new CfgInvoiceSettingDTO.ShopInfoDTO();
                    BeanUtil.copyProperties(entity, shopInfoDTO);
                    return shopInfoDTO;
                })
                .collect(Collectors.toList());
        BeanMapperUtils.copy(old, dto);
        dto.setShopList(shopInfoDTOS);
        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateStatus(CfgInvoiceSettingDTO.UpdateDTO dto) {
        boolean b = false;
        if (ObjectUtil.isNotEmpty(dto.getDisabled())) {
            CfgInvoiceSettingEntity entity = new CfgInvoiceSettingEntity();
            entity.setId(dto.getId());
            entity.setDisabled(dto.getDisabled());
            b= super.updateById(entity);
        }
        return b;
    }

}
