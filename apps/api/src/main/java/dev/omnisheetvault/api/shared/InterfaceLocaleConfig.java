package dev.omnisheetvault.api.shared;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

/** The request's interface language from {@code Accept-Language}: English or Brazilian Portuguese, English otherwise. */
@Configuration
public class InterfaceLocaleConfig {

    static final Locale ENGLISH = Locale.ENGLISH;
    static final Locale BRAZILIAN_PORTUGUESE = Locale.forLanguageTag("pt-BR");

    /**
     * Player-facing API texts: every {@code locales/<name>_en.properties} bundle on the classpath (the platform's
     * {@code messages}, each game system's own), with its other languages beside it; a missing code keeps the English
     * default.
     */
    @Bean
    MessageSource messageSource() throws IOException {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        Set<String> basenames = new TreeSet<>();
        for (Resource english : new PathMatchingResourcePatternResolver().getResources("classpath*:locales/*_en.properties")) {
            String file = english.getFilename();
            basenames.add("locales/" + file.substring(0, file.length() - "_en.properties".length()));
        }
        source.setBasenames(basenames.toArray(String[]::new));
        source.setDefaultEncoding("UTF-8");
        source.setFallbackToSystemLocale(false);
        source.setDefaultLocale(ENGLISH);
        return source;
    }

    @Bean
    LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setSupportedLocales(List.of(ENGLISH, BRAZILIAN_PORTUGUESE));
        resolver.setDefaultLocale(ENGLISH);
        return resolver;
    }
}
