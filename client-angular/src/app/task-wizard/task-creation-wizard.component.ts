import {Component, OnInit} from '@angular/core';
import { Location } from '@angular/common';

@Component({
  selector: 'app-task-creation-wizard',
  templateUrl: './task-creation-wizard.component.html',
  styleUrls: ['./task-creation-wizard.component.css']
})
export class TaskCreationWizardComponent implements OnInit {

  constructor(private location: Location) {
  }

  ngOnInit() {
  }

  back(): void {
    this.location.back();
  }
}
