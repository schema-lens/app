import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { LayoutAuthComponent } from '../layouts/layout-auth.component';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslatePipe } from '../translate/translate.pipe';
import { FormatGenericValidationErrorsPipe } from '../../shared/pipes/format-generic-validation-errors.pipe';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatAnchor, MatButton } from '@angular/material/button';
import { AuthService, USERNAME_REGEX } from './auth.service';
import { Router, RouterLink } from '@angular/router';
import { verifyPasswordValidator } from '../validators/verify-password.validator';
import { ProjectService } from '../../projects/services/project.service';
import { RegistrationData } from '../models/registration-data.model';
import { finalize, forkJoin, map, mergeMap, of } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RegistrationResult } from '../models/auth.model';
import { NgOptimizedImage } from '@angular/common';
import {
  ChangeLocaleButtonComponent,
} from '../../shared/components/change-locale-button/change-locale-button.component';
import { IconLinkComponent } from '../../shared/components/icon-link/icon-link.component';
import { DataModelService } from '../../data-models/services/data-model.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    LayoutAuthComponent,
    ReactiveFormsModule,
    TranslatePipe,
    FormatGenericValidationErrorsPipe,
    MatFormFieldModule,
    MatInput,
    MatButton,
    MatAnchor,
    RouterLink,
    NgOptimizedImage,
    ChangeLocaleButtonComponent,
    IconLinkComponent,
  ],
})
export class RegisterComponent {
  private authService = inject(AuthService);
  private destroyRef = inject(DestroyRef);
  private projectService = inject(ProjectService);
  private dataModelService = inject(DataModelService);
  private router = inject(Router);

  registerForm = new FormGroup({
    username: new FormControl('', [
      Validators.required,
      Validators.minLength(4),
      Validators.maxLength(32),
      Validators.pattern(USERNAME_REGEX),
    ]),
    email: new FormControl('', [Validators.required, Validators.email, Validators.maxLength(128)]),
    password: new FormControl('', [Validators.required, Validators.minLength(8)]),
    verifyPassword: new FormControl('', [Validators.required]),
  });

  loading = signal<boolean>(false);
  error = signal<string | null>(null);

  constructor() {
    const { password, verifyPassword } = this.registerForm.controls;
    verifyPassword.addValidators(verifyPasswordValidator(password));
  }

  register(): void {
    if (this.registerForm.invalid) {
      return;
    }

    this.loading.set(true);
    this.registerForm.disable();

    const registrationData = this.registerForm.value;
    delete registrationData.verifyPassword;

    this.authService.register(registrationData as RegistrationData).pipe(
      mergeMap(result => result === RegistrationResult.SUCCESS
        ? forkJoin([this.projectService.loadProjects(), this.dataModelService.loadDataModels()]).pipe(map(() => result))
        : of(result)),
      takeUntilDestroyed(this.destroyRef),
      finalize(() => {
        this.loading.set(false);
        this.registerForm.enable();
      }),
    ).subscribe({
      next: async result => {
        if (result === RegistrationResult.SUCCESS) {
          await this.router.navigate(['/project']);
        } else {
          this.error.set(
            result === RegistrationResult.USERNAME_TAKEN
              ? 'AUTH.ERRORS.USERNAME_TAKEN'
              : 'AUTH.ERRORS.EMAIL_TAKEN',
          );
        }
      },
      error: () => this.error.set('GENERIC.ERROR_LABEL'),
    });
  }
}
