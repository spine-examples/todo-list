import {Component} from '@angular/core';

/**
 * The component which displays dynamically obtained text in an error-like style.
 */
@Component({
  selector: 'app-error-viewport',
  templateUrl: './error-viewport.component.html',
  styleUrls: ['./error-viewport.component.css']
})
export class ErrorViewport {
  text: string;
}
