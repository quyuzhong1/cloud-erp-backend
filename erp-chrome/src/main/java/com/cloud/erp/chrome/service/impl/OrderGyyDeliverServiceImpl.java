package com.cloud.erp.chrome.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.erp.chrome.constant.TaskState;
import com.cloud.erp.chrome.dto.GyyShipmentsDTO;
import com.cloud.erp.chrome.entity.OrderGyyDeliverEntity;
import com.cloud.erp.chrome.mapper.OrderGyyDeliverMapper;
import com.cloud.erp.chrome.service.ChromeTaskInfoService;
import com.cloud.erp.chrome.service.CsvServer;
import com.cloud.erp.chrome.service.OrderGyyDeliverService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 管易云ERP 发货信息表 服务实现类
 * </p>
 *
 * @author yl
 * @since 2022-09-01
 */
@Service
public class OrderGyyDeliverServiceImpl extends ServiceImpl<OrderGyyDeliverMapper, OrderGyyDeliverEntity> implements OrderGyyDeliverService {


    @Autowired
    private CsvServer<OrderGyyDeliverEntity> csvServer;

    @Autowired
    private ChromeTaskInfoService chromeTaskInfoService;


    /**
     * 保存管易云发货信息表
     *
     * @param dto   参数信息
     * @author yl
     * @date 2022-09-01 18:04
     */
    @Override
    @Transactional
    public void saveDeliverCsvByUrl(GyyShipmentsDTO dto) {
        File file = null;
        try {
            String projectPath = System.getProperty("user.dir"); //当前项目
//            String path = projectPath + "/erp-chrome/src/main/java/temp";
            String path = projectPath + "/erp-chrome/attachement";
            FileUtil.mkdir(path);
            File tempFile = new File(path);
            file = HttpUtil.downloadFileFromUrl(dto.getOssUrl(), tempFile);
            List<OrderGyyDeliverEntity> saveList = csvServer.getObjectListByFile(file, OrderGyyDeliverEntity.class);
            if (CollectionUtils.isNotEmpty(saveList)) {
                saveList=saveList.stream().filter(o->StringUtils.isNotBlank(o.getSkuNo())).collect(Collectors.toList());
                this.saveBatch(saveList);
            }
            if (file != null) {
                FileUtil.del(file);
            }
            chromeTaskInfoService.updateTaskState(dto.getTaskId(), TaskState.FINISH);
        } catch (Exception e) {
            e.printStackTrace();
//            throw new ServiceException(1, "管易云保存数据失败");
            throw new RuntimeException("管易云保存数据失败",e);
        }
    }


}
