import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TaskCreationWizardComponent } from './task-creation-wizard.component';

describe('TaskCreationWizardComponent', () => {
  let component: TaskCreationWizardComponent;
  let fixture: ComponentFixture<TaskCreationWizardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TaskCreationWizardComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TaskCreationWizardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
