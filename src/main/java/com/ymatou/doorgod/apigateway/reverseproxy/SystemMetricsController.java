package com.ymatou.doorgod.apigateway.reverseproxy;

import com.ymatou.doorgod.apigateway.utils.Utils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by tuwenjie on 2016/9/29.
 */
@Controller
public class SystemMetricsController {

    @RequestMapping("/warmup")
    @ResponseBody
    public String warmup() {
        if ( VertxVerticleDeployer.startUpSuccess) {
            return "ok";
        } else {
            return "Vertx verticles not deployed successfully yet";
        }
    }

    @RequestMapping(value = "/version", produces = {"text/plain"})
    @ResponseBody
    public String version() {
        return Utils.readVersion();
    }

}

