import { Route } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { homeGuard } from './core/guards/home.guard';
import { hasAdminRoleGuard } from './core/guards/has-role.guard';
import { dataModelerUnsavedGuard } from './data-models/modeler/data-modeler-unsaved.guard';

export const appRoutes: Route[] = [
  { path: '', redirectTo: 'project', pathMatch: 'full' },
  { path: '404', loadComponent: () => import('./error-404.component').then(c => c.Error404Component) },
  {
    path: 'login',
    loadComponent: () => import('./core/auth/login.component').then(c => c.LoginComponent),
    data: { disableTopBar: true },
    canActivate: [homeGuard],
  },
  {
    path: 'register',
    loadComponent: () => import('./core/auth/register.component').then(c => c.RegisterComponent),
    data: { disableTopBar: true },
    canActivate: [homeGuard],
  },
  {
    path: 'profile',
    loadComponent: () => import('./profile/profile.component').then(c => c.ProfileComponent),
    canActivate: [authGuard],
  },
  {
    path: 'model',
    loadComponent: () =>
      import('./data-models/components/data-model-list/data-model-list.component').then(c => c.DataModelListComponent),
    canActivate: [authGuard],
  },
  {
    path: 'model/create',
    loadComponent: () =>
      import('./data-models/components/data-model-create/data-model-create.component').then(c => c.DataModelCreateComponent),
    canActivate: [authGuard],
  },
  {
    path: 'model/:dataModelId',
    loadComponent: () => import('./data-models/data-model.component').then(c => c.DataModelComponent),
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'logical/properties', pathMatch: 'full' },
      { path: 'logical', redirectTo: 'logical/properties', pathMatch: 'full' },
      { path: 'oracle', redirectTo: 'oracle/properties', pathMatch: 'full' },
      {
        path: 'logical/properties',
        loadComponent: () =>
          import('./data-models/components/data-model-properties-edit/data-model-properties-edit.component').then(
            c => c.DataModelPropertiesEditComponent,
          ),
        data: { dataModelingContext: 'logical' },
      },
      {
        path: 'logical/domain/:dataTypeId',
        loadComponent: () =>
          import('./data-models/components/data-model-data-type/data-model-data-type.component').then(
            c => c.DataModelDataTypeComponent,
          ),
        data: { dataModelingContext: 'logical' },
      },
      {
        path: 'logical/entity/:nodeId',
        loadComponent: () =>
          import('./data-models/components/data-model-node/data-model-node.component').then(
            c => c.DataModelNodeComponent,
          ),
        data: { dataModelingContext: 'logical' },
      },
      {
        path: 'logical/relationship/:edgeId',
        loadComponent: () =>
          import('./data-models/components/data-model-edge/data-model-edge.component').then(
            c => c.DataModelEdgeComponent,
          ),
        data: { dataModelingContext: 'logical' },
      },
      {
        path: 'oracle/properties',
        loadComponent: () =>
          import('./data-models/components/data-model-properties-edit/data-model-properties-edit.component').then(
            c => c.DataModelPropertiesEditComponent,
          ),
        data: { dataModelingContext: 'oracle' },
      },
      {
        path: 'oracle/data-type/:dataTypeId',
        loadComponent: () =>
          import('./data-models/components/data-model-data-type/data-model-data-type.component').then(
            c => c.DataModelDataTypeComponent,
          ),
        data: { dataModelingContext: 'oracle' },
      },
      {
        path: 'oracle/table/:nodeId',
        loadComponent: () =>
          import('./data-models/components/data-model-node/data-model-node.component').then(
            c => c.DataModelNodeComponent,
          ),
        data: { dataModelingContext: 'oracle' },
      },
      {
        path: 'oracle/relationship/:edgeId',
        loadComponent: () =>
          import('./data-models/components/data-model-edge/data-model-edge.component').then(
            c => c.DataModelEdgeComponent,
          ),
        data: { dataModelingContext: 'oracle' },
      },
    ],
  },
  {
    path: 'modeler/:dataModelId/logical/:diagramId',
    loadComponent: () =>
      import('./data-models/modeler/data-modeler.component').then(c => c.DataModelerComponent),
    canActivate: [authGuard],
    canDeactivate: [dataModelerUnsavedGuard],
    data: { dataModelingContext: 'logical' },
  },
  {
    path: 'modeler/:dataModelId/oracle/:diagramId',
    loadComponent: () =>
      import('./data-models/modeler/data-modeler.component').then(c => c.DataModelerComponent),
    canActivate: [authGuard],
    canDeactivate: [dataModelerUnsavedGuard],
    data: { dataModelingContext: 'oracle' },
  },
  {
    path: 'project',
    loadComponent: () => import('./projects/components/project-list/project-list.component').then(c => c.ProjectListComponent),
    canActivate: [authGuard],
  },
  {
    path: 'project/create',
    loadComponent: () =>
      import('./projects/components/project-create/project-create.component').then(c => c.ProjectCreateComponent),
    canActivate: [authGuard],
  },
  {
    path: 'project/:projectId',
    loadComponent: () => import('./projects/project.component').then(c => c.ProjectComponent),
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'properties', pathMatch: 'full' },
      {
        path: 'properties',
        loadComponent: () =>
          import('./projects/components/project-properties-edit/project-properties-edit.component').then(
            c => c.ProjectPropertiesEditComponent,
          ),
      },
      {
        path: 'table/404',
        loadComponent: () =>
          import('./tables/components/table-not-found/table-not-found.component').then(c => c.TableNotFoundComponent),
      },
      {
        path: 'table/:tableName',
        loadComponent: () => import('./tables/table.component').then(c => c.TableComponent),
        children: [
          { path: '', redirectTo: 'columns', pathMatch: 'full' },
          {
            path: 'columns',
            loadComponent: () =>
              import('./tables/components/table-columns/table-columns.component').then(c => c.TableColumnsComponent),
          },
          {
            path: 'constraints',
            loadComponent: () =>
              import('./tables/components/table-constraints/table-constraints.component').then(c => c.TableConstraintsComponent),
          },
          {
            path: 'indexes',
            loadComponent: () =>
              import('./tables/components/table-indexes/table-indexes.component').then(c => c.TableIndexesComponent),
          },
          {
            path: 'ddl',
            loadComponent: () => import('./tables/components/table-ddl/table-ddl.component').then(c => c.TableDdlComponent),
          },
          {
            path: 'relationships',
            loadComponent: () =>
              import('./tables/components/table-relationships/table-relationships.component').then(
                c => c.TableRelationshipsComponent,
              ),
          },
        ],
      },
      {
        path: 'table-relationships',
        loadComponent: () =>
          import('./tables/components/table-all-relationships/table-all-relationships.component').then(
            c => c.TableAllRelationshipsComponent,
          ),
      },
      {
        path: 'table-relationships/view',
        loadComponent: () =>
          import('./tables/components/table-all-relationships/table-all-relationships-view/table-all-relationships-view.component').then(
            c => c.TableAllRelationshipsViewComponent,
          ),
      },
      {
        path: 'collaborators',
        loadComponent: () =>
          import('./projects/components/project-collaborators/project-collaborators.component').then(
            c => c.ProjectCollaboratorsComponent,
          ),
      },
    ],
  },
  { path: 'help', redirectTo: 'help/faq' },
  {
    path: 'help/faq',
    loadComponent: () => import('./help/components/help-faq/help-faq.component').then(c => c.HelpFaqComponent),
  },
  {
    path: 'admin',
    loadComponent: () => import('./admin/admin.component').then(c => c.AdminComponent),
    canActivate: [authGuard, hasAdminRoleGuard],
    children: [
      { path: '', redirectTo: 'faq', pathMatch: 'full' },
      {
        path: 'faq',
        loadComponent: () => import('./admin/components/admin-faq/admin-faq.component').then(c => c.AdminFaqComponent),
      },
      {
        path: 'default-data-types',
        loadComponent: () => import('./admin/components/admin-data-types/admin-data-types.component').then(c => c.AdminDataTypesComponent),
      },
      {
        path: 'backup',
        loadComponent: () => import('./admin/components/admin-backup/admin-backup.component').then(c => c.AdminBackupComponent),
      },
    ],
  },
  { path: '**', redirectTo: '404' },
];
