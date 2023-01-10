package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BusinessNoCreateUtil;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.dto.AddBomDTO;
import com.erp.model.plm.dto.BomSkuDTO;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.server.plm.constant.BomConstant;
import com.erp.server.plm.constant.BomOperateContent;
import com.erp.server.plm.enums.BomOperationTypeEnum;
import com.erp.server.plm.enums.BomStateEnum;
import com.erp.server.plm.mapper.BomInfoMapper;
import com.erp.server.plm.service.BomInfoService;
import com.erp.server.plm.service.BomOperateLogService;
import com.erp.server.plm.service.BomSkuService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * bom 信息表(BomInfo)表服务实现类
 *
 * @author yl
 * @since 2023-01-09 11:45:28
 */
@Service
public class BomInfoServiceImpl extends ServiceImpl<BomInfoMapper, BomInfoEntity> implements BomInfoService {


    @Resource
    private BomSkuService bomSkuService;

    @Resource
    private BomOperateLogService bomOperateLogService;

    /**
     * 添加bom
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-01-09 12:18
     */
    @Override
    @Transactional
    public Boolean insert(AddBomDTO dto) {
        //sku信息
        List<BomSkuDTO> bomSkuList = dto.getSkuList();
        if (CollectionUtils.isEmpty(bomSkuList)) {
            throw new ServiceException(ApiError.ERROR_95094);
        }
        //获取到最大的序号
        Integer maxSequence = getMaxSequence();
        //获取到 编号
        String serialNumber = BusinessNoCreateUtil.getBusinessNo(BomConstant.BOM, maxSequence);
        BomInfoEntity bom = new BomInfoEntity();
        String bomId = IdWorker.getIdStr();
        bom.setType(dto.getType());
        bom.setVersion(dto.getVersion());
        bom.setId(bomId);
        bom.setSerialNumber(serialNumber);
        String submitAudit = BomConstant.SUBMIT_AUDIT;
        boolean isSubmitAudit = submitAudit.equals(dto.getSubmitType());
        if (isSubmitAudit) {
            bom.setState(BomStateEnum.WAIT_AUDIT.getState());
        }
        Boolean saveResult = this.save(bom);
        //保存成功
        if (saveResult) {
            //但是待审核的时候
            if (isSubmitAudit) {
                //这里要发起一个流程
            }
            //添加 bom 与sku 关系
            bomSkuService.saveBomSku(bomId, bomSkuList);

            //添加 bom的操作日志
            String operateContent = String.format(BomOperateContent.ADD, serialNumber);
            bomOperateLogService.saveOperate(bomId, BomOperationTypeEnum.ADD.getType(),operateContent);

        }

        return saveResult;
    }


    /**
     * 获取到最大的编号
     *
     * @param
     * @return java.lang.Integer
     * @author yl
     * @date 2023-01-10 14:58
     */
    private Integer getMaxSequence() {
        return baseMapper.getMaxSequence();
    }
}
