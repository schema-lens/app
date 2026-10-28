import { apiInterceptor } from './api.interceptor';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { MockBuilder, MockRender, ngMocks } from 'ng-mocks';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { lastValueFrom } from 'rxjs';

describe('apiInterceptor', () => {
  let httpClient: HttpClient;
  let httpController: HttpTestingController;
  ngMocks.faster();

  beforeAll(() => MockBuilder()
    .keep(provideHttpClient(
      withInterceptors([apiInterceptor]),
    ))
    .keep(provideHttpClientTesting()));

  beforeEach(() => {
    httpClient = MockRender(HttpClient).point.componentInstance;
    httpController = ngMocks.get(HttpTestingController);
  });

  afterEach(() => httpController.verify());

  it('should be created', () => {
    expect(httpClient).toBeTruthy();
  });

  it(`should prepend '/api' to url for API requests`, async () => {
    const result$ = httpClient.get<string[]>('/resource');
    const resultPromise = lastValueFrom(result$);

    const req = httpController.expectOne('/api/resource');
    req.flush(['hello', 'world']);

    expect(await resultPromise).toEqual(['hello', 'world']);
  });

  it(`should not prepend '/api' to url for non-API requests`, async () => {
    const result$ = httpClient.get<number[]>('https://example.com/external');
    const resultPromise = lastValueFrom(result$);

    const req = httpController.expectOne('https://example.com/external');
    req.flush([4, 2]);

    expect(await resultPromise).toEqual([4, 2]);
  });
});
