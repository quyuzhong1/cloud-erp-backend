package com.erp.server.dmp.service.impl;


import java.util.Optional;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.AdsErpOutstockDiffFlowDTO;
import com.erp.model.dmp.dto.AdsErpOutstockDiffFlowDTO.ExpotParamDTO;
import com.erp.model.dmp.dto.AdsErpOutstockDiffFlowDTO.PagingParamDTO;
import com.erp.model.dmp.dto.AdsErpOutstockDiffFlowDTO.ReCreateDTO;
import com.erp.model.dmp.dto.AdsErpOutstockDiffFlowDTO.TotalDTO;
import com.erp.model.dmp.dto.AdsErpOutstockDiffFlowDTO.UpdateRemarkDTO;
import com.erp.model.dmp.entity.doris.AdsErpOutstockDiffFlowEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.dmp.mapper.doris.AdsErpOutstockDiffFlowMapper;
import com.erp.server.dmp.service.AdsErpOutstockDiffFlowService;
import com.erp.server.dmp.service.OperateLogService;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 第三方仓出库单据差异表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2025-11-12
 */
@DS("adsDoris")
@Slf4j
@Service
public class AdsErpOutstockDiffFlowServiceImpl extends SuperServiceImpl<AdsErpOutstockDiffFlowMapper, AdsErpOutstockDiffFlowEntity> implements AdsErpOutstockDiffFlowService {
    @Autowired
    private OperateLogService operateLogService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AdsErpOutstockDiffFlowDTO.AddDTO addDTO) {
        AdsErpOutstockDiffFlowEntity adsErpOutstockDiffFlowEntity = new AdsErpOutstockDiffFlowEntity();
        BeanMapperUtils.copy(addDTO, adsErpOutstockDiffFlowEntity);

        // 数据处理
        handleData(adsErpOutstockDiffFlowEntity);

        log.info("开始新增第三方仓出库单据差异单");
        boolean save = super.save(adsErpOutstockDiffFlowEntity);
        if(!save) {
            throw new ServiceException("第三方仓出库单据差异单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "第三方仓出库单据差异单" , adsErpOutstockDiffFlowEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, adsErpOutstockDiffFlowEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(adsErpOutstockDiffFlowEntity.getId(), adsErpOutstockDiffFlowEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AdsErpOutstockDiffFlowDTO.UpdateDTO addOrUpdateDTO) {
        AdsErpOutstockDiffFlowEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "第三方仓出库单据差异单"));
        AdsErpOutstockDiffFlowEntity adsErpOutstockDiffFlowEntity =  BeanMapperUtils.map(AdsErpOutstockDiffFlowEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(adsErpOutstockDiffFlowEntity);
        log.info("编辑 开始修改第三方仓出库单据差异单数据，id：【{}】", old.getId());
        boolean save = super.updateById(adsErpOutstockDiffFlowEntity);
        if(!save) {
            throw new ServiceException("第三方仓出库单据差异单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录第三方仓出库单据差异单日志数据，id：【{}】", adsErpOutstockDiffFlowEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), adsErpOutstockDiffFlowEntity.getId(), "第三方仓出库单据差异单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, adsErpOutstockDiffFlowEntity, null, adsErpOutstockDiffFlowEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(AdsErpOutstockDiffFlowEntity adsErpOutstockDiffFlowEntity) {
    // TODO 验证数据 & 数据赋值
    }

	@Override
	public PagingVO<AdsErpOutstockDiffFlowDTO.PagingDTO> paging(PagingDTO<PagingParamDTO> dto) {
		PagingParamDTO params = dto.getParams();
		Page query = new Page(dto.getCurrPage(), dto.getPageSize());
		IPage<AdsErpOutstockDiffFlowDTO.PagingDTO> pageData = baseMapper.paging(query, params);
		return new PagingVO<>(pageData);
	}

	@Override
	public TotalDTO total(PagingDTO<PagingParamDTO> dto) {
		return baseMapper.total(dto.getParams());
	}

	@Override
	public Boolean updateRemark(UpdateRemarkDTO dto) {
		return lambdaUpdate().eq(AdsErpOutstockDiffFlowEntity::getId, dto.getId()).set(AdsErpOutstockDiffFlowEntity::getRemark, dto.getRemark()).update();
	}
	
	@Override
	public Boolean reCreate(ReCreateDTO dto) {
		return true;
	}

	@Override
	public Boolean exportExcel(ExpotParamDTO dto) {
		downloadTaskFeign.saveDownloadTask("平台单据差异", FileTaskEventEnum.EXPORT_ADS_ERP_OUTSTOCK_DIFF_FLOW.getCode(), dto);
		return true;
	}
}
