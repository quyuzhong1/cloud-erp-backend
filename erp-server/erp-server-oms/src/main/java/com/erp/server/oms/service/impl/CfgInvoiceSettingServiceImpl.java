package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.CfgInvoiceInvalidDTO;
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
    DictBasicService dictBasicService;

    @Override
    public PagingVO<CfgInvoiceSettingDTO.PagingViewDTO> paging(PagingDTO<CfgInvoiceSettingDTO.PagingParamDTO> dto) {
        CfgInvoiceSettingDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<CfgInvoiceSettingDTO.PagingViewDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        //分页数据
        IPage<CfgInvoiceSettingDTO.PagingViewDTO> pageData = baseMapper.paging(query, params);
        //获取绑定店铺
        List<CfgInvoiceSettingDetailDTO.ViewDetailShop> detailShopList = cfgInvoiceSettingDetailService.getDetailShop();
        //获取店铺名称
        Set<String> dictPlatformSet = detailShopList.stream().map(CfgInvoiceSettingDetailDTO.ViewDetailShop::getDictPlatform).collect(Collectors.toSet());
        List<DictBasicEntity> dictBasicEntityList = dictBasicService.list(new LambdaQueryWrapper<DictBasicEntity>().in(DictBasicEntity::getValue, dictPlatformSet));
        //platformValue-platformName
        Map<String, String> dictValueNameMap = dictBasicEntityList.stream().collect(Collectors.toMap(DictBasicEntity::getValue, DictBasicEntity::getName,(existing, replacement) -> existing));
        //发票设置主键-绑定店铺集合
        Map<String, List<CfgInvoiceSettingDetailDTO.ViewDetailShop>> mainIdToDetailsMap = detailShopList.stream()
                .collect(Collectors.groupingBy(CfgInvoiceSettingDetailDTO.ViewDetailShop::getMainId));
        pageData.getRecords().forEach(item -> {
            List<CfgInvoiceSettingDetailDTO.ViewDetailShop> detailShopListByMainIdList = mainIdToDetailsMap.get(item.getId());
            if (CollUtil.isEmpty(detailShopListByMainIdList)) {
                return;
            }
            List<CfgInvoiceSettingDTO.ShopInfoDTO> shopInfoList = BeanUtil.copyToList(detailShopListByMainIdList, CfgInvoiceSettingDTO.ShopInfoDTO.class);
            shopInfoList.forEach(shopInfo -> {
                shopInfo.setDictPlatformName(dictValueNameMap.get(shopInfo.getDictPlatform()));
            });
            item.setShopList(shopInfoList);
        });
        //遍历分页数据集合
        return new PagingVO(pageData);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgInvoiceSettingDTO.AddDTO dto) {
        if (StrUtil.isNotBlank(dto.getLeiCode())) {
            // 示例格式：XX XXX XXX/XXX，例如：12 345 678/901
            String regex = "^\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}$";
            if (!dto.getLeiCode().matches(regex)) {
                throw new ServiceException("CNPJ 格式不正确，格式应为：XX XXX XXX/XXXX-XX");
            }
        }
        if (StrUtil.isNotBlank(dto.getLeiCode())) {
            LambdaQueryWrapper<CfgInvoiceSettingEntity> queryWrapper = new LambdaQueryWrapper<CfgInvoiceSettingEntity>().eq(CfgInvoiceSettingEntity::getLeiCode, dto.getLeiCode());
            if (super.count(queryWrapper) > 0) {
                throw new ServiceException("CNPJ 已存在，不能重复");
            }
        }
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
        if (ObjectUtil.isEmpty(old)){
            throw new ServiceException("此发票设置不存在");
        }
        if (!StrUtil.equals(old.getLeiCode(), dto.getLeiCode())) {
            throw new ServiceException("CNPJ 不允许修改");
        }
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "发票设置"));
        CfgInvoiceSettingEntity cfgVatInvoiceEntity = BeanMapperUtils.map(CfgInvoiceSettingEntity.class, dto);
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
        BeanUtil.copyProperties(old, dto);
        if (ObjectUtil.isEmpty(old)) {
            throw new ServiceException("此发票设置不存在");
        }
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(CfgInvoiceSettingDTO.UpdateStatusDTO dto) {
        CfgInvoiceSettingEntity invoiceSettingEntity = baseMapper.selectById(dto.getId());
        if (ObjectUtil.isEmpty(invoiceSettingEntity)) {
            throw new ServiceException("当前发票设置不存在");
        }
        if (invoiceSettingEntity.getDisabled().equals(dto.getDisabled())) {
            throw new ServiceException("当前状态与修改状态一致，无需修改");
        }
        CfgInvoiceSettingEntity entity = BeanUtil.copyProperties(dto, CfgInvoiceSettingEntity.class);
        return baseMapper.updateById(entity) > 0;
    }

    @Override
    public List<CfgInvoiceInvalidDTO.DropDownDTO> getCompanyName() {
        List<CfgInvoiceSettingEntity> invoiceSettingEntityList = this.list(new LambdaQueryWrapper<CfgInvoiceSettingEntity>()
                .select(CfgInvoiceSettingEntity::getCompanyName, CfgInvoiceSettingEntity::getId));
        List<CfgInvoiceInvalidDTO.DropDownDTO> dropDownList = invoiceSettingEntityList.stream()
                .map(entity -> {
                    CfgInvoiceInvalidDTO.DropDownDTO dto = new CfgInvoiceInvalidDTO.DropDownDTO();
                    dto.setCode(entity.getCompanyName());
                    dto.setValue(entity.getId());
                    return dto;
                })
                .collect(Collectors.toList());
        return dropDownList;
    }
}
