import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { LayoutHeaderAndContentComponent } from '../../../core/layouts/layout-header-and-content.component';
import { TranslatePipe } from '../../../core/translate/translate.pipe';
import { AdminDataTypeInput, AdminDataType } from '../../models/admin-data-type.model';
import { AdminDataTypeService } from '../../services/admin-data-type.service';
import { rxResource, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { filter, finalize, mergeMap, Observable, tap } from 'rxjs';
import { ProgressSpinnerComponent } from '../../../shared/components/progress-spinner/progress-spinner.component';
import { AlertComponent } from '../../../shared/components/alert/alert.component';
import { AdminDataTypeTableComponent } from '../admin-data-type-table/admin-data-type-table.component';
import { MatDialog } from '@angular/material/dialog';
import {
  AdminDataTypeModifyDialogComponent,
  AdminDataTypeModifyDialogData,
} from '../admin-data-type-modify-dialog/admin-data-type-modify-dialog.component';
import { MatButton } from '@angular/material/button';
import { IconEmphasisDirective } from '../../../shared/directives/icon-emphasis.directive';
import { MatIcon } from '@angular/material/icon';
import { DialogService } from '../../../core/dialog.service';

@Component({
  selector: 'app-admin-data-types',
  templateUrl: './admin-data-types.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    LayoutHeaderAndContentComponent,
    TranslatePipe,
    ProgressSpinnerComponent,
    AlertComponent,
    AdminDataTypeTableComponent,
    MatButton,
    MatIcon,
    IconEmphasisDirective,
  ],
})
export class AdminDataTypesComponent {
  private adminDataTypeService = inject(AdminDataTypeService);
  private matDialog = inject(MatDialog);
  private dialogService = inject(DialogService);
  private destroyRef = inject(DestroyRef);

  dataTypesResource = rxResource({
    stream: () => this.adminDataTypeService.getDataTypes(),
    defaultValue: [],
  });

  private modificationRunning = signal(false);
  loading = computed(() => this.dataTypesResource.isLoading() || this.modificationRunning());
  resultChange = signal<boolean | null>(null);

  create(): void {
    if (this.loading() || this.dataTypesResource.error()) {
      return;
    }

    this.openModifyDialog().pipe(
      mergeMap(input => this.adminDataTypeService.createDataType(input)),
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.modificationRunning.set(false)),
    ).subscribe({
      next: createdType => {
        this.dataTypesResource.update(types => [...types, createdType]);
        this.resultChange.set(true);
      },
      error: () => this.resultChange.set(false),
    });
  }

  update(dataType: AdminDataType): void {
    if (this.loading() || this.dataTypesResource.error()) {
      return;
    }

    this.openModifyDialog(dataType).pipe(
      mergeMap(input => this.adminDataTypeService.updateDataType({ ...dataType, ...input })),
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.modificationRunning.set(false)),
    ).subscribe({
      next: updatedType => {
        this.dataTypesResource.update(types => types.map(type => type.typeId === updatedType.typeId ? updatedType : type));
        this.resultChange.set(true);
      },
      error: () => this.resultChange.set(false),
    });
  }

  delete(dataType: AdminDataType): void {
    if (this.loading() || this.dataTypesResource.error()) {
      return;
    }

    this.dialogService.openConfirmationDialog('ADMIN.DATA_TYPES.CONFIRM_DELETE_TITLE', 'ADMIN.DATA_TYPES.CONFIRM_DELETE_DESCRIPTION', 'danger').pipe(
      filter(result => !!result),
      tap(() => this.beginModifyOperation()),
      mergeMap(() => this.adminDataTypeService.deleteDataType(dataType.typeId)),
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.modificationRunning.set(false)),
    ).subscribe({
      next: () => {
        this.dataTypesResource.update(types => types.filter(type => type.typeId !== dataType.typeId));
        this.resultChange.set(true);
      },
      error: () => this.resultChange.set(false),
    });
  }

  private openModifyDialog(dataType?: AdminDataType): Observable<AdminDataTypeInput> {
    const existingNames = this.dataTypesResource.value().filter(type => type.typeId !== dataType?.typeId).map(type => type.name);
    const data: AdminDataTypeModifyDialogData = { existingNames, dataType };

    return this.matDialog.open(AdminDataTypeModifyDialogComponent, { data }).afterClosed().pipe(
      filter((result): result is AdminDataTypeInput => result != null),
      tap(() => this.beginModifyOperation()),
    );
  }

  private beginModifyOperation(): void {
    this.modificationRunning.set(true);
    this.resultChange.set(null);
  }
}
