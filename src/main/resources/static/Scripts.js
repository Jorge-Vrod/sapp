var DEFAULT_CONFIRM_MESSAGE = "Confirm?";

/**
 * 
 * @param {type} message
 * @param {type} f
 */
function doConfirm(message, f) {
    var $div = $('<div>');
    $div.append($('#modal-fragment').html());
    $div.find('.modal-body p').text(message);
    $('body').append($div);
    $div.find('.modal').modal('show').on('hidden.bs.modal', function () {
        $div.remove();
    });
    $div.find('.modal').find('.btn-primary').click(function(e) {
        e.preventDefault();
        f();
        $div.find('.modal').modal('hide');
    });
}

/**
 * 
 * @param {type} displayOnly
 * @param {type} size
 */
function doCreateRatings(displayOnly, size) {
    $('input.rating-loading').rating({
        displayOnly: displayOnly,
        size: size,
        step: displayOnly ? 0.5 : 1,
        emptyStar: '<i class="far fa-star text-success"></i>',
        filledStar: '<i class="fas fa-star text-success"></i>',
        showClear: false,
        showCaption: false
    });
}

/**
 * 
 */
function doConfigureFormValidation() {
    $.validate({
        modules: 'security',
        validateOnBlur: false,
        onElementValidate: function (valid, $el) {
            if (valid) {
                $($el.data('validation-error-msg-container')).closest('.form-group').addClass('d-none');
            } else {
                $($el.data('validation-error-msg-container')).closest('.form-group').removeClass('d-none');
            }
        },
        errorMessagePosition: 'top',
        scrollToTopOnError: true,
        borderColorOnError: '#dc3545',
        onSuccess: function ($form) {
            return true;
        },
        onError: function ($form) {
            alert('Validation failed!');
            return false;
        }
    });

    // Custom validation rules based on backend logic

    // Email validation
    $.formUtils.addValidator({
        name: 'custom_email',
        validatorFunction: function (value, $el, config, language, $form) {
            return /^[A-Za-z0-9+_.-]+@(.+)$/.test(value); // Matches backend EMAIL_PATTERN
        },
        errorMessage: 'Invalid email address',
        errorMessageKey: 'badEmail'
    });

    // Password validation (non-null check)
    $.formUtils.addValidator({
        name: 'custom_password',
        validatorFunction: function (value, $el, config, language, $form) {
            return value !== null && value.trim() !== '' && value.length >= 4; // Matches validatePassword logic
        },
        errorMessage: 'Password cannot be empty',
        errorMessageKey: 'emptyPassword'
    });

    // Name validation (max 50 characters)
    $.formUtils.addValidator({
        name: 'custom_name',
        validatorFunction: function (value, $el, config, language, $form) {
            return value.length <= 50; // Matches MAX_NAME_LENGTH
        },
        errorMessage: 'Name cannot exceed 50 characters',
        errorMessageKey: 'badNameLength'
    });

    // Address validation (max 100 characters)
    $.formUtils.addValidator({
        name: 'custom_address',
        validatorFunction: function (value, $el, config, language, $form) {
            return value === '' || value.length <= 100; // Matches validateAddress logic
        },
        errorMessage: 'Address cannot exceed 100 characters',
        errorMessageKey: 'badAddressLength'
    });

    // Image file name validation
    $.formUtils.addValidator({
        name: 'custom_image_name',
        validatorFunction: function (value, $el, config, language, $form) {
            return /^[a-zA-Z0-9-_]+\.(jpg|jpeg|png)$/.test(value) && value.length <= 50; // Matches IMAGE_NAME_PATTERN and length check
        },
        errorMessage: 'Invalid image file name. Allowed formats: jpg, jpeg, png, max length 50 characters',
        errorMessageKey: 'badImageName'
    });

    // Credit card validation (16-digit numbers)
    $.formUtils.addValidator({
        name: 'custom_credit_card',
        validatorFunction: function (value, $el, config, language, $form) {
            return /^[0-9]{16}$/.test(value); // Matches CREDIT_CARD_PATTERN
        },
        errorMessage: 'Invalid credit card number. Must be 16 digits',
        errorMessageKey: 'badCreditCard'
    });

    // CVV validation (3-4 digits)
    $.formUtils.addValidator({
        name: 'custom_cvv',
        validatorFunction: function (value, $el, config, language, $form) {
            return /^[0-9]{3,4}$/.test(value); // Matches CVV_PATTERN
        },
        errorMessage: 'Invalid CVV. Must be 3 or 4 digits',
        errorMessageKey: 'badCVV'
    });

    // Expiration year validation
    $.formUtils.addValidator({
        name: 'custom_expiration_year',
        validatorFunction: function (value, $el, config, language, $form) {
            const year = parseInt(value, 10);
            return year >= 2023 && year <= 2050; // Matches MIN_EXPIRATION_YEAR and MAX_EXPIRATION_YEAR
        },
        errorMessage: 'Invalid expiration year. Must be between 2023 and 2050',
        errorMessageKey: 'badExpirationYear'
    });

    // Expiration month validation
    $.formUtils.addValidator({
        name: 'custom_expiration_month',
        validatorFunction: function (value, $el, config, language, $form) {
            const month = parseInt(value, 10);
            return month >= 1 && month <= 12; // Matches validateExpirationMonth logic
        },
        errorMessage: 'Invalid expiration month. Must be between 1 and 12',
        errorMessageKey: 'badExpirationMonth'
    });
}


/**
 * 
 */
function doConfigureInputMasking() {
    $('[data-inputmask]').inputmask();
}