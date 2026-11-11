import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService, USERNAME_REGEX } from './auth.service';
import { Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize, forkJoin, map, mergeMap, of } from 'rxjs';
import { LayoutAuthComponent } from '../layouts/layout-auth.component';
import { MatInputModule } from '@angular/material/input';
import { MatAnchor, MatButton } from '@angular/material/button';
import { TranslatePipe } from '../translate/translate.pipe';
import { FormatGenericValidationErrorsPipe } from '../../shared/pipes/format-generic-validation-errors.pipe';
import { ProjectService } from '../../projects/services/project.service';
import { NgOptimizedImage } from '@angular/common';
import {
  ChangeLocaleButtonComponent,
} from '../../shared/components/change-locale-button/change-locale-button.component';
import { IconLinkComponent } from '../../shared/components/icon-link/icon-link.component';
import { DataModelService } from '../../data-models/services/data-model.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatButton,
    MatInputModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    LayoutAuthComponent,
    TranslatePipe,
    FormatGenericValidationErrorsPipe,
    MatAnchor,
    RouterLink,
    NgOptimizedImage,
    ChangeLocaleButtonComponent,
    IconLinkComponent,
  ],
})
export class LoginComponent {
  private authService = inject(AuthService);
  private destroyRef = inject(DestroyRef);
  private projectService = inject(ProjectService);
  private dataModelService = inject(DataModelService);
  private router = inject(Router);

  loginForm = new FormGroup({
    username: new FormControl('', [
      Validators.required,
      Validators.minLength(4),
      Validators.maxLength(32),
      Validators.pattern(USERNAME_REGEX),
    ]),
    password: new FormControl('', [Validators.required, Validators.minLength(8)]),
  });

  loading = signal<boolean>(false);
  error = signal<string | null>(null);

  login(): void {
    const { username, password } = this.loginForm.value;

    if (this.loginForm.invalid || !username || !password) {
      return;
    }

    this.loading.set(true);
    this.loginForm.disable();

    this.authService
      .login(username, password)
      .pipe(
        mergeMap(authenticated => (authenticated
          ? forkJoin([this.projectService.loadProjects(), this.dataModelService.loadDataModels()]).pipe(map(() => true))
          : of(false)),
        ),
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.loading.set(false);
          this.loginForm.enable();
        }),
      )
      .subscribe({
        next: async success => {
          if (success) {
            await this.router.navigate(['/project']);
          } else {
            this.error.set('AUTH.ERRORS.CREDENTIALS_WRONG');
          }
        },
        error: () => this.error.set('GENERIC.ERROR_LABEL'),
      });
  }
}
