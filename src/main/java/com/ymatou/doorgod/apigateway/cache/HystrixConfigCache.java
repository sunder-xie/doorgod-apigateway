package com.ymatou.doorgod.apigateway.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ymatou.doorgod.apigateway.integration.MySqlClient;
import com.ymatou.doorgod.apigateway.model.BlacklistRule;
import com.ymatou.doorgod.apigateway.model.HystrixConfig;
import com.ymatou.doorgod.apigateway.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by tuwenjie on 2016/9/18.
 */
@Component
public class HystrixConfigCache implements Cache {

    @Autowired
    private MySqlClient mySqlClient;

    private List<HystrixConfig> configs = new ArrayList<>();

    private LoadingCache<String, HystrixConfig> uriToHystrixConfigCache;

    @PostConstruct
    @Override
    public void reload() throws Exception {

        List<HystrixConfig> configs = mySqlClient.loadHystrixConfigs();

        //uri长的在前面
        Collections.sort(configs);

        this.configs = configs;

        if (uriToHystrixConfigCache == null) {
            uriToHystrixConfigCache = CacheBuilder.newBuilder()
                    .maximumSize(Constants.MAX_CACHED_URIS)
                    .build(
                            new CacheLoader<String, HystrixConfig>() {
                                public HystrixConfig load(String uri) {
                                    for ( HystrixConfig config : configs ) {
                                        if ( uri.toLowerCase().equals(config.getUri())
                                                //或者正则表达式匹配
                                                || Pattern.matches(config.getUri(), uri.toLowerCase())){
                                            return config;
                                        }
                                    }
                                    return null;
                                }
                            });
        }

        uriToHystrixConfigCache.invalidateAll();

    }



    public HystrixConfig locate( String uri ) {
        return uriToHystrixConfigCache.getUnchecked(uri);
    }
}
