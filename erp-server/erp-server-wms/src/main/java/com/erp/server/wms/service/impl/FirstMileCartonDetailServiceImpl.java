package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.dto.FirstMileCartonBillDTO;
import com.erp.model.wms.dto.FirstMileCartonDTO;
import com.erp.model.wms.entity.FirstMileCartonDetailEntity;
import com.erp.server.wms.mapper.FirstMileCartonDetailMapper;
import com.erp.server.wms.service.FirstMileCartonBillService;
import com.erp.server.wms.service.FirstMileCartonDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.FirstMileCartonDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 发货单箱子信息表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@Service
public class FirstMileCartonDetailServiceImpl extends SuperServiceImpl<FirstMileCartonDetailMapper, FirstMileCartonDetailEntity> implements FirstMileCartonDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private FirstMileCartonBillService firstMileCartonBillService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(FirstMileCartonDTO.AddDTO addDTO, String mainId) {
        List<FirstMileCartonDetailEntity> detailEntityList = BeanMapper.copyList(addDTO.getDetailList(), FirstMileCartonDetailEntity.class);
        // 数据处理
        handleData(detailEntityList, mainId);

        log.info("开始新增发货单箱子信息单");
        boolean save = super.saveBatch(detailEntityList);
        if(!save) {
            throw new ServiceException("发货单箱子信息单保存失败");
        }
        //箱子信息明细
        this.firstMileCartonBillSave(addDTO.getBoxQty(), detailEntityList, mainId);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(FirstMileCartonDTO.UpdateDTO updateDTO, String mainId) {
        List<FirstMileCartonDetailEntity> detailEntityList = BeanMapper.copyList(updateDTO.getDetailList(), FirstMileCartonDetailEntity.class);

        // 数据处理
        handleData(detailEntityList, mainId);
        log.info("编辑 开始修改发货单箱子信息单数据，id：【{}】", mainId);
        boolean save = super.saveOrUpdateBatch(detailEntityList);
        if(!save) {
            throw new ServiceException("发货单箱子信息单保存失败");
        }
        //箱子信息明细
        this.firstMileCartonBillSave(updateDTO.getBoxQty(), detailEntityList, mainId);
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<FirstMileCartonDetailEntity> detailEntityList, String mainId) {
        for (FirstMileCartonDetailEntity firstMileCartonDetailEntity : detailEntityList) {
            firstMileCartonDetailEntity.setCartonId(mainId);
        }
    }

    /**
     * 保存箱子明细信息
     * @Author Luo_WG
     * @Date 2023/11/28 16:36
     * @param boxQty 箱数
     * @param detailEntityList 包装信息
     * @param mainId 箱规id
     * @return void
     **/
    private void firstMileCartonBillSave(Integer boxQty, List<FirstMileCartonDetailEntity> detailEntityList, String mainId) {
        firstMileCartonBillService.deleteByCartonId(mainId);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < detailEntityList.size(); i++) {
            sb.append(detailEntityList.get(i).getSkuNo());
            sb.append(StringPool.ASTERISK);
            sb.append(detailEntityList.get(i).getPackQty());
            sb.append(StringPool.PLUS);
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1); // 去掉最后的+号
        }
        for (Integer i = 1; i <= boxQty; i++) {
            FirstMileCartonBillDTO.AddDTO billAdd = new FirstMileCartonBillDTO.AddDTO();
            billAdd.setBoxDesc(sb.toString());
            billAdd.setBoxNo(String.valueOf(i));
            billAdd.setCartonId(mainId);
            firstMileCartonBillService.add(billAdd);
        }
    }
}
