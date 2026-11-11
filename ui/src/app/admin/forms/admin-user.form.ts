import { FormControl, Validators } from '@angular/forms';

export function createAdminUserFormControls() {
  return {
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email, Validators.maxLength(128)],
    }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.minLength(8)] }),
    active: new FormControl(true, { nonNullable: true }),
    admin: new FormControl(false, { nonNullable: true }),
  };
}
