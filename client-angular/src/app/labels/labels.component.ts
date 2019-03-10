import {Component, OnInit} from '@angular/core';
import {Location} from '@angular/common';

@Component({
  selector: 'app-labels',
  templateUrl: './labels.component.html',
  styleUrls: ['./labels.component.css']
})
export class LabelsComponent implements OnInit {

  constructor(private readonly location: Location) {
  }

  ngOnInit() {
  }

  back(): void {
    this.location.back();
  }

  addLabel(): void {
    console.log('To be honest I cannot add a new label');
  }
}
