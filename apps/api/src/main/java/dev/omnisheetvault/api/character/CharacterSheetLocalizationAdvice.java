package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.catalogue.CatalogueTextMemory;
import dev.omnisheetvault.api.shared.InterfaceLocale;
import java.util.Locale;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/** Every sheet the API returns shows its copied catalogue texts in the request's language; the stored sheet stays English. */
@RestControllerAdvice
class CharacterSheetLocalizationAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectProvider<CatalogueTextMemory> textMemory;

    /** A provider, so web slice tests without the catalogue still start; the sheet then stays as stored. */
    CharacterSheetLocalizationAdvice(ObjectProvider<CatalogueTextMemory> textMemory) {
        this.textMemory = textMemory;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return CharacterSheetResponse.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        Locale locale = InterfaceLocale.current();
        CatalogueTextMemory memory = textMemory.getIfAvailable();
        if (!(body instanceof CharacterSheetResponse sheet) || memory == null || Locale.ENGLISH.getLanguage().equals(locale.getLanguage())) {
            return body;
        }
        return sheet.localized(new SheetTextLocalizer(memory.translator(locale)));
    }
}
