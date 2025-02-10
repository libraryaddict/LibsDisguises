package me.libraryaddict.disguise.utilities.mineskin.models.structures;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MineSkinCodes {
    // This list of codes is by no means comphrensive, the codes are not listed anywhere and there's plenty of codes that are tucked away.
    // Codes from generator.ts, which doesn't seem to exist anywhere in source and is entirely dependent on an old version of itself in npm?
    FAILED_TO_CREATE_ID,
    NO_ACCOUNT_AVAILABLE,
    SKIN_CHANGE_FAILED,
    INVALID_IMAGE,
    INVALID_IMAGE_URL,
    INVALID_IMAGE_UPLOAD,
    INVALID_SKIN_DATA,
    NO_DUPLICATE,
    // Codes from Authentication.ts
    UNSUPPORTED_ACCOUNT,
    MISSING_CREDENTIALS,
    MOJANG_AUTH,
    FAILED,
    MOJANG_REFRESH_FAILED,
    MOJANG_CHALLENGES_FAILED,
    MICROSOFT_AUTH_FAILED,
    MICROSOFT_REFRESH_FAILED,
    DOES_NOT_OWN_MINECRAFT,

    // Random other codes
    INTERNAL_ERROR,
    UNEXPECTED_ERROR,
    VALIDATION_ERROR,
    INVALID_REQUEST,
    INVALID_CONTENT_TYPE,
    DEPRECATED,

    UNAUTHORIZED,
    INVALID_API_KEY,
    NO_API_KEY,
    INVALID_JWT,
    INVALID_CLIENT,
    INVALID_USER,

    USER_NOT_FOUND,
    ALREADY_LIKED,
    ALREADY_REPORTED,
    BLOCKED_URL_HOST,
    REPORTED,

    NOT_FOUND,
    SKIN_NOT_FOUND,
    IMAGE_NOT_FOUND,
    UNKNOWN_VARIANT,

    // Job-related states & errors
    JOB_QUEUED,
    JOB_PENDING,
    JOB_COMPLETED,
    JOB_FAILED,
    GENERATOR_TIMEOUT,
    JOB_LIMIT,
    CONCURRENCY_LIMIT,
    JOB_NOT_FOUND,

    // Credits & Billing
    NO_CREDITS,
    INVALID_CREDITS,
    INSUFFICIENT_CREDITS,
    INVALID_BILLING,
    INSUFFICIENT_GRANTS,

    INVALID_FILE,
    MISSING_FILE,
    UPLOAD_ERROR,

    SKIN_FOUND,
    VOTE_ADDED,
    // Expect many codes to be unknown, there's far too many to really handle it tbh.
    UNKNOWN;

    public static MineSkinCodes getCode(String id) {
        for (MineSkinCodes code : MineSkinCodes.values()) {
            if (!code.name().equalsIgnoreCase(id)) {
                continue;
            }

            return code;
        }

        return UNKNOWN;
    }
}
