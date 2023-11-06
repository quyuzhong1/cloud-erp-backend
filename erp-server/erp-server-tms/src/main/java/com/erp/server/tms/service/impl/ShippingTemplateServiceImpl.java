package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.entity.ShippingTemplateEntity;
import com.erp.model.wms.dto.TransferInfoDTO;
import com.erp.server.tms.mapper.ShippingTemplateMapper;
import com.erp.server.tms.service.ShippingTemplateService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.ShippingTemplateDTO;

import java.math.BigDecimal;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 运费模板 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-11-03
 */
@Slf4j
@Service
public class ShippingTemplateServiceImpl extends SuperServiceImpl<ShippingTemplateMapper, ShippingTemplateEntity> implements ShippingTemplateService {

    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CommonService commonService;


    @Override
    public PagingVO<ShippingTemplateDTO.ListDTO> paging(PagingDTO<ShippingTemplateDTO.PagingParamDTO> dto) {
        return null;
    }

    @Override
    public Boolean exportExcel(ShippingTemplateDTO.ExportExcelParamDTO dto, HttpServletResponse response) {
        return null;
    }

    @Override
    public BigDecimal trialCalculation(ShippingTemplateDTO.TrialCalculationParamDTO dto) {
        return null;
    }

    @Override
    public ShippingTemplateDTO.ViewDTO view(String id) {
        return null;
    }

    @Override
    public Boolean updateChannel(ShippingTemplateDTO.ChannelParamDTO dto) {
        return null;
    }

    @Override
    public BatchResultDTO updateStatus(String id) {
        return null;
    }

    @Override
    public BatchResultDTO delete(String id) {
        return null;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response,String billingMethod,String billingType) {

    }

    @Override
    public Boolean importFile(String billingMethod, String billingType, MultipartFile excelFile, HttpServletResponse response) {
        return null;
    }

    @Override
    public List<ShippingTemplateDTO.TabListDTO> tabList(PermissionsDTO dto) {
        return null;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ShippingTemplateDTO.AddDTO addDTO) {
        ShippingTemplateEntity shippingTemplateEntity = new ShippingTemplateEntity();
        BeanMapperUtils.copy(addDTO, shippingTemplateEntity);

        // 数据处理
        handleData(shippingTemplateEntity);

        log.info("开始新增运费模板");
        boolean save = super.save(shippingTemplateEntity);
        if(!save) {
            throw new ServiceException("运费模板保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "运费模板" , shippingTemplateEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, shippingTemplateEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(shippingTemplateEntity.getId(), shippingTemplateEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ShippingTemplateDTO.UpdateDTO updateDTO) {
        ShippingTemplateEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "运费模板"));
        ShippingTemplateEntity shippingTemplateEntity =  BeanMapperUtils.map(ShippingTemplateEntity.class, updateDTO);

        // 数据处理
        handleData(shippingTemplateEntity);
        log.info("编辑 开始修改运费模板数据，id：【{}】", old.getId());
        boolean save = super.updateById(shippingTemplateEntity);
        if(!save) {
            throw new ServiceException("运费模板保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录运费模板日志数据，id：【{}】", shippingTemplateEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), shippingTemplateEntity.getId(), "运费模板");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, shippingTemplateEntity, null, shippingTemplateEntity.getId(), msg);
        return Boolean.TRUE;
    }





    /**
    * 新增修改处理数据
    */
    private void handleData(ShippingTemplateEntity shippingTemplateEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
