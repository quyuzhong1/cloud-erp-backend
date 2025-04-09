package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.CfgInvoiceSettingDTO;
import com.erp.model.oms.dto.CfgInvoiceSettingDetailDTO;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.mapper.CfgInvoiceSettingDetailMapper;
import com.erp.server.oms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 发票设置明细 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2025-04-07
 */
@Slf4j
@Service
public class CfgInvoiceSettingDetailServiceImpl extends SuperServiceImpl<CfgInvoiceSettingDetailMapper, CfgInvoiceSettingDetailEntity> implements CfgInvoiceSettingDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Resource
    DictBasicService dictBasicService;

    @Resource
    CfgInvoiceSettingService cfgInvoiceSettingService;

    @Override
    public List<CfgInvoiceSettingDetailDTO.ViewDTO> view(String mainId, String key, List<String> names) {
        ArrayList<CfgInvoiceSettingDetailDTO.ViewDTO> viewDTOS = new ArrayList<>();
        //查询详情列表
        List<CfgInvoiceSettingDetailDTO.ViewDTO> viewDTOList = baseMapper.selectDetailDict(mainId);
        //查询平台对应字典
        List<DictBasicDTO.ViewDTO> keyList = dictBasicService.getByKey(key);
        if (ObjectUtil.isNotEmpty(names)) {
            keyList = keyList.stream().filter(item -> ObjectUtil.isNotEmpty(names) && names.contains(item.getName())).collect(Collectors.toList());
        }
        // 列表为空，第一次点击，返回平台情况即可
        if (ObjectUtil.isEmpty(viewDTOList)) {
            keyList.forEach(item -> {
                CfgInvoiceSettingDetailDTO.ViewDTO viewDTO = new CfgInvoiceSettingDetailDTO.ViewDTO();
                viewDTO.setPlatformName(item.getName());
                viewDTO.setPlatformValue(item.getValue());
                viewDTOS.add(viewDTO);
            });
            return viewDTOS;
            }
        return viewDTOList;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgInvoiceSettingDetailDTO.AddDTO dto) {
        CfgInvoiceSettingEntity cfgInvoiceSettingEntity = cfgInvoiceSettingService.getById(dto.getMainId());
        if (ObjectUtil.isEmpty(cfgInvoiceSettingEntity)){
            throw new ServiceException("数据校验失败！请检验mainId");
        }
        CfgInvoiceSettingDetailEntity cfgInvoiceSettingDetail = new CfgInvoiceSettingDetailEntity();
        BeanMapperUtils.copy(dto, cfgInvoiceSettingDetail);
        //保存平台value
        cfgInvoiceSettingDetail.setDictPlatform(dto.getPlatformValue());
        log.info("开始新增发票设置明细");
        boolean save = super.save(cfgInvoiceSettingDetail);
        if(!save) {
            throw new ServiceException("发票设置明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "发票设置明细" , cfgInvoiceSettingDetail.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_VAT_INVOICE.getCode(), cfgInvoiceSettingDetail.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(cfgInvoiceSettingDetail.getId(), cfgInvoiceSettingDetail.getId());
    }

    @Override
    public List<CfgInvoiceSettingDetailDTO.ViewDetailShop> getDetailShop() {
        return baseMapper.selectDetailShop();
    }
}
