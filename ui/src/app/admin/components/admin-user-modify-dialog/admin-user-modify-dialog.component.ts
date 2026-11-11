import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButton } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatCheckbox } from '@angular/material/checkbox';
import { createAdminUserFormControls } from '../../forms/admin-user.form';
import { TranslatePipe } from '../../../core/translate/translate.pipe';
import { FormatGenericValidationErrorsPipe } from '../../../shared/pipes/format-generic-validation-errors.pipe';
import { USERNAME_REGEX } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-admin-user-modify-dialog',
  templateUrl: './admin-user-modify-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatDialogModule,
    MatFormFieldModule,
    MatInput,
    MatCheckbox,
    MatButton,
    TranslatePipe,
    ReactiveFormsModule,
    FormatGenericValidationErrorsPipe,
  ],
})
export class AdminUserModifyDialogComponent {
  private dialogRef = inject(MatDialogRef);

  formGroup = new FormGroup({
    username: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(4), Validators.maxLength(32), Validators.pattern(USERNAME_REGEX)],
    }),
    ...createAdminUserFormControls(),
    password: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(8)],
    }),
  });

  submit(): void {
    if (this.formGroup.invalid) {
      this.formGroup.markAllAsTouched();
      return;
    }
    this.dialogRef.close(this.formGroup.getRawValue());
  }
}
