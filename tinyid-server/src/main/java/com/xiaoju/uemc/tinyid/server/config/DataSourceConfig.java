package com.xiaoju.uemc.tinyid.server.config;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.*;

/**
 * @author du_imba
 */
@Configuration
public class DataSourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);

    @Autowired
    private Environment environment;
    private static final String SEP = ",";

    private static final String DEFAULT_DATASOURCE_TYPE = "org.apache.tomcat.jdbc.pool.DataSource";

    @Bean
    public DataSource getDynamicDataSource() {
        DynamicDataSource routingDataSource = new DynamicDataSource();
        List<String> dataSourceKeys = new ArrayList<>();

        Iterable sources = ConfigurationPropertySources.get(environment);
        Binder binder = new Binder(sources);
        BindResult<HashMap> bindResult = binder.bind("datasource.tinyid", HashMap.class);
        HashMap stringObjectMap = bindResult.get();
        String names = (String) stringObjectMap.get("names");
        String dataSourceType = (String)stringObjectMap.get("type");

        Map<Object, Object> targetDataSources = new HashMap<>(4);
        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDataSourceKeys(dataSourceKeys);
        // 多个数据源
        for (String name : names.split(SEP)) {
            Map<String, Object> dsMap = (Map<String, Object>) stringObjectMap.get(name);
            DataSource dataSource = buildDataSource(dataSourceType, dsMap);
            buildDataSourceProperties(dataSource, dsMap);
            targetDataSources.put(name, dataSource);
            dataSourceKeys.add(name);
        }
        return routingDataSource;
    }

    private void buildDataSourceProperties(DataSource dataSource, Map<String, Object> dsMap) {
        try {
            // 此方法性能差，慎用
            BeanUtils.copyProperties(dataSource, dsMap);
        } catch (Exception e) {
            logger.error("error copy properties", e);
        }
    }

    private DataSource buildDataSource(String dataSourceType, Map<String, Object> dsMap) {
        try {
            String className = DEFAULT_DATASOURCE_TYPE;
            if (dataSourceType != null && !"".equals(dataSourceType.trim())) {
                className = dataSourceType;
            }
            Class<? extends DataSource> type = (Class<? extends DataSource>) Class.forName(className);
            String driverClassName = dsMap.get("driver-class-name").toString();
            String url = dsMap.get("url").toString();
            String username = dsMap.get("username").toString();
            String password = dsMap.get("password").toString();

            return DataSourceBuilder.create()
                    .driverClassName(driverClassName)
                    .url(url)
                    .username(username)
                    .password(password)
                    .type(type)
                    .build();

        } catch (ClassNotFoundException e) {
            logger.error("buildDataSource error", e);
            throw new IllegalStateException(e);
        }
    }


}
