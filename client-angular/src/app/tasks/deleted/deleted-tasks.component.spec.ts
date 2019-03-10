import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeletedTasksComponent } from './deleted-tasks.component';

describe('DeletedTasksComponent', () => {
  let component: DeletedTasksComponent;
  let fixture: ComponentFixture<DeletedTasksComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DeletedTasksComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeletedTasksComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
