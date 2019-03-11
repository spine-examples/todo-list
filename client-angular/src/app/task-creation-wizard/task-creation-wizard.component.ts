import {Component} from '@angular/core';
import {Location} from '@angular/common';

@Component({
  selector: 'app-task-creation-wizard',
  templateUrl: './task-creation-wizard.component.html',
  styleUrls: ['./task-creation-wizard.component.css']
})
export class TaskCreationWizardComponent {

  constructor(private readonly location: Location) {
  }

  back(): void {
    this.location.back();
  }
}
