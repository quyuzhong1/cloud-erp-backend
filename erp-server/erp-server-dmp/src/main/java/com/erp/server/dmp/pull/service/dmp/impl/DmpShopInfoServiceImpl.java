package com.erp.server.dmp.pull.service.dmp.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.dmp.dto.DmpShopInfoDTO;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.model.sys.dto.SysUserDeptDTO;
import com.erp.server.dmp.pull.mapper.DmpShopInfoMapper;
import com.erp.server.dmp.pull.service.dmp.DmpShopInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 店铺信息服务类
 */
@Service
public class DmpShopInfoServiceImpl extends ServiceImpl<DmpShopInfoMapper, DmpShopInfoEntity>
    implements DmpShopInfoService {

    @Autowired
    private DmpShopInfoMapper dmpShopInfoMapper;

    /**
     * 添加店铺信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpShopInfoEntity 店铺信息
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean add(DmpShopInfoEntity dmpShopInfoEntity){
        return this.save(dmpShopInfoEntity);
    }

    /**
     * 根据店铺编号查询店铺信息
     * @Author Luo_WG
     * @Date 2022/11/16 19:35
     * @param shopNo 店铺编号
     * @param platformSign 平台标识
     * @return com.erp.model.dmp.entity.DmpSkuInfoEntity
     **/
    @Override
    public DmpShopInfoEntity getShopByShopNo(String shopNo, String platformSign){
        LambdaQueryWrapper<DmpShopInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpShopInfoEntity::getPlarformShopNo, shopNo);
        lambdaQueryWrapper.eq(DmpShopInfoEntity::getPlatformSign, platformSign);
        return this.getOne(lambdaQueryWrapper);
    }

    /**
     * 根据平台店铺id修改店铺信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:39
     * @param dmpShopInfoEntity 店铺信息
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean updateShopByShopNo(DmpShopInfoEntity dmpShopInfoEntity) {
        LambdaQueryWrapper<DmpShopInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpShopInfoEntity::getPlarformShopNo, dmpShopInfoEntity.getPlarformShopNo());
        lambdaQueryWrapper.eq(DmpShopInfoEntity::getPlatformSign, dmpShopInfoEntity.getPlatformSign());
        return this.update(dmpShopInfoEntity, lambdaQueryWrapper);
    }

    /**
     * 校验店铺在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    @Override
    public void checkOrder(DmpShopInfoEntity dmpShopInfoEntity) {
        DmpShopInfoEntity dmpOrderInfoEntity = this.getShopByShopNo(dmpShopInfoEntity.getPlarformShopNo(), dmpShopInfoEntity.getPlatformSign());
        if (dmpOrderInfoEntity != null) {
            //如果数据有变动需要更新数据库订单信息
            if (!dmpOrderInfoEntity.toString().equals(dmpShopInfoEntity.toString())) {
                if (StrUtil.isNotEmpty(dmpOrderInfoEntity.getSite())){
                    dmpShopInfoEntity.setSite(dmpOrderInfoEntity.getSite());
                }
                this.updateShopByShopNo(dmpShopInfoEntity);
            }
        } else {
            this.add(dmpShopInfoEntity);
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
    public DmpShopInfoDTO queryShopByPlatformList(String shopNo, String platformSign, List<SysUserDeptDTO> userDeptList) {
        DmpShopInfoEntity req = getShopByShopNo(shopNo, platformSign);
        DmpShopInfoDTO dmpShopInfoDTO = new DmpShopInfoDTO();
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
}




