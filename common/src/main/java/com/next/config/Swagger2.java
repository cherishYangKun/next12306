package com.next.config;

import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@EnableSwagger2
@Configuration
public class Swagger2 {

    // http://localhost:8088/swagger-ui.html   访问路径
    // http://localhost:8080/doc.html

    //配置Swagger2核心配置 docket
    public Docket createDocket() {
        return new Docket(DocumentationType.SWAGGER_2)  //指定api类型为swagger2
                .apiInfo(createApiInfo())                       //用于定义api文档汇总信息
                .select().apis(RequestHandlerSelectors.basePackage("com.next.controller"))  //指定扫描controller
                .paths(PathSelectors.any())                     //所有controller
                .build();
    }


    public ApiInfo createApiInfo() {
        return new ApiInfoBuilder().title("12306售票")  //文档标题
                .contact(new Contact("nathenyang", "https://www.baidu.com", "vodkavodka38@hotmail.com")) //联系人信息
                .description("12306售票系统")   //详细信息
                .version("1.0.1")  //文档版本号
                .termsOfServiceUrl("https://www.baidu.com").build();
    }
}
