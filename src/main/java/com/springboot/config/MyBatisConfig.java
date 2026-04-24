package com.springboot.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

@Configuration
@MapperScan("com.springboot.repository")
public class MyBatisConfig {

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        
        // 设置实体类别名包路径
        sessionFactory.setTypeAliasesPackage("com.springboot.entity");
        
        // 注意：本项目使用注解方式，不需要加载 XML mapper 文件
        // 如果将来需要使用 XML 方式，可以取消下面这行的注释
        // PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        // sessionFactory.setMapperLocations(resolver.getResources("classpath:mapper/*.xml"));
        
        return sessionFactory.getObject();
    }
}
