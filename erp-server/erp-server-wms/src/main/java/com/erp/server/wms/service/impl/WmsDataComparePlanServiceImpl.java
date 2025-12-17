package com.erp.server.wms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.WmsDataComparePlanDTO;
import com.erp.model.wms.dto.WmsDataComparePlanDTO.GetDTO;
import com.erp.model.wms.dto.WmsDataComparePlanDTO.ViewDTO;
import com.erp.model.wms.entity.WmsDataComparePlanEntity;
import com.erp.server.wms.mapper.WmsDataComparePlanMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.WmsDataComparePlanService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
/**
 * <p>
 * 数据对比映射方案 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-03-20
 */
@Slf4j
@Service
public class WmsDataComparePlanServiceImpl extends SuperServiceImpl<WmsDataComparePlanMapper, WmsDataComparePlanEntity> implements WmsDataComparePlanService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(WmsDataComparePlanDTO.AddDTO addDTO) {
    	Integer count = lambdaQuery().eq(WmsDataComparePlanEntity::getName, addDTO.getName()).count();
    	if(count != null && count > 0) {
    		throw new ServiceException("数据对比映射方案名称已存在");
    	}
        WmsDataComparePlanEntity wmsDataComparePlanEntity = new WmsDataComparePlanEntity();
        BeanMapperUtils.copy(addDTO, wmsDataComparePlanEntity);

        // 数据处理
        handleData(wmsDataComparePlanEntity);

        log.info("开始新增数据对比映射方案");
        boolean save = super.save(wmsDataComparePlanEntity);
        if(!save) {
            throw new ServiceException("数据对比映射方案保存失败");
        }

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "数据对比映射方案" , wmsDataComparePlanEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, wmsDataComparePlanEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(wmsDataComparePlanEntity.getId(), wmsDataComparePlanEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(WmsDataComparePlanDTO.UpdateDTO updateDTO) {
        WmsDataComparePlanEntity old = super.getById(updateDTO.getId());
        if (Objects.isNull(old)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "数据对比映射方案");
        }
        WmsDataComparePlanEntity wmsDataComparePlanEntity =  BeanMapperUtils.map(WmsDataComparePlanEntity.class, updateDTO);

        // 数据处理
        handleData(wmsDataComparePlanEntity);
        log.info("编辑 开始修改数据对比映射方案数据，id：【{}】", old.getId());
        boolean save = super.updateById(wmsDataComparePlanEntity);
        if(!save) {
            throw new ServiceException("数据对比映射方案保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录数据对比映射方案日志数据，id：【{}】", wmsDataComparePlanEntity.getId());
            String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), wmsDataComparePlanEntity.getId(), "数据对比映射方案");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, wmsDataComparePlanEntity, null, wmsDataComparePlanEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(WmsDataComparePlanEntity wmsDataComparePlanEntity) {
    // TODO 验证数据 & 数据赋值
    }

	@Override
	public List<ViewDTO> get(GetDTO dto) {
		String billType = dto.getBillType();
		List<WmsDataComparePlanEntity> list = this.list(Wrappers.<WmsDataComparePlanEntity>lambdaQuery().eq(WmsDataComparePlanEntity::getBillType, billType));
		return BeanMapperUtils.copyList(ViewDTO.class, list);
	}
}
