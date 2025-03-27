package com.erp.server.oms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.CfgSettingEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.CfgSettingEnum;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.FullyManagedTabEnum;
import com.erp.server.oms.mapper.SoB2cMapper;
import com.erp.server.oms.query.FullyManagedQueryHandler;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author zdy
 * @ClassName FullyManagedOrderServiceImpl
 * @description: 全托管订单服务
 * @date 2025年03月25日
 * @version: 1.0
 */
@Slf4j
@Service
public class FullyManagedOrderServiceImpl extends SuperServiceImpl<SoB2cMapper, SoB2cEntity> implements FullyManagedOrderService {
    @Resource
    private ShopSysUserAuthService shopSysUserAuthService;
    @Resource
    @Qualifier("soB2cTabExecutorPool")
    private ExecutorService soB2cTabExecutorPool;
    @Resource
    private FullyManagedQueryHandler fullyManagedQueryHandler;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private DictBasicService dictBasicService;
    
    @Override
    public List<SoB2cDTO.TabListDTO> fullyManagedTabList(PermissionsDTO param) {
        FullyManagedTabEnum[] values = FullyManagedTabEnum.values();
        List<Future<SoB2cDTO.TabListDTO>> futureList = new ArrayList<>();
        List<SoB2cDTO.TabListDTO> list = new ArrayList<>();
        SoB2cDTO.ShopAuthResultDTO shopAuthResultDTO = handleShopSysUserAuth();
        for (FullyManagedTabEnum item : values) {
            Future<SoB2cDTO.TabListDTO> submit = soB2cTabExecutorPool.submit(() -> {
                SoB2cDTO.PagingParamDTO searchParamDTO = new SoB2cDTO.PagingParamDTO();
                searchParamDTO.setPermissionSql(param.getPermissionSql());
                SoB2cDTO.TabListDTO resultDTO = new SoB2cDTO.TabListDTO();
                String tabSql = fullyManagedQueryHandler.getTabSql(item.getCode());
                HashMap<String,String> map = new HashMap<>();
                map.put("default",tabSql);
                searchParamDTO.setSqlMap(map);
                //查询店铺设置权限
                Integer count;
                if (ObjectUtil.isEmpty(shopAuthResultDTO)) {
                    count = MathUtil.ZERO;
                } else {
                    count = this.baseMapper.listFullManagedCount(searchParamDTO, shopAuthResultDTO);
                }
                resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
                resultDTO.setTabFlag(item.getCode());
                resultDTO.setTabFlagName(item.getName());
                return resultDTO;
            });
            futureList.add(submit);
        }
        for(Future<SoB2cDTO.TabListDTO> f : futureList) {
            try {
                list.add(f.get());
            } catch (InterruptedException e) {
                // 恢复线程的中断状态，确保中断标志不会被忽略
                Thread.currentThread().interrupt();
                log.error("线程被中断", e);
                throw new ServiceException("线程被中断", e);
            } catch (ExecutionException e) {
                log.error("线程任务执行异常", e);
                throw new ServiceException("线程任务执行异常", e.getCause());
            } catch (ThreadDeath td) {
                log.error("捕获到 ThreadDeath，线程终止", td);
                throw td; // 重新抛出以允许线程正常终止
            }
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void timeOutConfig(CfgSettingDTO.TimeOutSettingDTO timeOutSettingDTO) {
        //检查预警设置是否存在 更新配置
        CfgSettingEntity setting = cfgSettingService.getSettingByKey(CfgSettingEnum.TIME_OUT_CONFIG.getCode());
        if (Objects.isNull(setting)) {
            setting = new CfgSettingEntity();
        }
        setting.setKey(CfgSettingEnum.TIME_OUT_CONFIG.getCode());
        setting.setValue(JSONUtil.parseObj(timeOutSettingDTO).toString());
        cfgSettingService.saveOrUpdate(setting);
        //修改全托管订单的预警时间
        List<DictBasicDTO.ViewDTO> dtoList = dictBasicService.getByKey(DictBasicTypeEnum.FULLY_MANAGED.getType());
        List<String> typeList = dtoList.stream().map(DictBasicDTO.ViewDTO::getType).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
        //更新全托管订单的预警时间
        this.baseMapper.updateTimeOutConfig(typeList,timeOutSettingDTO.getWarningTime().multiply(new BigDecimal(60)).intValue());
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/fullyManagedOrderTemplate.xlsx";
        String excelName = "template.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.DEFAULT);
        }
    }

    /**
     * 查询店铺权限设置
     */
    private SoB2cDTO.ShopAuthResultDTO handleShopSysUserAuth() {
        SoB2cDTO.ShopAuthResultDTO resultDTO = new SoB2cDTO.ShopAuthResultDTO();
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        if (ObjectUtil.isEmpty(userInfo) || StringUtils.isBlank(userInfo.getUid())) {
            return null;
        }
        List<ShopSysUserAuthDTO.ViewDTO> list = shopSysUserAuthService.listShopSysUserAuthByUserIdList(Arrays.asList(userInfo.getUid()));
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        resultDTO.setUserId(userInfo.getUid());
        resultDTO.setAuthType(list.get(0).getAuthType());
        return resultDTO;
    }
}
