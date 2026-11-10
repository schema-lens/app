import { ChangeDetectionStrategy, Component, input, output, signal } from '@angular/core';
import { AdminUpdateUser, AdminUser } from '../../models/admin-user.model';
import { MatTableModule } from '@angular/material/table';
import { MatIconButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { AdminUserEditorComponent } from '../admin-user-editor/admin-user-editor.component';
import { TranslatePipe } from '../../../core/translate/translate.pipe';
import { StatusIconComponent } from '../../../shared/components/status-icon/status-icon.component';

@Component({
  selector: 'app-admin-user-table',
  templateUrl: './admin-user-table.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatTableModule, AdminUserEditorComponent, MatIconButton, MatIcon, TranslatePipe, StatusIconComponent],
})
export class AdminUserTableComponent {
  readonly DISPLAYED_COLUMNS = ['id', 'username', 'email', 'active', 'admin', 'expand'];

  users = input.required<AdminUser[]>();
  currentUsername = input<string>();
  saving = input(false);
  save = output<{ user: AdminUser; changes: AdminUpdateUser }>();
  delete = output<AdminUser>();

  expandedUserId = signal<number | null>(null);

  toggleExpansion(user: AdminUser): void {
    if (!this.saving()) {
      this.expandedUserId.update(id => (id === user.id ? null : user.id));
    }
  }

  isExpanded(user: AdminUser): boolean {
    return this.expandedUserId() === user.id;
  }
}
