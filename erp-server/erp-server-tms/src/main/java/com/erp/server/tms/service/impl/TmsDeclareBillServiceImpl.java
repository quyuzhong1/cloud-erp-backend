package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.entity.TmsDeclareBillEntity;
import com.erp.server.tms.mapper.TmsDeclareBillMapper;
import com.erp.server.tms.service.TmsDeclareBillService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 报关单 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-03-27
 */
@Slf4j
@Service
public class TmsDeclareBillServiceImpl extends SuperServiceImpl<TmsDeclareBillMapper, TmsDeclareBillEntity> implements TmsDeclareBillService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TmsDeclareBillDTO.AddDTO addDTO) {
        TmsDeclareBillEntity tmsDeclareBillEntity = new TmsDeclareBillEntity();
        BeanMapperUtils.copy(addDTO, tmsDeclareBillEntity);

        // 数据处理
        handleData(tmsDeclareBillEntity);

        log.info("开始新增报关单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        tmsDeclareBillEntity.setCode(code);
        boolean save = super.save(tmsDeclareBillEntity);
        if(!save) {
            throw new ServiceException("报关单保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "报关单" , tmsDeclareBillEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, tmsDeclareBillEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(tmsDeclareBillEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TmsDeclareBillDTO.UpdateDTO updateDTO) {
        TmsDeclareBillEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "报关单"));
        TmsDeclareBillEntity tmsDeclareBillEntity =  BeanMapperUtils.map(TmsDeclareBillEntity.class, updateDTO);

        // 数据处理
        handleData(tmsDeclareBillEntity);
        log.info("编辑 开始修改报关单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(tmsDeclareBillEntity);
        if(!save) {
            throw new ServiceException("报关单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录报关单日志数据，单号：【{}】", tmsDeclareBillEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), tmsDeclareBillEntity.getCode(), "报关单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, tmsDeclareBillEntity, null, tmsDeclareBillEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<TmsDeclareBillDTO.TabListDTO> tabList() {
        return null;
    }

    @Override
    public PagingVO<TmsDeclareBillDTO.PagingVO> paging(PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto) {
        return null;
    }

    @Override
    public TmsDeclareBillDTO.StatisticsVO statistics() {
        return null;
    }

    @Override
    public List<TmsDeclareBillDTO.DeliveryDTO> getCanGenerateDeliveryOrder() {
        return null;
    }

    @Override
    public TmsDeclareBillDTO.ViewDTO view(String id) {
        return null;
    }

    @Override
    public List<BatchResultDTO> updateToDeclare(TmsDeclareBillDTO.UpdateDeclareStatusDTO dto) {
        return null;
    }

    @Override
    public List<BatchResultDTO> cancelDeclare(TmsDeclareBillDTO.UpdateDeclareStatusDTO dto) {
        return null;
    }

    @Override
    public List<BatchResultDTO> mergeDeclare(TmsDeclareBillDTO.MergeDeclareDTO dto) {
        return null;
    }

    @Override
    public List<BatchResultDTO> cancelMerge(TmsDeclareBillDTO.MergeDeclareDTO dto) {
        return null;
    }

    @Override
    public void export(TmsDeclareBillDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) {

    }

    @Override
    public void exportDeclare(TmsDeclareBillDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) {

    }

    @Override
    public List<BatchResultDTO> delete(TmsDeclareBillDTO.DeleteDTO dto) {
        return null;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(TmsDeclareBillEntity tmsDeclareBillEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
