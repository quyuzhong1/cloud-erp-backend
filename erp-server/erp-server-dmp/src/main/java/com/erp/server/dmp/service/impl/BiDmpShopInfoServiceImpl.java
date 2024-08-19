package com.erp.server.dmp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.BiShopInfoDTO;
import com.erp.model.dmp.dto.DmpShopInfoDTO;
import com.erp.model.dmp.entity.BiShopInfoEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.sys.dto.SysUserDeptDTO;
import com.erp.server.dmp.pull.mapper.BiShopInfoMapper;
import com.erp.server.dmp.service.BiDmpShopInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 店铺信息服务类
 */
@Service
@Slf4j
public class BiDmpShopInfoServiceImpl extends ServiceImpl<BiShopInfoMapper, BiShopInfoEntity>
    implements BiDmpShopInfoService {

    /**
     * 添加店铺信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param biShopInfoEntity 店铺信息
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean add(BiShopInfoEntity biShopInfoEntity){
        return this.save(biShopInfoEntity);
    }

    /**
     * 根据店铺编号查询店铺信息
     * @Author Luo_WG
     * @Date 2022/11/16 19:35
     * @param shopNo 店铺编号
     * @return com.erp.model.dmp.entity.DmpSkuInfoEntity
     **/
    @Override
    public BiShopInfoEntity getShopByShopNo(String shopNo){
        LambdaQueryWrapper<BiShopInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(BiShopInfoEntity::getPlatformShopNo, shopNo);
        return this.getOne(lambdaQueryWrapper);
    }

    /**
     * 根据平台店铺id修改店铺信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:39
     * @param biShopInfoEntity 店铺信息
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean updateShopByShopNo(BiShopInfoEntity biShopInfoEntity) {
        LambdaQueryWrapper<BiShopInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(BiShopInfoEntity::getPlatformShopNo, biShopInfoEntity.getPlatformShopNo());
        lambdaQueryWrapper.eq(BiShopInfoEntity::getPlatformSign, biShopInfoEntity.getPlatformSign());
        return this.update(biShopInfoEntity, lambdaQueryWrapper);
    }

    /**
     * 校验店铺在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkOrder(BiShopInfoEntity biShopInfoEntity) {
        BiShopInfoEntity dmpOrderInfoEntity = this.getShopByShopNo(biShopInfoEntity.getPlatformShopNo());
        if (dmpOrderInfoEntity != null) {
            //如果数据有变动需要更新数据库订单信息
            if (!dmpOrderInfoEntity.toString().equals(biShopInfoEntity.toString())) {
                if (StrUtil.isNotEmpty(dmpOrderInfoEntity.getSite())){
                    biShopInfoEntity.setSite(dmpOrderInfoEntity.getSite());
                }
                this.updateShopByShopNo(biShopInfoEntity);
            }
        } else {
            this.add(biShopInfoEntity);
        }
    }

    /**
     * 根据平台查询店铺信息
     *
     * @param shopNo       店铺编号
     * @param platformSign 平台
     * @param userDeptList
     * @return java.util.List<com.erp.model.dmp.dto.ShopDTO>
     * @Author Luo_WG
     * @Date 2022/12/13 17:48
     **/
    @Override
    public BiShopInfoDTO queryShopByPlatformList(String shopNo, String platformSign, List<SysUserDeptDTO> userDeptList) {
        BiShopInfoEntity req = getShopByShopNo(shopNo);
        BiShopInfoDTO dmpShopInfoDTO = new BiShopInfoDTO();
        BeanUtil.copyProperties(req, dmpShopInfoDTO);

        List<SysUserDeptDTO> collect = userDeptList.stream().filter(udl -> udl.getUid().equals(req.getChargeId())).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(collect)) {
            dmpShopInfoDTO.setChargeId(collect.get(0).getUid());
            dmpShopInfoDTO.setChargeName(collect.get(0).getUserName());
            dmpShopInfoDTO.setDeptName(collect.get(0).getDeptName());
            dmpShopInfoDTO.setDeptId(collect.get(0).getDeptId());
        }
        return dmpShopInfoDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkShopByKingDee(BiShopInfoEntity biShopInfoEntity) {
        List<BiShopInfoEntity> shopInfoEntity = lambdaQuery()
                .eq(PlatformEnum.KINGDEE.getDesc().equals(biShopInfoEntity.getPlatformSign()) , BiShopInfoEntity::getFinanceCode, biShopInfoEntity.getPlatformShopNo())
                .eq(PlatformEnum.KINGDEE_ECC.getDesc().equals(biShopInfoEntity.getPlatformSign()) , BiShopInfoEntity::getPlatformShopNo, biShopInfoEntity.getPlatformShopNo())
                .list();
        if (CollectionUtil.isEmpty(shopInfoEntity)) {
            return;
        }
        shopInfoEntity.forEach(entity -> {
            //如果数据有变动需要更新
            if (Objects.equals(biShopInfoEntity.getUseOrgId(), entity.getUseOrgId())
                    && Objects.equals(biShopInfoEntity.getUseOrgName(), entity.getUseOrgName())) {
                return;
            }
            biShopInfoEntity.setId(entity.getId());
            entity.setIsVijim(biShopInfoEntity.getIsVijim());
            entity.setUseOrgName(biShopInfoEntity.getUseOrgName());
            entity.setUseOrgId(biShopInfoEntity.getUseOrgId());
            entity.setCustomerId(biShopInfoEntity.getCustomerId());
            entity.setCreateUserName(biShopInfoEntity.getCreateUserName());
            entity.setCountry(biShopInfoEntity.getCountry());
            updateById(entity);
        });
    }

    @Override
    public BiShopInfoDTO getShopById(String shopId) {
        BiShopInfoEntity entity = this.getById(shopId);
        BiShopInfoDTO dto = new BiShopInfoDTO();
        if (ObjectUtils.isEmpty(entity)) {
            return dto;
        }
        BeanMapperUtils.copy(entity,dto);
        return dto;
    }
}




