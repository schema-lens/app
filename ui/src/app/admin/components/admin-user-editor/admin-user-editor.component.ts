import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatCheckbox } from '@angular/material/checkbox';
import { createAdminUserFormControls } from '../../forms/admin-user.form';
import { AdminUpdateUser, AdminUser } from '../../models/admin-user.model';
import { TranslatePipe } from '../../../core/translate/translate.pipe';
import { FormatGenericValidationErrorsPipe } from '../../../shared/pipes/format-generic-validation-errors.pipe';
import { SaveDeleteControlComponent } from '../../../shared/components/save-delete-control/save-delete-control.component';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-admin-user-editor',
  templateUrl: './admin-user-editor.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInput,
    MatCheckbox,
    TranslatePipe,
    FormatGenericValidationErrorsPipe,
    SaveDeleteControlComponent,
  ],
})
export class AdminUserEditorComponent {
  user = input.required<AdminUser>();
  currentUsername = input<string>();
  saving = input(false);

  save = output<AdminUpdateUser>();
  delete = output<void>();

  formGroup = new FormGroup(createAdminUserFormControls());
  isCurrentUser = computed(() => this.user().username === this.currentUsername());

  constructor() {
    toObservable(this.user)
      .pipe(takeUntilDestroyed())
      .subscribe(({ email, active, admin }) => this.formGroup.reset({ email, active, admin, password: '' }));

    toObservable(computed(() => ({ saving: this.saving(), isCurrentUser: this.isCurrentUser() })))
      .pipe(takeUntilDestroyed())
      .subscribe(({ saving, isCurrentUser }) => {
        if (saving) {
          this.formGroup.disable();
          return;
        }

        this.formGroup.enable();

        if (isCurrentUser) {
          this.formGroup.controls.active.disable();
          this.formGroup.controls.admin.disable();
        }
      });
  }

  submit(): void {
    if (this.saving()) {
      return;
    }

    if (this.formGroup.invalid) {
      this.formGroup.markAllAsTouched();
      return;
    }

    const { email, active, admin, password } = this.formGroup.getRawValue();
    this.save.emit({ email, active, admin, password: password || undefined });
  }
}
