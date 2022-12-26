package com.erp.server.bi.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.bi.dto.BiTargetManagementImportExcelDTO;
import com.erp.model.dmp.entity.BiTargetManagementEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.service.BiTargetManagementService;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BiTargetManagementExcelListener extends AnalysisEventListener<BiTargetManagementImportExcelDTO> {

    private BiTargetManagementService biTargetManagementService;

    private PlmTaskFeign plmTaskFeign;

    private SysUserFeign sysUserFeign;

    private List<BiTargetManagementImportExcelDTO> list;

    public BiTargetManagementExcelListener(BiTargetManagementService biTargetManagementService, PlmTaskFeign plmTaskFeign
            , SysUserFeign sysUserFeign) {
        this.biTargetManagementService = biTargetManagementService;
        this.plmTaskFeign = plmTaskFeign;
        this.sysUserFeign = sysUserFeign;
        this.list = new ArrayList<>();
    }

   /**
    * @description: 每解析一行数据回调一遍
    * @author Will
    * @date: 2022/12/16 10:26
    * @param dto
    * @param analysisContext

    */
    @Override
    public void invoke(BiTargetManagementImportExcelDTO dto, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        BiTargetManagementEntity entity = new BiTargetManagementEntity();
        if (StringUtils.isBlank(dto.getPlatformName())) {
            errorMsgList.add("平台名称不能为空");
        }

        String errStr = "";
        if (errorMsgList.size() > 0) {
            for (int i = 0; i < errorMsgList.size(); i++) {
                Integer indexTemp = i + 1;
                errStr = errStr + indexTemp + "、" + errorMsgList.get(i) + "；";
            }
            dto.setErrorMsg(errStr);
            list.add(dto);
            return;
        }
        BeanMapperUtils.copy(dto,entity);
        boolean flag = biTargetManagementService.save(entity);
    }

    public List<BiTargetManagementImportExcelDTO> getDateList(){
        return list;
    }

    /**
     * @description: 全部解析完回调此方法
     * @author Will
     * @date: 2022/12/16 10:26
     * @param analysisContext

     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
