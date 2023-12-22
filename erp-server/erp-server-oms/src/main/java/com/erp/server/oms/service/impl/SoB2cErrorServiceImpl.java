package com.erp.server.oms.service.impl;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.entity.SoB2cErrorEntity;
import com.erp.server.oms.mapper.SoB2cErrorMapper;
import com.erp.server.oms.service.SoB2cErrorService;
import com.erp.server.oms.service.SoB2cService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * <p>
 * B2C销售订单异常表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-12-20
 */
@Slf4j
@Service
public class SoB2cErrorServiceImpl extends SuperServiceImpl<SoB2cErrorMapper, SoB2cErrorEntity> implements SoB2cErrorService {

    @Resource
    private SoB2cService soB2cService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(SoB2cErrorDTO.AddDTO addDTO) {
        SoB2cErrorEntity soB2cErrorEntity = new SoB2cErrorEntity();
        BeanMapperUtils.copy(addDTO, soB2cErrorEntity);
        // 数据处理
        handleData(soB2cErrorEntity);
        boolean save = super.save(soB2cErrorEntity);
        if(!save) {
            throw new ServiceException("B2C销售订单异常单保存失败");
        }
        soB2cService.addSignError(soB2cErrorEntity.getMainId(),soB2cErrorEntity.getType());
        return true;
    }




    /** 
     * @description
     * @param dto
     * @author Lambda
     * @return 
     * @create 2023-12-20 11:24
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(SoB2cErrorDTO.DeleteDTO dto) {
        Boolean result = baseMapper.deleteB2cError(dto);
        if(result){
            soB2cService.removeSignError(dto.getMainId(),dto.getType());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateErrorOrder(String mainId, String type, String message, String paramJson, String returnJson) {
        SoB2cErrorEntity soB2cErrorEntity = new SoB2cErrorEntity();
        soB2cErrorEntity.setMainId(mainId);
        soB2cErrorEntity.setType(type);
        soB2cErrorEntity.setMessage(message);
        soB2cErrorEntity.setParamJson(paramJson);
        soB2cErrorEntity.setReturnJson(returnJson);
        this.save(soB2cErrorEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeErrorOrder(String mainId, String type) {
        SoB2cErrorDTO.DeleteDTO dto=new SoB2cErrorDTO.DeleteDTO();
        dto.setType(type);
        dto.setMainId(mainId);
        Boolean result = baseMapper.deleteB2cError(dto);
        if(result){
            soB2cService.removeSignError(dto.getMainId(),dto.getType());
        }
        return result;
    }

    /**
     * @description
     * @param dto
     * @author Lambda
     * @return
     * @create 2023-12-22 9:08
     */
    @Override
    public SoB2cErrorDTO.ViewDTO info(SoB2cErrorDTO.InfoDTO dto) {
        SoB2cErrorDTO.ViewDTO viewDTO=new SoB2cErrorDTO.ViewDTO();
        SoB2cErrorEntity entity = this.getByMainIdAndType(dto.getId(),dto.getType());
        if(Objects.nonNull(entity)){
             BeanMapperUtils.copy(entity,viewDTO);
        }
        return viewDTO;
    }

    private SoB2cErrorEntity getByMainIdAndType(String mainId, String type) {
       return this.lambdaQuery().eq(SoB2cErrorEntity::getMainId, mainId).
                eq(SoB2cErrorEntity::getType,type).last("LIMIT 1").
                one();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SoB2cErrorEntity soB2cErrorEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
