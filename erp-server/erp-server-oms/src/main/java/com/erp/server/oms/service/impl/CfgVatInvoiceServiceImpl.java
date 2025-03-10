package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.CfgVatInvoiceDTO;
import com.erp.model.oms.entity.CfgVatInvoiceEntity;
import com.erp.model.oms.enums.CfgVatInvoiceTemplateTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.mapper.CfgVatInvoiceMapper;
import com.erp.server.oms.service.CfgVatInvoiceService;
import com.erp.server.oms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * VAT发票设置 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-03-07
 */
@Slf4j
@Service
public class CfgVatInvoiceServiceImpl extends SuperServiceImpl<CfgVatInvoiceMapper, CfgVatInvoiceEntity> implements CfgVatInvoiceService {
    @Autowired
    private OperateLogService operateLogService;
    @Resource
    private SysUserFeign sysUserFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgVatInvoiceDTO.AddDTO addDTO) {
        CfgVatInvoiceEntity cfgVatInvoiceEntity = new CfgVatInvoiceEntity();
        BeanMapperUtils.copy(addDTO, cfgVatInvoiceEntity);
        // 数据处理
        handleData(cfgVatInvoiceEntity);
        log.info("开始新增VAT发票设置");
        boolean save = super.save(cfgVatInvoiceEntity);
        if(!save) {
            throw new ServiceException("VAT发票设置保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "VAT发票设置" , cfgVatInvoiceEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_VAT_INVOICE.getCode(), cfgVatInvoiceEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(cfgVatInvoiceEntity.getId(), cfgVatInvoiceEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgVatInvoiceDTO.UpdateDTO addOrUpdateDTO) {
        CfgVatInvoiceEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "VAT发票设置"));
        CfgVatInvoiceEntity cfgVatInvoiceEntity =  BeanMapperUtils.map(CfgVatInvoiceEntity.class, addOrUpdateDTO);
        //编辑时不修改字段重新赋值
        cfgVatInvoiceEntity.setShopId(old.getShopId());
        cfgVatInvoiceEntity.setShopCountryCode(old.getShopCountryCode());
        cfgVatInvoiceEntity.setDisabled(old.getDisabled());
        // 数据处理
        handleData(cfgVatInvoiceEntity);
        log.info("编辑 开始修改VAT发票设置数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgVatInvoiceEntity);
        if(!save) {
            throw new ServiceException("VAT发票设置保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录VAT发票设置日志数据，id：【{}】", cfgVatInvoiceEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgVatInvoiceEntity.getId(), "VAT发票设置");
        operateLogService.addModuleOperateLogByObj(old, cfgVatInvoiceEntity, ModuleTypeEnum.CFG_VAT_INVOICE.getCode(), cfgVatInvoiceEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<CfgVatInvoiceDTO.PagingViewDTO> paging(PagingDTO<CfgVatInvoiceDTO.PagingParamDTO> dto) {
        CfgVatInvoiceDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<CfgVatInvoiceDTO.PagingViewDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<CfgVatInvoiceDTO.PagingViewDTO> pageData = baseMapper.paging(query, params);
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public void updateState(CfgVatInvoiceEntity entity, Boolean disabled) {

    }

    @Override
    public CfgVatInvoiceEntity getEnableCfgByShopId(String shopId) {
        List<CfgVatInvoiceEntity> list = this.lambdaQuery().eq(CfgVatInvoiceEntity::getShopId, shopId).list();
        if (CollUtil.isEmpty(list)){
            return null;
        }
        //获取启用时间 最新的一条
        CfgVatInvoiceEntity entity = list.stream().filter(e -> !e.getDisabled() && e.getEnableTime().isBefore(LocalDateTime.now())).max(Comparator.comparing(CfgVatInvoiceEntity::getEnableTime)).orElse(null);
        if (Objects.isNull(entity)){
            //返回最新一条配置
            CfgVatInvoiceEntity entity1 = list.stream().max(Comparator.comparing(CfgVatInvoiceEntity::getCreateTime)).orElse(null);
            return entity1;
        }
        return entity;
    }

    @Override
    public List<CfgVatInvoiceEntity> listCfgByShopIds(List<String> shopIdList) {
        if (CollUtil.isEmpty(shopIdList)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(CfgVatInvoiceEntity::getShopId,shopIdList).list();
    }

    private void fillList(List<CfgVatInvoiceDTO.PagingViewDTO> records) {
        if (CollUtil.isEmpty(records)){
            return;
        }
        List<DictCountryDTO.ListDTO> listDTOS = sysUserFeign.countryList();
        Map<String, String> countryMap = listDTOS.stream().collect(Collectors.toMap(DictCountryDTO.ListDTO::getId, DictCountryDTO.ListDTO::getNameCn));
        records.forEach(pagingViewDTO -> {
            pagingViewDTO.setShopCountryName(countryMap.getOrDefault(pagingViewDTO.getShopCountryCode(), CharSequenceUtil.EMPTY));
            pagingViewDTO.setCountryName(countryMap.getOrDefault(pagingViewDTO.getCountryCode(), CharSequenceUtil.EMPTY));
        });
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgVatInvoiceEntity cfgVatInvoiceEntity) {
        if (Objects.isNull(cfgVatInvoiceEntity.getIsAutoUpload())){
            cfgVatInvoiceEntity.setIsAutoUpload(Boolean.TRUE);
        }
        if (CharSequenceUtil.isBlank(cfgVatInvoiceEntity.getTemplateType())){
            cfgVatInvoiceEntity.setTemplateType(CfgVatInvoiceTemplateTypeEnum.ERP.getCode());
        }
        //详细地址+城市+州/省+邮编+国家
        cfgVatInvoiceEntity.setCompanyAddress(cfgVatInvoiceEntity.getAddress() + cfgVatInvoiceEntity.getCity() + cfgVatInvoiceEntity.getProvince() + cfgVatInvoiceEntity.getPostCode() + cfgVatInvoiceEntity.getCountryCode());
    }
}
