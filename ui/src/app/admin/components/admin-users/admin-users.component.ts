import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { LayoutHeaderAndContentComponent } from '../../../core/layouts/layout-header-and-content.component';
import { TranslatePipe } from '../../../core/translate/translate.pipe';
import { AdminCreateUser, AdminUpdateUser, AdminUser } from '../../models/admin-user.model';
import { AdminUserService } from '../../services/admin-user.service';
import { rxResource, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { filter, finalize, mergeMap, Observable, tap } from 'rxjs';
import { ProgressSpinnerComponent } from '../../../shared/components/progress-spinner/progress-spinner.component';
import { AlertComponent } from '../../../shared/components/alert/alert.component';
import { AdminUserTableComponent } from '../admin-user-table/admin-user-table.component';
import { MatDialog } from '@angular/material/dialog';
import { AuthService } from '../../../core/auth/auth.service';
import { HttpErrorResponse } from '@angular/common/http';
import { AdminUserModifyDialogComponent } from '../admin-user-modify-dialog/admin-user-modify-dialog.component';
import { MatButton } from '@angular/material/button';
import { IconEmphasisDirective } from '../../../shared/directives/icon-emphasis.directive';
import { MatIcon } from '@angular/material/icon';
import { DialogService } from '../../../core/dialog.service';

@Component({
  selector: 'app-admin-users',
  templateUrl: './admin-users.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    LayoutHeaderAndContentComponent,
    TranslatePipe,
    ProgressSpinnerComponent,
    AlertComponent,
    AdminUserTableComponent,
    MatButton,
    MatIcon,
    IconEmphasisDirective,
  ],
})
export class AdminUsersComponent {
  private adminUserService = inject(AdminUserService);
  private matDialog = inject(MatDialog);
  private dialogService = inject(DialogService);
  readonly authService = inject(AuthService);
  private destroyRef = inject(DestroyRef);

  usersResource = rxResource({
    stream: () => this.adminUserService.getUsers(),
    defaultValue: [],
  });

  saving = signal(false);
  resultChange = signal<{ type: 'info' | 'error'; message: string } | null>(null);

  create(): void {
    if (this.usersResource.isLoading() || this.saving() || this.usersResource.error()) {
      return;
    }

    this.openCreateDialog()
      .pipe(
        mergeMap(updatedUser => this.adminUserService.createUser(updatedUser)),
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.saving.set(false)),
      )
      .subscribe({
        next: updatedUser => {
          this.usersResource.update(users => [...users, updatedUser]);
          this.resultChange.set({ type: 'info', message: 'GENERIC.SUCCESS_LABEL' });
        },
        error: (error: HttpErrorResponse) => this.handleModifyError(error),
      });
  }

  update(user: AdminUser, changes: AdminUpdateUser): void {
    if (this.usersResource.isLoading() || this.saving() || this.usersResource.error()) {
      return;
    }

    this.beginModifyOperation();
    this.adminUserService
      .updateUser(user.id, changes)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.saving.set(false)),
      )
      .subscribe({
        next: updatedUser => {
          this.usersResource.update(users =>
            users.map(existingUser => (existingUser.id === updatedUser.id ? updatedUser : existingUser)),
          );
          this.resultChange.set({ type: 'info', message: 'GENERIC.SUCCESS_LABEL' });
        },
        error: (error: HttpErrorResponse) => this.handleModifyError(error),
      });
  }

  delete(user: AdminUser): void {
    if (this.usersResource.isLoading() || this.saving() || this.usersResource.error()) {
      return;
    }

    this.dialogService
      .openConfirmationDialog('ADMIN.USERS.CONFIRM_DELETE_TITLE', 'ADMIN.USERS.CONFIRM_DELETE_DESCRIPTION', 'danger')
      .pipe(
        filter(result => !!result),
        tap(() => this.beginModifyOperation()),
        mergeMap(() => this.adminUserService.deleteUser(user.id)),
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.saving.set(false)),
      )
      .subscribe({
        next: () => {
          this.usersResource.update(users => users.filter(existingUser => existingUser.id !== user.id));
          this.resultChange.set({ type: 'info', message: 'GENERIC.SUCCESS_LABEL' });
        },
        error: (error: HttpErrorResponse) => this.handleModifyError(error),
      });
  }

  private openCreateDialog(): Observable<AdminCreateUser> {
    return this.matDialog
      .open(AdminUserModifyDialogComponent, {
        width: '32rem',
        maxWidth: 'calc(100vw - 2rem)',
        maxHeight: 'calc(100dvh - 2rem)',
      })
      .afterClosed()
      .pipe(
        filter((result): result is AdminCreateUser => !!result),
        tap(() => this.beginModifyOperation()),
      );
  }

  private handleModifyError(error: HttpErrorResponse): void {
    this.resultChange.set({
      type: 'error',
      message: error.status === 409 ? 'ADMIN.USERS.CONFLICT_LABEL' : 'GENERIC.ERROR_LABEL',
    });
  }

  private beginModifyOperation(): void {
    this.saving.set(true);
    this.resultChange.set(null);
  }
}
