import { ValidatorFn, ValidationErrors, AbstractControl } from '@angular/forms';

export function lastCharacterIsNumber(size): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
        const number = control.value.substr(-size);
        return number.length === size && !(/^[0-9]+$/.test(number)) ? { lastCharacterIsNumber: { value: control.value } } : null;
    };
}