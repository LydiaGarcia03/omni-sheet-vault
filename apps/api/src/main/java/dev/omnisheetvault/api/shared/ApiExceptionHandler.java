package dev.omnisheetvault.api.shared;

import dev.omnisheetvault.api.catalogue.CatalogueEntryNotFoundException;
import dev.omnisheetvault.api.character.BuildNotReadyException;
import dev.omnisheetvault.api.character.CharacterIsDraftException;
import dev.omnisheetvault.api.character.CharacterNotDraftException;
import dev.omnisheetvault.api.character.CharacterNotFoundException;
import dev.omnisheetvault.api.character.InvalidPortraitException;
import dev.omnisheetvault.api.character.LevelUpNotStartedException;
import dev.omnisheetvault.api.character.NotDyingException;
import dev.omnisheetvault.api.ruleset.LevelUpNotAllowedException;
import dev.omnisheetvault.api.ruleset.InvalidBuildException;
import dev.omnisheetvault.api.dice.IneligibleRollModeException;
import dev.omnisheetvault.api.ruleset.AttunementLimitExceededException;
import dev.omnisheetvault.api.ruleset.AttunementNotAllowedException;
import dev.omnisheetvault.api.ruleset.InsufficientHitDiceException;
import dev.omnisheetvault.api.ruleset.InvalidHitDiceRecoveryException;
import dev.omnisheetvault.api.ruleset.InvalidCoinDenominationException;
import dev.omnisheetvault.api.ruleset.InvalidConditionException;
import dev.omnisheetvault.api.ruleset.InvalidCustomizationException;
import dev.omnisheetvault.api.ruleset.InvalidSheetThemeException;
import dev.omnisheetvault.api.ruleset.InvalidBackgroundFieldException;
import dev.omnisheetvault.api.ruleset.InvalidCustomActionFieldException;
import dev.omnisheetvault.api.ruleset.InvalidItemFieldException;
import dev.omnisheetvault.api.ruleset.SpellLimitExceededException;
import dev.omnisheetvault.api.ruleset.SpellPreparationLimitExceededException;
import dev.omnisheetvault.api.ruleset.UnresolvableRollException;
import dev.omnisheetvault.api.ruleset.UnsupportedGameSystemException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * The single seam every domain exception maps through, as RFC 7807 problem details —
 * see ground-rules.md.
 */
@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(CharacterNotFoundException.class)
    ProblemDetail handleCharacterNotFound(CharacterNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({CharacterIsDraftException.class, CharacterNotDraftException.class, BuildNotReadyException.class,
            NotDyingException.class, LevelUpNotStartedException.class, LevelUpNotAllowedException.class})
    ProblemDetail handleWrongCreationState(RuntimeException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(InvalidBuildException.class)
    ProblemDetail handleInvalidBuild(InvalidBuildException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(UnsupportedGameSystemException.class)
    ProblemDetail handleUnsupportedGameSystem(UnsupportedGameSystemException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(UnresolvableRollException.class)
    ProblemDetail handleUnresolvableRoll(UnresolvableRollException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(IneligibleRollModeException.class)
    ProblemDetail handleIneligibleRollMode(IneligibleRollModeException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(InvalidCustomizationException.class)
    ProblemDetail handleInvalidCustomization(InvalidCustomizationException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(InvalidSheetThemeException.class)
    ProblemDetail handleInvalidSheetTheme(InvalidSheetThemeException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(InvalidConditionException.class)
    ProblemDetail handleInvalidCondition(InvalidConditionException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(InvalidCoinDenominationException.class)
    ProblemDetail handleInvalidCoinDenomination(InvalidCoinDenominationException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(AttunementLimitExceededException.class)
    ProblemDetail handleAttunementLimitExceeded(AttunementLimitExceededException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(AttunementNotAllowedException.class)
    ProblemDetail handleAttunementNotAllowed(AttunementNotAllowedException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(InsufficientHitDiceException.class)
    ProblemDetail handleInsufficientHitDice(InsufficientHitDiceException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(InvalidHitDiceRecoveryException.class)
    ProblemDetail handleInvalidHitDiceRecovery(InvalidHitDiceRecoveryException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(CatalogueEntryNotFoundException.class)
    ProblemDetail handleCatalogueEntryNotFound(CatalogueEntryNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(SpellLimitExceededException.class)
    ProblemDetail handleSpellLimitExceeded(SpellLimitExceededException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(SpellPreparationLimitExceededException.class)
    ProblemDetail handleSpellPreparationLimitExceeded(SpellPreparationLimitExceededException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(InvalidBackgroundFieldException.class)
    ProblemDetail handleInvalidBackgroundField(InvalidBackgroundFieldException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(InvalidCustomActionFieldException.class)
    ProblemDetail handleInvalidCustomActionField(InvalidCustomActionFieldException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(InvalidPortraitException.class)
    ProblemDetail handleInvalidPortrait(InvalidPortraitException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ProblemDetail handleUploadTooLarge(MaxUploadSizeExceededException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONTENT_TOO_LARGE, "The file is too large: a portrait can be at most 3 MB.");
    }

    @ExceptionHandler(InvalidItemFieldException.class)
    ProblemDetail handleInvalidItemField(InvalidItemFieldException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }
}
