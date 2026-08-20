import { TestBed } from '@angular/core/testing';

import { JobNotificationService } from './job-notification-service';

describe('JobNotificationService', () => {
  let service: JobNotificationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(JobNotificationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
