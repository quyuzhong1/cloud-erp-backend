package com.erp.server.wms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.wms.dto.CfgInventoryAgeDTO;
import com.erp.model.wms.dto.CfgSettingVirtualValueDTO;
import com.erp.model.wms.entity.CfgInventoryAgeEntity;
import com.erp.server.wms.mapper.CfgInventoryAgeMapper;
import com.erp.server.wms.service.CfgInventoryAgeService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * <p>
 * 库龄配置表 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-08-20
 */
@Slf4j
@Service
public class CfgInventoryAgeServiceImpl extends SuperServiceImpl<CfgInventoryAgeMapper, CfgInventoryAgeEntity> implements CfgInventoryAgeService {

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgInventoryAgeDTO.AddDTO addDTO) {
        // 数据处理
        CfgInventoryAgeEntity cfgInventoryAgeEntity = handleData(addDTO);

        log.info("开始新增库龄配置单");
        boolean save = super.saveOrUpdate(cfgInventoryAgeEntity);
        if(!save) {
            throw new ServiceException("库龄配置单保存失败");
        }
        return new BaseResultDTO.AddDTO(cfgInventoryAgeEntity.getId(), cfgInventoryAgeEntity.getId());
    }



    @Override
    public CfgInventoryAgeEntity getByUserIdOrDefault() {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        CfgInventoryAgeEntity cfgInventoryAgeEntity = this.getByUserId(userInfo.getUid());
        if (ObjectUtil.isEmpty(cfgInventoryAgeEntity)) {
            //如果没有配置则返回默认配置
            return this.getByUserId("");
        }
        return cfgInventoryAgeEntity;
    }

    @Override
    public CfgInventoryAgeDTO.ViewDTO viewVirtual() {
        CfgInventoryAgeEntity cfgInventoryAgeEntity = this.getByUserIdOrDefault();
        if (ObjectUtil.isEmpty(cfgInventoryAgeEntity)) {
            return new CfgInventoryAgeDTO.ViewDTO();
        }
        //转换json
        return BeanUtil.toBean(cfgInventoryAgeEntity.getDataJson(), CfgInventoryAgeDTO.ViewDTO.class);
    }

    /**
     * 数据格式化
     * @author will
     * @date 2025/8/20 09:55
     * @param addDTO
     * @return CfgInventoryAgeEntity
     */
    private CfgInventoryAgeEntity handleData ( CfgInventoryAgeDTO.AddDTO addDTO) {
        CfgInventoryAgeEntity entity = new CfgInventoryAgeEntity();
        //
        //数据验证
        checkInventoryAge(addDTO.getList());

        //根据人员查询配置信息
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        CfgInventoryAgeEntity cfgInventoryAgeEntity = this.getByUserId(userInfo.getUid());
        if (ObjectUtil.isNotEmpty(cfgInventoryAgeEntity)) {
            entity.setId(cfgInventoryAgeEntity.getId());
        }
        //系统配置json
        JSONObject jsonObject =JSONUtil.parseObj(addDTO);
        //查询是否是修改
        entity.setDataJson(jsonObject);
        entity.setUserId(userInfo.getUid());
        return entity;
    }


    /**
     * 根据用户id查询
     * @author will
     * @date 2025/8/20 09:52
     * @param userId
     * @return CfgInventoryAgeEntity
     */
    private CfgInventoryAgeEntity getByUserId(String userId) {
        return lambdaQuery().eq(CfgInventoryAgeEntity::getUserId,userId).last("limit 1").one();
    }

    /**
     * 验证库龄配置信息
     * @author will
     * @date 2024/12/25 14:56
     */
    private void checkInventoryAge ( List<CfgSettingVirtualValueDTO.InventoryAgeDateTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        for (int i = 0; i < list.size() ;i++) {
            CfgSettingVirtualValueDTO.InventoryAgeDateTO inventoryAgeDateTO = list.get(i);
            //结束数量和开始数量验证
            if (ObjectUtil.isNotEmpty(inventoryAgeDateTO.getEndDays()) && MathUtil.compareTo(inventoryAgeDateTO.getStartDays(),inventoryAgeDateTO.getEndDays()) >= MathUtil.ZERO) {
                throw new ServiceException("结束天数【{}】必须大于开始天数【{}】",inventoryAgeDateTO.getEndDays(),inventoryAgeDateTO.getStartDays());
            }
            //当i>0时需要验证开始天数必须等于上一条数据的结束天数
            if (i > 0) {
                Boolean isEquals = MathUtil.compareTo(inventoryAgeDateTO.getStartDays(),list.get(i-1).getEndDays()) == MathUtil.ZERO;
                if (!isEquals) {
                    throw new ServiceException("开始数量【{}】必须和上一条结束数量【{}】一致",inventoryAgeDateTO.getStartDays(),list.get(i-1).getEndDays());
                }
            }
        }
    }
}
