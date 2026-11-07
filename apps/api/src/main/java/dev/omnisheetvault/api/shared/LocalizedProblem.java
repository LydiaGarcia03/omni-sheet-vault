package dev.omnisheetvault.api.shared;

/**
 * An exception whose problem detail the player reads: the handler looks its code up in
 * {@code locales/messages_*.properties} in the request's language, falling back to the exception's English message.
 */
public interface LocalizedProblem {

    String messageCode();

    default Object[] messageArguments() {
        return new Object[0];
    }
}
