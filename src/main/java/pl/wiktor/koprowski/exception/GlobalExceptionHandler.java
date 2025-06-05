package pl.wiktor.koprowski.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pl.wiktor.koprowski.service.TranslationService;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final TranslationService translationService;

    @Autowired
    public GlobalExceptionHandler(TranslationService translationService) {
        this.translationService = translationService;
    }

    private String extractLanguageFromRequest(HttpServletRequest request) {
        String lang = request.getParameter("lang");
        return (lang == null || lang.isEmpty()) ? "pl" : lang;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        String lang = extractLanguageFromRequest(request);
        String translatedMessage = translationService.getTranslation(ex.getMessage(), lang);
        if (translatedMessage == null || translatedMessage.isEmpty()) {
            translatedMessage = ex.getMessage();
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse(translatedMessage));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        String lang = extractLanguageFromRequest(request);
        String translatedMessage = translationService.getTranslation(ex.getMessage(), lang);
        if (translatedMessage == null || translatedMessage.isEmpty()) {
            translatedMessage = ex.getMessage();
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse(translatedMessage));
    }
}
