import { TableHttpClientService } from './table-http-client.service';
import { MockBuilder, MockRender, ngMocks } from 'ng-mocks';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { lastValueFrom } from 'rxjs';

describe('TableHttpClientService', () => {
  let service: TableHttpClientService;
  let httpController: HttpTestingController;
  ngMocks.faster();

  beforeAll(() => MockBuilder(TableHttpClientService)
    .keep(provideHttpClient())
    .keep(provideHttpClientTesting()));

  beforeEach(() => {
    service = MockRender(TableHttpClientService).point.componentInstance;
    httpController = ngMocks.get(HttpTestingController);
  });

  afterEach(() => httpController.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getTableNames', () => {
    it('should get table names', async () => {
      const tableNamesPromise = lastValueFrom(service.getTableNames('a074c780-af5a-4fd6-9fb3-183a6637e171'));

      const req = httpController.expectOne('/project/a074c780-af5a-4fd6-9fb3-183a6637e171/table');
      expect(req.request.method).toEqual('GET');
      req.flush(['TEST_TABLE1', 'TEST_TABLE2']);

      await expect(tableNamesPromise).resolves.toEqual(['TEST_TABLE1', 'TEST_TABLE2']);
    });

    it('should throw error when API return an error', async () => {
      const tableNamesPromise = lastValueFrom(service.getTableNames('fe383acd-46f4-44eb-840b-63c60dfd23bb'));

      httpController.expectOne('/project/fe383acd-46f4-44eb-840b-63c60dfd23bb/table').flush(null, {
        status: 404,
        statusText: 'Not found',
      });

      await expect(tableNamesPromise).rejects.toHaveProperty('status', 404);
    });
  });
});
