package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.SoB2cDeclareProductDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.SoB2cDeclareProductDTO;
import com.erp.model.oms.entity.SoB2cDeclareProductEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.DeclareLabelTypeEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.convert.B2cOrderConverter;
import com.erp.server.oms.mapper.SoB2cDeclareProductMapper;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoB2cDeclareProductService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.server.oms.service.SoB2cService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * B2C销售订单申报产品信息表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-05-09
 */
@Slf4j
@Service
public class SoB2cDeclareProductServiceImpl extends SuperServiceImpl<SoB2cDeclareProductMapper, SoB2cDeclareProductEntity> implements SoB2cDeclareProductService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private SoB2cService soB2cService;


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SoB2cDeclareProductDTO.AddDTO addDTO) {
        SoB2cDeclareProductEntity soB2cDeclareProductEntity = new SoB2cDeclareProductEntity();
        BeanMapperUtils.copy(addDTO, soB2cDeclareProductEntity);

        // 数据处理
        handleData(soB2cDeclareProductEntity);

        log.info("开始新增B2C销售订单申报产品信息单");
        boolean save = super.save(soB2cDeclareProductEntity);
        if(!save) {
            throw new ServiceException("B2C销售订单申报产品信息单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "B2C销售订单申报产品信息单" , soB2cDeclareProductEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, soB2cDeclareProductEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(soB2cDeclareProductEntity.getId(), soB2cDeclareProductEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SoB2cDeclareProductDTO.UpdateDTO updateDTO) {
        SoB2cDeclareProductEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "B2C销售订单申报产品信息单"));
        SoB2cDeclareProductEntity soB2cDeclareProductEntity = B2cOrderConverter.INSTANCE.convertDeclareProductByDto(updateDTO);
        //销售订单
        SoB2cEntity soB2cEntity = soB2cService.getById(old.getSoId());
        if (!StrUtil.equals(soB2cEntity.getApproveStatus().getCode(), ApproveStatusEnum.APPROVE.getCode())
            || !StrUtil.equals(soB2cEntity.getBillStatus(), SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode())) {
            throw new ServiceException("仅支持已审核-配货中的订单可操作");
        }

        // 数据处理
        handleData(soB2cDeclareProductEntity);
        log.info("编辑 开始修改B2C销售订单申报产品信息单数据，id：【{}】", old.getId());
        boolean save = super.updateById(soB2cDeclareProductEntity);
        if(!save) {
            throw new ServiceException("B2C销售订单申报产品信息单保存失败");
        }
        // 记录主单操作日志
            log.info("编辑 开始记录B2C销售订单申报产品信息单日志数据，id：【{}】", soB2cDeclareProductEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), soB2cDeclareProductEntity.getId(), "B2C销售订单申报产品信息单");
        // 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, soB2cDeclareProductEntity, ModuleTypeEnum.SO_B2C_DECLARE.getCode(), soB2cDeclareProductEntity.getSoId(), null, msg, "批量修改报关");
        return Boolean.TRUE;
    }
    /**
     * 根据销售订单id获取申报信息
     * @param id
     * @return
     */
    @Override
    public List<SoB2cDeclareProductEntity> listBySoId(String id) {
        if (StringUtils.isEmpty(id)){
            return Collections.emptyList();
        }
        return lambdaQuery().eq(SoB2cDeclareProductEntity::getSoId,id).list();
    }

    /**
     * 根据订单id删除申报信息
     * @param id
     */
    @Override
    public void removeBySoId(String id) {
        if (StringUtils.isNotEmpty(id)){
            lambdaUpdate().eq(SoB2cDeclareProductEntity::getSoId, id).remove();
        }
    }

    @Override
    public List<SoB2cDeclareProductDTO.ViewDTO> listViewBySoIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)){
            return Collections.emptyList();
        }
        List<SoB2cDeclareProductDTO.ViewDTO> viewDTOS = baseMapper.listViewBySoIds(ids);
        buildDeclareProductInfo(viewDTOS);
        return viewDTOS;
    }

    private void buildDeclareProductInfo(List<SoB2cDeclareProductDTO.ViewDTO> viewDTOS) {
        if (CollectionUtils.isEmpty(viewDTOS)){
            return;
        }
        viewDTOS.forEach(viewDTO ->{
            viewDTO.setDeclareLabelName(DeclareLabelTypeEnum.getName(viewDTO.getDeclareLabel()));
        });
    }

    @Override
    public Boolean exportExcel(SoB2cDeclareProductDTO.ListDTO dto, HttpServletResponse response) {
        List<SoB2cDeclareProductDTO.ViewDTO> resultList = this.listViewBySoIds(dto.getIds());
        if (CollectionUtils.isEmpty(resultList)) {
            throw new ServiceException(ApiError.ERROR_DECLARE_NOT_EXIST);
        }
        String name = "申报信息";
        StringBuffer sb = new StringBuffer();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        String excelPath = "excel/b2cDeclareProductExport.xlsx";
        try {
            new ExcelPrintUtils().patchExport(resultList, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("申报信息导出出错 >>>>>{}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SoB2cDeclareProductEntity soB2cDeclareProductEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
