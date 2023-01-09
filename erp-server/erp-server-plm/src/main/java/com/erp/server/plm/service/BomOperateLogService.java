package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.BomOperateLogEntity;

/**
 * bom 操作记录日志表(BomOperateLog)表服务接口
 *
 * @author yl
 * @since 2023-01-09 11:45:25
 */
public interface BomOperateLogService  extends IService<BomOperateLogEntity> {


    void saveOperate(String bomId, String operateType, String content);
}
