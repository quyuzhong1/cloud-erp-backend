package com.erp.server.mrp.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.common.business.utils.IdGeneratorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author Administrator
 * @Classname MybatisPlusConfig

 * @Date 2022-07-06 9:30
 * @Created by yl
 */
@Slf4j
@Configuration
public class MybatisPlusConfig {

    /**
     * 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.POSTGRE_SQL);
        //关闭生成countSql 优化掉join
        paginationInnerInterceptor.setOptimizeJoin(false);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
//        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

    /**
     * 修改雪花算法生成方式
     */
    @Resource
    private IdGeneratorUtil idGeneratorUtil;
    @Bean
    public IdentifierGenerator idGenerator() {
        return idGeneratorUtil.idGenerator();
    }



}
