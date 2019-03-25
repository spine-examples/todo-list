/*
 * Copyright 2019, TeamDev. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import {TestBed} from '@angular/core/testing';
import {Client} from 'spine-web';

import {LabelService} from '../../../src/app/labels/label.service';
import {mockSpineWebClient} from '../given/mock-spine-web-client';
import {label1, label2} from '../given/labels';

describe('LabelService', () => {
  const mockClient = mockSpineWebClient();
  let service: LabelService;

  beforeEach(() => {
      TestBed.configureTestingModule({
        providers: [LabelService, {provide: Client, useValue: mockClient}]
      });
      service = TestBed.get(LabelService);
    }
  );

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch all labels', () => {
    const fetch = jasmine.createSpyObj<Client.Fetch>('Fetch', ['atOnce']);
    mockClient.fetchAll.and.returnValue(fetch);
    const theLabels = [label1(), label2()];
    fetch.atOnce.and.returnValue(Promise.resolve(theLabels));

    service.fetchAllLabels()
      .then(labels => {
        expect(labels.length).toEqual(2);
        expect(labels[0]).toEqual(theLabels[0]);
        expect(labels[1]).toEqual(theLabels[1]);
      });
  });

  it('should propagate errors from Spine Web Client on `fetchAllLabels`', () => {
    const fetch = jasmine.createSpyObj<Client.Fetch>('Fetch', ['atOnce']);
    mockClient.fetchAll.and.returnValue(fetch);
    const errorMessage = 'Labels lookup rejected';
    fetch.atOnce.and.returnValue(Promise.reject(errorMessage));

    service.fetchAllLabels()
      .then(() => fail('Labels fetch should have been rejected'))
      .catch(err => expect(err).toEqual(errorMessage));
  });

  it('should fetch a single label details', () => {
    const theLabel = label1();
    mockClient.fetchById.and.callFake((cls, id, resolve) => resolve(theLabel));

    service.fetchLabelDetails(theLabel.getId())
      .then(label => expect(label).toEqual(theLabel))
      .catch(err =>
        fail(`Label details should have been resolved, actually rejected with an error: ${err}`)
      );
  });

  it('should propagate errors from Spine Web Client on `fetchLabelDetails`', () => {
    const errorMessage = 'Label details lookup rejected';
    mockClient.fetchById.and.callFake((cls, id, resolve, reject) => reject(errorMessage));

    service.fetchLabelDetails(label1().getId())
      .then(() => fail('Label details lookup should have been rejected'))
      .catch(err => expect(err).toEqual(errorMessage));
  });

  it('should produce an error when no matching label is found during lookup', () => {
    mockClient.fetchById.and.callFake((cls, id, resolve) => resolve(null));
    const labelId = label1().getId();

    service.fetchLabelDetails(labelId)
      .then(() => fail('Label details lookup should have been rejected'))
      .catch(err => expect(err).toEqual(`No label view found for ID: ${labelId}`));
  });
});
