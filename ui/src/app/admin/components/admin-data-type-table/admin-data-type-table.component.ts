import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { AdminDataType } from '../../models/admin-data-type.model';
import { MatTableModule } from '@angular/material/table';
import { MatIconButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { TranslatePipe } from '../../../core/translate/translate.pipe';

@Component({
  selector: 'app-admin-data-type-table',
  templateUrl: './admin-data-type-table.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatTableModule,
    MatMenuModule,
    MatIconButton,
    MatIcon,
    TranslatePipe,
  ],
})
export class AdminDataTypeTableComponent {
  readonly DISPLAYED_COLUMNS = ['name', 'parameters', 'physical', 'actions'];

  dataTypes = input.required<AdminDataType[]>();
  edit = output<AdminDataType>();
  delete = output<AdminDataType>();
}
