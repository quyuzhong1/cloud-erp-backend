package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.BomOperateLogEntity;
import com.erp.server.plm.mapper.BomOperateLogMapper;
import com.erp.server.plm.service.BomOperateLogService;
import org.springframework.stereotype.Service;

/**
 * bom 操作记录日志表(BomOperateLog)表服务实现类
 *
 * @author yl
 * @since 2023-01-09 11:45:26
 */
@Service
public class BomOperateLogServiceImpl extends ServiceImpl<BomOperateLogMapper, BomOperateLogEntity> implements BomOperateLogService {
   

 /**
  * 保存 bom 的操作记录
  * @author yl
  * @date 2023-01-09 17:24
  * @param bomId
  * @param operateType
  * @param content
  * @return void
  */
    @Override
    public void saveOperate(String bomId, String operateType, String content) {
        
    }
}
