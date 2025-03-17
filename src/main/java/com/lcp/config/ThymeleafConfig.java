package com.lcp.config;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public class ThymeleafConfig {
    private static TemplateEngine templateEngine;

    public static void initialize() {
        // Создаем резолвер шаблонов
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);    // Режим шаблонов — HTML
        templateResolver.setPrefix("/views/");           // Папка, где находятся шаблоны
        templateResolver.setSuffix(".html");             // Расширение файлов шаблонов
        templateResolver.setCacheable(false);         // Отключите кэширование для разработки

        // Создаем и настраиваем движок Thymeleaf
        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }

    public static TemplateEngine getTemplateEngine() {
        return templateEngine;
    }
}